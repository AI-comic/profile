# GitHub Actions 실습 — CI/CD 파이프라인 구축 및 AWS ECR 연동

## 1. GitHub Actions 개요

**GitHub Actions**는 GitHub 저장소에 통합된 **CI/CD(지속적 통합 및 배포)** 자동화 플랫폼입니다.
코드가 push되거나 pull request가 생성될 때 자동으로 빌드, 테스트, 배포 과정을 수행할 수 있습니다.

### 주요 특징

* **완전한 GitHub 통합**: 별도 서버 없이 GitHub 저장소 내에서 CI/CD 수행
* **이벤트 기반 실행**: push, PR, issue 생성 등 다양한 이벤트 트리거
* **다양한 환경 지원**: Linux, macOS, Windows 환경에서 실행 가능
* **YAML 구성 파일**: `.github/workflows/*.yml` 파일로 손쉽게 정의
* **Marketplace 연동**: 1,000개 이상의 오픈소스 액션 활용 가능
* **무료 실행 시간 제공**: 퍼블릭 리포지토리의 경우 무제한 사용 가능

---

## 2. GitHub Actions 구조

| 구성요소         | 설명                                           |
| ------------ | -------------------------------------------- |
| **Workflow** | 전체 자동화 작업을 정의하는 단위. YAML 파일로 구성              |
| **Event**    | 워크플로우 실행을 트리거하는 조건 (예: push, pull_request 등) |
| **Job**      | 실행할 작업 단위. 병렬 혹은 순차적으로 수행 가능                 |
| **Step**     | Job 내에서 실행되는 개별 명령어 또는 Action                |
| **Runner**   | 실제로 Job을 실행하는 서버 환경 (GitHub 호스팅 or 자체 서버)    |

---

## 3. 기본 예제 — Node.js CI 파이프라인

다음은 `main` 브랜치로 코드가 push될 때마다 자동으로 Node.js 앱을 빌드하고 테스트하는 예시입니다.

```yaml
name: CI Pipeline

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: 16

      - name: Install dependencies
        run: npm install

      - name: Run tests
        run: npm test
```

 **핵심 요약**

* `on:`은 워크플로우 실행 조건
* `jobs:`는 수행할 작업 정의
* `steps:` 안에 실제 명령 또는 액션 지정

---

## 4. Hands-on 실습 — Spring PetClinic CI 파이프라인

이제 실제 예시로 **GitHub → AWS ECR**로 연결되는 CI 파이프라인을 구성합니다.

### 4.1 GitHub Repository 생성

* 리포지토리 이름: `gitops_petclinic`
* 소스코드: [spring-petclinic 공식 저장소](https://github.com/spring-projects/spring-petclinic.git)

```bash
git clone https://github.com/spring-projects/spring-petclinic.git
rm -rf .git   # 기존 Git 이력 제거
git clone https://github.com/<your_id>/gitops_petclinic.git
cp -R spring-petclinic/* gitops_petclinic/
```

---

### 4.2 AWS ECR 생성

* ECR 저장소 이름: `petclinic`
* URI 예시:

  ```
  850995557801.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic
  ```

---

### 4.3 CI Workflow 구성 (`.github/workflows/ci.yaml`)

```yaml
name: Build and Push to ECR

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for Maven Wrapper
        run: chmod +x ./mvnw

      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}

      - name: Build with Maven
        run: ./mvnw package -DskipTests

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ap-northeast-2

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v1

      - name: Build, Tag, and Push Docker Image
        env:
          ECR_REGISTRY: 850995557801.dkr.ecr.ap-northeast-2.amazonaws.com
          ECR_REPOSITORY: petclinic
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
```

---

### 4.4 GitHub Secrets 등록

GitHub Actions가 AWS에 접근하려면 인증 정보를 저장해야 합니다.
리포지토리 → `Settings` → `Secrets and variables` → `Actions` → **New repository secret** 클릭 후 등록합니다.

| 이름                      | 예시 값             | 설명            |
| ----------------------- | ---------------- | ------------- |
| `AWS_ACCESS_KEY_ID`     | AKIA********ABCD | IAM 사용자 액세스 키 |
| `AWS_SECRET_ACCESS_KEY` | IhL1********Mk   | IAM 사용자 시크릿 키 |

---

### 4.5 Dockerfile 생성

```dockerfile
FROM openjdk:17-jdk-slim
EXPOSE 8080
ADD target/*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

### 4.6 SSH 키 등록 및 GitHub Push

GitHub와 안전하게 통신하기 위해 SSH 키를 등록합니다.

```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
cat ~/.ssh/id_ed25519.pub
```

* GitHub → `Settings → SSH and GPG keys → New SSH key`
  → 위 공개키를 등록하고 이름은 예: `gitopskey`

Push 명령어:

```bash
git remote set-url origin git@github.com:<your_id>/gitops_petclinic.git
git add .
git commit -m "Add GitHub Actions CI workflow"
git push origin main
```

 GitHub → Actions 탭에서 빌드 진행 상태 확인
 빌드 완료 시 AWS ECR에 자동 업로드 확인 가능

---

## 5. 참고 자료

| 구분                   | 링크                                                                                                                                                 |
| -------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| GitHub Actions 공식 문서 | [https://docs.github.com/en/actions](https://docs.github.com/en/actions)                                                                           |
| AWS ECR 가이드          | [https://docs.aws.amazon.com/AmazonECR/latest/userguide/what-is-ecr.html](https://docs.aws.amazon.com/AmazonECR/latest/userguide/what-is-ecr.html) |
| Spring PetClinic 예제  | [https://github.com/spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic)                                         |
| GitHub Secrets 설정법   | [https://docs.github.com/en/actions/security-guides/encrypted-secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)       |

---

## 마무리

이번 실습을 통해 GitHub Actions를 활용하여

* **CI 빌드 자동화**,
* **Docker 이미지 생성**,
* **AWS ECR 배포**
  까지 완전한 CI 파이프라인을 구성했습니다.

