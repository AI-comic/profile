### 1. t_readonly 테이블 생성
```
create table t_readonly(no number, name varchar2(10));

insert into t_readonly values (1,'AAA');
commit;
```
t_readonly라는 테이블을 생성하고, no와 name을 포함한 데이터를 삽입합니다.

### 2. t_readonly 테이블을 읽기 전용으로 설정
```
alter table t_readonly read only;
```
t_readonly 테이블을 읽기 전용으로 설정합니다.

이 상태에서 데이터 삽입, 수정, 삭제는 불가능합니다.

### 3. t_readonly 테이블에 데이터 삽입 시도
```
insert into t_readonly values (2,'BBB');
```
읽기 전용 테이블에 데이터를 삽입하려고 하면 오류가 발생합니다.

### 4. t_readonly 테이블에 컬럼 추가
```
alter table t_readonly add(tel number default 111);
```
읽기 전용 상태에서 테이블에 tel 컬럼을 추가하려면 먼저 읽기 전용 상태를 해제해야 합니다.

### 5. t_readonly 테이블을 읽기/쓰기 가능 상태로 변경
```
alter table t_readonly read write;
```
t_readonly 테이블을 읽기/쓰기 가능 상태로 변경합니다.

### 6. t_readonly 테이블 삭제
```
drop table t_readonly;
```
t_readonly 테이블을 삭제합니다.


