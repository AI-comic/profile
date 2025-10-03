## 1. 데이터베이스란?
데이터베이스(Database)는 여러 사람이 **공유하여 사용할 목적으로 데이터를 체계적으로 조직화하고 관리**하는 데이터 집합이다.  
예: 고객 이름, 주문 내역, 상품 정보 등  

### 관계형 데이터베이스 (Relational Database)
- 데이터를 **행(Row)** 과 **열(Column)** 로 구성된 **테이블(Table)** 형태로 저장  
- 사전에 정의된 관계(Relation)를 통해 데이터 간 연관성을 쉽게 이해 가능  

---

## 2. DBMS (Database Management System)
- 데이터베이스를 관리하는 소프트웨어  
- 사용자가 데이터를 **생성(Create), 조회(Read), 수정(Update), 삭제(Delete)** 할 수 있도록 지원  
- 대표 DBMS: MySQL, Oracle, PostgreSQL, SQL Server 등  

---

## 3. AWS RDS란?
**AWS Relational Database Service(RDS)** 는 클라우드에서 **관계형 데이터베이스를 쉽게 설정, 운영, 확장**할 수 있도록 지원하는 AWS 관리형 서비스이다.  

### ✅ 주요 특징
- 하드웨어 및 OS 관리 불필요  
- DB 백업 및 패치 자동화  
- 고가용성과 확장성 제공  
- 개발자는 인프라 관리 대신 비즈니스 로직에 집중 가능  

---

## 4. 온프레미스 DB vs AWS RDS

| 구분 | 사용자가 관리 | AWS가 관리 |
|------|--------------|-------------|
| 애플리케이션 최적화 | ✅ | ❌ |
| OS 설치 및 패치 | ❌ | ✅ |
| DB 소프트웨어 설치/패치 | ❌ | ✅ |
| 백업/복구 | ❌ | ✅ |
| 고가용성 | ❌ | ✅ |
| 스토리지/서버 유지보수 | ❌ | ✅ |

---

## 5. AWS RDS 지원 엔진
- **MySQL**: 널리 사용되는 오픈 소스 DB  
- **PostgreSQL**: 강력한 기능과 규정 준수 지원  
- **Oracle**: 상용 DBMS, 엔터프라이즈 환경에 적합  
- **SQL Server**: Microsoft 제품과 강력한 통합  
- **MariaDB**: MySQL 기반 오픈 소스 DB  
- **Amazon Aurora**: AWS가 개발한 DB, MySQL·PostgreSQL 호환, 고성능/고확장성  

---

## 6. AWS RDS 구성 요소
- **Subnet Group**: RDS가 위치할 서브넷 집합  
- **Parameter Group**: DB 엔진 동작을 정의하는 설정 집합  
- **Option Group**: 백업/모니터링 등 고급 기능 옵션  

---

## 7. 실습: AWS RDS 생성하기

### 🔹 Task 1: 보안 그룹 만들기
- `web-sg`: HTTP, SSH 허용 (Anywhere)  
- `rds-sg`: MySQL(3306) 허용 (출처: web-sg)  

### 🔹 Task 2: DB 관련 그룹 생성
- **Subnet Group**: Private Subnet 2a, 2c 포함  
- **Parameter Group**: MySQL 8.0 설정  
- **Option Group**: MySQL 8.0 옵션 추가  

### 🔹 Task 3: RDS 인스턴스 생성
- 엔진: MySQL 8.0  
- 인스턴스 클래스: `t3.micro`  
- 스토리지: gp3 SSD, 20GB  
- 배포: 다중 AZ (고가용성)  
- DB 이름: `labdb`  
- 마스터 계정: `master / master-password`  

### 🔹 Task 4: 웹 서버 인스턴스 생성
- EC2 Amazon Linux 2023  
- 보안 그룹: `web-sg`  
- User Data 예시:
  ```bash
  #!/bin/bash -ex
  dnf update -y
  dnf install httpd php php-mysqlnd -y
  systemctl enable --now httpd
  cd /var/www/html/
  wget https://aws-largeobjects.s3.ap-northeast-2.amazonaws.com/AWS-AcademyACF/lab7-app-php7.zip
  unzip lab7-app-php7.zip -d /var/www/html/
  chown apache:root /var/www/html/rds.conf.php
  ```

### 🔹 Task 5: DB 연동 및 Failover 테스트
- 웹 서버에서 DB 엔드포인트 입력
- `nslookup <RDS Endpoint>` 실행 → Failover 시 IP 변경 확인

### 🔹 Task 6: 리소스 삭제
- DB, Subnet Group, Parameter/Option Group, 보안 그룹 삭제

---

## 8. Review

- AWS RDS는 관리형 관계형 데이터베이스 서비스(PaaS)
- 콘솔, CLI, API를 통한 손쉬운 관리 가능
- 컴퓨팅 및 스토리지 확장성 지원
- 자동화된 백업, 복제, 장애조치 제공
- 지원 엔진: Aurora, MySQL, PostgreSQL, MariaDB, Oracle, SQL Server
