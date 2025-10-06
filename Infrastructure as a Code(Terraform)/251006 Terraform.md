## 1. IaC(Infrastructure as Code) 개요

### 1.1 IaC 정의
- 인프라를 **코드로 정의하고 관리**하는 방법론  
- 서버, 네트워크, DB 등 리소스를 코드로 설정 → 배포와 관리 자동화  
- 코드 기반이라 **재현 가능성**과 **일관성** 보장  

### 1.2 IaC의 이점
- **자동화**: 수동 설정 대신 반복 가능한 코드 실행  
- **버전 관리**: Git을 통해 변경 이력 추적, 롤백 가능  
- **팀 협업**: 공유 및 협업에 유리, 개발·운영 환경 일관성 확보  
- **확장성**: 동일 코드로 여러 환경 구성  

### 1.3 주요 IaC 도구 비교

| 도구 | 유형 | 지원 플랫폼 | 언어 | 상태 관리 | 적합 용도 |
|------|------|-------------|------|------------|-----------|
| **Terraform** | 선언형 | 멀티 클라우드 | HCL | 상태파일(.tfstate) | 복잡한 리소스 관리 |
| **CloudFormation** | 선언형 | AWS 전용 | JSON/YAML | AWS가 관리 | AWS 인프라 관리 |
| **Ansible** | 선언형+명령형 | 멀티 플랫폼 | YAML | 없음 | 서버 구성, 앱 배포 |

**IaC 도구 종류**  
Terraform, CloudFormation, ARM Templates, Ansible, Pulumi, Deployment Manager, Chef, SaltStack, Kubernetes Manifests, Crossplane

---

## 2. Terraform이란?

### 2.1 개요
- HashiCorp에서 개발한 **오픈소스 IaC 도구**  
- 선언형 방식, 멀티 클라우드 지원 (AWS, Azure, GCP, Kubernetes 등)  
- 주요 특징: 상태 관리, 멀티 클라우드 지원, 모듈화, 확장성  

```hcl
# AWS Provider
provider "aws" {
  region = "us-east-1"
}

# GCP Provider
provider "google" {
  project = "my-project"
  region  = "us-central1"
}

# AWS EC2
resource "aws_instance" "aws_vm" {
  ami           = "ami-123456"
  instance_type = "t2.micro"
}

# GCP VM
resource "google_compute_instance" "gcp_vm" {
  name         = "gcp-vm"
  machine_type = "n1-standard-1"
  zone         = "us-central1-a"
}
```

### 2.2 Terraform 워크플로우
- **Write**: 구성 작성 (HCL 기반)
- **Plan**: 실행 계획 검증 (`terraform plan`)
- **Apply**: 실제 리소스 생성 (`terraform apply`)
- **Destroy**: 리소스 제거 (`terraform destroy`)

### 2.3 실습 환경 구성 (AWS)
- 1. AWS EC2 인스턴스 생성 (Amazon Linux 2023, t3.medium)
- 2. 보안그룹 생성 (SSH/HTTP/HTTPS 허용)
- 3. MobaXterm 원격 접속
- 4. AWS IAM Access Key 발급 및 CLI 설정
```bash
aws configure
aws sts get-caller-identity
```
- 5. Terraform 설치 (Amazon Linux 2023 예시)
```bash
sudo dnf install -y yum-utils
sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
sudo dnf install -y terraform
terraform -version
terraform -install-autocomplete
```
---
## 3. Terraform 기본 사용법

### 3.1 Terraform 기본 명령어

| 명령어                  | 설명                |
| -------------------- | ----------------- |
| `terraform`          | 명령어 목록 표시         |
| `terraform version`  | 버전 확인             |
| `terraform init`     | 초기화 (provider 설치) |
| `terraform plan`     | 실행 계획 미리보기        |
| `terraform apply`    | 인프라 생성/변경         |
| `terraform destroy`  | 리소스 삭제            |
| `terraform validate` | 코드 유효성 검사         |
| `terraform fmt`      | 코드 포맷 정리          |

### 3.2 코드 예시
```hcl
resource "aws_vpc" "lab-vpc" {
  cidr_block       = "10.0.0.0/16"
  instance_tenancy = "dedicated"
}
```

---
## 4. Terraform 블록과 Resource 블록

### 4.1 Terraform 블록

- 프로젝트 전반 설정 (CLI/Provider 버전, backend 저장소 등)
```hcl
terraform {
  required_version = ">= 1.13.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
  backend "s3" {
    bucket = "my-terraform-state"
    key    = "dev/terraform.tfstate"
    region = "자기 리전을 넣는 곳"
  }
}
```
### 4.2 Resource 블록 구조
```hcl
resource "<PROVIDER>_<TYPE>" "<NAME>" {
  [CONFIG ...]
}
```
- 예시: Local Provider 파일 생성
  ```hcl
  resource "local_file" "foo" {
    content  = "hello world!"
    filename = "${path.module}/foo.txt"
  }
  ```

---

## 5. 실습

📂 파일 구조
```
terraform-test/
├── provider.tf
├── vpc.tf
└── outputs.tf
```
provider.tf
```hcl
terraform {
  required_version = ">= 1.13.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = "자기 리전을 넣는 곳"
}
```

vpc.tf
```hcl
resource "aws_vpc" "test" {
  cidr_block = "10.10.0.0/16"
  tags = {
    Name = "vpc-sample"
  }
}
```

outputs.tf
```
output "output-vpc" {
  value = aws_vpc.test
}
```

실행
```bash
terraform init
terraform plan
terraform apply
terraform destroy --auto-approve
```
