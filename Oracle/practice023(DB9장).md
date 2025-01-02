### 연습 문제 1.
Professor 테이블과 department 테이블을 조인하여 교수 번호와 교수 이름, 소속 학과 이름을 조회하는 view를 생성하세요. View 이름은 v_prof_dept2로 하세요.
```
create or replace view v_prof_dept2
as
    select profno, name, dname
    from professor p, department d
    where p.deptno = d.deptno;
```
create or replace view v_prof_dept2: v_prof_dept2라는 뷰를 생성하거나 이미 존재하는 뷰를 덮어쓰는 명령어입니다.

select profno, name, dname: 교수 번호(profno), 교수 이름(name), 학과 이름(dname)을 조회합니다.

from professor p, department d: professor 테이블을 p라는 별칭으로, department 테이블을 d라는 별칭으로 사용합니다.

where p.deptno = d.deptno: 교수의 소속 학과 번호(deptno)와 학과 번호(deptno)가 일치하는 교수와 학과만 조회합니다.

### 연습 문제 2.
Inline View를 사용하여 Student 테이블과 department 테이블을 이용하여 학과별로 학생들의 최대 키와 최대 몸무게, 학과 이름을 출력하세요.
```
select d.dname, max_height, max_weight
from (select deptno1, max(height) max_height, max(weight) max_weight
      from student
      group by deptno1) s, department d
where s.deptno1 = d.deptno;
```
select d.dname, max_height, max_weight: department 테이블에서 학과 이름(dname)을 조회하고, Inline View에서 최대 키(max_height)와 최대 몸무게(max_weight)를 조회합니다.

(select deptno1, max(height) max_height, max(weight) max_weight from student group by deptno1) s:
student 테이블에서 각 학과(deptno1)별로 최대 키와 몸무게를 구하는 서브쿼리입니다. 이 서브쿼리 결과는 s라는 별칭을 붙여 Inline View로 사용됩니다.

from department d: department 테이블을 사용하여 학과 이름을 조회합니다.

where s.deptno1 = d.deptno: student 테이블의 deptno1과 department 테이블의 deptno를 일치시켜, 각 학과에 대한 정보를 연결합니다.

### 연습 문제 3.
Student 테이블과 department 테이블을 사용하여 학과 이름, 학과별 최대 키, 학과별로 가장 키가 큰 학생들의 이름과 키를 Inline View를 사용하여 출력하세요.
```
select d.dname, a.max_height, s.name, s.height
FROM (SELECT deptno1, max(height) max_height
      FROM student
      group by deptno1) a, student s, department d
where s.deptno1 = a.deptno1
  and s.height = a.max_height
  and s.deptno1 = d.deptno;
```
select d.dname, a.max_height, s.name, s.height: 학과 이름(dname), 최대 키(max_height), 학생 이름(name), 학생의 키(height)를 조회합니다.

(SELECT deptno1, max(height) max_height FROM student group by deptno1) a:
student 테이블에서 각 학과별로 최대 키를 구하는 서브쿼리입니다.
이 서브쿼리 결과는 a라는 별칭을 붙여 Inline View로 사용됩니다.

from student s, department d: student 테이블과 department 테이블을 사용하여 데이터를 결합합니다.

where s.deptno1 = a.deptno1: student 테이블의 deptno1과 Inline View의 deptno1을 일치시켜, 학과별로 데이터를 결합합니다.

and s.height = a.max_height: 해당 학과에서 키가 최대인 학생을 찾기 위해, student 테이블의 키(height)가 Inline View에서 계산한 최대 키(max_height)와 일치하는지 확인합니다.

and s.deptno1 = d.deptno: student 테이블과 department 테이블을 학과 번호(deptno1, deptno)를 기준으로 조인하여, 각 학과의 이름을 조회합니다.

