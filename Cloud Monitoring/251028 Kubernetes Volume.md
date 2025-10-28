# Kubernetes Volume과 StorageClass

쿠버네티스(Kubernetes)에서는 **Pod가 종료되어도 데이터를 유지하기 위해 Volume을 사용**합니다.
이번 글에서는 **Volume의 종류, PersistentVolume(PV) & PersistentVolumeClaim(PVC), StorageClass의 개념과 실습**을 다룹니다.

---

## 1. Kubernetes Volume의 종류

쿠버네티스의 Volume은 컨테이너 간 데이터 공유나 Pod 재시작 시 데이터 유지를 위해 사용됩니다.

| 타입           | 설명                                                                          |
| ------------ | --------------------------------------------------------------------------- |
| **emptyDir** | Pod가 생성될 때 자동으로 만들어지는 임시 디렉터리. Pod가 삭제되면 함께 삭제됨.                            |
| **hostPath** | 노드의 특정 경로를 Pod 내부에 마운트. 로컬 파일 시스템 접근이 가능하지만, Pod가 다른 노드로 이동하면 데이터가 유지되지 않음. |


---

## 2. Persistent Volume(PV) & Persistent Volume Claim(PVC)

**Pod가 삭제되어도 데이터를 유지**하려면 **PersistentVolume(PV)** 과 **PersistentVolumeClaim(PVC)** 을 사용해야 합니다.

### PV (PersistentVolume)

* 클러스터 관리자가 미리 생성한 **실제 저장소 리소스**
* NFS, hostPath, AWS EBS, GCE PD 등 다양한 백엔드를 지원
* Pod와 직접 연결되지 않고 **PVC를 통해 요청**받음

---

### PVC (PersistentVolumeClaim)

* 사용자가 요청하는 **스토리지 리소스**
* PV 중 조건이 일치하는 것을 자동으로 바인딩
* Pod은 PVC를 통해 PV를 사용

---

### Pod에 PVC 연결하기

Pod은 PVC를 통해 PV를 마운트해 스토리지를 사용할 수 있습니다.

---

## 3. 실습: PV, PVC, Pod 생성

### (1) PV 생성

`disk1.yaml`

```bash
cat <<EOF > disk1.yaml
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
EOF

kubectl apply -f disk1.yaml
kubectl get pv
```

**출력 예시**

```
NAME    CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   AGE
disk1   1Gi        RWO,ROX        Recycle          Available           ssd            17s
```

---

### (2) PVC 생성

`pvc-webdata.yaml`

```bash
cat <<EOF > pvc-webdata.yaml
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
EOF

kubectl apply -f pvc-webdata.yaml
kubectl get pvc
```

**출력 예시**

```
NAME          STATUS   VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
pvc-webdata   Bound    disk1    1Gi        RWO,ROX        ssd            42s
```

---

### (3) Pod에 PVC 마운트

`pod01.yaml`

```bash
cat <<EOF > pod01.yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod01
spec:
  containers:
    - name: nginx
      image: public.ecr.aws/nginx/nginx:1.26
      volumeMounts:
      - name: mypd
        mountPath: /usr/share/nginx/html
  volumes:
    - name: mypd
      persistentVolumeClaim:
        claimName: pvc-webdata
EOF

kubectl apply -f pod01.yaml
kubectl describe pod pod01
```

---

### (4) 삭제

```bash
kubectl delete pod pod01
kubectl delete pvc pvc-webdata
kubectl delete pv disk1
```

---

## 4. StorageClass

### 개념

StorageClass는 **동적 볼륨 프로비저닝(Dynamic Volume Provisioning)** 을 가능하게 하는 리소스입니다.
PVC가 생성될 때, 지정된 StorageClass 설정에 따라 자동으로 PV가 만들어집니다.

---

### 주요 속성

| 속성                       | 설명                                                              |
| ------------------------ | --------------------------------------------------------------- |
| **provisioner**          | 스토리지를 제공하는 드라이버 (예: `ebs.csi.aws.com`, `pd.csi.storage.gke.io`) |
| **parameters**           | 스토리지 백엔드 설정 (예: 디스크 타입, IOPS 등)                                 |
| **reclaimPolicy**        | PV 삭제 시 정책 (`Delete` or `Retain`)                               |
| **volumeBindingMode**    | 볼륨 할당 시점 (`Immediate` 또는 `WaitForFirstConsumer`)                |
| **allowVolumeExpansion** | 볼륨 크기 확장 허용 여부 (`true`/`false`)                                 |

---

### 정적 StorageClass

* 동적 생성이 아닌 **관리자가 직접 PV를 생성**할 때 사용
* `provisioner: kubernetes.io/no-provisioner` 설정
* 로컬 디스크(HDD, SSD 등)에 적합

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: local-storage
provisioner: kubernetes.io/no-provisioner
volumeBindingMode: Immediate
```

---

### 실습: StorageClass 생성

참고: [Kubernetes 공식 문서 – Storage Classes](https://kubernetes.io/docs/concepts/storage/storage-classes/)

```bash
cat <<EOF > storageclass.yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: app-hostpath-sc
  annotations:
    storageclass.kubernetes.io/is-default-class: "false"
provisioner: kubernetes.io/no-provisioner
allowVolumeExpansion: true
EOF

kubectl apply -f storageclass.yaml
kubectl get storageclass
```

**출력 예시**

```
NAME              PROVISIONER                    RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
app-hostpath-sc   kubernetes.io/no-provisioner   Delete          Immediate           true                   13s
```

---

### 기본 StorageClass 지정하기

PVC에서 `storageClassName`을 생략하면 기본(StorageClass) 설정이 자동으로 사용됩니다.

```yaml
metadata:
  annotations:
    storageclass.kubernetes.io/is-default-class: "true"
```

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: my-pvc
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

> 위 PVC는 StorageClass를 지정하지 않아도 **기본 StorageClass(local-path)** 를 자동으로 사용합니다.

---

## 참고 문서

* [Kubernetes 공식 문서 - Volumes](https://kubernetes.io/docs/concepts/storage/volumes/)
* [Kubernetes 공식 문서 - Persistent Volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/)
* [Kubernetes 공식 문서 - StorageClass](https://kubernetes.io/docs/concepts/storage/storage-classes/)
