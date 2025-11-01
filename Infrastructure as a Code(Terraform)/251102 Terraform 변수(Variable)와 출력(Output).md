# Terraform 변수(Variable)와 출력(Output)


> **목표**
>
> * Terraform의 **변수(variable)** 개념과 활용법을 이해한다.
> * **출력(output)** 블록을 통해 리소스 정보를 확인하고 재활용하는 방법을 익힌다.
> * `variable.tf`와 `outputs.tf`를 직접 작성해 Terraform 코드의 구조화와 확장성을 높인다.

---

## 1. Variable(변수) 이해하기

Terraform의 변수는 **반복적으로 사용하는 값들을 따로 분리해 관리**할 수 있게 해준다.
이를 통해 코드의 **가독성, 재사용성, 유지보수성**이 모두 향상된다.

> 공식 문서: [https://developer.hashicorp.com/terraform/language/block/variable](https://developer.hashicorp.com/terraform/language/block/variable)

### 변수 선언 기본 문법

```hcl
variable "<이름>" {
  description = "<설명>"
  default     = <기본값>
  type        = <타입>
  sensitive   = <true|false>
  nullable    = <true|false>

  validation {
    condition     = <표현식>
    error_message = "<에러 메시지>"
  }
}
```

| 속성            | 설명                                            |
| ------------- | --------------------------------------------- |
| `description` | 변수의 설명                                        |
| `default`     | 기본값 (없을 경우 실행 시 입력 필요)                        |
| `type`        | 데이터 타입 지정 (string, number, bool, list, map 등) |
| `sensitive`   | 민감 정보로 처리 (콘솔에 값이 표시되지 않음)                    |
| `validation`  | 변수 값 검증 규칙 정의                                 |

---

### 예시 1: 기본 변수 선언

```hcl
variable "aws_region" {
  description = "리소스를 생성할 AWS 리전"
  default     = "ap-northeast-2"
}
```

### 예시 2: 타입 지정 및 리스트/맵 사용

```hcl
variable "project_name" {
  description = "프로젝트 접두사"
  type        = string
  default     = "lab12"
}

variable "public_subnet_cidrs" {
  description = "퍼블릭 서브넷 CIDR 목록"
  type        = list(string)
  default     = ["10.10.1.0/24", "10.10.2.0/24"]
}

variable "tags" {
  description = "공통 태그"
  type        = map(string)
  default = {
    Owner       = "Training"
    Environment = "Dev"
  }
}
```

### 예시 3: 검증 및 민감 변수

```hcl
variable "instance_type" {
  description = "EC2 인스턴스 타입"
  type        = string
  default     = "t2.micro"

  validation {
    condition     = can(regex("^t[2-4]\\.", var.instance_type))
    error_message = "t2/t3/t4 계열만 허용합니다. 예: t3.micro"
  }
}

variable "db_password" {
  description = "DB 접속 비밀번호"
  type        = string
  sensitive   = true  # 출력/로그 시 마스킹 처리
}
```

---

### 변수 사용법

변수는 `var.<변수이름>` 형태로 참조한다.

예시:

```hcl
# variables.tf
variable "aws_region" {
  description = "리소스를 생성할 AWS 리전"
  default     = "ap-northeast-2"
}

# provider.tf
provider "aws" {
  region = var.aws_region
}
```

---

## 2. Output(출력) 이해하기

Terraform의 Output은 실행 후 **생성된 리소스의 중요한 값을 외부로 출력**하기 위한 기능이다.
예를 들어, VPC ID, 서브넷 ID, Bastion 접속 명령어 등을 확인할 때 사용된다.

> 공식 문서: [https://developer.hashicorp.com/terraform/language/block/output](https://developer.hashicorp.com/terraform/language/block/output)

### 기본 문법

```hcl
output "<이름>" {
  description = "<설명>"
  value       = <표현식>       # 필수
  sensitive   = <true|false>   # 선택
  depends_on  = [<리소스 참조>] # 선택
}
```

| 속성            | 설명                     |
| ------------- | ---------------------- |
| `value`       | 출력할 표현식 (필수)           |
| `description` | 출력 내용의 의미 설명 (권장)      |
| `sensitive`   | 콘솔에서 마스킹할 값 지정         |
| `depends_on`  | 특정 리소스 이후 출력되도록 의존성 명시 |

---

### 자주 사용하는 출력 예시

#### 리소스 ID 출력

```hcl
output "vpc_id" {
  description = "생성된 VPC ID"
  value       = aws_vpc.this.id
}
```

#### 목록 출력

```hcl
output "public_subnet_names" {
  description = "퍼블릭 서브넷 이름 목록"
  value = [
    aws_subnet.pub_a.tags["Name"],
    aws_subnet.pub_b.tags["Name"],
  ]
}
```

#### 가공된 문자열 출력 (SSH 접속 명령어 예시)

```hcl
output "bastion_ssh_command" {
  description = "Bastion SSH 접속 명령어"
  value       = "ssh -i <KEY_PATH> ubuntu@${aws_instance.bastion.public_ip}"
}
```

#### 민감 값 출력 예시

```hcl
output "bastion_ami_id" {
  description = "Bastion AMI ID (Ubuntu 24.04)"
  value       = nonsensitive(data.aws_ssm_parameter.ubuntu2404.value)
}
```

---

### 운영 시 주의사항

* **필요한 값만 출력**해야 보안과 가독성이 유지된다.
* 비밀번호, 토큰 등은 `sensitive = true` 로 설정한다.
* 팀 협업 시, 루트 모듈에서는 “서비스 운영에 필요한 최소 정보만 출력”하는 것이 원칙이다.

---

## 3. 실습: Variable과 Output 적용하기

### 1단계 — VPC 생성

#### 디렉터리 구조

```bash
lab12-variable-output/
├── provider.tf
├── variables.tf
├── vpc.tf
└── outputs.tf
```

#### provider.tf

```hcl
terraform {
  required_version = ">= 1.4.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}
```

#### variables.tf

```hcl
variable "aws_region" {
  description = "AWS 리전 (예: ap-northeast-2)"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR (예: 10.10.0.0/16)"
  type        = string
}

variable "tags" {
  description = "공통 태그"
  type        = map(string)
  default = {
    Owner       = "Training"
    Environment = "Dev"
    Name        = "lab12-vpc"
  }
}
```

#### vpc.tf

```hcl
resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                 = var.tags
}
```

#### outputs.tf

```hcl
output "vpc_id"   { value = aws_vpc.this.id }
output "vpc_cidr" { value = aws_vpc.this.cidr_block }
output "region"   { value = var.aws_region }
```

---

### 2단계 — 퍼블릭 서브넷 추가 및 출력

#### variables.tf (추가)

```hcl
variable "public_subnet_cidrs" {
  description = "퍼블릭 서브넷 CIDR 목록 (A, B)"
  type        = list(string)
  default     = ["10.10.1.0/24", "10.10.2.0/24"]
}
```

#### vpc.tf (서브넷 및 IGW 추가)

```hcl
data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidrs[0]
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
  tags = merge(var.tags, { Name = "${var.tags["Name"]}-pub-a" })
}

resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidrs[1]
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true
  tags = merge(var.tags, { Name = "${var.tags["Name"]}-pub-b" })
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags   = merge(var.tags, { Name = "${var.tags["Name"]}-igw" })
}
```

#### outputs.tf (서브넷 출력 추가)

```hcl
output "public_subnet_ids" {
  description = "퍼블릭 서브넷 ID 목록 (A, B)"
  value       = [aws_subnet.public_a.id, aws_subnet.public_b.id]
}

output "public_subnet_names" {
  description = "퍼블릭 서브넷 이름 태그 (A, B)"
  value = [
    aws_subnet.public_a.tags["Name"],
    aws_subnet.public_b.tags["Name"],
  ]
}
```

---

## 4. Terraform 실행 명령어

```bash
terraform init
terraform fmt
terraform validate
terraform plan
terraform apply --auto-approve
terraform output
terraform destroy --auto-approve
```

---

## 정리

| 구분                | 핵심 포인트                       |
| ----------------- | ---------------------------- |
| **Variable**      | 코드 재사용성 향상, 설정값을 별도로 관리      |
| **Output**        | 리소스 정보를 확인하거나 다른 모듈에서 활용     |
| **보안 관리**         | 민감 정보는 `sensitive = true` 사용 |
| **Best Practice** | 불필요한 출력은 지양, 의미 있는 값만 명시     |

---

> **Tip:**
> 변수와 출력은 Terraform 프로젝트의 구조를 “유지보수 가능한 코드”로 만드는 핵심입니다.
> 실습 후, 직접 `variable.tf`와 `outputs.tf`를 수정해보며 자신만의 템플릿으로 발전시켜 보세요.

