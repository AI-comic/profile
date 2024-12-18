### 1. new_emp 테이블 생성
```
create table new_emp(
    no number(5), 
    name varchar2(20), 
    hiredate date, 
    bonus number(6,2)
);
```
new_emp라는 이름의 테이블을 생성하는 명령입니다.

no: 사원의 번호를 나타내는 숫자형 컬럼입니다. number(5)는 5자리까지 숫자를 저장할 수 있음을 의미합니다.

name: 사원의 이름을 저장하는 문자형 컬럼입니다. 최대 20자까지 저장할 수 있습니다.

hiredate: 사원의 입사일을 저장하는 date 타입의 컬럼입니다.

bonus: 사원의 보너스를 나타내는 숫자형 컬럼입니다. number(6,2)는 최대 6자리 숫자 중 소수점 2자리까지 저장할 수 있음을 의미합니다.

### 2. new_emp 테이블에서 일부 컬럼을 선택하여 new_emp2 테이블 생성
```
create table new_emp2
as select no, name, hiredate
from new_emp;
```
new_emp 테이블에서 no, name, hiredate 컬럼만 선택하여 new_emp2라는 새로운 테이블을 생성하는 명령입니다.

select no, name, hiredate from new_emp: new_emp 테이블에서 세 가지 컬럼을 선택합니다.

create table new_emp2 as: 선택한 데이터를 기반으로 새로운 테이블 new_emp2를 생성합니다.

이 방법으로 생성된 테이블은 new_emp 테이블의 일부 컬럼을 갖게 되며, 해당 컬럼의 데이터도 함께 복사됩니다.

### 3. new_emp2 테이블과 동일한 구조로 new_emp3 테이블 생성 (데이터 제외)
```
create table new_emp3
as select * from new_emp2
where 1 = 2;
```
new_emp2 테이블과 동일한 구조를 가진 new_emp3 테이블을 생성하지만, 데이터를 포함하지 않도록 하는 명령입니다.

select * from new_emp2 where 1 = 2: new_emp2 테이블에서 모든 컬럼을 선택하지만 where 1 = 2 조건을 사용하여 결과가 없게 만듭니다. 조건 1 = 2는 항상 false이므로, 아무 데이터도 선택되지 않습니다.

이 쿼리는 new_emp2의 컬럼 구조만 가져오고, 데이터는 포함되지 않는 새로운 테이블 new_emp3를 생성합니다.

### 4. new_emp2 테이블에 BIRTHDAY 컬럼 추가 (자동으로 현재 날짜 입력)
```
alter table new_emp2
add (birthday date default sysdate);
```
new_emp2 테이블에 birthday라는 date 타입의 새로운 컬럼을 추가하는 명령입니다.

alter table new_emp2 add (birthday date default sysdate): new_emp2 테이블에 birthday라는 컬럼을 추가합니다.

birthday date: 새로운 컬럼의 데이터 타입은 date입니다. 이 컬럼은 날짜 값을 저장합니다.

default sysdate: 이 컬럼의 기본값은 sysdate입니다. sysdate는 Oracle에서 현재 시스템 날짜와 시간을 반환하는 함수입니다. 이 설정을 통해 birthday 컬럼에 값을 명시적으로 삽입하지 않으면, 기본값으로 현재 날짜가 자동으로 입력됩니다.

### 5. new_emp2 테이블의 BIRTHDAY 컬럼 이름을 BIRTH로 변경
```
alter table new_emp2 rename column birthday to birth;
```
new_emp2 테이블의 BIRTHDAY 컬럼의 이름을 BIRTH로 변경하는 명령입니다.

alter table new_emp2: new_emp2 테이블을 수정하는 명령입니다.

rename column birthday to birth: birthday 컬럼의 이름을 birth로 변경합니다.

BIRTHDAY는 기존의 컬럼 이름이고, birth는 변경할 새로운 이름입니다.

### 6. new_emp2 테이블의 NO 컬럼의 길이를 NUMBER(7)로 변경
```
alter table new_emp2
modify(no number(7));
```
new_emp2 테이블에서 NO 컬럼의 데이터 타입을 NUMBER(7)로 변경하는 명령입니다.

alter table new_emp2: new_emp2 테이블을 수정하는 명령입니다.

modify(no number(7)): NO 컬럼의 데이터 타입을 NUMBER(7)로 변경합니다.

기존에는 NUMBER(5)로 정의되어 있을 수 있으며, 이 작업을 통해 NO 컬럼에 저장할 수 있는 숫자의 길이를 7자리로 확장합니다.

NUMBER(7)은 최대 7자리 숫자를 저장할 수 있게 합니다.

### 7. new_emp2 테이블에서 BIRTH 컬럼 삭제
```
alter table new_emp2 drop column birth;
```
new_emp2 테이블에서 BIRTH 컬럼을 삭제하는 명령입니다.

alter table new_emp2: new_emp2 테이블을 수정하는 명령입니다.

drop column birth: birth 컬럼을 삭제합니다.

이 명령을 실행하면 new_emp2 테이블에서 birth 컬럼은 완전히 제거됩니다. 컬럼의 데이터와 메타데이터도 삭제됩니다.

### 8. 테이블의 컬럼은 남겨 두고 데이터만 삭제
```
truncate table new_emp2;
```
new_emp2 테이블의 모든 데이터를 삭제합니다.

truncate 명령은 데이터를 빠르고 효율적으로 삭제하는 방법입니다.

truncate는 테이블 구조는 그대로 두지만, 데이터만 삭제합니다. 이 명령은 롤백이 불가능하고, 삭제된 데이터를 복구할 수 없습니다.

truncate는 데이터 삭제 후 자동으로 테이블의 공간을 회수하므로 빠르게 수행됩니다.

### 9. 테이블의 컬럼은 남겨 두고 데이터만 삭제
```
delete from new_emp2;
```
new_emp2 테이블의 모든 데이터를 삭제하는 명령입니다.

delete 명령은 데이터를 삭제하지만, 테이블의 구조(컬럼 정의)는 그대로 남깁니다.

delete는 롤백이 가능합니다. 즉, 트랜잭션을 사용하여 데이터 삭제를 되돌릴 수 있습니다.

delete는 truncate에 비해 상대적으로 느리며, 행 단위로 삭제가 이루어집니다.

truncate 명령은 데이터만 삭제하고 테이블 구조는 그대로 두는 빠른 방법이지만, 롤백이 불가능하므로 신중하게 사용해야 합니다. 반면 delete는 롤백이 가능하지만 상대적으로 느릴 수 있습니다.

### 10. new_emp2 테이블을 완전히 삭제
```
drop table new_emp2;
```
new_emp2 테이블을 완전히 삭제하는 명령입니다.

drop table new_emp2: new_emp2 테이블을 데이터와 함께 완전히 삭제합니다.

drop table 명령을 실행하면 해당 테이블과 테이블에 포함된 데이터, 인덱스, 제약조건 등이 모두 삭제됩니다.

이 명령은 되돌릴 수 없으며, 삭제된 테이블은 복구할 수 없습니다.
