### 1. INSERT와 서브 쿼리를 사용하여 여러 행 입력하기
```
create table professor3
as
    select * from professor
    where 1 = 2;
```
professor3 테이블을 생성하는데, professor 테이블에서 where 1=2 조건을 사용하여 테이블 구조만 복사합니다.

즉, professor3는 professor 테이블과 동일한 구조를 가지지만, 데이터는 없습니다.

### 2. INSERT와 서브 쿼리를 사용하여 여러 행 입력하기
```
insert into professor3
select * from professor;
```
professor3 테이블에 professor 테이블의 모든 데이터를 삽입합니다.

### 3. professor4 테이블 생성 및 데이터 삽입
```
create table professor4
as
    select profno, name, pay
    from professor
    where 1=2;
```
professor4 테이블을 생성하는데, professor 테이블에서 profno, name, pay 컬럼만 선택하고,

where 1=2 조건으로 데이터를 가져오지 않고, 테이블 구조만 복사합니다.

### 4. professor4 테이블 생성 및 데이터 삽입
```
insert into professor4
select profno, name, pay
from professor
where profno>4000;
```
professor4 테이블에 professor 테이블에서 profno가 4000보다 큰 교수들의 profno, name, pay 값을 삽입합니다.

### 5. INSERT ALL 을 이용한 여러 테이블에 여러 행 입력하기
```
create table prof_3(
    profno  number,
    name    varchar2(25));
```
```
create table prof_4(
    profno  number,
    name    varchar2(25));
```
테스트용 테이블 prof_3, prof_4 생성합니다.

### 5. INSERT ALL 을 이용한 여러 테이블에 여러 행 입력하기
Professor 테이블에서 교수 번호가 1000번에서 1999번까지인 교수의 번호와 교수 이름은 prof_3 테이블에 입력하세요.

교수 번호가 2000번에서 2999번까지인 교수의 번호와 이름은 prof_4 테이블에 입력하세요.
```
insert all
when profno between 1000 and 1999 then into prof_3 values(profno, name)
when profno between 2000 and 2999 then into prof_4 values(profno, name)
select profno, name
from professor;
```
### 5. INSERT ALL 을 이용한 여러 테이블에 여러 행 입력하기
prof_3 과 prof_4 테이블의 데이터를 TRUNCATE로 삭제한 후,

Professor 테이블에서 교수 번호가 3000번에서 3999번인 교수들의 교수 번호와 이름을 prof_3 테이블과 prof_4 테이블에 동시에 입력하세요.
```
truncate table prof_3;
truncate table prof_4;
insert all
    into prof_3 values(profno, name)
    into prof_4 values(profno, name)
select profno, name
from professor
where profno between 3000 and 3999;
```
