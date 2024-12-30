### 1. 인덱스 생성
```
create unique index idx_dept2_dname
on dept2(dname);
```
dept2 테이블의 dname 컬럼에 대해 유니크 인덱스 idx_dept2_dname을 생성합니다. 

이 인덱스는 dname 컬럼의 값이 중복되지 않도록 보장하며, 중복되는 값을 삽입하려고 하면 오류가 발생합니다.

### 2. 데이터 삽입
```
insert into dept2 values(9100,'temp01',1006,'Seoul Branch');
insert into dept2 values(9101,'temp01',1006,'Busan Branch');
```
dept2 테이블에 두 개의 행을 삽입합니다. 
```
ERROR at line 1:
ORA-00001: unique constraint (SCOTT.IDX_DEPT2_DNAME) violated
```
첫 번째 dname의 값 'temp01'은 중복이 되지 않아서 들어갔지만, 두 번째로 입력하는 건 이미 들어간 dname이라서 에러가 발생합니다.

### 3. 인덱스 생성 2
생성 문법
```
create index 인덱스명
on 테이블명(컬럼명1 ASC | DESC, 컬럼명2, ······);
```
생성 예시
```
create index idx_dept2_area
on dept2(area);
```
dept2 테이블의 area 컬럼에 Non Unique Index(idx_dept2_area)를 생성합니다. 

이 인덱스는 area 컬럼을 기준으로 데이터를 빠르게 조회할 수 있도록 도와줍니다.

### 4. 함수 기반 인덱스(Function Based Index, FBI)
```
create index idx_prof_pay_fbi
on professor(pay+1000);
```
professor 테이블의 pay 컬럼에 1000을 더한 값을 기준으로 수식 인덱스 idx_prof_pay_fbi를 생성합니다. 

즉, pay + 1000을 계산한 값에 인덱스를 적용하여 해당 값으로 빠르게 조회할 수 있도록 합니다.

### 5. 함수 기반 쿼리 실행
```
select * from professor where pay + 1000 = 1500;
```
professor 테이블에서 pay + 1000 = 1500인 교수 정보를 조회합니다.

여기서 pay + 1000 값을 계산하기 위해 idx_prof_pay_fbi 인덱스가 사용됩니다.

### 6. 내림차순 인덱스(Descending Index)
```
create index idx_prof_pay
on professor(pay desc);
```
professor 테이블의 pay 컬럼에 대해 내림차순(desc) 인덱스를 생성합니다.

이렇게 하면 급여가 높은 순으로 빠르게 데이터를 조회할 수 있습니다.

### 7. 결합 인덱스(Composite Index)
```
create index idx_emp_comp
on emp(ename, sex);
```
emp 테이블의 ename(이름)과 sex(성별) 컬럼에 대해 복합 인덱스 idx_emp_comp를 생성합니다.

이 인덱스는 이름과 성별을 기준으로 효율적인 조회를 도와줍니다.

### 8. 비트맵 인덱스(Bitmap Index)
```
create bitmap index idx_emp_sex_bit
on emp(sex);
```
emp 테이블의 sex(성별) 컬럼에 대해 비트맵 인덱스 idx_emp_sex_bit를 생성합니다. 

성별 컬럼의 값이 제한적인 경우(예: 'M', 'F')에 유용하게 사용되며, 성별 정보로 빠르게 조회할 수 있습니다.
