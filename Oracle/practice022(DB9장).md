### 1. Complex View(복합 뷰)
```
create or replace view v_emp
as
    select e.ename, d.dname
    from emp e, dept d
    where e.deptno = d.deptno;
```
v_emp라는 뷰를 생성합니다. 

이 뷰는 emp 테이블과 dept 테이블을 결합하여 각 사원의 이름(ename)과 해당 부서 이름(dname)을 조회할 수 있도록 합니다.

emp 테이블의 deptno와 dept 테이블의 deptno 컬럼을 기준으로 조인합니다.

create or replace 문을 사용하면 v_emp 뷰가 이미 존재할 경우, 이를 새로 교체(replace)하여 생성합니다.

### 2. Inline View(인라인 뷰)
```
select e.deptno, d.dname, e.sal
from(select deptno, max(sal) sal
    from emp
    group by deptno) e, dept d
where e.deptno = d.deptno;
```
이 쿼리는 각 부서에서 급여가 가장 높은 사원을 조회하는 것입니다.

내부 서브쿼리 (select deptno, max(sal) sal from emp group by deptno)에서 부서별 최대 급여를 구하고, 그 결과를 외부 쿼리에서 dept 테이블과 조인하여 각 부서 이름(dname)과 해당 부서에서 가장 높은 급여를 가진 사원의 급여(sal)를 조회합니다.

```
select deptno, max(sal)
from emp
group by deptno;
```
emp 테이블에서 각 부서(deptno)별로 최대 급여(max(sal))를 조회하는 쿼리입니다.

group by deptno를 사용하여 부서별로 급여의 최대값을 구합니다.

### 3. DECODE 함수 사용 예시
```
select decode(deptno, ndeptno, null, deptno) deptno, profno, name
from (
    select lag(deptno) over (order by deptno) ndeptno, deptno, profno, name
    from professor
);
```
DECODE 함수: DECODE는 조건에 따라 값을 반환하는 함수입니다. 여기서는 deptno가 ndeptno(이전 deptno)와 동일하면 null을 반환하고, 아니면 원래 deptno 값을 반환하는 구조입니다.

lag(deptno) over (order by deptno)는 창 함수(window function)인 LAG를 사용하여 deptno 컬럼의 이전 값을 ndeptno로 가져옵니다.

LAG 함수는 주어진 순서대로 이전 값을 가져오는 함수로, 여기서는 deptno 컬럼을 기준으로 이전 부서 번호를 조회합니다.

### 4. View 조회 및 삭제하기
```
select view_name, text, read_only
from user_views;
```
이 쿼리는 현재 사용자 계정에서 생성된 뷰(View)의 이름(view_name), SQL 정의(text), 읽기 전용 여부(read_only)를 조회합니다.

user_views는 현재 사용자가 생성한 뷰들의 메타데이터를 저장하는 시스템 뷰입니다.

### 5. 물리화된 뷰(Materialized View)
```
create materialized view m_prof
build immediate
refresh
on demand
complete
enable query rewrite
as
    select profno, name, pay
    from professor;
```
물리화된 뷰(Materialized View) m_prof를 생성합니다.

build immediate: 뷰가 생성되자마자 즉시 데이터를 조회하여 저장합니다.

refresh on demand: 뷰의 데이터는 수동으로 갱신(refresh)할 수 있습니다. 즉, 자동 갱신이 아니라 필요할 때마다 사용자가 갱신을 실행해야 합니다.

complete: 이 옵션은 물리화된 뷰가 데이터를 완전히 다시 조회하여 갱신되도록 설정합니다.

enable query rewrite: 물리화된 뷰에서 데이터를 읽을 때 자동으로 쿼리 리라이트(query rewrite)를 사용하여 최적화된 쿼리를 실행합니다.

select profno, name, pay from professor: professor 테이블에서 profno, name, pay 컬럼을 조회하여 물리화된 뷰로 저장합니다.

```
create index idx_m_prof_pay
on m_prof(pay);
```
m_prof 물리화된 뷰의 pay 컬럼에 인덱스를 생성합니다.

이 인덱스는 물리화된 뷰에서 pay 컬럼을 기준으로 하는 쿼리 성능을 향상시킬 수 있습니다.
