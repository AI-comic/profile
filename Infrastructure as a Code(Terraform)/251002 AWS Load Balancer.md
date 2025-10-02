## 가용영역과 고가용성 서비스

### 가용영역 (Availability Zones, AZ)
- 가용영역은 **물리적으로 격리된 데이터 센터 그룹**이다.  
- 각 AZ는 독립된 전력, 냉각, 네트워크 인프라를 가진다.  
- 여러 AZ를 활용하면 서비스가 한 곳에 장애가 발생해도 **연속성**을 유지할 수 있다.  
- 예시:  
  - 서울 리전 → 4개 AZ  
  - 미국 동부(버지니아 북부) → 6개 AZ  
  - 오사카 리전 → 3개 AZ  

### 고가용성 (High Availability, HA)
- 동일 목적의 서버를 서로 다른 AZ에 배치한다.  
- 특정 AZ에 문제가 발생해도 다른 AZ의 서버가 동작해 서비스가 중단되지 않는다.  

---

## 로드 밸런싱 (Load Balancing)

- 여러 AZ에 배치된 서버로 트래픽을 분산해야 한다.  
- **로드 밸런서(Load Balancer)** 는 클라이언트 요청을 서버들에 고르게 분배하는 역할을 한다.  
- AWS에서는 2개 이상의 AZ에 서버를 배치하고, 로드 밸런서를 이용해 안정적인 서비스 제공이 가능하다.  

---

## AWS 로드 밸런서 종류

### 1. Application Load Balancer (ALB)
- OSI 7계층(애플리케이션 계층)에서 동작  
- HTTP/HTTPS 트래픽 분산에 적합  
- 콘텐츠 기반 라우팅 (경로/호스트 기반) 기능 제공  
- **마이크로서비스 아키텍처**에 유리  

### 2. Network Load Balancer (NLB)
- OSI 4계층(전송 계층)에서 동작  
- TCP, UDP, TLS 트래픽 분산에 적합  
- **초고성능, 낮은 지연시간**이 필요한 서비스에 사용  

### 3. Gateway Load Balancer (GWLB)
- OSI 3계층(네트워크 계층)에서 동작  
- IP 패킷 단위로 트래픽을 전달  
- **보안 장비(방화벽, IDS/IPS, DPI 등)** 를 가상 어플라이언스로 배포·확장·관리하는 데 활용  
- VPC 간 트래픽 교환 시 엔드포인트를 통해 안전한 연결 제공  

---

## AWS 로드 밸런서 생성 절차

1. **대상 그룹(Target Group) 생성**
   - 트래픽을 전달할 대상을 지정  
   - EC2 인스턴스, IP 주소, Lambda 함수 등 등록 가능  
   - 상태 검사(Health Check)를 통해 정상 대상만 트래픽을 받도록 설정  

2. **로드 밸런서 생성**
   - **리스너(Listener)** 를 설정해 클라이언트 요청을 감지 (예: HTTP/80)  
   - 리스너 규칙에 따라 대상 그룹으로 트래픽 라우팅  

---

## 실습: ALB 구축하기

### 1. VPC 생성
- 이름: **lab-vpc**  
- CIDR: `10.0.0.0/16`  
- 서브넷:  
  - Public Subnet: `10.0.0.0/24`, `10.0.1.0/24`  
  - Private Subnet: `10.0.2.0/24`, `10.0.3.0/24`  

### 2. 보안 그룹 생성
- **ALB-SG**
  - 인바운드: `80 → Anywhere`  
- **WEB-SG**
  - 인바운드: `80 → ALB-SG`  

### 3. 웹 서버 생성
- **Web1 / Web2**
  - AMI: Amazon Linux 2023  
  - 인스턴스 타입: `t2.micro`  
  - 키페어: `lab-key`  
  - 네트워크: Private Subnet  
  - 보안 그룹: `WEB-SG`  
  - 사용자 데이터:
    ```bash
    #!/bin/bash
    yum install httpd -y
    systemctl start httpd
    systemctl enable httpd
    echo "<h1>WEB1 or WEB2</h1>" > /var/www/html/index.html
    ```

### 4. ALB 생성
- 대상 그룹 생성  
  - 이름: `web-alb-tg`  
  - 프로토콜/포트: HTTP/80  
  - 상태 검사: `/`  
  - Web1, Web2 등록  

- ALB 생성  
  - 이름: `web-alb`  
  - 네트워크 매핑: Public Subnet 2개  
  - 보안 그룹: `ALB-SG`  
  - 리스너: HTTP/80 → 대상 그룹 `web-alb-tg`  

### 5. 테스트
- ALB의 DNS 이름 확인  
- 웹 브라우저로 접속하면 Web1, Web2 페이지가 번갈아 표시되는지 확인  

### 6. 리소스 정리
- ALB  
- 대상 그룹  
- EC2 인스턴스 (Web1, Web2)  
- 보안 그룹 (WEB-SG, ALB-SG)  
- VPC  
