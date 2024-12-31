### 1. 인덱스 정보 조회
```
select table_name, column_name, index_name
from user_ind_columns
where table_name = 'DEPT2';
```
DEPT2 테이블의 인덱스에 대한 정보를 조회합니다. 

user_ind_columns 뷰에서 테이블 이름, 컬럼 이름, 인덱스 이름을 가져옵니다.

이는 테이블에 어떤 인덱스들이 존재하는지 확인하는 데 사용됩니다.

### 2. 인덱스 사용 모니터링 설정
```
alter index idx_dept2_dname monitoring usage;
```
idx_dept2_dname 인덱스에 대해 사용 모니터링을 활성화합니다.

이를 통해 이 인덱스가 실제로 얼마나 자주 사용되는지 모니터링할 수 있습니다.

### 3. 인덱스 사용 모니터링 해제
```
alter index idx_dept2_dname nomonitoring usage;
```
idx_dept2_dname 인덱스의 사용 모니터링을 해제합니다. 

이제 더 이상 이 인덱스의 사용 상태가 추적되지 않습니다.

### 4. 인덱스 사용 현황 조회
```
select index_name, used
from v$object_usage
where index_name = 'IDX_DEPT2_DNAME';
```
IDX_DEPT2_DNAME 인덱스가 얼마나 자주 사용되었는지 확인합니다.

v$object_usage 뷰는 인덱스의 사용 정보를 제공합니다. 

이 조회를 통해 해당 인덱스가 실제로 조회에 사용되었는지 알 수 있습니다.
