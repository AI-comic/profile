# AWS ECR (Elastic Container Registry)

## 개요

**AWS ECR (Elastic Container Registry)** 는
**AWS에서 제공하는 완전관리형 Docker 컨테이너 이미지 저장소**입니다.

개발자가 **Docker 이미지를 손쉽게 저장·관리·배포**할 수 있으며,
**ECS (Elastic Container Service)** 또는 **EKS (Elastic Kubernetes Service)** 와 함께 자주 사용됩니다.

---

## ECR 주요 특징

| 항목         | 설명                                            |
| ---------- | --------------------------------------------- |
| **저장소 유형** | 공개(Public) 및 비공개(Private) 저장소 모두 지원           |
| **보안**     | IAM 인증 기반 접근 제어, KMS 암호화 지원                   |
| **이미지 관리** | 버전 관리, 태그 지정, 수명 주기(Lifecycle) 관리 지원          |
| **통합성**    | AWS ECS, EKS, Lambda 등과 통합 가능                 |
| **자동화**    | AWS CLI, SDK, Terraform, Ansible 등으로 자동 관리 가능 |

---

## 요금 정책

* **Public Repository:** 월 50GB 무료 스토리지 제공
* **Private Repository:** AWS 프리 티어에서 **월 500MB 무료 (12개월간)** 제공

[ECR 요금 안내 페이지](https://aws.amazon.com/ko/ecr/pricing/)

---

## 실습 1: Public Repository 생성하기

### 1. Docker 이미지 준비

```bash
docker images
```

예시:

```
REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
smlinux/petclinic   latest    7ac1b41e2fbe   3 months ago   469MB
```

---

### 2. AWS ECR Public Repository 생성

1. AWS 콘솔 접속 → **Elastic Container Registry (ECR)** 검색
2. 왼쪽 메뉴에서 **[Public registry] → [Repositories] → [리포지토리 생성]**
3. 설정

   * **리포지토리 이름:** `petclinic`
   * **설명:** `Spring PetClinic Demo`
   * **아키텍처:** x86-64, Linux
   * (선택) 로고 업로드
4. 생성 후 리포지토리 주소 확인

   ```
   public.ecr.aws/<repo_alias>/petclinic
   ```

---

### 3. AWS CLI 및 인증 설정

#### AWS CLI 설치 (Ubuntu)

```bash
sudo apt update
sudo apt install -y unzip
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
```

출력 예시:

```
aws-cli/2.17.36 Python/3.11.9 Linux/6.8.0-1012-aws
```

---

#### AWS 프로필 등록

```bash
aws configure
```

예시 입력:

```
AWS Access Key ID [None]: AKIA************
AWS Secret Access Key [None]: M7x**********
Default region name [None]: ap-northeast-2
Default output format [None]: json
```

#### 현재 계정 확인

```bash
aws sts get-caller-identity
```

출력 예시:

```json
{
  "Account": "123456789012",
  "UserId": "AID...M",
  "Arn": "arn:aws:iam::123456789012:user/example"
}
```

---

### 4. Docker 로그인 및 이미지 푸시

#### 로그인

```bash
aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/<repo_alias>
```

#### 이미지 태그 지정

```bash
docker tag petclinic:latest public.ecr.aws/<repo_alias>/petclinic:latest
```

#### 이미지 푸시

```bash
docker push public.ecr.aws/<repo_alias>/petclinic:latest
```

---

### 5. 업로드된 이미지 실행 테스트

```bash
sudo apt install -y docker.io
sudo usermod -aG docker ubuntu
exit  # 로그아웃 후 다시 로그인
```

컨테이너 실행:

```bash
docker run --name petclinic -p 8080:8080 public.ecr.aws/<repo_alias>/petclinic:latest
```

실행 확인:

```bash
docker ps
```

중지 및 삭제:

```bash
docker rm -f petclinic
```

---

### 6. Public Repository 삭제

AWS 콘솔 → **ECR > Public registry > Repositories**

* `petclinic` 선택 → 이미지 태그 삭제 → 리포지토리 삭제

---

## 실습 2: Private Repository 생성하기

### 1. Private Repository 생성

1. **ECR > Private registry > Repositories > 리포지토리 생성**
2. 설정:

   * 이름: `petclinic`
   * 태그 변경 가능성: **Mutable**
   * 암호화: **AES-256**
3. 생성 완료 후 “푸시 명령 보기” 클릭

---

### 2. 로그인 및 이미지 푸시

```bash
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com
```

#### 이미지 빌드

```bash
docker build -t petclinic .
```

#### 태그 지정

```bash
docker tag petclinic:latest <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
```

#### 푸시

```bash
docker push <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
```

---

### 3. 컨테이너 실행 및 테스트

```bash
docker run --name petclinic -d -p 8080:8080 <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
docker ps
docker rm -f petclinic
```

---

### 4. 리포지토리 및 이미지 정리

AWS 콘솔 → **ECR > Private registry > Repositories**

* 이미지 태그 삭제
* 리포지토리 삭제

Docker 로컬 이미지 삭제:

```bash
docker rmi <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
```

---

## 정리

| 구분     | Public Repository    | Private Repository |
| ------ | -------------------- | ------------------ |
| 접근 권한  | 누구나 접근 가능            | IAM 사용자만 접근 가능     |
| 요금     | 50GB 무료              | 500MB (1년간 프리티어)   |
| 주요 사용처 | 오픈소스 이미지 공유          | 내부 서비스용 이미지 저장     |
| 로그인 명령 | `aws ecr-public ...` | `aws ecr ...`      |

---

## 참고 문서

* [AWS 공식 문서 – Amazon ECR 소개](https://docs.aws.amazon.com/ko_kr/AmazonECR/latest/userguide/what-is-ecr.html)
* [AWS CLI 설치 가이드 (Linux)](https://docs.aws.amazon.com/ko_kr/cli/latest/userguide/install-cliv2-linux.html)
* [ECR 요금 정책](https://aws.amazon.com/ko/ecr/pricing/)
* [ECR 인증 및 이미지 푸시 가이드](https://docs.aws.amazon.com/ko_kr/AmazonECR/latest/userguide/docker-push-ecr-image.html)

