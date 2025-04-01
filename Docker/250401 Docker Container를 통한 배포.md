### Docker Container를 통한 웹 서비스 배포

docker 설치
```
sudo amazon-linux-extras install docker
```

docker 서비스 등록 및 시작
```
sudo systemctl enable docker
sudo systemctl start docker
```

도커 상태 확인
```
docker ps
```

권한 부여
```
sudo usermod -a -G docker ec2-user
```

auto-start에 docker 등록
```
sudo chkconfig docker on

혹은

sudo systemctl enable docker
```
도커 버전 확인
```
sudo docker version
```

도커 가상 NIC 확인
```
ip a
```

최신 Docker Compose 설치
```
sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
```

실행 권한 부여
```
sudo chmod +x /usr/local/bin/docker-compose
```

Docker Compose 설치 확인
```
docker-compose version
```
