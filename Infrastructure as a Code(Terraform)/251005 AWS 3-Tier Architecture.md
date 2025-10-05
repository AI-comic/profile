# 애플리케이션 운영: 3-Tier Architecture

## 1. 3-Tier Architecture란?
**3-Tier Architecture**는 애플리케이션을 **3개의 계층(프레젠테이션, 애플리케이션, 데이터)** 으로 나누어 운영하는 방식이다.  

- 각 계층은 **독립적으로 개발, 배포, 확장**될 수 있다.  
- 특정 계층의 변경이 다른 계층에 영향을 최소화한다.  
- 운영 및 관리가 체계적으로 분리되어 대규모 시스템 운영에 적합하다.  

### 🔹 3개의 계층 설명
1. **프레젠테이션 계층 (Presentation Tier)**  
   - 사용자와 직접 상호작용하는 인터페이스(UI)  
   - 웹 브라우저, 모바일 앱 등이 해당  
   - 사용자 요청을 수집하고 결과를 시각적으로 보여줌  

2. **애플리케이션 계층 (Application Tier)**  
   - 핵심 비즈니스 로직을 담당  
   - Python, Java, PHP, Ruby 등으로 구현 가능  
   - API 호출을 통해 데이터 계층과 통신  
   - 예: 로그인 처리, 장바구니 기능, 결제 처리  

3. **데이터 계층 (Data Tier)**  
   - 데이터를 저장·관리하는 백엔드 계층  
   - 주로 **관계형 데이터베이스(RDS)** 또는 **NoSQL** 기반으로 운영  
   - 사용자 정보, 주문 내역, 로그 등 모든 비즈니스 데이터를 보관  

---

## 2. AWS 클라우드의 3-Tier Architecture
AWS 환경에서는 3-Tier Architecture를 손쉽게 구현할 수 있으며, 이를 통해 **확장성, 가용성, 보안성, 유연성**을 확보할 수 있다.  

### 🔹 Architecture 흐름
1. **Presentation Tier**  
   - **Amazon EC2, AWS Elastic Beanstalk, Amazon S3(정적 웹 호스팅)** 등을 사용하여 UI를 제공  

2. **Application Tier**  
   - **EC2, AWS Elastic Beanstalk, AWS Lambda** 등에서 애플리케이션 로직 실행  
   - 로드 밸런서(AWS ELB)를 사용해 트래픽 분산  

3. **Data Tier**  
   - **Amazon RDS, Amazon Aurora, DynamoDB** 등을 통해 데이터 저장 및 관리  
   - 보안 그룹과 서브넷을 이용해 접근 제어  

---

## 3. AWS 3-Tier Architecture 예시
- 사용자가 웹 브라우저를 통해 접속하면 **웹 서버(Presentation)** 가 요청을 받음  
- 요청은 **WAS(Application)** 로 전달되어 로그인/상품 조회/결제 등 로직을 처리  
- 최종적으로 **데이터 계층(Database)** 에서 정보를 가져와 사용자에게 응답 반환  

---

## 4. 정리 (Review)
- **3-Tier Architecture**는 애플리케이션을 **UI, 비즈니스 로직, 데이터 관리**로 구분하여 운영하는 구조  
- AWS에서는 EC2, RDS, ELB, S3, Lambda 등 다양한 서비스를 조합해 구현 가능  
- 장점:
  - 확장성: 각 계층별로 독립 확장 가능  
  - 가용성: 다중 AZ 및 오토스케일링 활용  
  - 보안: 네트워크 분리 및 보안 그룹 적용  
  - 유연성: 특정 계층만 별도로 업데이트 가능  

