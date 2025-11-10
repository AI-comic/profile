# GitOps와 ArgoCD 개요

## 1. GitOps란?

**GitOps**는 Git을 중심으로 애플리케이션 및 인프라를 **자동으로 배포하고 관리**하는 방식입니다.
모든 설정(YAML, Helm Chart 등)을 Git에 저장하고,
Kubernetes 클러스터의 **현재 상태(Current State)** 를
Git에 정의된 **원하는 상태(Desired State)** 와 **자동으로 동기화(Sync)** 합니다.

---

### GitOps 동작 흐름

1. 개발자가 애플리케이션 코드 또는 인프라 코드를 수정하여 Git에 Push
2. CI 도구(GitHub Actions, Jenkins 등)가 코드 검사 및 테스트 수행
3. 컨테이너 이미지를 빌드하여 **ECR 등 레지스트리에 Push**
4. 변경된 매니페스트(`deployment.yaml`)가 Git에 커밋
5. ArgoCD가 Git 변경사항을 감지하고, 클러스터에 자동 반영

```plaintext
개발자 → Git Push → GitHub Actions → ECR Push → ArgoCD Sync → EKS 배포
```

---

### GitOps의 주요 특징

| 구분            | 설명                                        |
| ------------- | ----------------------------------------- |
| **완전 자동화**    | 인프라 구성부터 배포까지 자동화                         |
| **투명한 변경 이력** | Git Commit으로 모든 변경 추적 가능                  |
| **보안성 향상**    | 배포는 CI/CD 파이프라인에서만 수행되며, 운영자는 클러스터 접근 불필요 |
| **재현성 보장**    | Git 저장소만으로 동일한 환경 재구성 가능                  |
| **리뷰 프로세스**   | 코드 변경 시 PR(리뷰) 절차로 신뢰성 확보                 |

---

## 2. Infrastructure as Code (IaC)

**IaC**는 인프라를 코드로 관리하는 개념입니다.
직접 서버를 구성하는 대신, YAML·HCL 등의 코드로 **자동 배포 및 구성 관리**를 수행합니다.

### IaC 등장 배경

* 클라우드 환경이 API로 인프라 제어를 지원하게 되면서
  인프라를 코드로 정의하고 자동화할 수 있게 됨.

### IaC의 장점

| 항목        | 설명                    |
| --------- | --------------------- |
| **재현성**   | 언제든 동일한 인프라를 반복 생성 가능 |
| **유연성**   | 변수 조합으로 다양한 환경 구성 가능  |
| **형상 관리** | Git으로 코드 변경 이력 관리     |

### IaC 주요 도구

| 목적                           | 도구              | 설명               |
| ---------------------------- | --------------- | ---------------- |
| **Provisioning**             | Terraform       | 클라우드 리소스 생성 및 관리 |
| **Configuration Management** | Ansible         | 서버 설정 자동화        |
| **Application Manifest**     | Kubernetes YAML | 애플리케이션 배포 정의     |

---

## 3. ArgoCD 소개

### ArgoCD란?

**ArgoCD (Argo Continuous Delivery)** 는
CNCF(Cloud Native Computing Foundation) 산하의 **GitOps 기반 CD 도구**입니다.
Git 저장소를 기준으로 **쿠버네티스 클러스터의 상태를 자동 동기화**하며
선언적(Declarative) 방식으로 애플리케이션을 배포·관리합니다.

---

### ArgoCD 주요 특징

| 항목                     | 설명                               |
| ---------------------- | -------------------------------- |
| **Git 기반 관리**          | Git 저장소를 기준으로 배포 및 롤백 수행         |
| **자동 동기화 (Auto-Sync)** | Git과 클러스터 상태를 비교 후 자동 일치         |
| **시각화 UI 제공**          | 웹 UI로 배포 상태와 헬스체크 확인 가능          |
| **다양한 매니페스트 지원**       | YAML, Helm, Kustomize, Jsonnet 등 |
| **롤백 지원**              | Git Commit 단위로 손쉽게 이전 상태로 복구 가능  |

---

### ArgoCD 구성 요소

| 구성요소                       | 역할                          |
| -------------------------- | --------------------------- |
| **API Server**             | CLI·UI·Git 저장소와 통신하는 중심 API |
| **Repository Server**      | Git에서 매니페스트를 가져오고 파싱        |
| **Application Controller** | Git과 클러스터 상태 비교 및 Sync 수행   |
| **Dex (옵션)**               | SSO(Single Sign-On) 인증 제공   |

---

### ArgoCD 동작 흐름

1. Git 저장소에 배포할 매니페스트(`deployment.yaml` 등)를 커밋
2. ArgoCD가 해당 저장소를 주기적으로 모니터링
3. Git 상태와 Kubernetes 클러스터 상태 비교
4. 차이가 감지되면 자동/수동 Sync 수행
5. 배포 및 업데이트 완료 후 상태 시각화

```plaintext
Git Commit → ArgoCD 감지 → 상태 비교 → Sync → Kubernetes 반영
```

---

### ArgoCD의 장점 요약

| 항목            | 내용                      |
| ------------- | ----------------------- |
| **선언적 배포**    | YAML 파일 수정만으로 배포 자동화    |
| **이력 관리 용이**  | Git Commit으로 변경 추적      |
| **시각화된 모니터링** | UI에서 배포 상태를 한눈에 확인      |
| **간단한 롤백**    | 특정 Git 버전으로 손쉽게 되돌리기 가능 |

---

## 정리

| 구분         | 핵심 개념                | 주요 도구              |
| ---------- | -------------------- | ------------------ |
| **GitOps** | Git 기반 자동화된 배포 및 관리  | Git, ArgoCD        |
| **IaC**    | 인프라를 코드로 관리          | Terraform, Ansible |
| **CD 구현**  | Git 상태와 클러스터를 자동 동기화 | ArgoCD             |
