### 1. EMP 테이블에 데이터 삽입
```
INSERT INTO EMP(EMPNO,DEPTNO,ENAME,SAL)
2 VALUES(1000,10,'TIGER',3600);
INSERT INTO EMP(EMPNO,DEPTNO,ENAME,SAL)
2 VALUES(2000,30,'CAT',3000);
COMMIT;
```
INSERT INTO: EMP 테이블에 새로운 데이터를 삽입합니다. 직원 번호(EMPNO), 부서 번호(DEPTNO), 이름(ENAME), 급여(SAL)를 입력합니다.

COMMIT: 트랜잭션을 커밋하여 변경 사항을 영구적으로 저장합니다.

### 2. 페이지 크기 설정 및 데이터 조회
```
SET PAGESIZE 50
SELECT EMPNO,ENAME,JOB,SAL 
FROM EMP;
```
SET PAGESIZE 50: 이 명령어는 출력되는 결과의 페이지 크기를 설정합니다. 여기서는 50개의 행이 한 페이지로 표시됩니다. 이 설정은 주로 SQL*Plus나 Oracle SQL Developer와 같은 툴에서 출력 형식을 제어할 때 사용됩니다.

SELECT EMPNO, ENAME, JOB, SAL FROM EMP: EMP 테이블에서 직원 번호(EMPNO), 이름(ENAME), 직무(JOB), 급여(SAL)를 조회합니다.

### 3. EMP 테이블에 데이터 삽입
```
INSERT INTO EMP(EMPNO,DEPTNO,ENAME,SAL)
VALUES(1000,10,'TIGER',3600);
INSERT INTO EMP(EMPNO,DEPTNO,ENAME,SAL)
VALUES(2000,30,'CAT',3000);
```
INSERT INTO: 이 명령어는 EMP 테이블에 새로운 데이터를 삽입하는 구문입니다.

VALUES: 삽입할 데이터입니다. 여기서는 직원 번호(EMPNO), 부서 번호(DEPTNO), 이름(ENAME), 급여(SAL)를 각각 입력합니다.

### 4. 직원 정보 조회 및 정렬
```
SELECT EMPNO, ENAME, JOB, SAL, DEPTNO
FROM EMP 
ORDER BY EMPNO;
```
ORDER BY EMPNO: EMPNO(직원 번호)를 기준으로 결과를 오름차순으로 정렬합니다.

SELECT: EMP 테이블에서 직원 번호, 이름, 직무, 급여, 부서 번호를 조회합니다.

### 5. DECODE 함수와 ROLLUP을 사용하여 집계 및 그룹화
```
SELECT DEPTNO,
    SUM(DECODE(JOB,'CLERK',SAL,0))AS"CLERK",
    SUM(DECODE(JOB,'MANAGER',SAL,0))AS"MANAGER",
    SUM(DECODE(JOB,'PRESIDENT',SAL,0))AS"PRESIDENT",
    SUM(DECODE(JOB,'ANALYST',SAL,0))AS"ANALYST",
    SUM(DECODE(JOB,'SALESMAN',SAL,0))AS"SALESMAN",
    SUM(NVL2(JOB,SAL,0))AS"TOTAL"
FROM EMP
GROUP BY ROLLUP(DEPTNO);
```
DECODE(): DECODE 함수는 조건에 따라 값을 반환하는 함수입니다. 여기서 직무(JOB)가 각각 'CLERK', 'MANAGER', 'PRESIDENT', 'ANALYST', 'SALESMAN'일 경우 해당 직무의 급여(SAL)를 합산하고, 그렇지 않으면 0을 반환하여 직무별 급여 합계를 계산합니다.

NVL2(): NVL2는 첫 번째 인자가 NULL이 아니면 두 번째 값을 반환하고, NULL이면 세 번째 값을 반환합니다. 여기서는 JOB이 존재할 경우 급여를 반환하고, 그렇지 않으면 0을 반환하여 전체 급여 합계를 계산합니다.

ROLLUP(): ROLLUP은 GROUP BY와 함께 사용하여 계층적인 집계 결과를 제공합니다. DEPTNO에 대해 각 부서별로 집계를 수행하고, ROLLUP이 추가로 전체 합계를 구합니다.

### 6. SUM을 사용한 윈도우 함수 예시
```
SELECT DEPTNO,ENAME,SAL,
    SUM(SAL) OVER(ORDER BY SAL) AS "TOTAL"
FROM EMP;
```
SUM() OVER: 윈도우 함수로, SAL(급여)의 합계를 구합니다. 이 함수는 ORDER BY 절로 급여를 오름차순으로 정렬한 후, 각 행에 대해 급여 합계를 계산합니다. 즉, 급여가 작은 직원부터 누적 합계가 계산됩니다.

OVER(ORDER BY SAL): SAL을 기준으로 정렬한 뒤 각 행에 대해 합계를 계산하는 윈도우 함수입니다.

### 7. 부서별 평균 급여 및 직원 수 조회
```
SELECT DEPTNO, NULL JOB, ROUND(AVG(SAL),1) AVG_SAL, COUNT(*) CNT_EMP
FROM EMP
GROUP BY DEPTNO
ORDER BY DEPTNO, JOB;
```
AVG(SAL): SAL(급여)의 평균을 계산합니다.

COUNT(*): 부서별로 직원 수를 계산합니다.

ROUND(AVG(SAL), 1): 평균 급여를 소수점 첫째 자리까지 반올림합니다.

GROUP BY DEPTNO: 부서 번호(DEPTNO)별로 데이터를 그룹화합니다. 각 부서의 평균 급여와 직원 수를 조회합니다.

### 8. DECODE 함수로 가격 계산
```
select max(decode(name, 'apple', price)) as apple,
    max(decode(name, 'grape', price)) as grape,
    max(decode(name, 'orange', price)) as orange
from fruit;
```
DECODE(): DECODE 함수는 조건에 맞는 값을 반환합니다. 여기서는 name이 'apple', 'grape', 'orange'일 때 해당하는 가격(price)을 반환합니다.

MAX(): MAX() 함수는 최대값을 구하는 집계 함수로, DECODE 함수의 결과 중 가장 큰 값을 반환합니다. 따라서 각 과일별로 가장 높은 가격을 조회합니다.
