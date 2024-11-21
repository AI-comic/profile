1. 프로젝트 생성:

- Name: springboot_basic
- Language: Java
- Type: Maven
- Group: kopo.aisw
- Artifact: springboot_basic
- Package name: kopo.aisw.springboot_basic
- JDK: 17(Oracle OpenJDK 17.0.12)
- Java: 17
- Packaging: Jar
<br>

- Spring Boot: 3.2.11
- Dependencies:
- Spring Web
- MyBatis Framework
- Oracle Driver(MariaDB를 사용할 경우엔 MariaDB Driver)
- Thymeleaf
- Lombok
- Spring Security
- Validation

---
2. 회원 테이블 생성:
- MariaDB 버전
```
USE blog_db;

CREATE TABLE tb_user (
    user_id VARCHAR(50) PRIMARY KEY,
    user_password VARCHAR(100) NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    user_auth VARCHAR(20) DEFAULT 'USER',
    reg_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    mod_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```


- Oracle 버전
```
-- 테이블 생성
CREATE TABLE tb_user (
    user_id VARCHAR2(50) PRIMARY KEY,
    user_password VARCHAR2(100) NOT NULL,
    user_name VARCHAR2(100) NOT NULL,
    user_auth VARCHAR2(20) DEFAULT 'USER',
    reg_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    mod_date TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- 트리거 생성 (자동 수정 일자)
CREATE OR REPLACE TRIGGER trg_tb_user_update
    BEFORE UPDATE ON tb_user
    FOR EACH ROW
BEGIN
    :NEW.mod_date := SYSTIMESTAMP;
END;
```


