# Kubernetes Ingress

쿠버네티스에서 **Ingress**는 클러스터 외부의 HTTP(S) 요청을 내부 서비스로 라우팅하기 위한 핵심 API 오브젝트입니다.  
즉, Ingress는 **클러스터의 게이트웨이 역할**을 하며, URL 또는 도메인 기반으로 트래픽을 세밀하게 제어할 수 있습니다.

---

## 1. Ingress란?

### 개념
> **Ingress**는 클러스터 외부에서 내부 서비스로의 **HTTP 및 HTTPS 트래픽을 관리**하는 API 리소스입니다.  
> Ingress Controller가 이를 실제로 처리하며, 주로 **Nginx**, **HAProxy**, **Traefik** 등의 컨트롤러가 사용됩니다.

---

## 2. Ingress 주요 기능

| 기능 | 설명 |
|------|------|
| **URL 경로 기반 라우팅** | `/api` → 서비스 A, `/web` → 서비스 B 등 경로에 따라 트래픽 분기 가능 |
| **호스트 기반 라우팅** | `api.example.com`, `web.example.com` 처럼 도메인별 라우팅 가능 |
| **TLS/SSL 지원** | HTTPS 트래픽을 위한 인증서(TLS) 설정 지원 |
| **기본 백엔드(Backend)** | 규칙에 일치하지 않는 요청을 처리할 기본 서비스 지정 가능 |

 **공식 문서:**  
 [Kubernetes Ingress Concepts](https://kubernetes.io/docs/concepts/services-networking/ingress/)

---

## 3. Ingress Controller 설치

Ingress 리소스는 단독으로 동작하지 않으며, 반드시 **Ingress Controller**가 필요합니다.  
여기서는 **Nginx Ingress Controller**를 사용합니다.

---

### 방법 1: 직접 설치 (Bare-metal 예시)

**공식 배포 링크:**  
 [Ingress-Nginx Deploy Docs](https://kubernetes.github.io/ingress-nginx/deploy/#bare-metal-clusters)

설치 완료 후, 다음 명령으로 서비스 확인:
```bash
kubectl get svc -n ingress-nginx
````

예시 결과:

```
ingress-nginx-controller   NodePort   10.100.10.47   <none>   80:30080/TCP,443:30443/TCP
```

---

### 방법 2: Helm으로 설치 (추천)

1. **Helm 리포지토리 추가 및 업데이트**

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm repo list
```

2. **Ingress Controller 설치**

```bash
helm install my-nginx-ingress ingress-nginx/ingress-nginx \
  --set controller.service.type=NodePort
```

3. **서비스 확인**

```bash
kubectl get svc my-nginx-ingress-ingress-nginx-controller
```

예시 출력:

```
NAME                                        TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)                      AGE
my-nginx-ingress-ingress-nginx-controller   NodePort   10.107.126.47   <none>        80:32024/TCP,443:30608/TCP   36s
```

---

## 4. Default Backend 구성

`defaultBackend`는 Ingress 규칙에서 매칭되지 않은 요청을 처리하는 기본 서비스입니다.
여기서는 간단히 **nginx 웹서버**를 기본 백엔드로 사용합니다.

```bash
kubectl run nginx --image nginx --port 80
kubectl expose pod nginx --target-port 80 --port 80 --name nginx
```

확인:

```bash
kubectl get svc,pod
```

예시 출력:

```
service/nginx   ClusterIP   10.106.155.194   <none>   80/TCP   7s
pod/nginx       1/1         Running          0        12m
```

---

## 5. 애플리케이션 배포 (Deployment & Service)

Ingress로 연결할 두 가지 서비스를 생성합니다.

* `/` 경로 → **nginx 웹서버**
* `/login` 경로 → **smlinux/appjs 애플리케이션**

```bash
cat <<END > ingress-application.yaml
# (1) nginx 웹서버
apiVersion: apps/v1
kind: Deployment
metadata:
  name: example-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: example-app
  template:
    metadata:
      labels:
        app: example-app
    spec:
      containers:
      - name: example-app
        image: nginx:1.14
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: example-service
  labels:
    app: example-app
spec:
  selector:
    app: example-app
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
---
# (2) /login용 appjs 서비스
apiVersion: apps/v1
kind: Deployment
metadata:
  name: appjs
spec:
  replicas: 3
  selector:
    matchLabels:
      name: appjs
  template:
    metadata:
      labels:
        name: appjs
    spec:
      containers:
      - image: smlinux/appjs
        name: appjs
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: appjs-service
spec:
  ports:
  - port: 80
    targetPort: 8080
  selector:
    name: appjs
END
```

적용 및 확인:

```bash
kubectl apply -f ingress-application.yaml
kubectl get pods
kubectl get svc
```

테스트:

```bash
curl <example-service ClusterIP>
curl <appjs-service ClusterIP>
```

---

## 6. Ingress Rule 설정

Ingress를 생성하여 트래픽을 각 서비스로 라우팅합니다.

```bash
cat << END > ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: test-ingress-rule
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  defaultBackend:
    service:
      name: nginx
      port:
        number: 80
  rules:
  - http:
      paths:
        - path: /
          pathType: Prefix
          backend:
            service:
              name: example-service
              port:
                number: 80
        - path: /login
          pathType: Prefix
          backend:
            service:
              name: appjs-service
              port:
                number: 80
END
```

Ingress 적용:

```bash
kubectl apply -f ingress.yaml
kubectl get ingress
kubectl describe ingress test-ingress-rule
```

---

## 7. 접속 테스트

Ingress Controller의 NodePort를 확인한 뒤, 외부에서 curl로 접속합니다.

```bash
kubectl get svc my-nginx-ingress-ingress-nginx-controller
```

예시:

```
PORT(S): 80:32024/TCP,443:30608/TCP
```

테스트:

```bash
curl <Node_IP>:32024/
curl <Node_IP>:32024/login
curl <Node_IP>:32024/login/admin
```

---

## 8. 리소스 정리

```bash
# Ingress 및 애플리케이션 리소스 삭제
kubectl delete -f ingress.yaml -f ingress-application.yaml

# Default Backend 삭제
kubectl delete pod nginx
kubectl delete svc nginx

# Ingress Controller 삭제 (Helm)
helm list
helm uninstall my-nginx-ingress
helm list --all
```

---

## 참고 자료

* [Kubernetes 공식 문서 - Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)
* [Ingress-Nginx 공식 배포 가이드](https://kubernetes.github.io/ingress-nginx/deploy/)
* [Helm 공식 문서](https://helm.sh/docs/)
* [Kubernetes Networking Concepts](https://kubernetes.io/docs/concepts/cluster-administration/networking/)

---

>  **Tip:**
>
> * Ingress는 쿠버네티스에서 **L7(HTTP/HTTPS) 트래픽 관리의 핵심**입니다.
> * 서비스 외부 노출 시 **LoadBalancer 대신 Ingress**를 사용하는 것이 더 유연합니다.
> * 실제 운영 환경에서는 **TLS 인증서 적용**, **Rewrite Rule**, **Rate Limiting** 등의 기능도 함께 구성합니다.

```
