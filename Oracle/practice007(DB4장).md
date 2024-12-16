### 1. 학생 테이블(student)과 학과 테이블(department) 테이블을 사용하여 학생이름, 1전공학과번호(deptno1) , 전공학과이름을 출력하세요.
```
select s.name "stu_name", s.deptno1 "deptno1", d.dname "dept_name"
from student s, department d
where s.deptno1 = d.deptno;
```
tudent 테이블과 department 테이블을 deptno1과 deptno를 기준으로 조인합니다.

s.deptno1 = d.deptno 조건을 사용하여, student 테이블의 deptno1과 department 테이블의 deptno가 일치하는 행을 찾아 연결합니다.

선택된 컬럼은:

student 테이블의 name을 "stu_name"으로,

student 테이블의 deptno1을 "deptno1"으로,

department 테이블의 dname을 "dept_name"으로 반환합니다.

### 2. emp2 테이블과 p_grade 테이블을 조회하여 현재 직급이 있는 사원의 이름과 직급, 현재 연봉, 해당 직급의 연봉의 하한금액과 상한 금액을 출력하세요.
```
select e.name "name", p.position "position", to_char(e.pay, '999,999,999') "pay",
    to_char(p.s_pay, '999,999,999') "low pay", to_char(p.e_pay, '999,999,999') "high pay"
from emp2 e, p_grade p
where e.position = p.position;
```
emp2 테이블과 p_grade 테이블을 position을 기준으로 조인합니다.

e.position = p.position 조건을 사용하여, emp2 테이블의 position과 p_grade 테이블의 position이 일치하는 행을 연결합니다.

선택된 컬럼은:

emp2 테이블의 name을 "name"으로,

emp2 테이블의 pay를 to_char(e.pay, '999,999,999') 형식으로 숫자 포맷을 적용해 "pay"로,

p_grade 테이블의 s_pay를 to_char(p.s_pay, '999,999,999') 형식으로 "low pay"로,

p_grade 테이블의 e_pay를 to_char(p.e_pay, '999,999,999') 형식으로 "high pay"로 반환합니다.

### 3. Emp2 테이블과 p_grade 테이블을 조회하여 사원들의 이름과 나이, 현재 직급, 예상 직급을 출력하세요. 예상 직급은 나이로 계산하며 해당 나이가 받아야 하는 직급을 의미합니다. 나이는 오늘(sysdate)을 기준으로 하되 trunc 로 소수점 이하는 절삭해서 계산하세요.
```
select e.name "name", trunc((sysdate - e.birthday) / 365.0) "age",
    e.position "curr_position", p.position "be_position"
from emp2 e, p_grade p
where trunc((sysdate - e.birthday) / 365.0) between p.s_age and p.e_age;
```
emp2 테이블과 p_grade 테이블을 birthday를 기준으로 연령대에 따라 조인합니다.

trunc((sysdate - e.birthday) / 365.0)는 emp2 테이블의 birthday에서 현재 날짜(sysdate)를 빼서 나이를 계산합니다.

365.0으로 나누어 연도 단위로 나이를 계산하고, trunc() 함수로 소수점을 버립니다.

where 절에서 trunc((sysdate - e.birthday) / 365.0)이 p_grade 테이블의 s_age(최소 나이)와 e_age(최대 나이) 범위에 포함되는지 조건을 설정합니다.

선택된 컬럼은:

emp2 테이블의 name을 "name"으로,

계산된 나이를 "age"로,

emp2 테이블의 position을 "curr_position"으로,

p_grade 테이블의 position을 "be_position"으로 반환합니다.

### 4. customer 테이블과 gift 테이블을 Join하여 고객이 자기 포인트보다 낮은 포인트의 상품 중 한가지를 선택할 수 있다고 할 때 Notebook 을 선택할 수 있는 고객명과 포인트, 상품명을 출력하세요.
```
select c.gname "cust_name", c.point "point", g.gname "gift_name"
from customer c, gift g
where g.g_start <= c.point
and g.gname = 'Notebook';
```
customer 테이블과 gift 테이블을 조인하여, customer의 포인트(c.point)가 gift 테이블의 시작 포인트(g.g_start) 이상인 조건을 설정합니다.

또한 g.gname = 'Notebook' 조건을 추가하여, 상품 이름이 'Notebook'인 상품만 조회합니다.

선택된 컬럼은:

customer 테이블의 gname을 "cust_name"으로,

customer 테이블의 point를 "point"으로,

gift 테이블의 gname을 "gift_name"으로 반환합니다.

### 5. professor 테이블에서 교수의 번호, 교수이름, 입사일, 자신보다 입사일 빠른 사람 인원수를 출력하세요. 단 자신보다 입사일이 빠른 사람수를 오름차순으로 출력하세요.
```
select p1.profno, p1.name, to_char(p1.hiredate, 'yyyy/mm/dd') "hiredate", count(p2.hiredate) "count"
from professor p1, professor p2
where p2.hiredate(+) < p1.hiredate
group by p1.profno, p1.name, p1.hiredate
order by 4;
```
professor 테이블을 두 번 사용하여 자기 자신에 대한 조인(Self Join)을 수행합니다.

p1은 주 테이블, p2는 비교할 대상 테이블입니다.

p2.hiredate(+) < p1.hiredate 조건은 p2의 hiredate가 p1의 hiredate보다 작은 경우에만 일치하도록 합니다.

count(p2.hiredate)는 p2에서 p1보다 먼저 고용된 교수의 수를 셉니다.

group by 절은 p1.profno, p1.name, p1.hiredate에 대해 그룹화하고, 결과를 count에 따라 오름차순으로 정렬합니다.

선택된 컬럼은:

p1.profno, p1.name, p1.hiredate, 그리고 p2.hiredate가 p1.hiredate보다 작은 교수들의 수(count)입니다.

### 6. emp 테이블에서 사원번호, 사원이름, 입사일, 자신보다 먼저 입사한 사람 인원수를 출력하세요. 단 자신보다 입사일이 빠른 사람수를 오름차순으로 출력하세요.
```
select e1.empno, e1.ename, to_char(e1.hiredate, 'yy/mm/dd') "hiredate", count(e2.hiredate) "count"
from emp e1, emp e2
where e2.hiredate(+) < e1.hiredate
group by e1.empno, e1.ename, e1.hiredate
order by 4;
```
emp 테이블을 두 번 사용하여 자기 자신에 대한 조인(Self Join)을 수행합니다.

e1은 주 테이블이고, e2는 비교 대상 테이블입니다.

e2.hiredate(+) < e1.hiredate 조건은 e2의 hiredate가 e1의 hiredate보다 작은 경우에만 일치합니다.

count(e2.hiredate)는 e2에서 e1보다 먼저 고용된 직원의 수를 셉니다.

group by 절은 e1.empno, e1.ename, e1.hiredate에 대해 그룹화하고, 결과를 count에 따라 오름차순으로 정렬합니다.

선택된 컬럼은:

e1.empno, e1.ename, e1.hiredate, 그리고 e2.hiredate가 e1.hiredate보다 작은 직원들의 수(count)입니다.
