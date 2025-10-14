## 1. Dockerfile 이해하기

### Dockerfile이란?
`Dockerfile`은 **Docker 이미지 생성을 자동화**하기 위한 스크립트 파일입니다.  
즉, "이 환경을 이렇게 만들어줘" 라는 **설명서(Recipe)** 역할을 합니다.

**Dockerfile → docker build → Docker 이미지 → 컨테이너 실행**

### 특징
- 애플리케이션 실행에 필요한 **패키지, 코드, 환경 설정**을 정의  
- 어디서 실행하든 **동일한 환경**을 재현 (개발–테스트–운영 일관성 보장)  
- 버전 관리(Git 등)로 환경 구성을 코드처럼 관리 가능

---

## 2. Dockerfile 주요 명령어 정리

| 명령어 | 설명 |
|:--------|:------|
| **FROM** | 기반(Base) 이미지 지정. 모든 Dockerfile의 시작점 |
| **LABEL** | 이미지 메타데이터(작성자, 버전 등) 추가 |
| **ENV** | 환경변수 설정 |
| **RUN** | 빌드 시 컨테이너 내부에서 명령어 실행 |
| **COPY** | 호스트의 파일/디렉토리를 컨테이너 내부로 복사 |
| **ADD** | COPY와 유사하나, URL 다운로드/압축 해제 지원 |
| **WORKDIR** | 명령어 실행 디렉토리 설정 |
| **EXPOSE** | 컨테이너에서 외부로 노출할 포트 지정 |
| **VOLUME** | 컨테이너의 데이터 영속화를 위한 볼륨 지정 |
| **USER** | 명령 실행 시 사용할 사용자 지정 |
| **CMD** | 컨테이너 시작 시 실행할 기본 명령어 지정 |
| **ENTRYPOINT** | 항상 실행되어야 하는 명령어 지정 |

---

## 3. 경량 컨테이너 빌드하기

### 왜 경량화가 필요한가?
| 항목 | 설명 |
|------|------|
| 빠른 배포 | 이미지 크기가 작을수록 전송·배포 속도 향상 |
| 저장 효율 | 저장 공간 절약 (레지스트리/호스트 디스크) |
| 보안 강화 | 불필요한 패키지 제거 → 공격 표면 축소 |

### 경량화 방법
- 최소한의 베이스 이미지 사용 (예: `alpine`, `slim`, `distroless`)
- `--no-install-recommends`, `-qq` 옵션으로 불필요한 패키지 설치 방지
- `apt clean`, `rm -rf /var/lib/apt/lists/*` 등으로 캐시 및 임시파일 삭제
- **멀티스테이지 빌드**로 빌드 환경과 실행 환경 분리


### 실습 1: 기본 Ubuntu 웹서버 빌드
```bash
mkdir -p ~/build/webserver-ubuntu
cd ~/build/webserver-ubuntu

cat > Dockerfile.base << 'EOF'
FROM ubuntu:24.04
LABEL maintainer="홍길동 <example@email.com>"

RUN apt update && apt install -y apache2
RUN echo "Web Server running on Ubuntu Base" > /var/www/html/index.html
CMD apachectl -DFOREGROUND
EOF

# 이미지 빌드
docker build -t webserver-ubuntu:base . -f Dockerfile.base
```

### 실습 2: 경량 Ubuntu 웹서버 빌드
```bash
cat > Dockerfile.lightweight << 'EOF'
FROM ubuntu:18.04
LABEL maintainer="홍길동 <example@email.com>"

RUN apt update && \
    apt install -y -qq apache2 --no-install-recommends && \
    apt clean -y && \
    apt autoremove -y && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN echo "Web Server running on Ubuntu Light" > /var/www/html/index.html
CMD apachectl -DFOREGROUND
EOF

docker build -t webserver-ubuntu:light . -f Dockerfile.lightweight

# 결과 비교

docker images webserver-ubuntu
```
