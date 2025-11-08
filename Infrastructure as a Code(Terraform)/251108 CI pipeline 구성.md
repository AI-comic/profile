# **CI pipeline 구성**

---

## **1. GitHub Actions로 자동화된 워크플로우 정의하기**

### `.github/workflows/` 디렉토리의 역할

GitHub Actions는 리포지토리 내 `.github/workflows/` 디렉토리를 자동으로 인식하며, 여기에 정의된 **YAML 파일**을 읽어 워크플로우를 실행합니다.

#### 주요 기능

1. **자동화 워크플로우 정의**

   * `push`, `pull_request`, `schedule` 등의 이벤트에 따라 자동 실행됨.
2. **CI/CD pipeline 설정**

   * 코드 빌드, 테스트, 컨테이너 이미지 빌드 및 ECR 푸시 등 자동화 가능.
3. **Job & Step 구성**

   * 각 Job은 여러 Step으로 이루어져 있으며, 쉘 명령어·도커·AWS CLI 등을 실행 가능.
4. **환경 변수 및 Secrets 관리**

   * GitHub Secrets로 AWS 키, 레지스트리 정보 등 안전하게 관리.
5. **조건부 실행 및 멀티 OS 지원**

   * 특정 브랜치나 파일 변경 시에만 실행하도록 제어 가능.

---

## **2. 실습: CI 워크플로우 구성하기**

### 목적

Spring Boot 기반 **PetClinic** 애플리케이션을
**Docker 이미지로 빌드 → AWS ECR 푸시 → K8S 매니페스트 자동 업데이트 → GitHub 반영**
하는 CI pipeline을 구성합니다.

* 트리거: `main` 브랜치에 **push** 또는 **pull request** 발생 시

---

### 파일 생성

경로:

```
gitops_petclinic/.github/workflows/ci.yaml
```

AWS ECR에서 생성한 **레지스트리 URI**를 복사해 아래에 반영합니다.

---

### CI pipeline 예시 (`ci.yaml`)

```yaml
name: Build, Push, and Update Deployment

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    # 1. 코드 가져오기
    - name: Checkout repository
      uses: actions/checkout@v3

    # 2. Java 환경 설정
    - name: Set up JDK 25
      uses: actions/setup-java@v3
      with:
        java-version: '25'
        distribution: 'temurin'

    # 3. Maven 빌드 준비
    - name: Grant execute permission for Maven Wrapper
      run: chmod +x ./mvnw

    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2/repository
        key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
        restore-keys: |
          ${{ runner.os }}-maven-

    # 4. 애플리케이션 빌드
    - name: Build with Maven Wrapper
      run: ./mvnw package -DskipTests

    # 5. AWS 인증 설정
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v1
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ap-northeast-2

    # 6. Amazon ECR 로그인
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v1

    # 7. Docker 이미지 빌드 & 푸시
    - name: Build, tag, and push image to Amazon ECR
      env:
        ECR_REGISTRY: 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com
        ECR_REPOSITORY: petclinic-repo
        IMAGE_TAG: ${{ github.sha }}
      run: |
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

    # 8. K8S 배포 파일 업데이트
    - name: Update Deployment YAML
      run: |
        sed -i 's|image: .*|image: 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/petclinic-repo:${{ github.sha }}|' k8s/deployment.yaml

    # 9. 변경사항 커밋 & 푸시
    - name: Commit and push changes
      run: |
        git config --global user.name 'GitHub Actions'
        git config --global user.email 'github-actions@github.com'
        git add k8s/deployment.yaml
        git diff --quiet && git diff --staged --quiet || (
          git commit -m "Update image tag to ${{ github.sha }}" &&
          git push https://${{ secrets.PAT }}@github.com/${{ github.repository }}.git HEAD:${{ github.ref }}
        )
```

---

## **3. Dockerfile 구성**

`gitops_petclinic` 리포지토리 루트에 `Dockerfile` 생성

```dockerfile
FROM eclipse-temurin:25-jdk-jammy
EXPOSE 8080
ADD target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## **4. PetClinic 소스코드 업로드**

Spring 공식 리포지토리에서 소스를 클론하고,
`gitops_petclinic` 리포지토리에 복사 후 푸시합니다.

```bash
git clone https://github.com/spring-projects/spring-petclinic.git
git clone https://github.com/<github_ID>/gitops_petclinic.git

cd spring-petclinic
rm -rf .git
cd ..

cp -R spring-petclinic/* gitops_petclinic/
cp -R spring-petclinic/.mvn* gitops_petclinic/
cd gitops_petclinic/

git config user.name "<github_ID>"
git config user.email "<your_email@example.com>"

git add .
git commit -m "Upload petclinic sources"
git push origin main
```

---

## **5. GitHub와 Git 기본 개념 정리**

| 영역                       | 이름      | 설명                   |
| ------------------------ | ------- | -------------------- |
| **Working Directory** | 작업 디렉토리 | 수정 중인 실제 코드          |
| **Staging Area**      | 준비 영역   | 커밋 후보 파일 저장소         |
| **Local Repository** | 로컬 저장소  | 커밋 이력 저장 (`.git` 내부) |
| **Remote Repository** | 원격 저장소  | GitHub 서버에 저장        |

### Git 명령 순서 요약

```bash
# 1. GitHub 저장소 복제
git clone https://github.com/<github_ID>/gitops_petclinic.git

# 2. 소스 복사
cp -R spring-petclinic/* gitops_petclinic/

# 3. 사용자 정보 설정
git config user.name "<github_ID>"
git config user.email "<your_email@example.com>"

# 4. 변경 파일 스테이징
git add .

# 5. 커밋 생성
git commit -m "Upload petclinic sources"

# 6. GitHub로 푸시
git push origin main
```

---

## **정리**

이 단계까지 수행하면:

* **GitHub Actions**가 push 이벤트를 감지해 **자동 빌드 및 배포 pipeline**을 실행
* 빌드된 Docker 이미지는 **AWS ECR**에 업로드
* **Kubernetes Deployment** 매니페스트가 자동 업데이트되어
  → ArgoCD에 의해 **실시간 반영**됩니다.
