# Terraform을 이용한 AWS 인프라 구축

## 목표

> * Terraform을 사용하여 AWS 인프라를 코드로 관리(IaC: Infrastructure as Code)하는 방법을 학습한다.
> * EKS, Bastion, ArgoCD 등 GitOps 기반 배포 환경을 자동으로 구성한다.
> * Terraform 주요 파일 구조와 커스터마이징 방법을 이해한다.

---

## 1. Terraform 코드 다운로드 및 구조 이해

### Terraform이란?

Terraform은 **클라우드 인프라를 코드로 관리**(IaC)할 수 있는 오픈소스 도구입니다.
AWS, Azure, GCP 등 다양한 클라우드 자원을 선언형 코드로 정의하고 자동으로 프로비저닝할 수 있습니다.

### 코드 다운로드

GitHub에서 미리 작성된 Terraform 코드를 다운로드합니다.

```bash
git clone https://github.com/237summit/gitops_terraform.git
cd gitops_terraform
```

---

### 저장소 구조 요약

| 파일/폴더                 | 역할                                                                 |
| --------------------- | ------------------------------------------------------------------ |
| `main.tf`             | 전체 인프라 orchestration — VPC, IAM, EKS, Bastion, ECR, ArgoCD 모듈 호출   |
| `variables.tf`        | 리전, 클러스터 이름, VPC CIDR, Subnet, NodeGroup 등 변수 정의                   |
| `providers.tf`        | AWS / Kubernetes / Helm provider 연결 (EKS 프로비저닝 후 Helm으로 ArgoCD 설치) |
| `outputs.tf`          | EKS endpoint, ECR repo URL, 보안그룹 등 주요 정보 출력                        |
| `modules/`            | 인프라 기능별 독립 모듈 디렉토리 (재사용 가능)                                        |
| ├── `vpc`             | VPC 생성 (terraform-aws-modules/vpc/aws 모듈 사용)                       |
| ├── `iam`             | EKS Cluster / NodeGroup IAM Role 정의                                |
| ├── `eks`             | EKS 클러스터 생성 (terraform-aws-modules/eks/aws 모듈 사용)                  |
| ├── `ecr`             | 컨테이너 이미지 저장용 ECR 생성                                                |
| ├── `bastion`         | Terraform 실행 및 클러스터 관리용 Bastion Host 생성                            |
| ├── `security_groups` | SG 설정 (Bastion ↔ EKS / Node ↔ API 통신)                              |
| └── `argocd`          | Helm을 이용한 ArgoCD 배포 및 values.yaml 적용                               |

---

## 2. 환경 맞춤 설정 (variables.tf 수정)

### 주요 변수 정의 예시

```hcl
variable "region" {
  default = "ap-northeast-2"   # 서울 리전
}

variable "cluster_name" {
  default = "petclinic-cluster"   # EKS 클러스터 이름
}

variable "vpc_cidr" {
  default = "10.0.0.0/16"   # VPC CIDR 블록
}

variable "azs" {
  default = ["ap-northeast-2a", "ap-northeast-2c"]   # 가용 영역
}

variable "private_subnets" {
  default = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "public_subnets" {
  default = ["10.0.4.0/24", "10.0.5.0/24"]
}

variable "cluster_version" {
  default = "1.32"   # EKS 버전
}

variable "namespace" {
  default = "petclinic"   # 기본 네임스페이스
}

variable "node_groups" {
  description = "EKS 노드 그룹 설정"
  type = map(object({
    name           = string
    instance_types = list(string)
    min_size       = number
    max_size       = number
    desired_size   = number
    disk_size      = number
  }))
  default = {
    eks_node_gitops = {
      name           = "eks_node_gitops"
      instance_types = ["t3.medium"]
      min_size       = 1
      max_size       = 3
      desired_size   = 2
      disk_size      = 20
    }
  }
}

variable "bastion_key_name" {
  description = "Bastion Host SSH 키 이름"
  default     = "fs-key"
}
```

> **사용자 환경에 따라 수정할 항목**
>
> * `region`: AWS 리전
> * `cluster_name`: EKS 클러스터 이름
> * `bastion_key_name`: EC2 로그인 키
> * `subnets`, `azs`: 사용 중인 네트워크 구조에 맞게 조정

---

### 모듈 디렉토리 구조 예시

