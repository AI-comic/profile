### 1. 연습 문제
아래의 표를 보고 테이블을 생성하면서 제약 조건을 설정하세요.
|테이블 이름|컬럼 이름|데이터 타입|제약 조건 종류|제약 조건 이름|기타 사항|
|---|---|---|---|---|---|
|tcons|no|number(5)|primary key|tcons_no_pk||
||name|varchar2(20)|not null|tcons_name_nn||
||jumin|varchar2(13)|not null|tcons_jumin_nn||
||jumin|varchar2(13)|unique|tcons_jumin_uk||
||area|number(1)|check|tcons_area_ck|1~4 사이 숫자|
||deptno|varchar2(6)|foreign key|tcons_deptno_fk|dept2(dcode)|
```
create table tcons(
    no      number(5)       constraint tcons_no_pk primary key,
    name    varchar2(20)    constraint tcons_name_nn not null,
    jumin   varchar2(13)    constraint tcons_jumin_nn not null
                            constraint tcons_jumin_uk unique,
    area    number(1)       constraint tcons_area_ck check(area < 5),
    deptno  varchar2(6)     constraint tcons_deptno_fk references dept2(dcode)
);
```

### 2. 연습 문제
tcons 테이블의 name 컬럼이 emp2 테이블의 ename 컬럼의 값을 참조하도록 참조키 제약 조건을 추가 설정하는 쿼리를 쓰세요.(tcons 테이블이 자식테이블입니다.)
```
alter table emp2
add constraint emp_name_uk unique(name);
```
이 과정이 먼저 수행되어야 하는 이유는, tcons 테이블의 name 컬럼이 emp2 테이블의 name 컬럼을 참조하는 외래 키 제약을 추가하려면, 참조되는 emp2.name 컬럼에 고유 제약(UNIQUE) 또는 기본 키(PK) 제약이 있어야 하기 때문입니다.
```
alter table tcons
add constraint tcons_name_fk foreign key(name)
    references emp2(name);
```
tcons 테이블의 name 컬럼에 FOREIGN KEY 제약을 추가하여, name 값이 emp2 테이블의 name 컬럼에 있는 값만 참조할 수 있도록 설정하는 쿼리입니다.

즉, tcons.name 값은 반드시 emp2.name에 존재하는 값이어야 하며, 이를 통해 두 테이블 간의 데이터 무결성을 유지합니다.

### 3. 연습 문제
emp2 테이블의 name 컬럼에 만들어져 있는 unique 제약 조건을 "사용 안 함"으로 변경하되 해당 테이블의 데이터에 DML까지 안 되도록 변경하는 쿼리를 쓰세요.(제약조건 이름은 test10_name_uk 입니다.)
```
alter table emp2
disable unique(name) cascade;
```
emp2 테이블의 name 컬럼에 설정된 제약 조건을 "사용 안 함"으로 변경
```
alter table emp2
disable validate constraint emp2_name_uk;
```
제약 상태를 "검증 안 함"으로 설정

disable validate는 제약을 비활성화하면서도, 기존에 emp2.name 컬럼에 있는 데이터가 제약을 위반하는지 여부를 검증하지 않도록 설정합니다.

이 제약을 비활성화하고 나서도 기존 데이터가 name 컬럼에 중복된 값이 있을 수 있음을 의미합니다.

그러나 향후 데이터가 들어올 때는 제약이 적용되지 않으므로, 새로운 데이터가 제약을 위반할 수 있습니다.

### 4. 연습 문제
emp 테이블에 설정되어 있는 제약 조건 중 자신이 생성한 제약 조건들을 테이블명, 컬럼명, 제약 조건명으로 검색하는 쿼리를 쓰세요.(단, Foreign key는 제외합니다.)
```
select owner, table_name, column_name, constraint_name
from user_cons_columns
where table_name = 'EMP'
  and constraint_type != 'R';
```
