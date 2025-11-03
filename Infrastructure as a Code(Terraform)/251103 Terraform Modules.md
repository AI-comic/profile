# Terraform Modules


> **목표**
>
> * Terraform 모듈의 개념과 역할을 이해한다.
> * 로컬/원격/Registry 모듈의 차이를 구분할 수 있다.
> * `terraform-aws-modules`를 활용해 **VPC + Subnet + NAT/IGW + Bastion** 구성을 빠르게 배포한다.
> * 로컬 모듈을 Registry 모듈로 **무중단 전환**하는 과정을 익힌다.

---

## 1. Terraform 모듈이란?

Terraform **모듈(Module)** 은 반복적으로 쓰이는 리소스 코드를 묶은 **재사용 가능한 구성 단위**다.
복잡한 인프라를 계층적으로 관리하고, 코드 중복을 줄여 유지보수를 단순화할 수 있다.

### 모듈의 특징

* **재사용성**: 한 번 정의한 모듈을 여러 환경에서 재활용 가능
* **구조화**: 인프라를 기능별로 나눠 관리 (예: VPC, EC2, IAM 등)
* **버전 관리**: Git, S3, Terraform Registry 등에서 버전별로 관리 가능
* **간결성**: 공통 패턴을 모듈화하여 코드량 감소

---

## 2. 모듈 구조와 기본 사용법

Terraform은 **Root 모듈**(main.tf가 있는 상위 디렉토리)에서 시작해 **Child 모듈**을 호출한다.

```bash
gitops_terraform/
├── main.tf           # Root 모듈 (시작점)
├── providers.tf
├── variables.tf
├── outputs.tf
└── modules/
    └── vpc/          # Child 모듈
        ├── main.tf
        ├── variables.tf
        ├── outputs.tf
```

---

### 로컬 모듈 호출 예시

```hcl
module "vpc" {
  source = "./modules/vpc"

  vpc_cidr            = var.vpc_cidr
  tags                = var.tags
  public_subnet_cidrs = var.public_subnet_cidrs
}
```

---

### 원격 모듈 호출 예시

```hcl
# GitHub에서 불러오기
module "vpc" {
  source = "git::https://github.com/my-org/infra-modules.git//vpc?ref=v1.2.3"
}

# S3에서 불러오기
module "vpc" {
  source = "s3::https://my-bucket.s3.amazonaws.com/modules/vpc.zip"
}
```

---

### Terraform Registry 모듈 호출 예시

```hcl
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.1"

  name = "lab-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["ap-northeast-2a", "ap-northeast-2c"]
  public_subnets  = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnets = ["10.0.3.0/24", "10.0.4.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true
  enable_vpn_gateway = true
}
```

> **Terraform Registry**: [https://registry.terraform.io/modules/terraform-aws-modules](https://registry.terraform.io/modules/terraform-aws-modules)

---

## 3. 로컬 모듈 실습 예제

디렉터리 구조:

```bash
lab-step1-vpc-module/
├── provider.tf
├── variables.tf
├── main.tf
├── outputs.tf
└── modules/
    └── vpc/
        ├── main.tf
        ├── variables.tf
        └── outputs.tf
```

### Root `main.tf`

```hcl
module "vpc" {
  source = "./modules/vpc"
  vpc_cidr = var.vpc_cidr
  tags = var.tags
  public_subnet_cidrs = var.public_subnet_cidrs
}
```

### Child `modules/vpc/main.tf`

```hcl
resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                 = var.tags
}

data "aws_availability_zones" "available" { state = "available" }

resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidrs[0]
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
  tags = merge(var.tags, { Name = "${lookup(var.tags, "Name", "vpc")}-pub-a" })
}
```

---

## 4. Terraform Registry 모듈 활용

Terraform 공식 모듈을 활용하면 VPC, Subnet, NAT, IGW 등을 직접 정의하지 않아도 된다.

```hcl
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.1"

  name = "training-vpc"
  cidr = "10.10.0.0/16"
  azs  = ["ap-northeast-2a", "ap-northeast-2c"]

  public_subnets  = ["10.10.1.0/24", "10.10.2.0/24"]
  private_subnets = ["10.10.3.0/24", "10.10.4.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true

  tags = {
    Owner       = "Training"
    Environment = "Dev"
  }
}
```

---

## 5. 실전: Bastion 포함 인프라 구축

다음 구조를 생성한다.

```
VPC
├─ Public Subnet (Bastion)
└─ Private Subnet (App/Web)
```

### 디렉토리 구조

```bash
poly10-registry/
├── provider.tf
├── variables.tf
├── main.tf
└── outputs.tf
```

### main.tf

```hcl
data "aws_availability_zones" "available" { state = "available" }

locals {
  azs = slice(data.aws_availability_zones.available.names, 0, 2)
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.1"

  name = var.name
  cidr = var.vpc_cidr
  azs  = local.azs

  public_subnets  = var.public_subnet_cidrs
  private_subnets = var.private_subnet_cidrs

  enable_nat_gateway   = true
  single_nat_gateway   = true
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = var.tags
}
```

> 이 구조는 실무에서도 자주 사용되며, `Bastion → Private EC2` 접근 구조를 구현할 때 유용하다.

---

## 정리

| 구분              | 설명                 | 예시                                         |
| --------------- | ------------------ | ------------------------------------------ |
| **로컬 모듈**       | 프로젝트 내부 디렉터리로 관리   | `source = "./modules/vpc"`                 |
| **원격 모듈**       | Git/S3 등 외부 저장소 참조 | `source = "git::https://...repo.git//vpc"` |
| **Registry 모듈** | Terraform 공식 모듈 사용 | `source = "terraform-aws-modules/vpc/aws"` |

---

## 실습 추천 순서

1. 로컬 모듈로 VPC/Subnet 구성
2. Registry 모듈로 동일 인프라 재구성
3. Bastion 인스턴스 추가로 SSH 테스트
