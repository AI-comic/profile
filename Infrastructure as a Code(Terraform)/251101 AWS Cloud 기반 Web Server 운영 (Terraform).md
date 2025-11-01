# AWS Cloud 기반 Web Server 운영 (Terraform)

> * Amazon VPC의 **퍼블릭/프라이빗 서브넷 구조**를 직접 설계하고 구축한다.
> * **NAT Gateway**를 통해 프라이빗 서브넷의 아웃바운드 트래픽을 제어한다.
> * **Bastion Host (점프 서버)** 를 퍼블릭 서브넷에 배포해 보안성을 높인다.
> * Terraform으로 **보안 그룹, EC2 인스턴스, 라우팅 구성**을 코드로 자동화한다.

---

## 1. AWS VPC 개념

AWS의 Virtual Private Cloud(VPC)는 AWS 리소스를 위한 **가상 네트워크 환경**이다.
온프레미스의 데이터센터 네트워크처럼 **CIDR 블록, 서브넷, 라우팅, 보안 그룹** 등을 자유롭게 설정할 수 있다.

### 주요 구성 요소

| 구성 요소                      | 역할                                   |
| -------------------------- | ------------------------------------ |
| **VPC**                    | AWS 상의 가상 네트워크 (CIDR 블록 기반)          |
| **Subnet**                 | VPC 내부 네트워크 구역 (Public / Private 분리) |
| **Internet Gateway (IGW)** | 퍼블릭 서브넷이 외부 인터넷과 통신하도록 연결            |
| **NAT Gateway**            | 프라이빗 서브넷에서 외부로만 나가도록 설정              |
| **Route Table**            | 트래픽을 전달할 경로를 지정하는 규칙 집합              |
| **Security Group**         | 인스턴스 수준의 방화벽 (인바운드/아웃바운드 제어)         |

---

## 2. Terraform으로 VPC 구현하기

이 실습에서는 Terraform을 이용해 다음 인프라를 자동으로 구축합니다.

* 1개의 VPC (`10.10.0.0/16`)
* 2개의 Public Subnet (AZ A, C)
* 2개의 Private Subnet (AZ A, B)
* Internet Gateway + NAT Gateway
* Web Server (EC2)
* Bastion Host

### 디렉터리 구조

```bash
terraform-aws-web/
├── provider.tf
├── vpc.tf
├── ec2.tf
└── outputs.tf
```

---

### 2.1 Provider 설정 (`provider.tf`)

```hcl
terraform {
  required_version = ">= 1.13"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2" # 서울 리전
}
```

---

### 2.2 VPC 및 네트워크 구성 (`vpc.tf`)

#### ① VPC 생성

```hcl
resource "aws_vpc" "lab" {
  cidr_block = "10.10.0.0/16"

  tags = {
    Name = "lab11-vpc"
  }
}
```

#### ② Public Subnet

```hcl
resource "aws_subnet" "pub_a" {
  vpc_id                  = aws_vpc.lab.id
  cidr_block              = "10.10.1.0/24"
  availability_zone       = "ap-northeast-2a"
  map_public_ip_on_launch = true
  tags = {
    Name = "lab11-pub-a"
    Tier = "public"
  }
}

resource "aws_subnet" "pub_c" {
  vpc_id                  = aws_vpc.lab.id
  cidr_block              = "10.10.2.0/24"
  availability_zone       = "ap-northeast-2c"
  map_public_ip_on_launch = true
  tags = {
    Name = "lab11-pub-c"
    Tier = "public"
  }
}
```

#### ③ Internet Gateway (IGW)

```hcl
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.lab.id
  tags = {
    Name = "lab11-igw"
  }
}
```

#### ④ 퍼블릭 라우팅 테이블

```hcl
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.lab.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "lab11-public-rt"
  }
}

# 각 퍼블릭 서브넷에 연결
resource "aws_route_table_association" "pub_a" {
  subnet_id      = aws_subnet.pub_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "pub_c" {
  subnet_id      = aws_subnet.pub_c.id
  route_table_id = aws_route_table.public.id
}
```

---

## 3. EC2 Web Server 배포

### 3.1 Security Group 생성

```hcl
resource "aws_security_group" "web_sg" {
  name        = "lab11-web-sg"
  description = "Allow SSH and HTTP"
  vpc_id      = aws_vpc.lab.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # 실습용 (운영 시 IP 제한)
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "lab11-web-sg"
  }
}
```

### 3.2 EC2 인스턴스 생성

```hcl
data "aws_ssm_parameter" "al2023_ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

resource "aws_instance" "web" {
  ami                    = data.aws_ssm_parameter.al2023_ami.value
  instance_type          = "t2.micro"
  subnet_id              = aws_subnet.pub_a.id
  vpc_security_group_ids = [aws_security_group.web_sg.id]
  key_name               = "exam-key"

  user_data = <<-EOF
              #!/bin/bash
              dnf update -y
              dnf install -y httpd
              systemctl enable httpd
              systemctl start httpd
              echo "<h1>My AWS Web Server</h1>" > /var/www/html/index.html
              EOF

  tags = {
    Name = "lab11-web"
  }
}
```

---

## 4. NAT Gateway 및 Private Subnet 구성

### 4.1 Private Subnet

```hcl
resource "aws_subnet" "pri_a" {
  vpc_id            = aws_vpc.lab.id
  cidr_block        = "10.10.3.0/24"
  availability_zone = "ap-northeast-2a"
  tags = { Name = "lab11-pri-a" }
}

resource "aws_subnet" "pri_b" {
  vpc_id            = aws_vpc.lab.id
  cidr_block        = "10.10.4.0/24"
  availability_zone = "ap-northeast-2b"
  tags = { Name = "lab11-pri-b" }
}
```

### 4.2 NAT Gateway 생성

```hcl
# Elastic IP
resource "aws_eip" "nat_eip" {
  domain = "vpc"
  tags = { Name = "lab11-nat-eip" }
}

# NAT Gateway
resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat_eip.id
  subnet_id     = aws_subnet.pub_c.id
  depends_on    = [aws_internet_gateway.igw]
  tags = { Name = "lab11-nat" }
}
```

### 4.3 Private Route Table

```hcl
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.lab.id
  tags   = { Name = "lab11-private-rt" }
}

resource "aws_route" "private_to_nat" {
  route_table_id         = aws_route_table.private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id         = aws_nat_gateway.nat.id
}

resource "aws_route_table_association" "pri_a_assoc" {
  subnet_id      = aws_subnet.pri_a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "pri_b_assoc" {
  subnet_id      = aws_subnet.pri_b.id
  route_table_id = aws_route_table.private.id
}
```

---

## 5. Terraform 실행 명령어

```bash
terraform fmt
terraform validate
terraform plan
terraform apply --auto-approve
terraform destroy --auto-approve
```

---

## 6. 정리

1. 퍼블릭/프라이빗 서브넷 구조를 설계하고, CIDR을 자신만의 범위로 변경해보기
2. Bastion Host를 `poly10-pub-b`에 생성하고, 프라이빗 EC2에 SSH로 접근 테스트
3. `outputs.tf`에서 VPC ID, 서브넷 이름, Bastion AMI 등을 출력하여 검증하기

---

> **Tip**
> Bastion Host는 외부에서 SSH 접속할 수 있는 유일한 진입점이므로,
> 반드시 **내 IP만 허용**하도록 설정하는 것이 보안의 핵심입니다.

