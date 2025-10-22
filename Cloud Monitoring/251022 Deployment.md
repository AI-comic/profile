# 애플리케이션 배포 및 롤링 업데이트

* 쿠버네티스 **Deployment**의 구조와 역할을 이해한다.
* CLI 및 YAML을 사용해 **애플리케이션을 배포**하고, **스케일링**, **롤링 업데이트**, **롤백**을 실습한다.
* **ReplicaSet, Label, Selector** 개념을 함께 익힌다.

---

## 1. Deployment란?

`Deployment`는 쿠버네티스에서 가장 일반적으로 사용하는 **애플리케이션 배포 객체**입니다.
Deployment는 내부적으로 **ReplicaSet**을 제어하며, Pod의 개수(Replicas)를 관리합니다.

> 즉, Deployment는 “이 앱을 몇 개의 Pod로 운영할지”와 “새 버전으로 업데이트할 때 어떻게 교체할지”를 정의하는 **자동화된 관리 단위**입니다.

---

## 2. 기본 구조

### 예시: 간단한 Nginx 배포

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: deploy-nginx
spec:
  replicas: 2
  selector:
    matchLabels:
      app: webui
  template:
    metadata:
      labels:
        app: webui
    spec:
      containers:
      - name: nginx-container
        image: nginx:1.14
```

* **replicas:** Pod의 개수
* **selector:** 어떤 Pod를 관리할지 정의
* **template:** 실제 Pod의 템플릿 (여기서 정의한 내용으로 ReplicaSet과 Pod 생성)

---

## 3. Deployment 생성 실습

### CLI로 생성 후 YAML로 저장

```bash
kubectl create deployment weplat --image=nginx:1.14 --replicas=2 --dry-run=client -o yaml > deployment.yaml
```

### YAML 수정

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: weplat
spec:
  replicas: 2
  selector:
    matchLabels:
      app: weplat
  template:
    metadata:
      labels:
        app: weplat
    spec:
      containers:
      - name: web
        image: nginx:1.14
        resources: {}
```

### 배포 및 확인

```bash
kubectl apply -f deployment.yaml
kubectl get all
```

---

## 4. Label과 Selector 이해

Deployment는 **Label**과 **Selector**를 이용해 관리 대상을 지정합니다.

### Label과 Selector 예시

```yaml
selector:
  matchLabels:
    component: redis
  matchExpressions:
    - { key: tier, operator: In, values: ["cache"] }
```

| Operator       | 설명                |
| -------------- | ----------------- |
| `In`           | 지정된 값 중 하나와 일치    |
| `NotIn`        | 지정된 값과 일치하지 않음    |
| `Exists`       | 특정 키 존재 여부만 확인    |
| `DoesNotExist` | 특정 키가 존재하지 않음을 확인 |

---

## 5. ReplicaSet 예시

Deployment는 실제로 **ReplicaSet**을 생성하여 Pod를 관리합니다.

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: rs-appjs
spec:
  replicas: 3
  selector:
    matchLabels:
      app: appjs
  template:
    metadata:
      labels:
        app: appjs
        name: nginx
    spec:
      containers:
      - name: appjs-container
        image: smlinux/appjs
        ports:
        - containerPort: 8080
```

> **참고:**
> Deployment의 **Pod Template(spec.template)**이 변경될 때만 새로운 ReplicaSet이 생성되어 “Rollout”이 발생합니다.
> 단순히 replicas 수를 변경하는 것은 새로운 rollout을 유발하지 않습니다.

---

## 6. Pod 확장 (Scaling)

### 명령어로 확장/축소

```bash
# nginx 컨테이너를 3개로 확장
kubectl scale deployment weplat --replicas=3

# 현재 상태 확인
kubectl get pods
```

> Pod 수를 늘리거나 줄여도 Deployment가 ReplicaSet을 통해 자동으로 조정합니다.

---

## 7. Rolling Update (무중단 배포)

`Rolling Update`는 서비스 중단 없이 Pod를 점진적으로 교체하는 방식입니다.
이전 버전의 Pod를 하나씩 제거하면서 새 버전의 Pod를 동시에 생성합니다.

### 1) 변경 이유(annotate) 등록

```bash
kubectl annotate deployment weplat kubernetes.io/change-cause="Upgrade to nginx 1.15"
```

### 2) 이미지 버전 업데이트

```bash
kubectl set image deployment weplat web=nginx:1.15
```

### 3) 롤아웃 상태 및 히스토리 확인

```bash
kubectl rollout status deployment weplat
kubectl rollout history deployment weplat
```

### YAML 예시

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
  annotations:
    kubernetes.io/change-cause: "Upgraded to nginx v1.15"
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.15
        ports:
        - containerPort: 80
```

---

## 8. Rollback (이전 버전으로 되돌리기)

롤링 업데이트 시 자동으로 **history**가 남으며, 이를 기반으로 손쉽게 되돌릴 수 있습니다.

### 롤백 명령어

```bash
# 변경 이력 조회
kubectl rollout history deployment weplat

# 직전 버전으로 롤백
kubectl rollout undo deployment weplat

# 특정 리비전으로 롤백
kubectl rollout undo deployment weplat --to-revision=1
```

### 버전 확인

```bash
kubectl describe pod weplat-xxx-yyy
```

> **Tip:**
> 롤백 시에도 서비스는 중단되지 않습니다.
> 이전 버전의 ReplicaSet을 활성화하고, 현재 버전의 Pod를 점진적으로 제거합니다.

---

## 참고 문서

* [Kubernetes 공식 문서 - Deployments](https://kubernetes.io/ko/docs/concepts/workloads/controllers/deployment/)
* [Kubernetes 공식 문서 - ReplicaSet](https://kubernetes.io/ko/docs/concepts/workloads/controllers/replicaset/)
* [Kubernetes 공식 문서 - Rolling Updates](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#updating-a-deployment)
* [kubectl rollout 사용법](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#rollout)
