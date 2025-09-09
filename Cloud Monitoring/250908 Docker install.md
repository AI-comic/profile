## Docker란?
- **Docker Client (CLI)**: 사용자가 터미널에서 입력하는 명령어 도구.  
- **Docker Server (Daemon)**: 실제로 컨테이너를 실행/관리하는 백그라운드 프로세스.  
- **이미지(Image)**: 실행 가능한 프로그램 + 실행 환경(라이브러리, 설정 등)을 패키징한 파일.  
- **컨테이너(Container)**: 이미지를 실행한 프로세스. (가상 머신보다 가볍고 빠름)

**이미지 다운로드 → 컨테이너 실행 → 테스트 → 중지/삭제** 순서로 실습.

## 1. 가상화 프로그램 설치
- **VMWare Workstation 17** 설치 (공식 사이트에서 다운로드)
- https://www.vmware.com/products/desktop-hypervisor/workstation-and-fusion

## 2. SSH 접속용 툴 설치
- **MobaXterm** 설치  (공식 사이트에서 다운로드)
- https://mobaxterm.mobatek.net/download.html

## 3. 클라우드 환경 (AWS)

### 3.1. EC2 인스턴스 생성 (Ubuntu 22.04)
- 이름: `ubuntu-docker`
- 보안 그룹: `docker-sg` (inbound → 22, 80, 8080 허용)  
- 스펙: `t2.medium`, 20GiB (gp3), Public IP 활성화  

### 3.2. Docker 설치
Docker는 공식 문서를 따르는 것이 가장 안전.  
참고: https://docs.docker.com/engine/install/ubuntu/

```
# 호스트 이름과 시간대 설정
sudo hostnamectl set-hostname docker-host
sudo timedatectl set-timezone Asia/Seoul

# 설치 준비
sudo apt-get update
sudo apt-get install -y ca-certificates curl

# Docker 공식 GPG key 추가
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Docker repository 등록
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo ${UBUNTU_CODENAME:-$VERSION_CODENAME}) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Docker 설치
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 3.3. Docker 권한 설정
- Docker는 기본적으로 root 권한을 필요로 함.
- 매번 sudo 입력하지 않도록, 현재 사용자를 docker 그룹에 추가.
```
sudo usermod -aG docker ubuntu
id ubuntu
```
- 로그아웃 후 다시 로그인해야 적용됨.
```
# 버전 확인
docker version
```

### 3.4. docker CLI 명령어 자동 완성 기능 설정
```
sudo curl -L https://raw.githubusercontent.com/docker/cli/master/contrib/completion/bash/docker -o /etc/bash_completion.d/docker
source /etc/bash_completion.d/docker

echo "source /etc/bash_completion.d/docker" >> ~/.bashrc
source ~/.bashrc
```

## 4. 실습
```
# 1. 컨테이너 이미지 검색
docker search nginx
docker search --filter is-official=true nginx

# 2. 이미지 다운로드
docker pull nginx

# 3. 이미지 확인
docker images
docker history nginx:latest

# 4. 컨테이너 실행 (80 포트 매핑)
docker run -d --name webserver -p 80:80 nginx:latest

# 5. 컨테이너 동작 확인
docker ps
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' webserver

# 호스트에서 바로 확인 가능
curl http://localhost

# 6. 컨테이너 내부 접속 후 index.html 수정
docker exec -it webserver /bin/bash
cd /usr/share/nginx/html/
cat index.html
echo "Hello Docker" > index.html
exit

# 수정된 내용 확인
curl http://localhost

# 7. 컨테이너 중지 및 삭제
docker stop webserver
docker rm webserver

# 8. 이미지 삭제
docker rmi nginx:latest
```
