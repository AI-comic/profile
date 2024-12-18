### 1. 테이블 vt1 생성
```
create table vt1(
    col1 number, 
    col2 number, 
    col3 number generated always as (col1 + col2)
);
```
vt1 테이블을 생성합니다.

col1과 col2는 일반적인 숫자 컬럼입니다.

col3은 가상 컬럼입니다.

이 컬럼은 col1과 col2 값을 더한 결과를 자동으로 계산하여 저장합니다. 

즉, col3의 값은 항상 col1 + col2의 결과입니다.

**generated always as (col1 + col2)** 는 이 가상 컬럼의 정의 방식입니다.

### 2. 데이터 삽입
```
insert into vt1 (col1, col2) values (3, 4);
```
vt1 테이블에 데이터를 삽입합니다

col1에는 3, col2에는 4를 넣습니다.

col3는 가상 컬럼이므로 명시적으로 값을 넣을 필요는 없습니다.

col3의 값은 col1 + col2 즉, 3 + 4 = 7로 자동 계산됩니다.

### 3. 데이터 업데이트
```
update vt1 set col1=5;
```
vt1 테이블의 col1 값을 5로 업데이트합니다.

이 경우, col2는 그대로 4이므로, col3는 자동으로 5 + 4 = 9로 계산됩니다.

여기서 col3은 가상 컬럼이므로, col3에 대해 명시적으로 값을 업데이트할 필요는 없습니다. 

col1이 변경되면 col3도 자동으로 갱신됩니다.

만약 여러 튜플이 있다면 where 조건을 사용하여 특정 행을 업데이트해야 합니다.

### 4. 테이블 sales10 생성
```
create table sales10(
    no number, 
    pcode char(4), 
    pdate char(8), 
    pqty number, 
    pbungi number(1) generated always as (
        case
            when substr(pdate, 5, 2) in ('01', '02', '03') then 1
            when substr(pdate, 5, 2) in ('04', '05', '06') then 2
            when substr(pdate, 5, 2) in ('07', '08', '09') then 3
            else 4
        end
    ) virtual
);
```
sales10 테이블을 생성합니다.

no는 번호, pcode는 상품 코드, pdate는 날짜, pqty는 수량을 나타내는 일반 컬럼입니다.

pbungi는 가상 컬럼으로, pdate 컬럼의 5번째와 6번째 문자를 추출하여 해당 날짜가 속한 분기를 자동으로 계산합니다.

substr(pdate, 5, 2)는 pdate에서 5번째와 6번째 문자를 추출하는 함수입니다.

예를 들어, pdate가 20110112인 경우, substr('20110112', 5, 2)는 '01'이 되며, 이 경우 pbungi 값은 1이 됩니다 (1분기).

날짜가 '04', '05', '06'이면 2분기(2), '07', '08', '09'이면 3분기(3), 나머지는 4분기(4)로 계산됩니다.

pbungi는 generated always as 문을 사용하여 정의된 가상 컬럼입니다.

### 5. 데이터 삽입
```
insert into sales10(no, pcode, pdate, pqty)
values(1, '100', '20110112', 10);
insert into sales10(no, pcode, pdate, pqty)
values(2, '200', '20110505', 20);
insert into sales10(no, pcode, pdate, pqty)
values(3, '300', '20120812', 30);
insert into sales10(no, pcode, pdate, pqty)
values(4, '400', '20121124', 40);
```
sales10 테이블에 데이터를 삽입합니다.

첫 번째 삽입은 pdate가 '20110112'로, 1분기(1)가 됩니다.

두 번째 삽입은 pdate가 '20110505'로, 2분기(2)가 됩니다.

세 번째 삽입은 pdate가 '20120812'로, 3분기(3)가 됩니다.

네 번째 삽입은 pdate가 '20121124'로, 4분기(4)가 됩니다.

pbungi는 자동으로 계산되어 각 행에 대해 적절한 분기 값이 채워집니다.

### 6. 테이블 컬럼 정보 조회
```
select column_name, data_type, data_default
from user_tab_columns
where table_name = 'VT1'
order by column_id;
```
user_tab_columns 뷰를 조회하여 vt1 테이블의 컬럼 정보(column_name, data_type, data_default)를 확인합니다.

테이블에 정의된 컬럼들의 이름, 데이터 타입, 기본값을 확인할 수 있습니다.
