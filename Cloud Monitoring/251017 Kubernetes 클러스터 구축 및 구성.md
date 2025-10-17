## 1. Kubernetes 주요 컴포넌트

### Control Plane Components

| 구성요소                   | 설명                                                |
| ---------------------- | ------------------------------------------------- |
| **API Server**         | 클러스터의 중심으로, 모든 요청(REST API)을 처리하고 상태를 etcd에 저장한다. |
| **etcd**               | 분산 Key-Value 저장소로, 클러스터의 모든 상태 데이터를 저장한다.         |
| **Controller Manager** | 리소스를 모니터링하며 클러스터를 원하는 상태로 유지한다.                   |
| **Scheduler**          | 새로운 Pod를 어떤 Node에 배치할지 결정한다.                      |

### Add-On Components

| 구성요소                   | 설명                                  |
| ---------------------- | ----------------------------------- |
| **CoreDNS**            | 서비스와 Pod에 대한 DNS 이름을 제공한다.          |
| **Ingress Controller** | 외부 트래픽을 클러스터 내부 서비스로 라우팅한다.         |
| **CNI Plugin**         | Pod 간 네트워크를 연결하는 네트워크 인터페이스 플러그인이다. |
| **Dashboard**          | 웹 기반 Kubernetes 관리 UI.              |

### Worker Node Components

| 구성요소                  | 설명                                                |
| --------------------- | ------------------------------------------------- |
| **Kubelet**           | Pod를 관리하고, Control Plane과 통신한다.                   |
| **Kube-Proxy**        | 네트워크 트래픽을 라우팅하며, 각 Node의 서비스 접근을 관리한다.            |
| **Container Runtime** | 실제 컨테이너를 실행하는 소프트웨어 (예: `containerd`, `CRI-O` 등). |

---

## 2. Kubernetes API 리소스 관계

* Kubernetes 리소스는 **계층적인 관계**를 가진다.
* 예: `Deployment → ReplicaSet → Pod → Container`
* CKA 시험이나 실무에서 리소스 간 관계를 이해하는 것이 중요하다.

참고 문서

* [Kubernetes Architecture Overview](https://kubernetes.io/docs/concepts/overview/components/)
* [Kubernetes API Concepts](https://kubernetes.io/docs/concepts/overview/kubernetes-api/)

---

## 3. 환경 준비

### 시스템 구성 예시

| 역할            | 호스트명          | IP          | OS               |
| ------------- | ------------- | ----------- | ---------------- |
| Control Plane | `k8s-master`  | `10.0.2.20` | Ubuntu 22.04 LTS |
| Worker 1      | `k8s-worker1` | `10.0.2.21` | Ubuntu 22.04 LTS |
| Worker 2      | `k8s-worker2` | `10.0.2.22` | Ubuntu 22.04 LTS |

### 필수 사양

* CPU: 2 core 이상
* RAM: 2GB 이상
* 모든 노드 간 네트워크 연결 필수
* **Swap 비활성화 필수**

```bash
sudo swapoff -a && sed -i '/swap/s/^/#/' /etc/fstab
```

---

## 4. 네트워크 및 커널 설정 (모든 노드)

```bash
sudo tee /etc/modules-load.d/k8s.conf <<EOF
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

sudo tee /etc/sysctl.d/k8s.conf <<EOF
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

---

## 5. 컨테이너 런타임 (containerd) 설치

참고 문서: [Install Containerd (공식 문서)](https://kubernetes.io/ko/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#installing-runtime)

```bash
sudo apt update
sudo apt install apt-transport-https ca-certificates curl gnupg lsb-release -y
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io -y
```

### containerd 설정

```bash
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml
sudo vi /etc/containerd/config.toml
# [plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc.options]
# SystemdCgroup = true

sudo systemctl restart containerd
sudo systemctl enable containerd
```

---

## 6. kubeadm, kubelet, kubectl 설치 (모든 노드)

```bash
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg

curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.30/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.30/deb/ /" | sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

---

## 7. Control Plane 초기화

```bash
sudo kubeadm init --cri-socket unix:///var/run/containerd/containerd.sock
```

초기화가 완료되면 다음과 같은 메시지가 표시됩니다:

```
Your Kubernetes control-plane has initialized successfully!
```

### kubectl 설정

```bash
mkdir -p $HOME/.kube
sudo cp /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

---

## 8. Pod Network (WeaveNet) 설정

참고: [Weave Net Add-on 설치](https://www.weave.works/docs/net/latest/kubernetes/kube-addon/)

```bash
kubectl apply -f https://github.com/weaveworks/weave/releases/download/v2.8.1/weave-daemonset-k8s.yaml
watch kubectl get pods -A
```

---

## 9. Worker Node 클러스터 조인

Control Plane 초기화 시 출력된 명령어를 Worker Node에서 실행:

```bash
sudo kubeadm join <MASTER_IP>:6443 --token <TOKEN> \
  --discovery-token-ca-cert-hash sha256:<HASH> \
  --cri-socket unix:///var/run/containerd/containerd.sock
```

마스터에서 확인:

```bash
kubectl get nodes -o wide
```

---

## 10. kubectl 자동 완성 설정

```bash
sudo apt install bash-completion -y
echo "source <(kubectl completion bash)" >> ~/.bashrc
source ~/.bashrc
```

---

## etcd 수동 설치 (선택)

```bash
export RELEASE=$(curl -s https://api.github.com/repos/etcd-io/etcd/releases/latest | grep tag_name | cut -d '"' -f 4)
wget https://github.com/etcd-io/etcd/releases/download/${RELEASE}/etcd-${RELEASE}-linux-amd64.tar.gz
tar xf etcd-${RELEASE}-linux-amd64.tar.gz
sudo mv etcd-${RELEASE}-linux-amd64/{etcd,etcdctl,etcdutl} /usr/local/bin/
etcd --version
```

---

## 컨테이너 디버깅 도구

| 도구       | 설명                                |
| -------- | --------------------------------- |
| `crictl` | CRI 런타임과 직접 상호작용하여 컨테이너 및 이미지 관리  |
| `ctr`    | containerd 런타임 관리용 CLI (고급 기능 포함) |

참고 문서

* [crictl 사용법 (GitHub)](https://github.com/kubernetes-sigs/cri-tools/blob/master/docs/crictl.md)
* [Kubernetes 클러스터 디버깅 (공식 문서)](https://kubernetes.io/ko/docs/tasks/debug/debug-cluster/crictl/)

---

## 최종 확인

```bash
kubectl get nodes
kubectl get pods -A
```

정상 출력 예시:

```
NAME          STATUS   ROLES           AGE   VERSION
k8s-master    Ready    control-plane   12m   v1.30.0
k8s-worker1   Ready    <none>          6m    v1.30.0
k8s-worker2   Ready    <none>          6m    v1.30.0
```

---

## 참고 문서

* [Kubernetes 공식 설치 가이드](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)
* [Weave Net Kubernetes Network](https://www.weave.works/docs/net/latest/kubernetes/kube-addon/)
* [crictl Command Reference](https://github.com/kubernetes-sigs/cri-tools/blob/master/docs/crictl.md)
* [Multi-node Kubernetes Cluster 구축 예시 (Notion 참고)](https://mud-riddle-377.notion.site/d60ddb1cb259489a968f879a8de71667)