```bash
modules/
├── argocd
│   ├── application.yaml
│   ├── main.tf
│   ├── values.yaml
│   └── variables.tf
├── bastion
│   ├── main.tf
│   ├── user_data.tpl
│   └── variables.tf
├── ecr
│   ├── main.tf
│   └── variables.tf
├── eks
│   ├── main.tf
│   └── variables.tf
├── iam
│   ├── main.tf
│   └── variables.tf
├── security_groups
│   ├── main.tf
│   └── variables.tf
└── vpc
    ├── main.tf
    └── variables.tf
```

---

## 3. Bastion 초기화 스크립트(user_data.tpl)

Bastion EC2 인스턴스가 부팅될 때 실행되는 자동 설정 스크립트입니다.
EKS 클러스터 연결, kubectl 설치, ArgoCD 초기 비밀번호 조회까지 자동화합니다.

```bash
#!/bin/bash
# 시스템 업데이트 및 패키지 설치
sudo apt-get update && apt-get upgrade -y
sudo apt-get install -y unzip curl wget jq

# AWS CLI 설치
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip && sudo ./aws/install

# kubectl 설치
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl && sudo mv kubectl /usr/local/bin/

# AWS 기본 리전 설정
mkdir -p /home/ubuntu/.aws
echo "[default]" > /home/ubuntu/.aws/config
echo "region = ap-northeast-2" >> /home/ubuntu/.aws/config

# EKS 클러스터 연결 설정
aws eks update-kubeconfig --name petclinic-cluster --region ap-northeast-2

# kubectl 환경 설정
chown -R ubuntu:ubuntu /home/ubuntu/.kube /home/ubuntu/.aws
echo "export KUBECONFIG=/home/ubuntu/.kube/config" >> /home/ubuntu/.bashrc
echo "source <(kubectl completion bash)" >> /home/ubuntu/.bashrc

# ArgoCD 관리자 비밀번호 확인
kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath="{.data.password}" | base64 -d

echo "Bastion 초기 설정 완료"
```

---

## 4. ArgoCD 구성 (modules/argocd/application.yaml)

GitOps의 핵심은 **Git 저장소의 변경을 감지해 자동 배포하는 것**입니다.
ArgoCD 모듈에서는 GitHub 저장소 URL을 지정해야 합니다.

```yaml
spec:
  project: default
  source:
    repoURL: https://github.com/<사용자명>/gitops_petclinic.git
    targetRevision: HEAD
```

> 반드시 자신의 GitHub 리포지토리 주소로 수정하세요.

---

## 5. Terraform 명령어 실행

### 1. 초기화

```bash
terraform init -upgrade
```

> Terraform 플러그인 다운로드 및 작업 디렉토리 초기화

### 2. 실행 계획 검토

```bash
terraform plan
```

> 실제 리소스 생성 전 변경사항을 미리 검토

### 3. 인프라 생성

```bash
terraform apply --auto-approve
```

> 코드에 정의된 AWS 리소스를 자동으로 생성
> (EKS, Bastion, ECR, ArgoCD 등 포함)

---

## 결과: 생성되는 주요 인프라 구성도

| 구성 요소                    | 설명                           |
| ------------------------ | ---------------------------- |
| **VPC**                  | 기본 네트워크 환경                   |
| **EKS 클러스터**             | 애플리케이션이 배포될 Kubernetes 클러스터  |
| **ECR**                  | 애플리케이션 컨테이너 이미지를 저장하는 레지스트리  |
| **Bastion Host**         | Terraform 관리 및 클러스터 접근용 EC2  |
| **ArgoCD**               | GitOps 기반 자동 배포 도구 (Helm 설치) |
| **IAM / Security Group** | 권한 및 접근 제어 설정                |

---

## 요약

| 단계           | 내용                                           |
| ------------ | -------------------------------------------- |
| 코드 다운로드      | Terraform 프로젝트 클론                            |
| 변수 설정        | `variables.tf` 사용자 환경에 맞게 수정                 |
| 모듈 구성        | 네트워크, 보안, Bastion, EKS, ArgoCD 자동화           |
| 명령어 실행       | `init → plan → apply` 순으로 인프라 생성             |
| GitOps 환경 구축 | EKS + ArgoCD + GitHub Actions 기반 자동 배포 환경 완성 |


