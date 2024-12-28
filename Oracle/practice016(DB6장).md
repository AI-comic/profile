### 1. 연습 문제.
Dept2 테이블에 아래와 같은 내용으로 새로운 부서 정보를 입력하세요.
* 부서번호 : 9010
* 부서명 : temp_10
* 상위부서 : 1006
* 지역 : temp area
```
insert into dept2
values(9010, 'temp_10', 1006, 'temp area');
```
### 2. 연습 문제.
Dept2 테이블에 아래와 같은 내용으로 특정 컬럼에만 정보를 입력하세요.
* 부서번호 : 9020
* 부서명 : temp_20
* 상위부서 : Business Department (1006 번 부서)
```
insert into dept2(dcode, dname, pdept)
values(9020, 'temp_20', 1006);
```
### 3. 연습 문제.
professor 테이블에서 profno가 3000번 이하의 교수들의 profno, name, pay를 가져와서 professor4 테이블에 한꺼번에 입력하는 쿼리를 쓰세요.
```
create table professor4
as
    select profno, name, pay
    from professor
    where profno <= 3000;
```
### 4. 연습 문제.
professor 테이블에서 'Sharon Stone' 교수의 BONUS를 200만 원으로 인상하세요.
```
update professor
set bonus = 200
where name = 'Sharon Stone';
```
