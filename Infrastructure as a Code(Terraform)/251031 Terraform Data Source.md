# Terraform Data Source

> **목표**
>
> * Terraform **Data Source** 개념을 이해한다.
> * Data Source를 사용해 **기존 리소스(AMI, VPC, Subnet 등)** 를 동적으로 조회한다.
> * Data Source로 조회한 **Ubuntu 22.04/24.04 AMI**를 활용해 EC2 인스턴스를 생성한다.

---

## 1. Data Source 개요

### 1.1 Data Source란?

Terraform에서 **이미 존재하는 리소스를 읽어와 사용하는 방법**을 말합니다.
`resource`가 **새로운 리소스를 생성**하는 블록이라면,
`data`는 **기존 리소스를 읽기 전용(Read-Only)** 으로 참조합니다.

#### 사용 목적

* **동적 조회**: 매번 바뀌는 최신 값(예: 최신 AMI ID)을 자동으로 검색
* **재사용성**: 이미 존재하는 VPC, 서브넷, 보안그룹을 코드에 재활용
* **일관성**: 여러 모듈·환경에서 공통된 값을 유지

#### 기본 문법

```hcl
data "<PROVIDER>_<TYPE>" "<NAME>" {
  [CONFIG ...]
}
```

| 항목             | 설명                               |
| -------------- | -------------------------------- |
| `<PROVIDER>`   | 공급자 이름 (예: `aws`)                |
| `<TYPE>`       | Data Source 유형 (예: `ami`, `vpc`) |
| `<NAME>`       | 코드 내에서 참조할 이름                    |
| `[CONFIG ...]` | 필터 조건 및 설정값                      |

---

### 1.2 Data Source 속성 참조

* **정의 구문**

  ```hcl
  data "<리소스 유형>" "<이름>" {
    <인수> = <값>
  }
  ```

* **참조 문법**

  ```
  data.<리소스 유형>.<이름>.<속성>
  ```

**예시**

```hcl
data "aws_vpc" "default" {
  default = true
}

data "aws_subnet_ids" "default" {
  vpc_id = data.aws_vpc.default.id
}
```

 `data.aws_vpc.default.id` : 기본 VPC의 ID
 `data.aws_subnet_ids.default.ids[0]` : 첫 번째 서브넷의 ID

---

## 2. 최신 AMI 조회 방식

Terraform에서 최신 AMI를 불러오는 방법은 **두 가지**가 있습니다.

---

### (1) aws_ami 방식

* **특정 조건**(이름, 아키텍처, 버전 등)을 직접 필터링
* 특정 버전을 **고정**해서 운영해야 할 때 유용

```hcl
# Ubuntu 24.04 최신 AMI 조회
data "aws_ami" "ubuntu2404" {
  most_recent = true
  owners      = ["099720109477"]  # Canonical 공식 계정 ID

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

output "ami_id" {
  value = data.aws_ami.ubuntu2404.id
}

output "public_ip" {
  value = aws_instance.ubuntu.public_ip
}
```

---

### (2) SSM Parameter Store 방식

* AWS System Manager(SSM)의 Parameter Store에서 **공식 AMI ID**를 직접 가져옴
* 항상 최신 버전 유지 (AWS가 자동 갱신)
* CI/CD 파이프라인 등 **운영 환경**에서 권장

```hcl
# Ubuntu 24.04 LTS 최신 AMI 조회
data "aws_ssm_parameter" "ubuntu2404_amd64" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

# output 예시
output "ami_id" {
  value     = data.aws_ssm_parameter.ubuntu2404_amd64.value
  sensitive = true
}

output "public_ip" {
  value = aws_instance.ubuntu.public_ip
}
```

> `aws_ssm_parameter`의 `value` 속성은 기본적으로 **민감(sensitive)** 처리됩니다.
> 이는 Terraform이 자동으로 보안상 숨김 표시를 적용하기 때문입니다.

---

## 3. 실습 예시

### 디렉터리 구조

```bash
terraform-aws-ec2-demo/
├── provider.tf
├── main.tf
└── outputs.tf
```

### provider.tf

```hcl
terraform {
  required_version = ">= 1.12.0, < 1.14.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = "ca-central-1"
}
```

---

### main.tf

```hcl
# 기본 VPC 조회
data "aws_vpc" "default" {
  default = true
}

# 기본 VPC의 서브넷 목록 조회
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Ubuntu 24.04 LTS (Noble) 최신 AMI 조회
data "aws_ami" "ubuntu2404" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2 인스턴스 생성
resource "aws_instance" "ubuntu" {
  ami           = data.aws_ami.ubuntu2404.id
  instance_type = "t3.micro"
  subnet_id     = data.aws_subnets.default.ids[0]

  tags = {
    Name = "lab-ubuntu2404"
  }
}
```

---

### outputs.tf

```hcl
output "ami_id" {
  value = data.aws_ami.ubuntu2404.id
}

output "public_ip" {
  value = aws_instance.ubuntu.public_ip
}
```

---

### 실행 순서

```bash
terraform fmt
terraform init -upgrade
terraform plan
terraform apply --auto-approve
terraform destroy --auto-approve
```

---

## 4. Amazon Linux 2023 AMI 조회 (SSM 방식)

Amazon 공식 계정(`137112412989`)의 **SSM Parameter Store**를 사용해 최신 AMI를 가져옵니다.

```hcl
data "aws_ssm_parameter" "al2023_amd64" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

resource "aws_instance" "al2023" {
  ami           = data.aws_ssm_parameter.al2023_amd64.value
  instance_type = "t3.micro"
  subnet_id     = data.aws_subnets.default.ids[0]

  tags = {
    Name = "lab10-amazon-linux-2023"
  }
}

output "al2023_ami_id" {
  value     = data.aws_ssm_parameter.al2023_amd64.value
  sensitive = true
}
```

