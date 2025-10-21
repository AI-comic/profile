# Pod 운영 (Kubernetes Pod Management)

* 쿠버네티스 **Namespace**의 개념과 생성 방법을 이해합니다.
* **Pod의 구조와 실행 방식**을 이해하고 CLI 및 YAML로 관리할 수 있습니다.
* Pod의 **이미지 정책**, **재시작 정책**, **리소스 관리**, **환경 변수**, **볼륨 마운트**를 실습합니다.

---

## 1. 네임스페이스 (Namespace)

### 개념

`Namespace`는 **단일 클러스터 내 리소스를 논리적으로 구분**하기 위한 격리 메커니즘입니다.
리소스 이름은 **네임스페이스 내에서만 고유**하면 됩니다.

> 즉, 같은 클러스터 내에서도 `dev`, `staging`, `prod` 환경을 분리할 수 있습니다.

* 네임스페이스 기반 오브젝트: Pod, Deployment, Service 등
* 클러스터 범위 오브젝트(네임스페이스 적용 불가): Node, StorageClass, PersistentVolume 등

### 실습: 네임스페이스 생성 및 파드 실행

```bash
# 네임스페이스 생성
kubectl create namespace product
kubectl get namespace

# product 네임스페이스 내 파드 확인
kubectl get pods -n product

# nginx 컨테이너를 포함한 webserver 파드 실행
kubectl run webserver -n product --image nginx:1.14

# 실행 확인
kubectl get pods -n product

# 파드 삭제
kubectl delete pod webserver -n product
```

---

## 2. Pod란?

### 개념

* **Pod**는 쿠버네티스에서 **컨테이너를 실행하는 최소 단위이자 배포 단위**입니다.
* 하나의 Pod에는 **하나 이상의 컨테이너**가 포함될 수 있으며, 컨테이너 간 **네트워크·스토리지 공유**가 가능합니다.

> 💡 Pod = 하나의 애플리케이션 단위
> 예: `nginx` + `log-agent` 같이 함께 동작해야 하는 컨테이너를 하나의 Pod로 구성

---

## 3. Pod의 격리 구조 (Linux Namespace 기반)

| 구분      | 설명                           |
| ------- | ---------------------------- |
| PID     | Pod 내부의 컨테이너가 각자 프로세스로 실행됨   |
| User    | 컨테이너별 애플리케이션 소유자 존재          |
| Mount   | Pod 내부 스토리지는 독립적으로 마운트 가능    |
| IPC     | Pod 내 프로세스 간 통신 가능           |
| UTS     | Pod는 고유한 호스트명/도메인을 가짐        |
| Network | Pod마다 독립된 IP와 네트워크 네임스페이스 가짐 |

---

## 4. Pod 실행 방법

### CLI로 실행

```bash
# nginx 1.14 버전 실행
kubectl run webserver --image=nginx:1.14 --port=80

# 파드 목록 확인
kubectl get pods

# 삭제
kubectl delete pod webserver
```

### YAML로 실행

YAML 파일을 이용하면 재사용과 관리가 쉬워집니다.

```bash
kubectl run webserver --image nginx:1.14 --port 80 --dry-run=client -o yaml > webserver.yaml
```

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: webserver
spec:
  containers:
  - name: nginx
    image: nginx:1.14
    command: ['bash', '-c', 'echo "Hello, Kubernetes!" && sleep 1d']
    ports:
    - containerPort: 80
```

```bash
kubectl apply -f webserver.yaml
```

---

## 5. 이미지 다운로드 정책 (imagePullPolicy)

| 정책             | 설명                            |
| -------------- | ----------------------------- |
| `Always`       | 새 파드 생성 시마다 이미지를 레지스트리에서 다운로드 |
| `IfNotPresent` | 로컬에 이미지가 없을 때만 다운로드           |
| `Never`        | 항상 로컬 이미지만 사용 (레지스트리 접근 X)    |

---

## 6. 컨테이너 재시작 정책 (restartPolicy)

| 정책              | 설명                 |
| --------------- | ------------------ |
| `Always`        | 항상 재시작 (기본값)       |
| `OnFailure`     | 비정상 종료 시 재시작       |
| `Never`         | 재시작하지 않음           |
| `UnlessStopped` | 수동 중지 시에는 재시작하지 않음 |

---

## 7. 멀티 컨테이너 Pod (Sidecar 패턴)

> 하나의 Pod에 여러 컨테이너를 실행하여 **보조 기능(sidecar)**을 함께 운용할 수 있습니다.

예: `nginx` 웹 서버와 로그 수집기(`fluentd`)를 함께 실행

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: multi-container
spec:
  containers:
  - name: web
    image: nginx
  - name: log-agent
    image: busybox
    command: ['sh', '-c', 'tail -f /var/log/nginx/access.log']
```

---

## 8. Pod 리소스 제한 (CPU / Memory)

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: limit-example
spec:
  containers:
  - name: demo
    image: smlinux/pause:2.0
    resources:
      requests:
        cpu: "500m"       # 최소 0.5 CPU 요청
      limits:
        cpu: "1"          # 최대 1 CPU 제한
        memory: "100Mi"
```

---

## 9. 환경 변수 전달 (Env Variables)

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: envar-demo
spec:
  containers:
  - name: app
    image: gcr.io/google-samples/hello-app:2.0
    env:
    - name: GREETING
      value: "Hello from Kubernetes!"
    - name: FAREWELL
      value: "Goodbye!"
```

---

## 10. Volume Mount (스토리지 연결)

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: mysql-pod
spec:
  containers:
  - name: mysql
    image: mysql:8.0
    volumeMounts:
    - mountPath: /var/lib/mysql
      name: db-volume
  volumes:
  - name: db-volume
    emptyDir: {}   # 임시 저장 공간 (Pod 종료 시 데이터 삭제)
```

---

## 참고 문서

* [Kubernetes 공식 문서 - Namespaces](https://kubernetes.io/ko/docs/concepts/overview/working-with-objects/namespaces/)
* [Kubernetes 공식 문서 - Pods](https://kubernetes.io/ko/docs/concepts/workloads/pods/)
* [Kubernetes 공식 문서 - Container Lifecycle](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/)
* [Docker Hub 공식 문서](https://docs.docker.com/docker-hub/)
* [AWS EKS 가이드](https://docs.aws.amazon.com/eks/latest/userguide/pods.html)
