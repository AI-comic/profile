### 1. 테스트용 테이블 및 인덱스 생성
```
create table i_test
(no number);
```
i_test라는 이름의 테이블을 생성합니다.

이 테이블은 no라는 숫자 컬럼을 가지고 있습니다.

no 컬럼은 숫자 데이터 타입(number)을 갖습니다.

```
begin
    for i in 1..10000 loop
        insert into i_test values(i);
    end loop;
commit;
end;
```
PL/SQL 블록을 사용하여 i_test 테이블에 1부터 10,000까지의 값을 삽입합니다.

즉, no 컬럼에 1, 2, 3, ..., 10000을 넣는 작업을 수행합니다.

```
create index idx_itest_no on i_test(no);
```
i_test 테이블의 no 컬럼에 대해 B-tree 인덱스 idx_itest_no를 생성합니다. 

이 인덱스는 no 컬럼을 기준으로 데이터를 빠르게 조회할 수 있도록 도와줍니다.

### 2. 인덱스 상태 조회
```
analyze index idx_itest_no validate structure;
```
idx_itest_no 인덱스의 구조를 분석합니다. 

analyze index 명령어는 인덱스의 상태와 통계 정보를 갱신하여 성능 최적화에 도움이 됩니다.

validate structure 옵션은 인덱스의 내부 구조에 문제가 있는지 확인합니다.

```
select (del_lf_rows_len / lf_rows_len) * 100 balance
from index_stats
where name='IDX_ITEST_NO';
```
index_stats 뷰에서 IDX_ITEST_NO 인덱스의 balance(균형)를 확인합니다.

del_lf_rows_len: 삭제된 리프 노드의 길이

lf_rows_len: 전체 리프 노드의 길이

이 두 값을 나누어 인덱스의 균형 상태를 퍼센트로 계산합니다.

이 값이 높으면 인덱스가 불균형 상태일 수 있습니다.

### 3. 일부 데이터 삭제 후 인덱스 상태 조회
```
delete from i_test
where no between 1 and 4000;
```
i_test 테이블에서 no 값이 1에서 4000까지인 레코드를 삭제합니다.

이 작업은 인덱스의 일부 항목을 삭제하여 인덱스가 "조각나게" 만들 수 있습니다.
```
select count(*) from i_test;
```
i_test 테이블에 남아있는 레코드 수를 조회합니다. 

```
analyze index idx_itest_no validate structure;
```
다시 idx_itest_no 인덱스의 구조를 분석하여 삭제된 데이터로 인해 인덱스가 어떻게 변경되었는지 확인합니다.

```
select (del_lf_rows_len / lf_rows_len) * 100 balance
from index_stats
where name='IDX_ITEST_NO';
```
삭제된 레코드에 의해 인덱스가 얼마나 불균형해졌는지 확인합니다. 

balance 값을 계산하여 인덱스 상태를 점검합니다.

### 4. 인덱스 리빌드 (Rebuild) 작업
```
alter index idx_itest_no rebuild;
```
idx_itest_no 인덱스를 리빌드합니다. 

리빌드 작업은 인덱스의 물리적 구조를 새로 만들어서 삭제된 데이터로 인한 불균형을 해결하고, 성능을 최적화하는 데 도움을 줍니다. 

리빌드 후 인덱스는 다시 효율적으로 동작할 수 있습니다.

```
analyze index idx_itest_no validate structure;
```
인덱스를 리빌드한 후 다시 인덱스 구조를 분석하여 리빌드 후 상태를 확인합니다.

```
select (del_lf_rows_len / lf_rows_len) * 100 balance
from index_stats
where name='IDX_ITEST_NO';
```
리빌드 후 인덱스의 균형 상태를 점검하여, 삭제된 리프 노드가 얼마나 정리되었는지 확인합니다.

### 5. 인덱스 관리
```
create index idx_emp_sal on emp(sal);
```
emp 테이블의 sal 컬럼에 대해 인덱스 idx_emp_sal를 생성합니다.

이 인덱스는 급여(sal) 컬럼을 기준으로 빠른 조회를 가능하게 합니다.

```
select table_name, index_name, visibility
from user_indexes
where table_name = 'EMP';
```
user_indexes 뷰를 조회하여 EMP 테이블에 존재하는 인덱스들의 이름과 가시성(visibility)을 확인합니다.

visibility는 해당 인덱스가 사용 가능한 상태인지, 또는 숨겨진 상태인지 여부를 나타냅니다.

```
alter index idx_emp_sal invisible;
```
idx_emp_sal 인덱스를 숨김(invisible) 상태로 변경합니다. 

인덱스는 여전히 존재하지만, SQL 쿼리에서 사용되지 않습니다.

이 옵션은 인덱스를 잠시 비활성화할 때 유용합니다.

```
alter index idx_emp_sal visible;
```
다시 idx_emp_sal 인덱스를 보이게(visible) 만듭니다.

이제 이 인덱스는 다시 쿼리에서 사용될 수 있습니다.
