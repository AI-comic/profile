### 1. LAG 함수
LAG(): LAG 함수는 윈도우 함수로, 결과 집합에서 이전 행의 값을 조회할 수 있게 해줍니다.

이 함수는 JOIN 없이 바로 이전 행의 데이터를 참조할 수 있습니다.

lag(컬럼명, 오프셋, 기본값) over (order by 컬럼).

이 구문은 sal(급여) 값을 이전 행에서 가져옵니다.

만약 이전 값이 없다면 기본값인 0을 반환합니다
```
select ename, hiredate, sal, lag(sal,1,0)over (order by hiredate) "LAG"
from emp;
```
이 쿼리는 직원의 이름(ename), 입사일(hiredate), 급여(sal), 그리고 이전 직원의 급여(LAG)를 조회합니다.

급여는 입사일(hiredate)을 기준으로 정렬됩니다.

### 2. PIVOT
PIVOT: PIVOT은 집계 결과를 열로 변환하는 기능입니다.

여기서는 각 직무(job)별로 직원 수(empno의 개수)를 집계하여 각 직무가 열로 변환됩니다.

PIVOT(count(컬럼명) for 컬럼 in (값 목록)).

job에 대한 각 값을 열로 변환하고 각 직무에 대해 직원 수를 계산합니다.
```
select * from(select deptno, job, empno from emp)
pivot(count(empno) for job in ('CLERK' as "CLERK", 'MANAGER' AS "MANAGER", 'PRESIDENT' AS "PRESIDENT", 'ANALYST' AS "ANSLYST", 'SALESMAN' AS "SALESMAN"))
order by deptno;
```
deptno별로 직무별 직원 수가 표시됩니다.

### 3. 최대/최소 급여 조회
MAX()와 MIN(): MAX()는 최대값을, MIN()은 최소값을 반환하는 집계 함수입니다.

이 쿼리는 emp 테이블에서 급여(sal)의 최대값과 최소값을 조회합니다.
```
select max(sal)as"MAX", min(sal)AS"MIN"
from emp;
```
전체 직원의 급여 중 최고액과 최저액이 반환됩니다.

### 4. 급여와 커미션의 최대/최소/평균 계산
NVL(): NVL() 함수는 NULL 값을 대체하는 함수입니다. COMM(커미션)이 NULL인 경우 0으로 처리합니다.

AVG(): AVG()는 평균값을 계산합니다.
```
SELECT max(SAL+NVL(COMM,0))AS "MAX" , MIN(SAL+NVL(COMM,0))AS"MIN", ROUND(AVG(SAL+NVL(COMM,0)),1)AS"AVG"
FROM EMP;
```
급여(SAL)와 커미션(COMM)을 합산하여, 그 합의 최대값, 최소값, 평균을 각각 계산합니다.

### 5. 학생 생일 조회
```
SELECT BIRTHDAY
FROM STUDENT;
```
STUDENT 테이블에서 모든 학생의 생일(BIRTHDAY)을 조회합니다.

### 6. 월별 생일 인원 수
DECODE(): DECODE() 함수는 조건에 따라 다른 값을 반환하는 함수입니다. 여기서는 BIRTHDAY의 월을 기준으로 각 월에 해당하는 생일 수를 카운트합니다.

TO_CHAR(): TO_CHAR()는 날짜를 문자열로 변환하는 함수입니다. 이를 통해 BIRTHDAY에서 월(MM)을 추출하여 월별 생일 인원수를 계산합니다.
```
SELECT COUNT(*) ||'EA' AS "TOTAL",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '01', 0))||'EA' AS "JAN",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '02', 0))||'EA' AS "FEB",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '03', 0))||'EA' AS "MAR",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '04', 0))||'EA' AS "APR",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '05', 0))||'EA' AS "MAY",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '06', 0))||'EA' AS "JUN",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '07', 0))||'EA' AS "JUL",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '08', 0))||'EA' AS "AUG",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '09', 0))||'EA' AS "SEP",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '10', 0))||'EA' AS "OCT",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '11', 0))||'EA' AS "NOV",
        COUNT(DECODE(TO_CHAR(BIRTHDAY,'MM'), '12', 0))||'EA' AS "DEC"
FROM STUDENT;
```
각 월별 생일자 수를 출력하며, 총 인원수도 포함됩니다.

### 7. 전화번호 지역별 분포
SUBSTR(): SUBSTR() 함수는 문자열의 일부를 추출하는 함수입니다. 여기서는 TEL에서 지역 코드 부분을 추출합니다.

INSTR(): INSTR() 함수는 문자열 내에서 특정 문자의 위치를 반환합니다. TEL에서 지역 코드의 끝을 찾는 데 사용됩니다.

DECODE(): DECODE() 함수는 전화번호 앞자리가 특정 값일 때 카운트를 합니다. 각 지역 코드에 맞는 전화를 분류하여 카운트합니다.
```
SELECT COUNT(*) AS "TOTAL", 
        COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'02',0)) AS "SEOUL",
        COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'031',0)) AS "GYEONGGI",
        COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'051',0)) AS "BUSAN",
        COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'052',0)) AS "ULSAN",
        COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'053',0)) AS "DAEGU",
        COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'055',0)) AS "GYEONGNAM"
FROM STUDENT;
```
각 지역별 전화번호 수를 카운트하여 출력합니다.
