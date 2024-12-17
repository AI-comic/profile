### 1. new_table 생성
```
create table new_table(
    no number(3),
    name varchar2(10),
    birth date);
```
테이블에는 3개의 컬럼이 있습니다:

no: 숫자형(3자리 숫자) 컬럼

name: 최대 10자까지 문자열을 저장할 수 있는 varchar2 컬럼

birth: 날짜형 date 컬럼

### 2. tt02 테이블 생성
```
create table tt02
    (no     number(3,1) default 0,
    name    varchar2(10) default 'NO Name',
    hiredate    date    default sysdate);
    
insert into tt02 (no) values(1);
```
tt02라는 테이블을 생성합니다.

no: number(3,1) 타입으로, 기본값을 0으로 설정합니다.

name: varchar2(10) 타입으로, 기본값을 'NO Name'으로 설정합니다.

hiredate: date 타입으로, 기본값을 sysdate(현재 시스템 날짜)로 설정합니다.

**insert into tt02 (no) values(1);** 는 tt02 테이블에 no 컬럼만 1로 지정하여 삽입하는 쿼리입니다. 다른 두 컬럼(name과 hiredate)은 기본값이 자동으로 적용됩니다.

### 3. 한글테이블 생성
```
create table 한글테이블
    (컬럼1    number,
     컬럼2    varchar2(10),
     컬럼3    date);
```
한글테이블이라는 테이블을 생성합니다.

컬럼1: 숫자형 number 컬럼

컬럼2: 최대 10자까지 문자열을 저장할 수 있는 varchar2 컬럼

컬럼3: 날짜형 date 컬럼

### 4. temp01 전역 임시 테이블 생성
```
create global temporary table temp01
    (no     number,
     name   varchar2(10));

insert into temp01 values(1, 'AAAAA');
```
temp01이라는 전역 임시 테이블(Global Temporary Table)을 생성합니다.

이 테이블은 세션 기반의 임시 테이블로, 세션 종료 시 데이터가 사라집니다.

**insert into temp01 values(1, 'AAAAA');** 는 temp01 테이블에 값을 삽입하는 쿼리입니다.

### 5. 테이블 복사
```
create table dept3
as
select * from dept2;
```
dept2 테이블의 모든 데이터를 dept3라는 새 테이블에 복사하는 쿼리입니다.

### 6. 테이블의 구조(컬럼)만 가져오고 데이터 안 가져오기
```
create table dept5
as select * from dept2
where 1=2;
```
dept2 테이블의 구조를 dept5라는 새 테이블에 복사하지만, where 1=2 조건을 사용하여 데이터를 복사하지 않습니다.

즉, dept5 테이블은 dept2 테이블과 동일한 구조를 가지지만 빈 테이블이 됩니다.
