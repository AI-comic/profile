# Kubernetes Service

쿠버네티스에서 **Service(서비스)** 는 여러 개의 **Pod** 에 공통된 접근점을 제공하는 **네트워크 추상화 객체**입니다.
즉, Service는 **Pod의 IP가 변해도 안정적인 접근 경로를 유지**할 수 있도록 도와줍니다.

---

## 1. Service 동작 원리

쿠버네티스의 서비스는 `kube-proxy`를 통해 구현됩니다.

* **kube-proxy 역할**

  * 각 노드에서 **서비스의 가상 IP(Virtual IP)** 와 **백엔드 Pod의 실제 IP** 를 연결합니다.
  * `iptables` 또는 `ipvs` 규칙을 설정해 **로드밸런싱**을 수행합니다.
  * 외부 요청이 들어오면 자동으로 적절한 Pod로 트래픽을 전달합니다.

 즉, `Service → kube-proxy → Pod` 순으로 요청이 전달됩니다.

---

## 2. Service 타입 (4가지)

| 타입                  | 설명                             | 예시                     |
| ------------------- | ------------------------------ | ---------------------- |
| **ClusterIP (기본값)** | 클러스터 내부에서만 접근 가능한 가상 IP 생성     | 내부 Pod 간 통신용           |
| **NodePort**        | 외부에서 접근 가능하도록 각 노드의 특정 포트 개방   | `NodeIP:NodePort` 로 접근 |
| **LoadBalancer**    | 클라우드 환경에서 외부 로드밸런서를 자동으로 프로비저닝 | AWS, Azure, GCP 등      |
| **ExternalName**    | 외부 도메인 이름을 내부 서비스 이름으로 매핑      | 외부 DB, API 연동 시 사용     |

---

## 3. ClusterIP 타입 서비스 예제

같은 기능을 하는 여러 Pod를 하나의 서비스로 묶는 기본적인 방식입니다.

### (1) Deployment 생성

```bash
kubectl create deployment web-ui --image=nginx --port=80 --replicas=2
```

### (2) Service 생성

```bash
kubectl expose deployment web-ui \
  --type=ClusterIP \
  --port=80 \
  --target-port=80 \
  --name=web-ui-svc
```

YAML 파일로 저장하려면:

```bash
kubectl expose deployment web-ui --type=ClusterIP \
--port 80 --target-port 80 --name web-ui-svc \
--dry-run=client -o yaml > svc-webserver.yaml
```

### (3) 적용 및 확인

```bash
kubectl apply -f svc-webserver.yaml
kubectl get svc
```

### (4) 내부 접근 테스트

```bash
curl <ClusterIP>
```

---

## 4. NodePort 타입 서비스 예제

외부에서도 접근할 수 있도록 각 **노드의 특정 포트(예: 30100)** 를 열어줍니다.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: web-ui-svc
spec:
  type: NodePort
  selector:
    app: web-ui
  ports:
    - port: 80
      targetPort: 80
      nodePort: 30100
      protocol: TCP
```

적용 후 확인:

```bash
kubectl apply -f svc-webserver.yaml
kubectl get svc
```

웹 브라우저에서 테스트:

```
http://<NodeIP>:30100
```

---

## 5. CoreDNS (내부 DNS 서비스)

`CoreDNS`는 쿠버네티스 클러스터 내에서 DNS 역할을 수행합니다.
서비스 이름으로 접근할 때 실제 IP로 변환해주는 역할을 합니다.

### CoreDNS 동작 원리

* `service 생성 시` 자동으로 **서비스명 → Cluster IP** 매핑 정보가 저장됩니다.
* 모든 Pod는 `/etc/resolv.conf` 파일을 통해 CoreDNS를 DNS 서버로 지정합니다.

### 서비스 및 파드 도메인 구조

| 대상          | 형식                                        | 예시                                         |
| ----------- | ----------------------------------------- | ------------------------------------------ |
| **Service** | `<service>.<namespace>.svc.cluster.local` | `order-svc.default.svc.cluster.local`      |
| **Pod**     | `<pod-ip>.<namespace>.pod.cluster.local`  | `192-168-126-18.default.pod.cluster.local` |

---

### CoreDNS 테스트 예제

1. 서비스 생성

```yaml
apiVersion: v1
kind: Service
metadata:
  name: web-ui-svc
  labels:
    app: web-ui
spec:
  ports:
    - port: 80
      protocol: TCP
      targetPort: 80
  selector:
    app: web-ui
```

2. 적용

```bash
kubectl apply -f svc-webserver.yaml
```

3. BusyBox로 DNS 질의 테스트

```bash
kubectl run dns-test --image=busybox:1.28 -it -- /bin/sh
```

4. DNS 설정 확인

```bash
cat /etc/resolv.conf
```

출력 예시:

```
search default.svc.cluster.local svc.cluster.local cluster.local
nameserver 10.96.0.10
options ndots:5
```

5. 서비스 이름으로 조회

```bash
nslookup web-ui-svc
```

---

## 정리

| 개념               | 설명                         |
| ---------------- | -------------------------- |
| **Service**      | Pod를 묶고 접근 경로를 제공하는 추상화 객체 |
| **ClusterIP**    | 클러스터 내부 접근 전용              |
| **NodePort**     | 외부 접근용 포트 개방               |
| **LoadBalancer** | 외부 로드밸런서 자동 생성             |
| **CoreDNS**      | 서비스 이름을 IP로 매핑하는 내부 DNS 서버 |

---

## 참고 문서

* [Kubernetes 공식 문서 - Services](https://kubernetes.io/docs/concepts/services-networking/service/)
* [Kubernetes 공식 문서 - DNS for Services and Pods](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/)
* [CoreDNS GitHub Repository](https://github.com/coredns/coredns)
