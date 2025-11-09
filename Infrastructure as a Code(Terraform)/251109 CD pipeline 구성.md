# CD pipeline 구성

## 1. CI/CD 전체 동작 개요

CI/CD는 애플리케이션의 **빌드 → 배포 → 운영** 과정을 자동화하는 pipeline입니다.

| 구분                              | 역할                       | 주요 도구              |
| ------------------------------- | ------------------------ | ------------------ |
| **CI (Continuous Integration)** | 코드 변경을 자동으로 빌드 및 테스트     | **GitHub Actions** |
| **CD (Continuous Deployment)**  | 빌드된 애플리케이션을 자동으로 배포 및 관리 | **ArgoCD**         |

**동작 흐름**

1. 개발자가 코드를 GitHub에 push
2. GitHub Actions가 빌드 및 테스트 수행 후, Docker 이미지를 **ECR(AWS)** 에 푸시
3. GitHub Actions가 `k8s/deployment.yaml` 내 이미지 태그를 최신으로 업데이트
4. **ArgoCD**가 Git 변경사항을 감지하고, **EKS 클러스터**에 자동 배포

---

## 2. ArgoCD 이해하기

ArgoCD는 **GitOps 방식의 CD 도구**로, Git 저장소를 기준으로 쿠버네티스 클러스터 상태를 자동 관리합니다.

### 주요 특징

1. **Git 기반 배포 관리 (GitOps)**

   * Git 리포지토리의 YAML 파일이 클러스터 상태를 정의
   * GitHub Actions에서 `deployment.yaml`이 변경되면 ArgoCD가 이를 감지

2. **자동 동기화 및 배포**

   * Git 변경 감지 → 최신 매니페스트를 클러스터에 반영
   * 변경된 Docker 이미지를 ECR에서 가져와 자동 배포

3. **상태 모니터링 및 복구**

   * 클러스터 상태와 Git 상태를 비교
   * 불일치 시 Git에 정의된 상태로 자동 복원 (Self-Heal 기능)

4. **롤백 기능**

   * 문제 발생 시 이전 버전으로 쉽게 되돌릴 수 있음

5. **시각화 UI 제공**

   * 웹 UI에서 배포 상태, Sync 여부, 리소스 헬스 등을 확인 가능

---

## 3. CD pipeline 구성 (GitHub → ArgoCD → EKS)

### GitHub Repository 구조

```
gitops_petclinic/
 ├── k8s/
 │   ├── deployment.yaml
 │   ├── service.yaml
 │   └── ingress.yaml
```

GitHub Actions에서 `deployment.yaml`을 수정하고 커밋하면,
ArgoCD가 이를 감지하여 EKS 클러스터에 자동 반영합니다.

---

### 1. deployment.yaml

> **CI가 업데이트하고 CD가 참조하는 핵심 파일**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: petclinic
spec:
  replicas: 3
  selector:
    matchLabels:
      app: petclinic
  template:
    metadata:
      labels:
        app: petclinic
    spec:
      containers:
        - name: petclinic
          image: 213899591783.dkr.ecr.ap-northeast-3.amazonaws.com/gitops2-repo:latest
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: 250m
              memory: 512Mi
            limits:
              cpu: 500m
              memory: 1Gi
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 15
```

---

### 2. service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: petclinic-service
spec:
  selector:
    app: petclinic
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: LoadBalancer
```

---

### 3. ingress.yaml

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: petclinic-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
spec:
  rules:
    - http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: petclinic-service
                port:
                  number: 80
```

---

## 4. ArgoCD 애플리케이션 자동화 설정

ArgoCD는 `Application` 리소스를 통해 어떤 Git 저장소의 어떤 경로를 배포할지 정의합니다.

### application.yaml 작성 및 적용

Bastion 서버에서 아래 명령어 실행:

```bash
ssh -i .ssh/cicd-key.pem ubuntu@<BASTION_HOST>

cat > application.yaml <<EOF
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: petclinic
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/<YOUR_ID>/gitops_petclinic.git
    targetRevision: HEAD
    path: k8s
  destination:
    server: https://kubernetes.default.svc
    namespace: petclinic
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - Validate=false
      - CreateNamespace=true
      - PrunePropagationPolicy=foreground
      - PruneLast=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
EOF

kubectl apply -f application.yaml
kubectl get applications -n argocd
```

**예시 출력**

```
NAME        SYNC STATUS   HEALTH STATUS
petclinic   Synced        Progressing
```

---

## 정리

* GitHub Actions → ECR → ArgoCD → EKS 순으로 자동 배포
* ArgoCD는 Git의 매니페스트 변경을 실시간 감지하여 **자동 동기화 및 롤백 지원**
* GitOps 방식으로 **코드와 인프라 상태를 일관되게 유지**
