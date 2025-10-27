# Kubernetes Gateway API

> Kubernetes의 최신 네트워크 트래픽 관리 표준인 **Gateway API**를 이해하고,
> Ingress 대비 구조적 차이와 실습 예시를 통해 직접 트래픽을 라우팅해본다.

---

## Gateway API란?

**Gateway API**는 Kubernetes에서 **외부 트래픽을 내부 서비스로 안전하고 유연하게 전달하기 위한 새로운 표준 API 집합**입니다.
기존 Ingress보다 더 세분화된 역할과 보안, 멀티테넌시를 지원합니다.

### 주요 특징

* **L4~L7 트래픽 지원** (HTTP, HTTPS, TCP, TLS, gRPC 등)
* **멀티 테넌시 및 권한 분리 구조** 제공
* **컨트롤러 독립적 구조** (NGINX, Istio, Traefik 등 사용 가능)
* **선언형(Declarative) 구성에 최적화**
* **Ingress보다 유연하고 확장 가능한 설계**

> 공식 문서: [Gateway API 공식 사이트](https://gateway-api.sigs.k8s.io/)

---

## Gateway API 구성 요소

| 리소스                                                    | 설명                                            |
| ------------------------------------------------------ | --------------------------------------------- |
| **GatewayClass**                                       | 인프라 제공자가 정의하는 Gateway 구현 클래스                  |
| **Gateway**                                            | 실제 L4/L7 트래픽을 수신하는 리소스 (Ingress의 Listener 역할) |
| **Route** (HTTPRoute, TCPRoute, TLSRoute, GRPCRoute 등) | 트래픽을 특정 서비스로 라우팅하는 규칙                         |

> 출처: [gateway-api.sigs.k8s.io](https://gateway-api.sigs.k8s.io/)

---

## Gateway API vs Ingress

| 구분          | Ingress                     | Gateway API                                  |
| ----------- | --------------------------- | -------------------------------------------- |
| **역할**      | 포트, 인증서, 라우팅 규칙을 한 리소스에서 관리 | Gateway와 Route 리소스로 역할 분리                    |
| **지원 프로토콜** | 주로 HTTP(S)                  | HTTP, HTTPS, TCP, TLS, gRPC 등                |
| **확장성**     | 단일 도메인/팀 중심                 | 멀티도메인, 멀티네임스페이스 지원                           |
| **보안 분리**   | 제한적                         | `allowedRoutes`를 통해 네임스페이스별 권한 분리 가능         |
| **컨트롤러**    | Ingress Controller          | Gateway Controller (nginx, istio, traefik 등) |

> 정리: Ingress는 단순 HTTP 라우팅에 적합하고,
> Gateway API는 대규모 환경이나 멀티팀 환경에 적합합니다.

---

## Gateway Controller란?

Gateway Controller는 Gateway API 리소스(`Gateway`, `HTTPRoute`)를 감시하고,
실제 네트워크 트래픽을 처리하는 역할을 수행합니다.

예시:

* `nginx-gateway-fabric`
* `istio`
* `traefik`

---

## 실습: Gateway API 설정 및 HTTP 라우팅

### STEP 1. frontend 서비스 배포

```bash
# NGINX Pod와 Service 생성
kubectl run frontend-app --image=nginx --port 80
kubectl expose pod frontend-app --name=frontend-svc --port=80

# 확인
kubectl get pods,svc
```

---

### STEP 2. Gateway 리소스 생성

Gateway는 포트 리스닝, 프로토콜, 인증서 설정 등을 담당합니다.

```bash
cat <<EOF > gateway-http.yaml
apiVersion: gateway.networking.k8s.io/v1beta1
kind: Gateway
metadata:
  name: nginx-gateway
  namespace: nginx-gateway
spec:
  gatewayClassName: nginx
  listeners:
  - name: http
    protocol: HTTP
    port: 80
    allowedRoutes:
      namespaces:
        from: All
EOF

kubectl apply -f gateway-http.yaml
kubectl get gateways -n nginx-gateway
kubectl describe gateway nginx-gateway -n nginx-gateway
```

> `PROGRAMMED: True` → Gateway Controller가 정상적으로 리소스를 인식했다는 의미입니다.

---

### STEP 3. HTTPRoute 생성

HTTPRoute는 Gateway와 연결되어 특정 경로 요청을 특정 서비스로 전달합니다.

```bash
cat <<EOF > httproute.yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: frontend-route
  namespace: default
spec:
  parentRefs:
  - name: nginx-gateway
    namespace: nginx-gateway
    sectionName: http
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: frontend-svc
      port: 80
EOF

kubectl apply -f httproute.yaml
kubectl get httproutes -n default
kubectl describe httproute frontend-route -n default
```

**접속 테스트**

```bash
curl http://<노드 IP>:<NodePort>/
```

> 예시 응답:
>
> ```
> <title>Welcome to nginx!</title>
> ```

---

## LAB: /web 경로 라우팅 실습

`/web` 경로로 요청이 들어오면 `frontend-svc`로 전달되도록 설정합니다.

```bash
cat <<EOF > httproute.yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: frontend-route
  namespace: default
spec:
  parentRefs:
  - name: nginx-gateway
    namespace: nginx-gateway
    sectionName: http
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /web
    backendRefs:
    - name: frontend-svc
      port: 80
EOF

kubectl apply -f httproute.yaml
curl http://<노드 IP>:<NodePort>/web
```

---

## TLS 종료 (TLS Termination)

### TLS란?

TLS는 **클라이언트와 서버 간 통신을 암호화**하여 보안을 강화하는 프로토콜입니다.

### Gateway에서의 TLS Termination

Gateway가 HTTPS 요청을 복호화(TLS 종료)하고,
내부 서비스에는 일반 HTTP로 전달하는 구조입니다.

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: Gateway
metadata:
  name: nginx-gateway-tls
  namespace: default
spec:
  gatewayClassName: nginx
  listeners:
  - name: https
    protocol: HTTPS
    port: 443
    tls:
      mode: Terminate
      certificateRefs:
      - kind: Secret
        name: tls-secret  # TLS 인증서 저장된 Secret
    allowedRoutes:
      namespaces:
        from: All
```

 흐름:

1. 클라이언트 → HTTPS 요청
2. Gateway → TLS 복호화 (`tls-secret` 사용)
3. 백엔드 서비스로 HTTP 요청 전달

---

## 리소스 정리

```bash
kubectl delete -f httproute.yaml -f gateway-http.yaml
kubectl delete pod frontend-app
kubectl delete svc frontend-svc
```

---

## 참고 자료

* [Gateway API 공식 문서](https://gateway-api.sigs.k8s.io/)
* [Kubernetes Gateway 가이드](https://kubernetes.io/docs/concepts/services-networking/gateway/)
* [Nginx Gateway Fabric GitHub](https://github.com/nginxinc/nginx-gateway-fabric)
* [Gateway API 실습 예시 (Simple Gateway)](https://gateway-api.sigs.k8s.io/guides/simple-gateway/)
