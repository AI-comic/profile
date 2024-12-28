### 1. UPDATE (데이터 변경하기)
Professor 테이블에서 직급이 조교수(assistant professor)인 교수들의 BONUS 를 200 만원으로 인상하세요.
```
update professor
set bonus = 200
where position = 'assistant professor';
```
### 2. UPDATE (데이터 변경하기)
Professor 테이블에서 'Sharon Stone' 교수의 직급과 동일한 직급을 가진 교수들 중 현재 급여가 250만 원이 안 되는 교수들의 급여를 15% 인상하세요.
```
update professor
set pay = pay * 1.15
where position = (select position from professor where name = 'Sharon Stone')
and pay < 250;
```
### 3. DELETE (데이터 삭제하기)
Dept2 테이블에서 부서 번호(DCODE)가 9000번에서 9999번 사이인 매장들을 삭제하세요.
```
delete from dept2
where dcode >= 9000 and dcode <= 9999;
```
### 4. MERGE (데이터 병합하기)
```
create table charge_01( u_date varchar2(6),
                        cust_no number,
                        u_time number,
                        charge number);
```
```
create table charge_02( u_date varchar2(6),
                        cust_no number,
                        u_time number,
                        charge number);
```
테스트용 테이블을 생성합니다.
```
insert into charge_01 values('141001',1000,2,1000);
insert into charge_01 values('141001',1001,2,1000);
insert into charge_01 values('141001',1002,1,500);
insert into charge_02 values('141002',1000,3,1500);
insert into charge_02 values('141002',1001,4,2000);
insert into charge_02 values('141002',1003,1,500);
```
테스트용 데이터를 입력합니다.
```
merge into ch_total total
using charge_01 ch01
on(total.u_date = ch01.u_date)
when matched then
update set total.cust_no = ch01.cust_no
when not matched then
insert values(ch01.u_date, ch01.cust_no, ch01.u_time, ch01.charge);
```
charge_01 테이블의 데이터를 ch_total 테이블에 병합합니다.

조건: ch_total과 charge_01 테이블의 u_date 값이 동일한 행들을 찾습니다.

매칭되는 경우: cust_no 값을 ch01 테이블의 cust_no 값으로 업데이트합니다.

매칭되지 않는 경우: charge_01 테이블에서 새로운 데이터를 ch_total 테이블에 삽입합니다.
```
merge into ch_total total
using charge_02 ch02
on(total.u_date = ch02.u_date)
when matched then
update set total.cust_no = ch02.cust_no
when not matched then
insert values(ch02.u_date, ch02.cust_no, ch02.u_time, ch02.charge);
```
charge_02 테이블의 데이터를 ch_total 테이블에 병합합니다.

조건: ch_total과 charge_02 테이블의 u_date 값이 동일한 행들을 찾습니다.

매칭되는 경우: cust_no 값을 ch02 테이블의 cust_no 값으로 업데이트합니다.

매칭되지 않는 경우: charge_02 테이블에서 새로운 데이터를 ch_total 테이블에 삽입합니다.

```
insert into ch_total
select * from charge_01
union
select * from charge_02;
```
charge_01과 charge_02 테이블의 데이터를 ch_total 테이블에 삽입합니다.

조건: union을 사용하여 중복된 데이터를 제거하고, 두 테이블에서 데이터를 합쳐서 삽입합니다.

효과: charge_01과 charge_02의 모든 데이터를 중복 없이 ch_total에 삽입합니다.

이 구문은 이전의 MERGE 구문들과 동일한 결과를 얻을 수 있습니다.
