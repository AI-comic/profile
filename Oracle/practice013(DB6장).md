### 1. dept2 테이블에 데이터 삽입
Dept2 테이블에 아래와 같은 내용으로 새로운 부서 정보를 입력하세요.
* 부서번호 : 9000
* 부서명 : temp_1
* 상위부서 : 1006
* 지역 : temp area  
```
insert into dept2(dcode, dname, pdept, area)
values(9000,'tempt_1',1006,'temp area');
```
### 2. dept2 테이블에 데이터 삽입
```
insert into dept2
values(9001,'tempt_2',1006,'temp area');
```
이 INSERT 명령도 비슷한 방식으로 데이터를 삽입하지만, 컬럼명을 명시하지 않고 values 절에 값만 제공합니다.

모든 컬럼에 값을 삽입할 때는 괄호 안의 컬럼명을 생략할 수 있습니다.

### 3. 특정 컬럼에 값을 입력하기
부서번호와 부서명, 상위부서 값만 아래의 값으로 입력하세요.
* 부서번호 : 9002
* 부서명 : temp_3
* 상위부서 : Business Department (1006번 부서)
```
insert into dept2(dcode, dname, pdept)
values(9002, 'temp_3', 1006);
```
### 4. 날짜 데이터 입력하기
아래 정보를 professor 테이블에 입력하세요.
* 교수번호 : 5001
* 교수이름 : James Bond
* ID : Love_me
* POSITION : 정교수
* PAY : 510
* 입사일 : 2014년 10월 23일
```
insert into professor(profno, name, id, position, pay, hiredate)
values(5001,'james bond','Love_me','a full professor',500,'2014-10-23');
```
날짜 때문에 에러가 발생합니다.

```
alter session set nls_date_format = 'yyyy-mm-dd:hh24:mi:ss';
```
이 명령은 세션에서 날짜 포맷을 변경합니다.

yyyy-mm-dd:hh24:mi:ss 형식으로 날짜와 시간을 출력하게 됩니다.

hh24는 24시간 형식의 시각을 의미합니다.

### 5. 음수 값 입력하기
```
create table t_minus
    (no1 number, no2 number(3), no3 number(3,2));
```
t_minus 테이블을 생성합니다.

no1은 숫자 타입이고, no2는 3자리 숫자, no3은 소수점 두 자리를 포함한 3자리 숫자 타입으로 정의됩니다.
```
insert into t_minus values(1,1,1);
insert into t_minus values(1.1,1.1,1.1);
insert into t_minus values(-1.1,-1.1,-1.1);
```
데이터를 입력해 보니 no1과 no3는 동일하게 정수와 소수와 음수까지 이상 없이 입력이 됩니다.

하지만 no2의 경우는 소수점 이하가 표시되지 않는 것을 알 수 있습니다.

그리고 음수를 입력하는 방법도 양수와 동일함을 알 수 있습니다.
