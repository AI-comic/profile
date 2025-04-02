root 계정으로 진입
```
sudo su -
```

현재 오픈된 포트 확인
(TCP/80 오픈되어있으면 다른 포트로 연결 필요 )
```
netstat -ntl
```

최신 NGINX 이미지 다운로드
```
docker pull nginx
```

nginx 이미지 다운로드 확인
```
docker images
```

현재 운영 및 동작 중인 Container 확인
```
docker ps
```

컨테이너로 등록된 Container 리스트 확인
```
docker ps -a
```

nginx01 Container 이름으로 TCP/81 포트로(-p) Container 실행 백그라운드로(-d)
```
docker run --name nginx01 -d -p 81:80 nginx:latest
```
nginx02 Container 이름으로 TCP/82 포트로 Container 실행
```
docker run --name nginx02 -d -p 82:80 nginx:latest
```
nginx03 Container 이름으로 TCP/83 포트로 Container 실행
```
docker run --name nginx03 -d -p 83:80 nginx:latest
```

가상 NIC 추가 확인
```
ip a
```
