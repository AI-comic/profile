## 1. 컨테이너 저장소 개념

### Registry (레지스트리)

> 컨테이너 이미지를 저장하고 배포하는 원격 저장소 서비스입니다.
> 쉽게 말하면 **“컨테이너 이미지의 클라우드 보관소”** 입니다.

**대표 서비스**

* [Docker Hub](https://hub.docker.com)
* [Amazon ECR (Elastic Container Registry)](https://aws.amazon.com/ecr/)
* Google Artifact Registry
* GitHub Container Registry

---

### Repository (리포지토리)

> 하나의 애플리케이션 이미지 버전(Tag)을 모아두는 “폴더/네임스페이스”입니다.

**구조 형식**

```
{registry주소}/{namespace}/{repository명}:{tag}
```

**예시**

```
docker.io/smlinux/petclinic:latest
public.ecr.aws/abcd1234/petclinic:v1
```

---

## 2. Docker Hub로 이미지 업로드

### (1) 이미지 다운로드 및 확인

```bash
# nginx 예시 이미지 다운로드
docker pull nginx:latest

# 이미지 목록 확인
docker images
```

---

### (2) Docker Hub 로그인

```bash
docker login
Username: <사용자명>
Password: <비밀번호>
```

> 참고: 비밀번호는 로컬에 암호화되지 않은 상태로 저장됩니다.
> 안전을 위해 [Credentials Helper 설정](https://docs.docker.com/engine/reference/commandline/login/#credentials-store)을 권장합니다.

---

### (3) 이미지 태그(Tag) 설정

이미지 이름을 Docker Hub 저장소 형태로 변경해야 push할 수 있습니다.

```bash
docker tag webserver-ubuntu:light <사용자명>/webserver-ubuntu:light
```

**예시**

```bash
docker tag petclinic-docker:latest smlinux/petclinic:latest
```

---

### (4) 이미지 업로드(Push)

```bash
docker push smlinux/petclinic:latest
```

업로드 완료 후 [Docker Hub](https://hub.docker.com/repositories)에서
리포지토리를 확인할 수 있습니다.

---

### (5) 로그아웃

```bash
docker logout
```

---

## 3. AWS ECR (Elastic Container Registry)

### 개요

AWS에서 제공하는 **완전관리형 컨테이너 이미지 저장소 서비스**

**특징**

* Public / Private Repository 지원
* IAM 기반 인증 및 접근 제어
* AWS ECS, EKS, Lambda 등과 연동 가능
* 이미지 버전 관리 및 자동 정리(Lifecycle Policy) 지원

**요금**

| 유형      | 무료 용량      | 비고      |
| ------- | ---------- | ------- |
| Public  | 50GB / 월   | 상시 무료   |
| Private | 500MB / 1년 | 프리티어 한정 |

🔗 [ECR 요금 확인](https://aws.amazon.com/ko/ecr/pricing/)

---

## 4. ECR 실습 – Public Repository

### (1) 컨테이너 이미지 준비

```bash
docker images
REPOSITORY         TAG       IMAGE ID       CREATED        SIZE
smlinux/petclinic  latest    7ac1b41e2fbe   3 months ago   469MB
```

---

### (2) AWS ECR Public Repository 생성

1. AWS 콘솔 → **Elastic Container Registry** 검색
2. 왼쪽 메뉴에서 **[Public registries] → [Repositories] → [리포지토리 생성]**
3. 설정

   * 리포지토리 이름: `petclinic`
   * 설명: `Spring PetClinic 예제`
   * 플랫폼: `Linux / x86-64`
4. 생성 후 URL 예시:

   ```
   public.ecr.aws/abcd1234/petclinic
   ```

---

### (3) Docker 인증

```bash
aws ecr-public get-login-password --region us-east-1 \
| docker login --username AWS --password-stdin public.ecr.aws/abcd1234
```

> 최신 버전의 AWS CLI와 Docker가 설치되어 있어야 합니다.

---

### (4) 이미지 빌드 및 태그 지정

```bash
docker build -t petclinic .
docker tag petclinic:latest public.ecr.aws/abcd1234/petclinic:latest
```

---

### (5) 이미지 푸시

```bash
docker push public.ecr.aws/abcd1234/petclinic:latest
```

---

### (6) 컨테이너 실행 테스트

```bash
docker run --name petclinic -d -p 8080:8080 public.ecr.aws/abcd1234/petclinic:latest
```

```bash
# 동작 확인
docker ps

# 로그 확인
docker logs -f petclinic
```

웹 브라우저에서
`http://<서버 Public IP>:8080` 접속 시 애플리케이션 확인 가능.

---

### (7) 리소스 정리

```bash
docker rm -f petclinic
docker rmi public.ecr.aws/abcd1234/petclinic:latest
```

> AWS ECR 콘솔에서도 업로드된 이미지 삭제 후 리포지토리 정리 가능

---

## 5. ECR 실습 – Private Repository

### (1) Private Repository 생성

1. AWS 콘솔 → **Elastic Container Registry** → **[Private registries] → [Repositories] → [리포지토리 생성]**
2. 설정

   * 이름: `petclinic`
   * 태그 변경 가능성: `Mutable`
   * 암호화: `AES-256`
3. 생성 완료 후 URI 예시:

   ```
   <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic
   ```

---

### (2) ECR 인증

```bash
aws ecr get-login-password --region ap-northeast-2 \
| docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com
```

---

### (3) 이미지 빌드 및 푸시

```bash
docker build -t petclinic .
docker tag petclinic:latest <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
```

---

### (4) 컨테이너 실행 테스트

```bash
docker rm -f $(docker ps -aq)
docker rmi -f $(docker images -q)

docker run --name petclinic -d -p 8080:8080 <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
docker logs -f petclinic
```

웹 접속:
`http://<EC2 Public IP>:8080`

---

### (5) 테스트 완료 후 정리

```bash
docker rm -f petclinic
docker rmi <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic:latest
```

ECR 콘솔에서도 이미지 및 리포지토리 삭제 가능.

---

## 📎 참고 문서

| 분류          | 제목                | 링크                                                                                                                       |
| ----------- | ----------------- | ------------------------------------------------------------------------------------------------------------------------ |
| 공식 문서       | Docker Hub        | [https://hub.docker.com](https://hub.docker.com)                                                                         |
| Docker Docs | Docker push & tag | [https://docs.docker.com/engine/reference/commandline/push/](https://docs.docker.com/engine/reference/commandline/push/) |
| AWS Docs    | Amazon ECR 소개     | [https://docs.aws.amazon.com/ecr/](https://docs.aws.amazon.com/ecr/)                                                     |
| AWS CLI     | ECR 명령어 가이드       | [https://docs.aws.amazon.com/cli/latest/reference/ecr/](https://docs.aws.amazon.com/cli/latest/reference/ecr/)           |
| AWS Pricing | ECR 요금            | [https://aws.amazon.com/ko/ecr/pricing/](https://aws.amazon.com/ko/ecr/pricing/)                                         |

---

## 요약 정리

| 항목             | 주요 명령어                                           | 설명          |
| -------------- | ------------------------------------------------ | ----------- |
| Docker Hub 로그인 | `docker login`                                   | 계정 인증       |
| 이미지 태그 변경      | `docker tag <기존> <계정>/<리포지토리>:<tag>`             | push 준비     |
| 이미지 업로드        | `docker push <계정>/<리포지토리>:<tag>`                 | Hub/ECR 업로드 |
| AWS ECR 로그인    | `aws ecr get-login-password \| docker login ...` | IAM 기반 인증   |
| 컨테이너 실행        | `docker run -d -p <호스트포트>:<컨테이너포트> <이미지>`        | 웹 애플리케이션 실행 |

