## 4\. Dockerfile 빌드 실습

도커 이미지를 직접 빌드해보고, 몇 가지 중요한 개념을 실습을 통해 배워보겠습니다.

### 실습 1: 간단한 웹 서버 빌드

Nginx 웹 서버가 포함된 간단한 이미지를 빌드하고 실행해 보세요.

**1. 빌드 디렉터리 생성 및 `Dockerfile` 작성**

```bash
mkdir -p build/webserver && cd build/webserver

cat << 'END' > Dockerfile
FROM rockylinux:8
RUN yum -y install epel-release \
    && yum -y install nginx curl \
    && yum clean all
CMD ["nginx", "-g", "daemon off;"]
END
```

**2. 컨테이너 이미지 빌드**

`-t` 옵션으로 이미지 이름과 태그를 지정하고, `--no-cache` 옵션으로 빌드 캐시를 사용하지 않도록 합니다.

```bash
docker build -t myweb:rocky-nginx --no-cache .
```

**3. 빌드된 이미지 확인 및 실행**

`docker images`로 이미지가 생성되었는지 확인하고, `docker run`으로 컨테이너를 실행합니다.

```bash
docker images
docker run -d --name webserver myweb:rocky-nginx
```

**4. 웹 서버 동작 확인**

컨테이너의 IP 주소를 확인하고 `curl`로 접속해 보세요.

```bash
docker inspect webserver | grep IPAddress
# 예시 출력: "IPAddress": "172.17.0.2"
curl 172.17.0.2
```

**5. 컨테이너 삭제**

```bash
docker rm -f webserver
```

-----

### 실습 2: Node.js 애플리케이션 빌드

간단한 Node.js 웹 애플리케이션을 도커 이미지로 빌드해 보겠습니다.

**1. 소스 코드 및 `Dockerfile` 작성**

```bash
mkdir -p ~/build/appjs && cd ~/build/appjs/

cat << 'EOF' > app.js
const http = require('http');
const os = require('os');
console.log("Test server starting…");
var handler = function(req, res) {
  res.writeHead(200);
  res.end("Container Hostname: " + os.hostname() + "\n");
};
var www = http.createServer(handler);
www.listen(8080);
EOF

cat << 'EOF' > Dockerfile
FROM node:12
COPY app.js /app.js
ENTRYPOINT ["node", "app.js"]
EOF
```

**2. 이미지 빌드 및 실행**

```bash
docker build -t appjs .
docker run -d --name appjs appjs
curl 172.17.0.2:8080
```

-----

### 실습 3: 멀티스테이지 빌드 (Multi-stage Build)

멀티스테이지 빌드는 최종 이미지 크기를 극적으로 줄이는 데 효과적입니다. 빌드 과정에 필요한 컴파일러, SDK 등은 최종 이미지에 포함되지 않도록 분리합니다.

**1. 소스 코드 및 `Dockerfile` 작성**

Go 언어로 된 간단한 애플리케이션을 예시로 사용합니다.

```bash
mkdir -p build/go-app && cd build/go-app

cat << 'EOF' > main.go
package main

import(
    "fmt"
    "time"
)

func main() {
    for {
        fmt.Println("Hello, world!")
        time.Sleep(10 * time.Second)
    }
}
EOF

cat << 'EOF' > Multi-dockerfile
# Stage 1: 빌드 환경
FROM golang:1.11 as builder
WORKDIR /usr/src/app
COPY main.go .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -ldflags '-s' -o main .

# Stage 2: 실행 환경 (매우 가벼운 scratch 이미지 사용)
FROM scratch
COPY --from=builder /usr/src/app/main /main 
CMD ["/main"]
EOF
```

**2. 멀티스테이지로 이미지 빌드**

```bash
docker build -t app:v2 . -f Multi-dockerfile
```

**3. 이미지 크기 비교**

기본 Go 이미지는 수백 MB에 달하지만, 멀티스테이지로 빌드한 최종 이미지는 단 10\~20MB에 불과합니다.

```bash
docker images app:v2
```

-----

## 5\. 컨테이너 빌드 Best Practices 8가지

최적화되고 안전한 도커 이미지를 만들기 위한 8가지 모범 사례입니다.

| 원칙 | 설명 | 예시 |
|:--- |:--- |:--- |
| **최소한의 베이스 이미지 사용** | 불필요한 패키지가 없는 경량 이미지를 사용해 이미지 크기와 보안 취약점을 줄입니다. | `FROM alpine:3.18` |
| **불필요한 파일 제외** | `.dockerignore` 파일을 작성해 빌드에 필요 없는 파일(로그, 캐시 등)을 제외합니다. | `.dockerignore`에 `logs/`, `node_modules/` 추가 |
| **계층(Layer) 최적화** | 여러 `RUN` 명령을 `&&`로 연결해 하나의 레이어로 만듭니다. 빌드 캐시 효율이 높아지고 최종 이미지 크기가 줄어듭니다. | `RUN apt update && apt install -y nginx` |
| **멀티스테이지 빌드 활용** | 빌드 도구는 최종 이미지에 포함하지 않고, 실행에 필요한 최종 결과물만 복사합니다. | 위 실습 3 참고 |
| **보안 강화** | 애플리케이션을 `root`가 아닌 일반 사용자로 실행합니다. `USER` 명령어로 권한을 낮춰 보안 사고를 예방합니다. | `USER appuser` |
| **`CMD`와 `ENTRYPOINT` 적절히 사용**| \*\*`ENTRYPOINT`\*\*는 항상 실행될 기본 명령을, \*\*`CMD`\*\*는 그 명령의 기본 인자를 정의합니다. `docker run` 시 인자를 추가하면 `CMD`만 덮어쓰여 유연합니다. | `ENTRYPOINT ["curl"]` <br> `CMD ["www.google.com"]` |
| **환경 변수 관리** | `ENV`로 환경 변수를 설정하고, 민감 정보(비밀번호)는 이미지에 포함하지 않고 `docker run -e`로 전달하거나 **Docker Secrets**를 사용합니다. | `ENV APP_VERSION=1.0.0` |
| **공식 이미지 및 정기적 업데이트** | Docker Hub의 **Verified Publisher**나 **Official Image**를 사용하고, `apt update && apt upgrade`와 같이 최신 보안 패치를 적용합니다. | `FROM node:18-slim` |


참고 문서:

Dockerfile 모범 사례 (공식 문서): https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
