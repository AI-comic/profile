## 1. EKS란?

**Amazon Elastic Kubernetes Service (EKS)** 는
AWS에서 **Kubernetes를 손쉽게 배포·운영할 수 있도록 제공하는 관리형 서비스**입니다.

EKS를 사용하면 **Control Plane(제어 평면)** 을 직접 구성하거나 유지할 필요 없이,
AWS가 자동으로 관리해줍니다.
사용자는 **Worker Node(작업 노드)** 와 **애플리케이션** 운영에 집중할 수 있습니다.

### 주요 특징

* **관리형 Control Plane** 제공 (자동 프로비저닝 및 패치)
* **다중 가용 영역(AZ)** 기반의 **고가용성**
* **간편한 클러스터 버전 업그레이드**
* **AWS 서비스 통합성** (ECR, IAM, ELB 등)
* **자동 노드 교체** 및 **확장성, 보안성 강화**

> 참고
> [EKS 공식 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/what-is-eks.html)

---

## 2. EKS 구성요소

EKS는 크게 두 부분으로 구성됩니다.

| 구성요소              | 설명                                                         |
| ----------------- | ---------------------------------------------------------- |
| **Control Plane** | 클러스터의 핵심 두뇌로, API 서버, etcd, 스케줄러 등 Kubernetes 핵심 구성 요소를 관리 |
| **Data Plane**    | 실제 애플리케이션이 실행되는 **Worker Node** 들로 구성. Pod를 통해 컨테이너 실행     |

### EKS VPC 구조

EKS 클러스터는 AWS VPC 내부에 구성되며,
보안 그룹(SG), 서브넷, IAM Role, ENI 등과 통합되어 동작합니다.

---

## 3. PrivateLink란?

AWS **PrivateLink** 는
AWS 서비스에 접근할 때 **인터넷을 거치지 않고 VPC 내부 프라이빗 네트워크를 통해 통신**할 수 있게 해줍니다.

즉,

* 인터넷 노출 없이 AWS 서비스와 연결
* **보안**과 **지연시간(속도)** 모두 개선
* **EKS 클러스터 ↔ AWS 서비스 간 안전한 통신 가능**

