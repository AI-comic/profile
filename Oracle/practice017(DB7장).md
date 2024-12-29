### 1. 테이블 생성 시 Constraint(제약 조건) 지정하기
```
create table new_emp1(
    no      number(4)    constraint emp1_no_pk primary key,
    name    varchar2(20)    constraint emp1_name_nn not null,
    jumin   varchar2(13)    constraint emp1_jumin_nn not null
                            constraint emp1_jumin_uk unique,
    loc_code number(1)  constraint emp1_area_ck check(loc_code < 5),
    deptno varchar2(6)  constraint emp1_deptno_fk references dept2(dcode)
);
```
no: number(4) 타입. 이 컬럼은 PRIMARY KEY로 설정되어 있습니다. 이는 no 컬럼이 반드시 고유하고 NULL 값을 가질 수 없다는 의미입니다.

name: varchar2(20) 타입. 이 컬럼에는 NOT NULL 제약이 걸려 있어, 반드시 값을 입력해야 합니다.

jumin: varchar2(13) 타입. 이 컬럼에는 NOT NULL 제약이 걸려 있으며, UNIQUE 제약이 추가되어 있어 중복된 주민번호가 입력될 수 없습니다.

loc_code: number(1) 타입. CHECK 제약으로 loc_code < 5를 설정하여, 5 이상의 값은 허용되지 않습니다.

deptno: varchar2(6) 타입. 이 컬럼은 FOREIGN KEY 제약을 가지고 있으며, dept2 테이블의 dcode 컬럼을 참조합니다.

### 2. 약식 제약 조건
```
create table new_emp2(
    no      number(4)       primary key,
    name    varchar2(20)    not null,
    jumin   varchar2(13)    not null    unique,
    loc_code number(1)      check(loc_code<5),
    deptno  varchar2(6)     references dept2(dcode)
);
```

### 3.  테이블 생성 후 제약 조건 추가하기
```
alter table new_emp2
add constraint emp2_name_uk unique(name);
```
new_emp2 테이블의 name 컬럼에 UNIQUE 제약을 추가합니다.

이제 name 값은 중복될 수 없게 됩니다. 

즉, name 컬럼에서 동일한 이름이 두 번 이상 입력될 수 없습니다.

### 4. 제약 조건 수정하기
```
alter table new_emp2
add constraint emp2_loccode_nn check (loc_code is not null);
```
이 쿼리는 new_emp2 테이블의 loc_code 컬럼에 NOT NULL 제약을 추가하려는 시도입니다.

그러나 Oracle에서 NOT NULL 제약을 ADD 명령으로 추가할 수는 없습니다. 

NOT NULL 제약은 별도의 제약 이름 없이 컬럼 정의에 직접 지정하는 제약입니다.

ADD CONSTRAINT 구문은 CHECK, FOREIGN KEY, PRIMARY KEY 등의 제약을 추가할 때 사용되며, NOT NULL 제약을 추가할 수 없습니다. 

NOT NULL 제약은 컬럼 정의에서 직접 설정해야 하기 때문에 ADD CONSTRAINT 구문으로는 올바르게 추가할 수 없습니다.
```
ORA-00904: : invalid identifier
```
NOT NULL을 추가하려는 방식이 잘못되었기 때문에 발생한 오류입니다. 

NOT NULL 제약은 **식별자(identifier)**로 인식할 수 없기 때문에 "잘못된 식별자"라는 오류가 발생합니다.
```
alter table new_emp2
modify(loc_code constraint emp2_loccode_nn not null);
```
loc_code 컬럼에 NOT NULL 제약을 추가하는 데 성공한 예시입니다.

MODIFY 구문은 기존 컬럼의 정의를 변경할 때 사용합니다.

NOT NULL 제약을 추가할 때는 MODIFY 구문을 사용해야 합니다. 

여기서는 loc_code 컬럼을 수정하면서 NOT NULL 제약을 추가한 것입니다.

constraint emp2_loccode_nn 부분은 제약 이름을 지정하는 부분입니다. 

이 제약 이름을 통해 나중에 이 제약을 식별하고 삭제하거나 변경할 수 있습니다.

### 5. 외래키 제약 추가하기
```
alter table new_emp2
add constraint emp2_no_fk foreign key(no)
references emp2(empno);
```
new_emp2 테이블에 no 컬럼을 emp2 테이블의 empno 컬럼과 연결하는 FOREIGN KEY 제약을 추가하는 명령입니다. 

이를 통해 new_emp2 테이블의 no 값은 반드시 emp2 테이블의 empno 컬럼에 존재하는 값이어야 합니다.

```
ALTER TABLE new_emp2
ADD CONSTRAINT emp2_name_fk FOREIGN KEY(name)
REFERENCES emp2(name);
```
new_emp2 테이블의 name 컬럼을 외래 키(Foreign Key)로 설정하여 emp2 테이블의 name 컬럼을 참조하려고 시도한 것입니다.

그러나 Oracle에서 외래 키 제약을 추가하려면 참조하는 컬럼(emp2.name)에 고유 제약(UNIQUE) 또는 기본 키(PRIMARY KEY)가 설정되어 있어야 합니다.

즉, 외래 키 제약을 추가하려면 참조하는 컬럼이 반드시 고유한 값만을 가져야 하므로, 해당 컬럼이 고유 제약(UNIQUE) 또는 기본 키(PRIMARY KEY)로 정의되어 있어야 합니다.

이 오류 메시지는 emp2 테이블의 name 컬럼에 고유 제약(UNIQUE) 또는 기본 키(PRIMARY KEY)가 없기 때문에 발생합니다.

name 컬럼에 고유한 값이 보장되지 않기 때문에 외래 키 제약을 설정할 수 없습니다.

```
ORA-02270: no matching unique or primary key for this column-list
```
외래 키를 추가하려고 할 때, 참조하는 컬럼(emp2.name)에 고유 제약(UNIQUE) 또는 기본 키(PRIMARY KEY)가 없다는 의미입니다.

```
ALTER TABLE emp2
ADD CONSTRAINT emp2_name_uk UNIQUE(name);
```
emp2 테이블의 name 컬럼에 UNIQUE 제약을 추가하는 쿼리입니다. 

UNIQUE 제약은 해당 컬럼의 값이 중복되지 않도록 보장합니다.

이 제약을 추가함으로써, emp2.name 컬럼이 고유한 값을 가지게 되고, 그 후에 다른 테이블에서 이 컬럼을 참조하는 외래 키 제약을 추가할 수 있게 됩니다.

```
ALTER TABLE new_emp2
ADD CONSTRAINT emp2_name_fk FOREIGN KEY(name)
REFERENCES emp2(name);
```
이 쿼리는 new_emp2 테이블의 name 컬럼에 외래 키 제약을 추가하여, 이 컬럼이 emp2 테이블의 name 컬럼을 참조하도록 설정하는 쿼리입니다.

이전 쿼리에서 emp2.name 컬럼에 UNIQUE 제약이 추가되었기 때문에, 이제 new_emp2.name 컬럼을 emp2.name 컬럼에 대해 외래 키로 참조할 수 있습니다.

외래 키 제약을 통해 new_emp2.name 컬럼에 입력되는 값은 반드시 emp2.name 컬럼에 존재하는 값이어야 합니다.
