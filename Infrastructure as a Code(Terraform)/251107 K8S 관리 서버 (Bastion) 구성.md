# **K8S 관리 서버 (Bastion) 구성**

## **개요**

Bastion 서버는 외부에서 **EKS 클러스터에 안전하게 접근**하기 위한 관리용 서버입니다.
Terraform 코드 실행 및 클러스터 제어, ArgoCD 상태 점검 등을 담당합니다.

이 장에서는 Bastion 서버에 **SSH 접속 후 초기화 스크립트(user_data.tpl)** 를 실행하여
AWS CLI, kubectl 등을 자동으로 설정하고 클러스터 관리 환경을 완성합니다.

---

## **1. Bastion 서버 접속**

* **서버명:** `cluster-bastion`
* **계정 정보:**

  * 사용자: `ubuntu`
  * 키 파일: `<KEY>.pem`

```bash
# Bastion 서버 접속
ssh -i ~/.ssh/fs-key.pem ubuntu@<퍼블릭IP>
```

---

## **2. 기본 환경 설정**

### 호스트명 및 타임존 변경

```bash
sudo hostnamectl set-hostname <bastion-서버이름>   # 예: user00-bastion
sudo timedatectl set-timezone Asia/Seoul
```

---

### AWS 자격 증명 등록

> Terraform을 통해 IAM Role이 이미 연결된 경우, `aws configure` 대신 리전 설정만 필요할 수도 있습니다.

```bash
aws configure
AWS Access Key ID [None]: AKI...ZE4M
AWS Secret Access Key [None]: 1E+A4...ygh
Default region name [ap-northeast-2]:
Default output format [None]: json
```

---

## **3. Bastion 초기화 스크립트 실행 (user_data.tpl)**

Terraform에서 Bastion 인스턴스가 생성될 때 자동 실행되는 초기화 스크립트입니다.
AWS CLI, kubectl, EKS 인증, ArgoCD 접속 비밀번호 조회까지 자동화됩니다.

```bash
#!/bin/bash
# ===============================
# Bastion 초기 설정 스크립트
# ===============================

# 시스템 업데이트 및 패키지 설치
sudo apt-get update && apt-get upgrade -y
sudo apt-get install -y unzip curl wget jq

# AWS CLI 설치
echo "Installing AWS CLI"
sudo curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo unzip awscliv2.zip
sudo ./aws/install

# kubectl 설치
echo "Installing kubectl"
sudo curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# AWS CLI 설정 (IAM Role 인증 사용)
echo "Configuring AWS CLI"
mkdir -p /home/ubuntu/.aws
echo "[default]" > /home/ubuntu/.aws/config
echo "region = ap-northeast-2" >> /home/ubuntu/.aws/config

# EKS kubeconfig 구성
echo "Configuring kubeconfig"
aws eks get-token --cluster-name <클러스터이름> --region ap-northeast-2
aws eks update-kubeconfig --name <클러스터이름> --region ap-northeast-2

# 권한 및 환경 변수 설정
echo "Setting permissions"
chown -R ubuntu:ubuntu /home/ubuntu/.kube /home/ubuntu/.aws
echo "export KUBECONFIG=/home/ubuntu/.kube/config" >> /home/ubuntu/.bashrc
echo "source <(kubectl completion bash)" >> /home/ubuntu/.bashrc

# 동작 확인
echo "Testing kubectl"
kubectl version --client
kubectl get nodes

# ArgoCD 초기 관리자 비밀번호 확인
echo "Get password for ArgoCD"
kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath="{.data.password}" | base64 -d

echo "Script execution completed"
```

---

## **4. Bastion 동작 확인**

Terraform으로 Bastion 인스턴스가 정상 생성된 뒤, SSH 접속하여 아래 명령어로 확인합니다.

```bash
# EKS 노드 상태 확인
kubectl get nodes
```

```bash
NAME                                             STATUS   ROLES    AGE   VERSION
ip-10-15-1-135.ap-northeast-2.compute.internal   Ready    <none>   8h    v1.30.11-eks-473151a
ip-10-15-2-222.ap-northeast-2.compute.internal   Ready    <none>   8h    v1.30.11-eks-473151a
```

---

## **5. ArgoCD 초기 비밀번호 확인**

ArgoCD 대시보드 접속 시 필요한 관리자 계정 비밀번호를 아래 명령으로 확인합니다.

```bash
kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath="{.data.password}" | base64 -d
```

> 출력된 비밀번호는 ArgoCD 웹 콘솔(admin 계정) 로그인 시 사용합니다.
> 이후 보안을 위해 즉시 변경하는 것이 좋습니다.

---

## **정리**

* Bastion은 외부에서 EKS 클러스터에 안전하게 접근하기 위한 관리 서버
* 초기화 스크립트(`user_data.tpl`)로 AWS CLI, kubectl, kubeconfig 자동 설정
* ArgoCD의 초기 관리자 비밀번호도 자동으로 확인 가능

