# AWS EKS (Elastic Kubernetes Service) Cluster 생성하기

> AWS EKS (Elastic Kubernetes Service)는 AWS에서 Kubernetes를 쉽고 안정적으로 운영할 수 있도록 도와주는 **관리형 Kubernetes 서비스**입니다.
> EKS Cluster를 실제로 **생성하고 관리 서버(Bastion)에서 제어하는 과정**입니다.

---

## 1. EKS Cluster 구성 개요

EKS Cluster는 크게 두 가지 주요 컴포넌트로 구성됩니다:

| 구성요소              | 설명                                                               |
| ----------------- | ---------------------------------------------------------------- |
| **Control Plane** | Kubernetes의 “두뇌” 역할. API 서버, etcd, 스케줄러 등이 포함되어 있으며 AWS에서 완전 관리됨 |
| **Data Plane**    | 실제 컨테이너(Pod)가 배포되는 영역. EC2 기반의 **Worker Node**로 구성               |

### 기본 아키텍처

```text
+--------------------------------------------------------+
|                   AWS EKS Cluster                      |
|                                                        |
|  +----------------------+    +----------------------+  |
|  |  Control Plane (AWS) |    | Data Plane (EC2 Node)|  |
|  |  Managed by AWS      |    | Runs Pods/Services   |  |
|  +----------------------+    +----------------------+  |
|                                                        |
+--------------------------------------------------------+
```

---

## 2. Bastion (관리 서버) 생성 및 환경 구성

### 2.1 Bastion 서버 생성

AWS에서 Cluster를 관리하기 위한 **관리용 EC2 인스턴스 (Bastion)** 를 준비합니다.

| 항목          | 설정값                                |
| ----------- | ---------------------------------- |
| **VPC**     | Default VPC                        |
| **이름**      | `mgmtXX-server`                    |
| **AMI**     | Ubuntu Server 24.04 LTS            |
| **인스턴스 유형** | `t2.medium`                        |
| **스토리지**    | 20GB (gp3)                         |
| **보안 그룹**   | SSH (22번 포트) – “내 IP”만 허용          |
| **키페어 이름**  | `smlee-key.pem` (⚠ `.ppk`로 만들지 않기) |

생성 후 SSH 접속:

```bash
ssh -i smlee-key.pem ubuntu@<EC2_PUBLIC_IP>
```

기본 환경 설정:

```bash
sudo hostnamectl set-hostname eks-mgmtXX
sudo timedatectl set-timezone Asia/Seoul
```

---

### 2.2 AWS CLI 설정

#### AWS CLI 설치

```bash
sudo apt update
sudo apt install -y unzip
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
```

#### AWS 프로필 등록

```bash
aws configure
# 입력 예시
AWS Access Key ID [None]: AKIA...
AWS Secret Access Key [None]: M7x...
Default region name [None]: ap-northeast-2
Default output format [None]: json
```

#### 연결 확인

```bash
aws sts get-caller-identity
```

---

### 2.3 kubectl 설치

> kubectl은 Kubernetes Cluster를 제어하는 CLI 도구입니다.

공식 문서: [Install kubectl](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/install-kubectl.html)

```bash
curl -O https://s3.us-west-2.amazonaws.com/amazon-eks/1.30.3/2025-08-03/bin/linux/amd64/kubectl
chmod +x ./kubectl
mkdir -p $HOME/bin && cp ./kubectl $HOME/bin/kubectl
echo 'export PATH=$PATH:$HOME/bin' >> ~/.bashrc
source ~/.bashrc
kubectl version --client
```

---

### 2.4 eksctl 설치

> eksctl은 EKS Cluster 생성을 자동화하는 명령줄 도구입니다.
> (EKS의 `Terraform` 같은 역할)

