## 컨테이너 이미지와 컨테이너란?

- **컨테이너 이미지**: 이미지는 청사진과 같다. 애플리케이션 실행에 필요한 모든 파일과 설정값을 포함하는 읽기 전용의 정적 스냅샷이다. 여러 개의 레이어로 구성되어 있으며, 일단 만들어지면 변경되지 않는다. 컨테이너를 삭제하더라도 이미지는 그대로 남아 있다.

- **컨테이너**: 컨테이너는 이미지의 실행 인스턴스다. 이미지를 기반으로 컨테이너를 실행하면, 읽기 전용 이미지 레이어 위에 새로운 읽기-쓰기 레이어가 추가된다. 모든 변경사항이나 새로운 파일은 이 레이어에 저장된다. 이렇게 여러 레이어를 하나로 합쳐서 보여주는 방식을 **유니온 파일시스템**(Union Filesystem)이라고 하는데, 덕분에 여러 컨테이너가 동일한 이미지 레이어를 효율적으로 공유하면서도 독립적으로 동작할 수 있다.


### 오버레이(Overlay) 구조와 유니온 파일시스템(Union Filesystem)

도커는 오버레이 구조를 사용한다. 여러 개의 이미지 레이어(읽기 전용) 위에 하나의 RW 레이어를 덧붙여서, 마치 하나의 파일시스템처럼 보이게 하는 기술.

- **Copy-on-Write (CoW)**: 기존 이미지 파일에 변경이 필요할 경우, 원본 파일은 그대로 두고 RW 레이어에 복사본을 만들어 수정한다. 이렇게 해서 원본 이미지는 손상되지 않고, 컨테이너들이 같은 이미지 레이어를 효율적으로 공유할 수 있다.

- **유니온 파일시스템**: 여러 레이어를 마치 하나의 디렉토리 구조처럼 통합하여 보여주는 기술. 사용자는 컨테이너 내부에서 여러 레이어가 겹쳐져 있다는 것을 알 필요 없이, 하나의 통합된 파일시스템으로 작업할 수 있다. 주로 OverlayFS가 사용된다.


## Docker 이미지 관리 명령어

| 명령어 | 설명 |
| :--- | :--- |
| `docker search <이미지명>` | Docker Hub에서 이미지를 검색. |
| `docker pull <이미지명>` | Docker Hub와 같은 레지스트리에서 이미지를 다운로드. |
| `docker images` | 로컬에 저장된 이미지 목록을 확인. |
| `docker rmi <이미지명>` | 로컬 이미지를 삭제. |
| `docker history <이미지명>` | 이미지의 생성 이력을 레이어 단위로 확인. |
| `docker inspect <이미지명>` | 이미지의 상세 정보를 JSON 형식으로 출력. |


## Docker 컨테이너 관리 명령어

| 명령어 | 설명 |
| :--- | :--- |
| `docker run` | 이미지를 다운로드하고 컨테이너를 생성 및 시작하는 과정을 한 번에 수행. |
| `docker ps` | 현재 실행 중인 컨테이너 목록을 확인. |
| `docker ps -a` | 모든 컨테이너(실행 중/종료된) 목록을 확인. |
| `docker create <이미지명>` | 컨테이너를 생성하지만, 실행하지는 않는다. |
| `docker start <컨테이너명 또는 ID>` | 중지된 컨테이너를 시작. |
| `docker stop <컨테이너명 또는 ID>` | 실행 중인 컨테이너를 정상적으로 종료. |
| `docker kill <컨테이너명 또는 ID>` | 실행 중인 컨테이너를 강제로 종료. |
| `docker rm <컨테이너명 또는 ID>` | 중지된 컨테이너를 삭제. |
| `docker rm -f <컨테이너명 또는 ID>` | 실행 중인 컨테이너를 강제로 삭제. |
| `docker exec -it <컨테이너명 또는 ID> <명령어>` | 실행 중인 컨테이너 내에서 명령을 실행하고 상호작용. |
| `docker inspect <컨테이너명 또는 ID>` | 컨테이너의 상세 정보를 JSON 형식으로 출력. |

---

## 실습

**이미지 다운로드 → 컨테이너 실행 → 테스트 → 중지/삭제**

### 1. 컨테이너 이미지 검색 및 다운로드

#### 이미지 검색
```bash
# Docker Hub에서 nginx 관련 이미지 검색
docker search nginx

# 공식 이미지(Official)만 필터링
docker search --filter is-official=true nginx
```

#### 이미지 다운로드

```bash
# nginx 최신 버전 이미지 다운로드
docker pull nginx:latest
```

#### 이미지 확인
```bash
# 다운로드된 이미지 목록 보기
docker images

# 이미지의 Layer(빌드 이력) 확인
docker history nginx:latest
```

### 2. 컨테이너 실행

#### Nginx 웹서버 컨테이너 실행
```bash
# -d : 백그라운드 실행
# --name : 컨테이너 이름 지정
# -p : 호스트포트:컨테이너포트 매핑
docker run -d --name webserver -p 80:80 nginx:latest
```

#### 컨테이너 상태 확인
```bash
docker ps           # 실행 중인 컨테이너 목록
docker ps -a        # 중지된 컨테이너 포함 전체 목록
```

#### 컨테이너 정보 확인
```bash
docker inspect webserver
# 또는 IP만 간단히 확인
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' webserver
```

#### 웹 접속 테스트
```bash
# 호스트에서 직접 테스트
curl http://localhost
# 또는 컨테이너 내부 IP로 접속 (bridge 네트워크)
curl http://172.17.0.2
```

### 3. 컨테이너 내부 접속 및 수정

#### 컨테이너 쉘 접속
```bash
docker exec -it webserver /bin/bash
```

#### 웹 콘텐츠 수정
```bash
cd /usr/share/nginx/html/
cat index.html
echo "Hello Docker" > index.html
exit
```

#### 변경된 내용 확인
```bash
curl http://localhost
```

### 4. 컨테이너 관리

#### 컨테이너 중지
```bash
docker stop webserver
```

#### 컨테이너 삭제
```bash
docker rm webserver
```

### 5. 이미지 관리

#### 이미지 목록 확인
```bash
docker images
```

#### 이미지 삭제
```bash
docker rmi nginx:latest
```
실행 중인 컨테이너가 있으면 이미지 삭제 불가.

필요 시 `docker rm -f <컨테이너명>` 후 삭제.

### 6. 네트워크 및 볼륨 정리

#### 사용하지 않는 네트워크 삭제
```bash
docker network prune -f
```

#### 사용하지 않는 볼륨 삭제
```bash
docker volume prune -f
```

#### 전체 정리 (이미지, 네트워크, 볼륨)
```bash
docker system prune --all --volumes -f
```

참고 문서

Docker 공식 문서 https://docs.docker.com/

Docker Hub - nginx 이미지 https://hub.docker.com/_/nginx
