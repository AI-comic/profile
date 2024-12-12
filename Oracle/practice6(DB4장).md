### 1. Custormer 테이블과 gift 테이블을 Join하여 고객별로 마일리지 포인트를 조회한 후 해당 마일리지 점수로 받을 수 있는 상품을 조회하여 고객의 이름과 받을 수 있는 상품 명을 출력
```
select c.gname "cust_name", to_char(c.point, '999,999') "point", g.gname "gift_name"
from customer c, gift g
where c.point between g.g_start and g.g_end;
```
customer 테이블과 gift 테이블을 조인하여, point 값이 gift 테이블의 g_start와 g_end 사이에 포함되는 레코드를 조회합니다.

to_char(c.point, '999,999')는 customer 테이블의 point 값을 숫자 형식으로 999,999 형태로 포맷합니다.

선택되는 컬럼은 customer 테이블의 gname을 "cust_name"으로, point 값을 999,999 형식으로, gift 테이블의 gname을 "gift_name"으로 반환합니다.

### 2. Student 테이블과 score 테이블 , hakjum 테이블을 조회하여 학생들의 이름과 점수와 학점을 출력
```
select s.name "stu_name", o.total "score", h.grade "credit"
from student s, score o, hakjum h
where s.studno = o.studno
and o.total >= h.min_point
and o.total <= h.max_point;
```
student, score, hakjum 세 테이블을 조인합니다.

s.studno = o.studno 조건을 통해 student와 score 테이블을 연결하고, o.total >= h.min_point와 o.total <= h.max_point 조건을 통해 score 테이블의 total 값이 hakjum 테이블의 min_point와 max_point 범위에 해당하는지 확인합니다.

선택된 컬럼은 student 테이블의 name을 "stu_name"으로, score 테이블의 total을 "score"로, hakjum 테이블의 grade를 "credit"으로 반환합니다.

### 3. 왼쪽 외부 조인 (LEFT OUTER JOIN)
이 쿼리는 LEFT OUTER JOIN으로, 왼쪽 테이블(student)의 모든 행을 유지하고, 오른쪽 테이블(professor)에서 일치하는 값이 없으면 해당 값은 NULL로 처리합니다.
```
select s.name "stu_name", p.name "prof_name"
from student s, professor p
where s.profno = p.profno(+);
```
student 테이블과 professor 테이블을 profno를 기준으로 왼쪽 외부 조인(Left Outer Join)합니다.

s.profno = p.profno(+)에서 (+)는 Oracle에서 왼쪽 외부 조인을 나타내는 기호입니다. 이 기호는 student 테이블에 있는 모든 학생을 반환하며, professor 테이블에 해당 교수 정보가 없으면 prof_name 컬럼에 NULL 값이 반환됩니다.

선택된 컬럼은 student 테이블의 name을 "stu_name"으로, professor 테이블의 name을 "prof_name"으로 반환합니다.

### 4. 오른쪽 외부 조인 (RIGHT OUTER JOIN)
이 쿼리는 RIGHT OUTER JOIN으로, 오른쪽 테이블(professor)의 모든 행을 유지하고, 왼쪽 테이블(student)에서 일치하는 값이 없으면 해당 값은 NULL로 처리합니다.
```
select s.name "stu_name", p.name "prof_name"
from student s, professor p
where s.profno(+) = p.profno;
```
student 테이블과 professor 테이블을 profno를 기준으로 오른쪽 외부 조인(Right Outer Join)합니다.

s.profno(+) = p.profno에서 (+)는 Oracle에서 오른쪽 외부 조인을 나타내는 기호입니다. 이 기호는 professor 테이블에 있는 모든 교수 정보를 반환하며, student 테이블에 해당 학생 정보가 없으면 stu_name 컬럼에 NULL 값이 반환됩니다.

선택된 컬럼은 student 테이블의 name을 "stu_name"으로, professor 테이블의 name을 "prof_name"으로 반환합니다.

### 5. 양쪽 외부 조인 (FULL OUTER JOIN)
이 쿼리는 FULL OUTER JOIN으로, 양쪽 테이블에서 일치하지 않는 모든 행을 포함하고, 일치하는 값이 없으면 해당 값은 NULL로 처리합니다.
```
select s.name "stu_name", p.name "prof_name"
from student s, professor p
where s.profno = p.profno(+)
union
select s.name "stu_name", p.name "prof_name"
from student s, professor p
where s.profno(+) = p.profno;
```
양쪽 외부 조인(Full Outer Join)을 구현합니다.

양쪽 외부 조인은 LEFT OUTER JOIN과 RIGHT OUTER JOIN의 결과를 결합하여 양쪽 테이블에서 일치하지 않는 모든 행을 반환합니다.

첫 번째 쿼리에서는 s.profno = p.profno(+)를 사용하여 왼쪽 외부 조인 결과를 가져오고, 두 번째 쿼리에서는 s.profno(+) = p.profno를 사용하여 오른쪽 외부 조인 결과를 가져옵니다.

UNION 연산자를 사용하여 두 결과를 합칩니다. 이렇게 하면 두 테이블에서 일치하지 않는 모든 행을 포함하는 결과가 반환됩니다.

### 6. 자기 자신에 대한 조인 (Self Join)
이 쿼리는 자기 자신을 조인하는 방식으로, 각 직원과 그 직원의 관리자 정보를 함께 반환합니다.
```
SELECT e1.ename "ename", e2.ename "mgr_ename"
from emp e1, emp e2
where e1.mgr = e2.empno;
```
emp 테이블을 두 번 사용하여 자기 자신에 대한 조인(Self Join)을 수행합니다.

e1과 e2는 모두 emp 테이블을 참조하는 별칭입니다.

e1.mgr = e2.empno 조건을 사용하여 emp 테이블의 mgr 컬럼(관리자 번호)을 기준으로 자기 자신을 조인합니다.

즉, e1은 직원이고, e2는 그 직원의 관리자를 나타냅니다.

선택된 컬럼은 e1.ename은 직원의 이름을 나타내고, e2.ename은 해당 직원의 관리자 이름을 나타냅니다.