> 참고
> [EKS PrivateLink 지원](https://aws.amazon.com/ko/about-aws/whats-new/2022/12/amazon-eks-supports-aws-privatelink/)

---

## 4. AWS의 컨테이너 서비스 비교

AWS는 컨테이너 오케스트레이션 서비스로
**ECS**, **EKS**, **ECR**을 제공합니다.

| 서비스                                  | 설명                                              |
| ------------------------------------ | ----------------------------------------------- |
| **ECS (Elastic Container Service)**  | AWS에서 Docker 컨테이너를 손쉽게 실행·관리할 수 있는 완전관리형 서비스    |
| **EKS (Elastic Kubernetes Service)** | 오픈소스 **Kubernetes** 기반으로, 대규모 컨테이너 환경을 자동화 및 관리 |
| **ECR (Elastic Container Registry)** | Docker 이미지 저장소 서비스로, EKS/ECS 모두와 통합 가능          |

### ECS vs EKS 비교

| 구분               | Amazon ECS | Amazon EKS       |
| ---------------- | ---------- | ---------------- |
| 관리 형태            | 완전관리형      | 관리형 Kubernetes   |
| 주요 기술            | Docker     | Kubernetes       |
| 사용 난이도           | 쉬움         | 중간~어려움           |
| 애플리케이션 규모        | 중소~대규모     | 대규모 및 복잡한 구조     |
| 커뮤니티             | AWS 중심     | Kubernetes + AWS |
| 이식성(Portability) | 제한적        | 멀티 클라우드 호환 우수    |

> [ECS 공식 문서](https://docs.aws.amazon.com/ko_kr/AmazonECS/latest/developerguide/Welcome.html)
> [ECR 공식 문서](https://docs.aws.amazon.com/ko_kr/AmazonECR/latest/userguide/what-is-ecr.html)

---

## 5. EKS 네트워킹

### (1) LoadBalancer 서비스

EKS에서 `Service` 리소스 타입을 `LoadBalancer`로 지정하면
AWS의 **ELB(Elastic Load Balancer)** 와 자동 연동됩니다.

* 기본값: **Classic Load Balancer**
* 옵션: **Network Load Balancer (NLB)** 도 가능
  → `service.beta.kubernetes.io/aws-load-balancer-type: nlb` 어노테이션 사용

> [EKS LoadBalancer 공식 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/network-load-balancing.html)

---

### (2) ALB Ingress Controller

* Kubernetes **Ingress 리소스**를 이용해 HTTP(S) 트래픽 라우팅 가능
* **AWS Application Load Balancer(ALB)** 를 동적으로 생성 및 관리
* **aws-load-balancer-controller** 애드온 설치 필요

> [AWS Load Balancer Controller 문서](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)

---

### (3) AWS VPC CNI (Container Network Interface)

* AWS EKS의 기본 네트워크 애드온
* 각 Pod가 EC2 ENI(Elastic Network Interface)의 **Secondary IP**를 통해
  VPC 네트워크에서 직접 IP를 할당받음
* 결과적으로 **Pod가 VPC 네트워크에 직접 연결**되어 NAT 불필요

> [AWS VPC CNI 공식 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/pod-networking.html)

---

## 6. EKS 스토리지

### (1) CSI (Container Storage Interface)

Kubernetes에서 다양한 스토리지 백엔드를 지원하기 위한 **표준 인터페이스**입니다.

EKS에서는 다음을 지원합니다.

* **EBS CSI Driver** → 블록 스토리지
* **EFS CSI Driver** → 네트워크 파일 시스템

> [EBS CSI Driver](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/ebs-csi.html)
> [EFS CSI Driver](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/efs-csi.html)

---

### (2) StorageClass 예시

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: slow
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp2
  iopsPerGB: "10"
  fsType: ext4
```

> [Kubernetes StorageClass 공식 문서](https://kubernetes.io/ko/docs/concepts/storage/storage-classes/)

---

### (3) EBS vs EFS

| 항목    | EBS          | EFS                |
| ----- | ------------ | ------------------ |
| 타입    | 블록 스토리지      | 파일 스토리지            |
| 사용 방식 | 단일 EC2에 연결   | 여러 EC2 인스턴스가 공유 가능 |
| 확장성   | 수동           | 자동 확장              |
| 사용 예시 | DB, 로그, 단일 앱 | 공유 파일 시스템, 웹서버 등   |

---

## 7. 인증과 권한 (aws-auth ConfigMap)

EKS에서는 IAM Role과 Kubernetes RBAC(Role-Based Access Control)을 연결하기 위해
**`aws-auth` ConfigMap**을 사용합니다.

```bash
kubectl get configmaps -n kube-system
kubectl describe configmaps aws-auth -n kube-system
```

`aws-auth` 안에는 `mapRoles`, `mapUsers` 필드가 있어
EC2 인스턴스나 IAM 사용자를 K8s 내부 권한으로 매핑할 수 있습니다.

---

## 8. 과금 구조

* EKS 클러스터별 **$0.10/hour**
* EC2, EBS 등 **리소스 사용료 별도**
* **프리티어 미지원**

> [EKS 요금 안내](https://aws.amazon.com/ko/eks/pricing/)

---

## 요약

| 항목             | 설명                              |
| -------------- | ------------------------------- |
| 관리형 Kubernetes | Control Plane 자동 관리             |
| AWS 서비스 통합     | IAM, ECR, ELB, CloudWatch 등과 연동 |
| 네트워크           | VPC CNI, PrivateLink 지원         |
| 스토리지           | EBS, EFS CSI 지원                 |
| 권한 관리          | aws-auth ConfigMap              |
| 과금             | 클러스터 단위 시간당 과금                  |

---

## 참고 문서

* [EKS 공식 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/what-is-eks.html)
* [EKS Networking](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/network-load-balancing.html)
* [AWS Load Balancer Controller](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)
* [EBS CSI Driver](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/ebs-csi.html)
* [EFS CSI Driver](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/efs-csi.html)
* [Kubernetes StorageClass](https://kubernetes.io/ko/docs/concepts/storage/storage-classes/)
* [EKS Pricing](https://aws.amazon.com/ko/eks/pricing/)
