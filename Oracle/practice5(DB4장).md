### 1. Join 문법
```
select e.empno, e.ename, d.dname
from emp e, dept d
where e.deptno = d.deptno;
```
emp 테이블과 dept 테이블을 deptno 컬럼을 기준으로 조인하여, empno, ename, 그리고 dname 값을 반환합니다.

e는 emp 테이블에 대한 별칭이고, d는 dept 테이블에 대한 별칭입니다.

e.deptno = d.deptno 조건을 사용하여 두 테이블을 deptno 컬럼을 기준으로 연결합니다.

### 2. 학생 테이블(student)과 교수 테이블(professor)을 join하여 학생의 이름과 지도교수번호, 지도교수 이름을 출력
```
select s.name "STU_NAME", p.name "PROF_NAME"
from student s, professor p
where s.profno = p.profno;
```
student 테이블과 professor 테이블을 profno 컬럼을 기준으로 조인합니다.

s.profno = p.profno 조건을 사용하여 두 테이블을 연결하고, 각 테이블의 name 컬럼을 STU_NAME과 PROF_NAME이라는 별칭을 붙여 선택합니다.

### 3. 학생 테이블(student)과 학과 테이블(department), 교수 테이블(professor)을 Join하여 학생의 이름과 학생의 학과이름, 학생의 지도교수 이름을 출력
```
select s.name "STU_NAME", d.dname "DEPT_NAME", p.name "PROF_NAME"
from student s, department d, professor p
where s.deptno1 = d.deptno
and s.profno = p.profno;
```
세 개의 테이블 (student, department, professor)을 deptno1과 profno를 기준으로 각각 조인합니다.

s.deptno1 = d.deptno와 s.profno = p.profno 조건을 사용하여 student 테이블을 department와 professor 테이블과 연결합니다.

선택된 컬럼은 student의 name을 STU_NAME으로, department의 dname을 DEPT_NAME으로, professor의 name을 PROF_NAME으로 반환합니다.

### 4.  student 테이블을 조회하여 1전공(deptno1)이 101번인 학생들의 이름과 각 학생들의 지도교수 번호와 지도교수 이름을 출력
```
select s.name "STU_NAME", p.name "PROF_NAME"
from student s, professor p
where s.profno = p.profno
and s.deptno1 = 101;
```
student 테이블과 professor 테이블을 profno 컬럼을 기준으로 조인합니다.

추가로 s.deptno1 = 101 조건을 사용하여, student 테이블에서 deptno1 값이 101인 레코드만 선택합니다.

결과로 student 테이블의 name을 STU_NAME으로, professor 테이블의 name을 PROF_NAME으로 선택합니다.
