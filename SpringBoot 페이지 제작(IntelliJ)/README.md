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

---
3. 게시판 구현:
- MariaDB 버전
```
CREATE TABLE tb_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    view_count INT DEFAULT 0,
    reg_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    mod_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);
```

- Oracle 버전
```
-- 시퀀스 생성
CREATE SEQUENCE board_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 테이블 생성
CREATE TABLE tb_board (
    board_id NUMBER PRIMARY KEY,
    title VARCHAR2(200) NOT NULL,
    content CLOB NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    view_count NUMBER DEFAULT 0,
    reg_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    mod_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_board_user FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

-- Auto_Increment 트리거
CREATE OR REPLACE TRIGGER trg_board_insert
    BEFORE INSERT ON tb_board
    FOR EACH ROW
    BEGIN
        SELECT board_seq.NEXTVAL
        INTO :NEW.board_id
        FROM DUAL;
    END;

-- 수정일자를 자동으로 업데이트하기 위한 트리거
CREATE OR REPLACE TRIGGER trg_board_update
    BEFORE UPDATE ON tb_board
    FOR EACH ROW
    BEGIN
        :NEW.mod_date := SYSTIMESTAMP;
    END;
```

---
4. 댓글 기능 구현:
- MariaDB 버전
```
CREATE TABLE IF NOT EXISTS tb_comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    reg_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    mod_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES tb_board(board_id) ON DELETE CASCADE
);
```

- Oracle 버전
```
-- 테이블 생성
CREATE TABLE tb_comment (
    comment_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    board_id NUMBER NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    content CLOB NOT NULL,
    reg_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    mod_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_comment_board FOREIGN KEY (board_id)
    REFERENCES tb_board(board_id) ON DELETE CASCADE
);

-- 수정일자를 자동으로 업데이트하기 위한 트리거
CREATE OR REPLACE TRIGGER trg_comment_update
    BEFORE UPDATE ON tb_comment
    FOR EACH ROW
    BEGIN
        :NEW.mod_date := SYSTIMESTAMP;
    END;
```

---
5. 파일 CRUD 기능 구현:
- MariaDB 버전
```
CREATE TABLE file_info (
    file_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    saved_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    reg_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES tb_board(board_id) ON DELETE CASCADE
);
```

- Oracle 버전
```
-- 시퀀스 생성 (AUTO_INCREMENT 대체)
CREATE SEQUENCE file_info_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 테이블 생성
CREATE TABLE file_info (
    file_id NUMBER PRIMARY KEY ,
    board_id NUMBER NOT NULL ,
    original_file_name VARCHAR2(255) NOT NULL ,
    saved_file_name VARCHAR2(255) NOT NULL ,
    file_path VARCHAR2(255) NOT NULL ,
    file_size NUMBER NOT NULL ,
    file_type VARCHAR2(100),
    reg_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_board_id FOREIGN KEY (board_id)
        REFERENCES tb_board(board_id) ON DELETE CASCADE
);

-- 트리거 생성 (AUTO_INCREMENT 기능 구현)
CREATE OR REPLACE TRIGGER file_info_bir
    BEFORE INSERT ON file_info
    FOR EACH ROW
    BEGIN
        SELECT file_info_seq.nextval
        INTO :NEW.file_id
        FROM DUAL;
    END;
```