### 연습 문제 4.
Student 테이블에서 학생의 키가 동일 학년의 평균 키보다 큰 학생들의 학년과 이름과 키, 해당 학년의 평균 키를 출력하되 Inline View를 사용해서 출력하세요. (학년 컬럼으로 오름차순 정렬해서 출력하세요.) 
```
select s.grade, s.name, s.height, a.avg_height
from (SELECT grade, avg(height) avg_height
      FROM student
      group by grade) a, student s
where a.grade = s.grade
  and s.height > a.avg_height
ORDER BY 1;
```
select s.grade, s.name, s.height, a.avg_height: 학생의 학년(grade), 이름(name), 키(height), 해당 학년의 평균 키(avg_height)를 조회합니다.

(SELECT grade, avg(height) avg_height FROM student group by grade) a:
student 테이블에서 각 학년별 평균 키를 계산하는 서브쿼리입니다.
이 서브쿼리 결과는 a라는 별칭을 붙여 Inline View로 사용됩니다.

from student s: student 테이블에서 학생 정보를 조회합니다.

where a.grade = s.grade: student 테이블과 Inline View에서 학년(grade)을 기준으로 데이터를 결합합니다.

and s.height > a.avg_height: 학생의 키가 해당 학년의 평균 키보다 큰 경우만 조회합니다.

ORDER BY 1: 결과를 학년(grade) 순으로 정렬합니다.

### 연습 문제 5.
professor 테이블을 조회하여 교수들의 급여 순위와 이름과 급여를 출력하세오. 단, 급여 순위는 급여가 많은 사람부터 1위~5위까지 출력하세요.
```
select rownum "ranking", name, pay
from (SELECT name, pay
      FROM professor
      ORDER by 2 desc)
where rownum between 1 and 5;
```
select rownum "ranking", name, pay: rownum을 사용하여 급여순위를 매기고, 교수 이름(name)과 급여(pay)를 조회합니다.

(SELECT name, pay FROM professor ORDER by 2 desc):
professor 테이블에서 교수 이름과 급여를 조회하고, 급여를 기준으로 내림차순(desc)으로 정렬합니다.

where rownum between 1 and 5: rownum을 이용해 1위에서 5위까지의 교수만 조회합니다.

### 연습 문제 6.
교수 테이블을 교수 번호로 정렬한 후 출력하되 3건씩 분리해서 급여 합계와 급여 평균을 출력하세요.
```
select num, profno, name, pay, sum(pay), round(avg(pay), 1)
from (select profno, name, pay, rownum num
      from professor)
group by ceil(num/3), rollup((profno, name, pay, num))
ORDER BY ceil(num/3);
```
select num, profno, name, pay, sum(pay), round(avg(pay), 1): 각 그룹의 순번(num), 교수 번호(profno), 교수 이름(name), 급여(pay), 급여의 합(sum(pay)), 급여의 평균(avg(pay))을 조회합니다.

(select profno, name, pay, rownum num from professor): professor 테이블에서 교수 번호, 이름, 급여를 조회하고, rownum을 사용해 각 행에 번호를 매깁니다. 이 서브쿼리 결과를 num으로 사용합니다.

group by ceil(num/3), rollup((profno, name, pay, num)):

ceil(num/3)는 rownum을 3명씩 그룹으로 묶기 위한 계산식입니다. ceil(num/3)는 num을 3으로 나누고 그 값을 올림 처리하여 3명의 교수씩 묶습니다.

rollup((profno, name, pay, num))은 롤업을 사용하여 그룹별 집계 결과를 포함한 합계 및 평균을 출력합니다. 이 구문은 각 교수별로 급여를 표시하는 것 외에, 그룹별 합계와 평균을 계산하여 마지막에 합계 및 평균을 출력하는 데 사용됩니다.

예를 들어, rollup은 3명의 교수 정보를 출력한 후, 그 3명에 대한 급여의 합계 및 평균을 출력하고, 마지막에는 전체 급여의 합계와 평균을 출력할 수 있습니다.

ORDER BY ceil(num/3):결과를 ceil(num/3)에 의해 묶인 그룹 단위로 정렬합니다. 3명씩 분리된 그룹이 순차적으로 출력됩니다.