공식 문서: [Install eksctl](https://eksctl.io/installation/)

```bash
ARCH=amd64
PLATFORM=$(uname -s)_$ARCH
curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$PLATFORM.tar.gz"

tar -xzf eksctl_$PLATFORM.tar.gz -C /tmp && rm eksctl_$PLATFORM.tar.gz
sudo mv /tmp/eksctl /usr/local/bin
eksctl version
```

---

## 3. EKS Cluster 생성하기

### 기본 명령어

```bash
eksctl create cluster \
  --name demoXX-cluster \
  --region ap-northeast-2 \
  --with-oidc \
  --nodegroup-name demoXX-ng \
  --zones ap-northeast-2a,ap-northeast-2b \
  --nodes 3 \
  --node-type t3.medium \
  --node-volume-size 20 \
  --managed \
  --ssh-access \
  --ssh-public-key smlee-key
```

- 생성 후 확인:

* **AWS CloudFormation 콘솔** → 스택 생성 진행 중 여부 확인
* **VPC, EKS, EC2** 리소스 생성 확인

---

### 노드 그룹 관리

#### 노드 개수 조정

```bash
eksctl scale nodegroup \
  --cluster demoXX-cluster \
  --region ap-northeast-2 \
  --name demoXX-ng \
  --nodes 4
```

#### 새로운 노드 그룹 추가

```yaml
# nodegroup.yaml
---
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig
metadata:
  name: root-eks
  region: ap-northeast-2
managedNodeGroups:
- name: ng-workers
  labels:
    role: workers
  instanceType: t3.medium
  desiredCapacity: 2
  volumeSize: 20
  privateNetworking: true
```

```bash
eksctl create nodegroup --config-file=nodegroup.yaml
```

#### 노드 그룹 삭제

```bash
eksctl delete nodegroup --cluster=root-eks --name=ng-workers
```

---

## 4. Cluster 동작 확인

```bash
kubectl get nodes
```

출력 예시:

```
NAME                                              STATUS   ROLES    AGE   VERSION
ip-192-168-11-26.ap-northeast-2.compute.internal  Ready    <none>   28m   v1.30.2
ip-192-168-42-149.ap-northeast-2.compute.internal Ready    <none>   28m   v1.30.2
```

---

## 5. 애플리케이션 배포 테스트

```bash
# 자동완성 기능 활성화
source <(kubectl completion bash)
echo "source <(kubectl completion bash)" >> ~/.bashrc

# nginx 컨테이너 5개 생성
kubectl create deployment webtest --image=nginx:1.14 --port=80 --replicas=5
kubectl get pods -o wide
```

### 서비스 노출 (LoadBalancer)

```bash
kubectl expose deployment webtest --port=80 --type=LoadBalancer
kubectl get svc
```

1~2분 후, 출력된 `EXTERNAL-IP` 혹은 `ELB 주소`로 접속:

```
http://<ELB-DNS>.ap-northeast-2.elb.amazonaws.com
```

### 정리 (삭제)

```bash
kubectl delete deployments.apps webtest
kubectl delete svc webtest
```

---

## 6. EKS Add-on 설치

EKS에는 기본적으로 다음 구성요소가 설치되어 있습니다:

* `Amazon VPC CNI`
* `kube-proxy`
* `CoreDNS`

여기에 추가로 **EBS CSI** 및 **EFS CSI** 드라이버를 설치할 수 있습니다.

### 설치 후 확인

```bash
kubectl get pods -A
```

출력 예시:

```
kube-system   ebs-csi-controller-xxxxx     Running
kube-system   efs-csi-controller-xxxxx     Running
kube-system   aws-node-xxxxx               Running
kube-system   coredns-xxxxx                Running
```

---

## 7. Cluster 삭제

```bash
eksctl delete cluster --name demoXX-cluster
```

---

## 참고 문서

| 항목                  | 링크                                                                                                                                                                          |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| AWS EKS 공식 문서       | [https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/what-is-eks.html](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/what-is-eks.html)                       |
| Install kubectl     |  [https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/install-kubectl.html](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/install-kubectl.html)               |
| Install eksctl      |  [https://eksctl.io/installation/](https://eksctl.io/installation/)                                                                                                       |
| AWS CLI 설치          |  [https://docs.aws.amazon.com/ko_kr/cli/latest/userguide/install-cliv2-linux.html](https://docs.aws.amazon.com/ko_kr/cli/latest/userguide/install-cliv2-linux.html)       |
| LoadBalancer 서비스 설정 |  [https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/network-load-balancing.html](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/network-load-balancing.html) |
| EBS CSI Driver      |  [https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/ebs-csi.html](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/ebs-csi.html)                               |
| EFS CSI Driver      |  [https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/efs-csi.html](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/efs-csi.html)                               |
