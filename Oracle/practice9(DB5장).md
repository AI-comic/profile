### 1. dept6 테이블 생성
```
create table dept6
as
select dcode, dname
from dept2
where dcode in(1000,1001,1002);
```
dept2 테이블에서 dcode가 1000, 1001, 1002인 레코드를 dept6 테이블에 복사하는 쿼리입니다.

### 2. dept6 테이블 컬럼 수정
```
alter table dept6
add (location2 varchar2(10) default 'seoul');
```
dept6 테이블에 location2라는 varchar2(10) 컬럼을 추가하고, 기본값을 'seoul'로 설정합니다.

### 3. 컬럼 이름 변경
```
alter table dept6
rename column location2 to loc;
```
dept6 테이블에서 location2 컬럼의 이름을 loc로 변경합니다.

### 4. 테이블 이름 변경
```
rename dept6 to dept7;
```
dept6 테이블의 이름을 dept7로 변경합니다.

### 5. dept7 테이블 컬럼 수정
```
alter table dept7
modify(loc varchar2(20));
```
dept7 테이블에서 loc 컬럼의 데이터 타입을 varchar2(20)으로 변경합니다.

### 6. dept7 테이블 컬럼 삭제
```
alter table dept7
drop column loc;
```
dept7 테이블에서 loc 컬럼을 삭제합니다.

### 7. dept7 테이블 데이터 삭제
```
truncate table dept7;
```
dept7 테이블의 모든 데이터를 삭제하는 쿼리입니다.

TRUNCATE는 데이터를 빠르게 삭제하며, 롤백이 불가능합니다.
