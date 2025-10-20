# AWS Load Balancer Controller 배포 및 실습

> AWS EKS 환경에서 **AWS Load Balancer Controller(ALB & NLB)** 를 설치하고
> Kubernetes 리소스(Service, Ingress)를 통해 트래픽을 분산하는 과정입니다.
> *(참고: [AWS 공식 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/lbc-helm.html))*

---

## 1. EKS에서의 로드 밸런싱 개요

EKS 클러스터에서 애플리케이션은 외부 트래픽을 수신하기 위해 **로드 밸런서**가 필요합니다.
AWS에서는 크게 두 가지 방식이 있습니다.

### Network Load Balancer (NLB)

* `Service` 리소스의 `type: LoadBalancer` 설정을 감시해 자동으로 NLB를 생성합니다.
* TCP/UDP 트래픽 처리에 특화되어 있으며, **저지연·고성능** 환경에 적합합니다.

```text
Client → NLB → EKS Node → Pod
```

> 참고: [AWS EKS NLB 공식 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/network-load-balancing.html)

---

### Application Load Balancer (ALB)

* `Ingress` 리소스를 감시하여 ALB를 자동 생성 및 관리합니다.
* HTTP/HTTPS 트래픽을 여러 파드로 분산하며, 경로 기반/호스트 기반 라우팅이 가능합니다.

```text
Client → ALB → Ingress → Service → Pod
```

> 참고: [AWS EKS ALB 공식 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/alb-ingress.html)

---

## 2. AWS Load Balancer Controller 배포

AWS Load Balancer Controller는
**Kubernetes와 AWS ALB/NLB를 연결하는 핵심 컨트롤러**로, Helm을 통해 설치합니다.

### 2.1 Helm 설치

```bash
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

helm version
```

---

### 2.2 클러스터 정보 확인 및 OIDC 구성

```bash
export cluster_name=demo-eks
oidc_id=$(aws eks describe-cluster --name $cluster_name \
  --query "cluster.identity.oidc.issuer" --output text | cut -d '/' -f 5)
echo $oidc_id

# 출력값이 없으면 OIDC 공급자 연결
eksctl utils associate-iam-oidc-provider --cluster $cluster_name --approve
```

---

### 2.3 IAM 정책 및 ServiceAccount 생성

AWS Load Balancer Controller가 AWS API를 호출할 수 있도록 IAM 권한을 부여합니다.

```bash
# IAM 정책 파일 다운로드
curl -O https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.7.2/docs/install/iam_policy.json

# 정책 생성
aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json

# ServiceAccount 생성 (eksctl)
eksctl create iamserviceaccount \
  --cluster=demo-eks \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --role-name AmazonEKSLoadBalancerControllerRole \
  --attach-policy-arn=arn:aws:iam::<ACCOUNT_ID>:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve
```

확인:

```bash
kubectl get sa -n kube-system | grep load
kubectl describe sa aws-load-balancer-controller -n kube-system
```

---

### 2.4 Helm으로 Controller 설치

```bash
# Helm repo 추가
helm repo add eks https://aws.github.io/eks-charts
helm repo update eks

# 설치
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=demo-eks \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller

# 배포 상태 확인
kubectl get deployment -n kube-system aws-load-balancer-controller
kubectl get pod -n kube-system
```

> 정상 상태 예시:
>
> ```
> aws-load-balancer-controller-7d6bb464b6-tq4kr   1/1   Running   0   2m
> ```

---

## 3. NLB 서비스 배포 예시

AWS Load Balancer Controller가 설치된 상태에서
Service를 `LoadBalancer`로 정의하면 **자동으로 NLB가 생성**됩니다.

```yaml
# echo-service-nlb.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: deploy-echo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: deploy-websrv
  template:
    metadata:
      labels:
        app: deploy-websrv
    spec:
      containers:
      - name: echo
        image: k8s.gcr.io/echoserver:1.5
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: svc-nlb-ip-type
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-nlb-target-type: ip
    service.beta.kubernetes.io/aws-load-balancer-scheme: internet-facing
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-port: "8080"
    service.beta.kubernetes.io/aws-load-balancer-cross-zone-load-balancing-enabled: "true"
spec:
  type: LoadBalancer
  loadBalancerClass: service.k8s.aws/nlb
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: deploy-websrv
```

배포 및 확인:

```bash
kubectl apply -f echo-service-nlb.yaml
kubectl get pod -o wide
kubectl get svc
kubectl get targetgroupbindings
```

> 생성된 NLB는 AWS 콘솔 > EC2 > Load Balancer 메뉴에서 확인 가능
> 브라우저로 `NLB DNS 주소` 접속 후 echoserver 응답 확인

---

## 4. ALB Ingress 예시

아래 예제는 `2048` 게임을 ALB를 통해 외부로 노출하는 구성입니다.

```yaml
# 2048_full.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: game-2048
---
apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: game-2048
  name: deployment-2048
spec:
  replicas: 2
  selector:
    matchLabels:
      app.kubernetes.io/name: app-2048
  template:
    metadata:
      labels:
        app.kubernetes.io/name: app-2048
    spec:
      containers:
      - name: app-2048
        image: public.ecr.aws/l6m2t8p7/docker-2048:latest
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  namespace: game-2048
  name: service-2048
spec:
  type: NodePort
  ports:
  - port: 80
    targetPort: 80
  selector:
    app.kubernetes.io/name: app-2048
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  namespace: game-2048
  name: ingress-2048
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
spec:
  ingressClassName: alb
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: service-2048
            port:
              number: 80
```

배포 및 확인:

```bash
kubectl apply -f 2048_full.yaml
kubectl get pods -n game-2048
kubectl get svc -n game-2048
kubectl get ingress -n game-2048
```

> AWS 콘솔에서 Application Load Balancer가 자동 생성되며
> “리스너 규칙” 및 “DNS 주소”를 통해 게임 페이지에 접근 가능.

---

## 참고 문서

| 구분                                  | 링크                                                                                                                                                                                         |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| AWS Load Balancer Controller 공식 가이드 | [https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/lbc-helm.html](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/lbc-helm.html)                                               |
| AWS ALB Ingress Controller 공식 가이드   | [https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/alb-ingress.html](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/alb-ingress.html)                                         |
| Helm 설치 매뉴얼                         | [https://helm.sh/docs/intro/install/](https://helm.sh/docs/intro/install/)                                                                                                                 |
| EKS OIDC 설정 가이드                     | [https://docs.aws.amazon.com/eks/latest/userguide/enable-iam-roles-for-service-accounts.html](https://docs.aws.amazon.com/eks/latest/userguide/enable-iam-roles-for-service-accounts.html) |

---

**요약**

* Helm으로 Controller 설치
* IAM Role 및 OIDC 구성 필수
* NLB(Service), ALB(Ingress) 각각 실습
* AWS 콘솔에서 리소스 생성 확인 가능

---
