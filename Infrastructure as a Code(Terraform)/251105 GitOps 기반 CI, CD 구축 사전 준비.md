# GitOps 기반 CI, CD 구축 사전 준비

## 목표

> * GitOps 개념과 CI/CD 환경의 기반을 이해한다.
> * Terraform, GitHub Actions, AWS CLI 등을 사용해 인프라를 자동화할 수 있다.
> * EKS 기반 배포 자동화를 위한 사전 준비를 완료한다.

---

## 주요 키워드

* GitOps
* CI/CD
* Terraform
* AWS IAM
* GitHub Actions
* AWS CLI

---

## 1. AWS 환경 구성

### 1. IAM 사용자 생성 및 액세스 키 발급

* Terraform으로 AWS 리소스(EC2, EKS 등)를 프로비저닝하기 위해 **Administrator 권한**을 가진 IAM 사용자 필요
* IAM 사용자 생성 후 **Access Key / Secret Key** 발급

**절차**

1. AWS 콘솔 로그인
2. `IAM > 사용자 > 추가` 선택
3. 권한 정책: `AdministratorAccess` 부여
4. `보안 자격 증명` 탭 → `액세스 키 생성`
5. 발급된 **Access Key / Secret Key**를 안전하게 보관

> 이후 `aws configure` 명령어에서 이 키를 사용합니다.

### 2. EC2 로그인 키 생성

* 관리 서버나 Terraform이 접근할 EC2 인스턴스에 접속하기 위한 PEM 키 생성
* AWS 콘솔 > **EC2 > 키 페어 생성**

  * 이름: `fs-key.pem`
  * PEM 형식으로 다운로드 후 안전하게 보관

---

## 2. GitHub 리포지토리 준비

### 1. GitHub 계정 생성

* [GitHub 공식 사이트](https://github.com) → **Sign up** 클릭 후 계정 생성

### 2. 개인 액세스 토큰 생성

* **GitHub Actions → AWS / Terraform** 연결 시 인증용으로 사용
* 경로: `Settings > Developer Settings > Personal access tokens > Tokens (classic)`
* [Generate new token] 선택 후 권한 부여:

  *  `repo`
  *  `workflow`
* 발급된 토큰은 별도로 저장 (예: `PAT` 환경 변수로 등록 예정)

### 3. GitHub Repository 생성

* 이름: `gitops_petclinic`
* 이 리포지토리에서 **코드 및 CI/CD 파이프라인 관리**

### 4. GitHub Actions 권한 설정

* 경로: `Repository > Settings > Actions > General`
* **Workflow permissions → Read and write permissions** 선택
* 저장 후 [Save] 클릭

---

## 3. Terraform 설치 (관리 서버용)

Terraform을 통해 AWS 인프라(EKS, EC2 등)를 자동으로 구성합니다.

### 설치 명령어 (Amazon Linux 기준)

```bash
sudo hostnamectl set-hostname mgmt
sudo timedatectl set-timezone Asia/Seoul

sudo dnf install -y yum-utils
sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
sudo dnf install -y terraform

terraform -version
```

**출력 예시**

```
Terraform v1.13.4
on linux_amd64
```

> 설치 후 Terraform 코드를 저장할 디렉토리를 생성해 두세요.
> 예: `/home/ec2-user/terraform-infra`

---

## 4. Git 설치

Terraform 코드 및 설정 파일을 GitHub에 업로드하기 위해 필요합니다.

```bash
sudo yum install -y git
git --version
```

설치 후 Git이 동작하는지 확인:

```bash
git help
```

---

## 5. AWS CLI 설치 및 프로필 설정

### 설치 확인

```bash
aws --version
```

### 프로필 등록

```bash
aws configure
```

**입력 예시**

```
AWS Access Key ID [None]: <발급한 Access Key>
AWS Secret Access Key [None]: <발급한 Secret Key>
Default region name [None]: ap-northeast-2
Default output format [None]: json
```

### 계정 정보 확인

```bash
aws sts get-caller-identity
```

**출력 예시**

```json
{
  "UserId": "AIDAXXXXXXX6AA",
  "Account": "123456789012",
  "Arn": "arn:aws:iam::123456789012:user/admin"
}
```

---

## 6. GitHub Repository Secrets 등록

GitHub Actions가 AWS 및 Terraform에 접근할 수 있도록 인증 정보를 등록합니다.

### 등록 경로

`Repository > Settings > Secrets and variables > Actions > New repository secret`

| Name                    | Secret                       | 설명                 |
| ----------------------- | ---------------------------- | ------------------ |
| `AWS_ACCESS_KEY_ID`     | 발급받은 AWS Access Key          | AWS 인증용            |
| `AWS_SECRET_ACCESS_KEY` | 발급받은 AWS Secret Key          | AWS 인증용            |
| `PAT`                   | GitHub Personal Access Token | GitHub Actions 인증용 |

> 등록 후 “Secrets” 탭에서 정상적으로 추가되었는지 반드시 확인하세요.

---

## 요약

| 구성 항목                            | 목적                         |
| -------------------------------- | -------------------------- |
| **IAM 사용자 / 키 생성**               | Terraform이 AWS 리소스 프로비저닝   |
| **GitHub 리포지토리 및 토큰**            | CI/CD 파이프라인 인증 및 관리        |
| **Terraform / Git / AWS CLI 설치** | IaC 및 자동화 환경 구축            |
| **GitHub Secrets 설정**            | GitHub Actions → AWS 인증 연결 |
