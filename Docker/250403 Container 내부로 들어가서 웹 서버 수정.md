Container 내부로 진입
```
docker exec -it nginx01 /bin/bash
```

NGINX Container OS 확인
```
cat /etc/*release
```

Index 파일 내용 변경
```
echo "nginx01" > /usr/share/nginx/html/index.html
```

변경된 웹 페이지 확인
```
curl localhost:81
```

Container Index 파일 변경 (Container 내부에 들어가지 않고)
```
docker exec -it nginx02 sh -c "echo nginx02 > /usr/share/nginx/html/index.html"
```

변경 확인
```
curl localhost:82
```

nginx03 Index 파일 변경
```
docker exec -it nginx03 sh -c "echo nginx03 > /usr/share/nginx/html/index.html"
```

변경 확인
```
curl localhost:83
```

Host에서 Container로 파일 복사
```
docker cp /bin/ls nginx01:/
```

Container에서 Host로 파일 복사
```
docker cp nginx02:/usr/share/nginx/html/index.html /home/ec2-user/
```
