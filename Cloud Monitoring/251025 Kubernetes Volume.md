# Kubernetes Volume

쿠버네티스에서 Pod는 휘발성(Volatile) 환경이므로, Pod가 재시작되면 내부 데이터가 사라집니다.  
이 문제를 해결하기 위해 **Volume**을 사용하며, **PersistentVolume(PV)** 과 **PersistentVolumeClaim(PVC)** 를 통해 영구 저장소를 관리할 수 있습니다.

---

## 1. Kubernetes Volume의 종류

### 1) hostPath
- 호스트 노드의 특정 디렉토리를 Pod 내부에 마운트하는 방식입니다.
- **개발 환경**이나 **단일 노드 클러스터**에서는 편리하지만, **다중 노드 환경에서는 비추천**됩니다.

```yaml
volumes:
  - name: webdata
    hostPath:
      path: /data/web
````

>  예시: `/data/web` 디렉토리를 Pod의 `/usr/share/nginx/html`에 마운트

---

### 2) emptyDir

* Pod가 생성될 때 자동으로 만들어지는 임시 디렉토리입니다.
* Pod가 삭제되면 함께 사라집니다.
* **캐시 데이터**나 **임시 파일 저장용**으로 자주 사용됩니다.

```yaml
volumes:
  - name: cache
    emptyDir: {}
```

---

## 2. Persistent Volume(PV) & Persistent Volume Claim(PVC)

쿠버네티스에서는 **스토리지 관리자가 미리 만들어 둔 디스크 자원(PV)** 을
**사용자가 요청(PVC)** 하여 사용하는 구조입니다.

---

### 1) Persistent Volume (PV)

**관리자(Admin)** 가 클러스터에 미리 등록해 둔 실제 저장소입니다.

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: disk1
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
    - ReadOnlyMany
  persistentVolumeReclaimPolicy: Recycle
  storageClassName: ssd
  hostPath:
    path: /webdata
```

```bash
kubectl apply -f disk1.yaml
kubectl get pv
```

**출력 예시**

```
NAME    CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   AGE
disk1   1Gi        RWO,ROX        Recycle          Available           ssd            17s
```

---

### 2) Persistent Volume Claim (PVC)

**개발자(User)** 가 필요한 스토리지 용량과 접근 권한을 요청하는 선언문입니다.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pvc-webdata
spec:
  accessModes:
    - ReadWriteOnce
    - ReadOnlyMany
  resources:
    requests:
      storage: 500Mi
  storageClassName: ssd
```

```bash
kubectl apply -f pvc-webdata.yaml
kubectl get pv
kubectl get pvc
```

**출력 예시**

```
NAME          STATUS   VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
pvc-webdata   Bound    disk1    1Gi        RWO,ROX        ssd            1m
```

>  `Bound` 상태는 PVC와 PV가 성공적으로 연결되었음을 의미합니다.

---

### 3) Pod에 PVC 마운트하기

PVC를 Pod에 연결해 실제 Nginx 웹서버가 PV를 사용하도록 설정합니다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod01
spec:
  containers:
    - name: nginx
      image: nginx:1.14
      volumeMounts:
        - name: mypd
          mountPath: /usr/share/nginx/html
  volumes:
    - name: mypd
      persistentVolumeClaim:
        claimName: pvc-webdata
```

```bash
kubectl apply -f pod01.yaml
kubectl describe pod pod01
```

---

## 3. Reclaim Policy (스토리지 반환 정책)

| 정책          | 설명                                         |
| ----------- | ------------------------------------------ |
| **Retain**  | PV가 해제되어도 데이터를 유지함. (수동 삭제 필요)             |
| **Recycle** | PV를 비워 재사용 가능하게 초기화함.                      |
| **Delete**  | PV 삭제 시 실제 스토리지도 함께 삭제됨. (클라우드 환경에서 주로 사용) |

---

## 4. 실습 순서 정리

1. **PV 생성**

   ```bash
   kubectl apply -f disk1.yaml
   kubectl get pv
   ```

2. **PVC 생성 및 바인딩 확인**

   ```bash
   kubectl apply -f pvc-webdata.yaml
   kubectl get pvc
   ```

3. **Pod 생성 및 PV 마운트**

   ```bash
   kubectl apply -f pod01.yaml
   kubectl get pods
   ```

4. **웹 서버 테스트**

   ```bash
   kubectl exec -it pod01 -- /bin/bash
   echo "Hello Kubernetes Volume" > /usr/share/nginx/html/index.html
   exit
   ```

5. **Pod 재시작 후 데이터 유지 확인**

   ```bash
   kubectl delete pod pod01
   kubectl apply -f pod01.yaml
   curl <Pod_IP>
   ```

---

## 참고 자료

* [Kubernetes 공식 문서 - Volumes](https://kubernetes.io/docs/concepts/storage/volumes/)
* [Kubernetes 공식 문서 - Persistent Volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/)
* [Helm Charts (이전 학습 내용)](https://helm.sh/)
* [Bitnami Helm Repository](https://bitnami.com/stacks/helm)
* [Artifact Hub (Helm 차트 허브)](https://artifacthub.io/)

---

> **Tip:**
> `emptyDir`는 **임시 데이터용**,
> `hostPath`는 **로컬 개발용**,
> `PersistentVolume + PVC`는 **운영환경용**으로 사용하는 것이 일반적입니다.

```
