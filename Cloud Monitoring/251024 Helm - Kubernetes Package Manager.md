# Helm — Kubernetes Package Manager

Helm은 쿠버네티스 환경에서 **애플리케이션을 쉽고 체계적으로 배포 및 관리**할 수 있는 **Package Manager**입니다.
리눅스에서 `apt`나 `yum`을 사용하는 것처럼, 쿠버네티스에서는 **Helm**을 이용해 애플리케이션을 배포합니다.

---

## 1. Helm 이란?

 [Helm 공식 홈페이지](https://helm.sh)

Helm은 **Kubernetes 리소스 정의 파일(yaml)** 과 **설정 정보(values.yaml)** 를 하나의 **Chart** 라는 Package로 묶어 관리합니다.

### Helm 주요 기능

| 기능             | 설명                                   |
| -------------- | ------------------------------------ |
| **애플리케이션 패키징** | Kubernetes 리소스를 Chart 단위로 묶어 관리      |
| **손쉬운 배포**     | `helm install` 명령으로 복잡한 리소스들을 한번에 배포 |
| **버전 관리**      | 차트 버전별로 이력 관리 및 롤백 가능                |
| **설정 관리**      | values.yaml을 통해 배포 시 환경별 설정 변경 가능    |
| **업그레이드 / 롤백** | 간단한 명령으로 기존 릴리스 업그레이드 및 복원 가능        |
| **차트 저장소 관리**  | 공용/사설 리포지토리에서 차트를 검색, 등록, 삭제 가능      |

---

## 2. Helm 설치

 [공식 설치 문서](https://helm.sh/docs/intro/install/)

### (1) 설치 스크립트 이용

```bash
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```

### (2) 명령어 확인

```bash
helm --help
helm [COMMAND] --help
```

### (3) 자동 완성 등록

```bash
echo "source <(helm completion bash)" >> ~/.bashrc
source ~/.bashrc
```

---

## 3. Helm 리포지토리 관리

Helm 리포지토리는 여러 개의 **차트(Chart)** 가 저장된 공간입니다.
대표적인 리포지토리는 다음과 같습니다.

| 리포지토리            | 설명                                            |
| ---------------- | --------------------------------------------- |
| **Bitnami**      | 가장 널리 사용되는 리포지토리. 안정적이고 미리 구성된 다양한 오픈소스 차트 제공 |
| **Artifact Hub** | CNCF에서 운영하는 공개 차트 허브. 다양한 조직이 만든 차트를 검색/활용 가능 |

 [Bitnami Helm Repository](https://bitnami.com/stacks/helm)
 [Artifact Hub](https://artifacthub.io/packages/search)

---

### (1) 리포지토리 추가 / 삭제

```bash
# 리포지토리 등록
helm repo add bitnami https://charts.bitnami.com/bitnami

# (구형) stable 리포지토리 등록 및 삭제
helm repo add stable https://charts.helm.sh/stable
helm repo remove stable

# 리포지토리 목록 확인
helm repo list

# 최신 차트 정보 갱신
helm repo update
```

---

## 4. Helm 주요 명령어 정리

| 명령어                | 설명                | 예시                                    |
| ------------------ | ----------------- | ------------------------------------- |
| `helm search repo` | 등록된 리포지토리에서 차트 검색 | `helm search repo nginx`              |
| `helm show chart`  | 차트 메타데이터 보기       | `helm show chart bitnami/nginx`       |
| `helm show values` | 차트의 기본 설정 값 출력    | `helm show values bitnami/nginx`      |
| `helm install`     | 새 릴리스(배포) 설치      | `helm install my-nginx bitnami/nginx` |
| `helm upgrade`     | 기존 릴리스 업그레이드      | `helm upgrade my-nginx bitnami/nginx` |
| `helm rollback`    | 이전 버전으로 복원        | `helm rollback my-nginx 1`            |
| `helm get values`  | 배포된 릴리스 설정 값 조회   | `helm get values my-nginx`            |
| `helm list`        | 설치된 릴리스 목록        | `helm list`                           |
| `helm uninstall`   | 릴리스 삭제            | `helm uninstall my-nginx`             |

---

## LAB: Helm으로 Nginx 배포 실습

### (1) Nginx 차트 검색

```bash
helm search repo nginx
```

예시 결과:

```
NAME                            	CHART VERSION	APP VERSION	DESCRIPTION
bitnami/nginx                   	18.1.9       	1.27.1     	NGINX Open Source Web Server
bitnami/nginx-ingress-controller	11.3.20      	1.11.2     	Ingress Controller for Kubernetes
```

---

### (2) 차트 정보 및 설정 확인

```bash
helm show chart bitnami/nginx
helm show values bitnami/nginx > my-nginx-values.yaml
```

`my-nginx-values.yaml`을 열어 필요한 부분(예: replica 수, 포트, 리소스 등)을 수정합니다.

---

### (3) Helm으로 Nginx 설치

```bash
helm install my-nginx bitnami/nginx -f my-nginx-values.yaml
```

배포 상태 확인:

```bash
kubectl get pods,deployments,services
```

웹 브라우저에서 `NodeIP:Port`로 접속해 확인합니다.

---

### (4) Helm 업그레이드 및 롤백

```bash
# 구성 파일 수정
vi my-nginx-values.yaml

# 업그레이드
helm upgrade my-nginx bitnami/nginx -f my-nginx-values.yaml

# 배포 이력 확인
helm history my-nginx

# 특정 버전으로 롤백
helm rollback my-nginx 1
```

---

### (5) Helm 릴리스 삭제

```bash
helm uninstall my-nginx
helm list
```

---

## 5. Helm 차트 구조 이해

Helm Chart는 다음과 같은 디렉터리 구조를 가집니다:

```
<chart-name>/
├── Chart.yaml        # 차트 메타데이터 (이름, 버전, 설명 등)
├── values.yaml       # 사용자 정의 설정 값 (환경 설정)
├── charts/           # 의존성 차트
├── templates/        # Kubernetes 리소스 템플릿
│   ├── deployment.yaml
│   ├── service.yaml
│   └── _helpers.tpl  # 템플릿 헬퍼 함수
└── README.md         # 차트 설명 (선택)
```

| 구성요소            | 설명                                            |
| --------------- | --------------------------------------------- |
| **Chart.yaml**  | 차트 메타정보 (이름, 버전, 설명 등)                        |
| **values.yaml** | 배포 시 변경 가능한 설정값 정의                            |
| **templates/**  | 실제 리소스 매니페스트 템플릿                              |
| **charts/**     | 의존성 차트 저장 위치                                  |
| **Release**     | 특정 차트 배포 인스턴스                                 |
| **Repository**  | 차트 저장소 (Bitnami, Artifact Hub, ChartMuseum 등) |

---

## LAB: Helm 차트 관리 실습

### (1) 차트 다운로드 및 압축 해제

```bash
helm pull bitnami/nginx
tar zxvf nginx-18.1.9.tgz
```

### (2) 차트 수정

* `values.yaml`: 포트, 리소스, replica 등 수정
* `templates/`: 매니페스트 템플릿 수정
* `Chart.yaml`: 메타데이터 수정

### (3) 수정한 차트 다시 패키징

```bash
helm package nginx
```

### (4) 로컬 차트로 배포

```bash
helm install my-nginx ./nginx-18.1.9.tgz
kubectl get svc,deployments,pods
```

---

## 요약 정리

| 항목              | 설명                                                                |
| --------------- | ----------------------------------------------------------------- |
| **Helm**        | Kubernetes용 Package Manager                                               |
| **Chart**       | 애플리케이션 Package 단위                                                     |
| **Values.yaml** | 환경별 설정 파일                                                         |
| **Repository**  | 차트 저장소 (Bitnami, Artifact Hub 등)                                  |
| **Release**     | 실제 배포된 차트 인스턴스                                                    |
| **명령어 핵심**      | `helm install`, `helm upgrade`, `helm rollback`, `helm uninstall` |

---

## 참고 문서

* [Helm 공식 문서](https://helm.sh/docs/)
* [Helm 설치 가이드](https://helm.sh/docs/intro/install/)
* [Bitnami Helm Charts](https://bitnami.com/stacks/helm)
* [Artifact Hub](https://artifacthub.io/packages/search)
* [Helm 차트 구조 설명](https://helm.sh/docs/topics/charts/)
