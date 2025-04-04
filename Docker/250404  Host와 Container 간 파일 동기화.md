Host에서 파일 작성
```
echo "This is CloudComputing NGINX06" > /root/index.html
```

내용 확인
```
cat /root/index.html
```

NGINX Container와 Host 파일 동기화
```
docker run --name nginx06 -v /root/index.html:/usr/share/nginx/html/index.html:ro -d -p 86:80 nginx:latest
```

웹 페이지 확인
```
curl localhost:86
```

Host에서 파일 변경 후 웹 페이지 확인
```
echo "NGINX 0066" > /root/index.html

curl localhost:86
```

#### Docker 명령어 활용
nginx03 Container의 상세 정보 확인
```
docker inspect nginx03
```

nginx02 Container의 웹 애플리케이션 서비스 로그를 확인
```
docker logs nginx02
```

동작 중인 Container의 process 및 상태를 확인
```
docker top nginx03
```


