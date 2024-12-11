### 1. 전화번호 지역별 통계
```
SELECT COUNT(*)||'EA '||'('||ROUND(COUNT(*)/COUNT(*)*100, 0)||'%)' AS TOTAL,
    COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'02',0))||'EA '||'('||ROUND(COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'02',0))/COUNT(*)*100, 0)||'%)'AS SEOUL,
    COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'031',0))||'EA '||'('||ROUND(COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'031',0))/COUNT(*)*100, 0)||'%)'AS GYEONGGI,
    COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'051',0))||'EA '||'('||ROUND(COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'051',0))/COUNT(*)*100, 0)||'%)'AS BUSAN,
    COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'052',0))||'EA '||'('||ROUND(COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'052',0))/COUNT(*)*100, 0)||'%)'AS ULSAN,
    COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'053',0))||'EA '||'('||ROUND(COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'053',0))/COUNT(*)*100, 0)||'%)'AS DAEGU,
    COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'055',0))||'EA '||'('||ROUND(COUNT(DECODE(SUBSTR(TEL,1,INSTR(TEL,')')-1),'055',0))/COUNT(*)*100, 0)||'%)'AS GYEONGNAM
FROM STUDENT;
```
**SUBSTR(), INSTR()**: SUBSTR()은 문자열의 부분을 추출하고, INSTR()은 특정 문자의 위치를 찾는 함수입니다. 여기서는 전화번호(TEL)에서 지역 코드 부분을 추출합니다.

**DECODE()**: DECODE 함수는 지역 코드별로 해당하는 값을 카운트합니다. 예를 들어, 서울의 지역 코드 '02'에 해당하는 전화번호를 카운트합니다.

**COUNT()**: 각 지역별 전화번호 개수를 카운트하고, 그 비율도 함께 계산하여 출력합니다.

### 2. 부서별 급여 합계 누적 조회
```
select deptno, ename, sal, sum(sal) over(partition by deptno order by sal) as total
from emp;
```
**SUM() OVER**: OVER 절을 사용하여 윈도우 함수로 급여 합계를 구합니다. PARTITION BY로 부서(deptno)별로 그룹화하고, ORDER BY로 급여(sal)를 기준으로 정렬하여 누적 급여 합계를 계산합니다.

**PARTITION BY deptno**: 부서별로 그룹화하여 급여의 합계를 계산합니다.

### 3. 각 사원의 급여액이 전체 직원 급여총액에서 몇 %의 비율을 차지하는지 출력
```
SELECT deptno, ename, sal,
    sum(sal) OVER() AS total_sal,
    ROUND(sal / sum(sal) OVER() * 100, 2) AS "%"
FROM emp
ORDER BY 5 DESC;
```
**SELECT deptno, ename, sal**: emp 테이블에서 부서 번호(deptno), 직원 이름(ename), 급여(sal)를 선택합니다.

**sum(sal) OVER()**: OVER() 절은 윈도우 함수로, 전체 테이블에서 급여(sal)의 합계를 계산합니다. PARTITION BY나 ORDER BY가 없으므로 모든 직원의 급여 합계가 동일하게 계산됩니다. 이 값은 total_sal로 표시됩니다.

**ROUND(sal / sum(sal) OVER() * 100, 2)**: 각 직원의 급여가 전체 급여 합계에서 차지하는 비율을 계산합니다. 이를 퍼센트로 표시하기 위해 급여를 전체 급여 합계로 나누고 100을 곱합니다. ROUND() 함수는 소수점 두 자리로 반올림하여 출력합니다. 이 값은 %라는 별칭으로 표시됩니다.

**ORDER BY 5 DESC**: ORDER BY 5는 SELECT 절에서 다섯 번째 컬럼인 %로 결과를 내림차순 정렬합니다. 즉, 급여 비율이 높은 순서대로 직원들을 정렬합니다.

### 4. 각 직원들의 급여가 해당 부서 합계금액에서 몇 %의 비중을 차지하는지를 출력
```
SELECT deptno, ename, sal, 
    SUM(sal) OVER(PARTITION BY deptno) AS sum_dept,
    ROUND(sal / SUM(sal) OVER(PARTITION BY deptno) * 100, 2) AS "%"
FROM emp
GROUP BY deptno, ename, sal
ORDER BY 1;
```
**SELECT deptno, ename, sal**: emp 테이블에서 부서 번호(deptno), 직원 이름(ename), 급여(sal)를 선택합니다.

**SUM(sal) OVER(PARTITION BY deptno)**: PARTITION BY deptno는 부서별로 그룹을 나누어, 각 부서 내에서 급여의 합계(sum_dept)를 계산합니다. 각 부서마다 급여 합계가 계산됩니다.

**ROUND(sal / SUM(sal) OVER(PARTITION BY deptno) * 100, 2)**: 각 직원의 급여가 속한 부서에서의 총 급여에서 차지하는 비율을 계산합니다. 부서별 급여 합계로 각 직원의 급여를 나누고 100을 곱하여 퍼센트 값을 계산한 후, ROUND() 함수로 소수점 두 자리까지 반올림합니다.

**GROUP BY deptno, ename, sal**: GROUP BY는 부서 번호(deptno), 직원 이름(ename), 급여(sal)를 기준으로 그룹화합니다. 하지만 이 경우 GROUP BY는 윈도우 함수와 함께 사용되므로 모든 행이 하나씩 포함됩니다. 일반적으로 윈도우 함수와 함께 GROUP BY는 비효율적일 수 있습니다.

**ORDER BY 1**: 첫 번째 컬럼인 deptno(부서 번호) 기준으로 결과를 오름차순 정렬합니다.

### 5. loan 테이블을 사용하여 1000번 지점의 대출일자, 대출종목코드, 대출건수, 대출총액, 누적대출금액을 출력
```
SELECT L_DATE AS "대출일자",
    L_CODE AS "대출종목코드",
    L_QTY AS "대출건수",
    L_TOTAL AS "대출총액",
    SUM(L_TOTAL) OVER(ORDER BY L_DATE) AS "누적대출금액"
FROM loan
WHERE L_STORE = 1000;
```
**SELECT L_DATE AS "대출일자", L_CODE AS "대출종목코드", L_QTY AS "대출건수", L_TOTAL AS "대출총액"**: loan 테이블에서 대출일자(L_DATE), 대출종목코드(L_CODE), 대출건수(L_QTY), 대출총액(L_TOTAL)을 선택합니다. 이 컬럼들은 각각 "대출일자", "대출종목코드", "대출건수", "대출총액"이라는 별칭을 부여합니다.

**SUM(L_TOTAL) OVER(ORDER BY L_DATE)**: L_DATE(대출일자) 순으로 데이터를 정렬하고, 해당 순서에 따라 L_TOTAL(대출총액)의 누적 합계를 계산합니다. 즉, 각 대출 일자별로 이전의 모든 대출액을 누적한 금액을 구합니다.

**WHERE L_STORE = 1000**: L_STORE = 1000 조건을 추가하여 대출 지점이 1000인 대출만을 필터링합니다.

### 6. loan 테이블을 사용하여 전체 지점의 대출종목코드, 대출지점, 대출일자, 대출건수, 대출액을 대출코드와 대출지점별로 누적합계를 출력
```
SELECT L_CODE AS "대출종목코드",
    L_STORE AS "대출지점",
    L_DATE AS "대출일자",
    L_QTY AS "대출건수",
    L_TOTAL AS "대출액",
    SUM(L_TOTAL) OVER(PARTITION BY L_CODE, L_STORE ORDER BY L_DATE) AS "누적대출금액"
FROM loan;
```
**SELECT L_CODE AS "대출종목코드", L_STORE AS "대출지점", L_DATE AS "대출일자", L_QTY AS "대출건수", L_TOTAL AS "대출액"**: loan 테이블에서 대출종목코드(L_CODE), 대출지점(L_STORE), 대출일자(L_DATE), 대출건수(L_QTY), 대출액(L_TOTAL)을 선택하고, 각 컬럼에 별칭을 지정합니다.

**SUM(L_TOTAL) OVER(PARTITION BY L_CODE, L_STORE ORDER BY L_DATE)**: PARTITION BY L_CODE, L_STORE는 대출종목코드(L_CODE)와 대출지점(L_STORE)별로 그룹을 나누어, 각 그룹 내에서 L_DATE(대출일자) 순으로 대출액(L_TOTAL)의 누적 합계를 계산합니다. 즉, 같은 대출 종목과 대출 지점 내에서 대출액이 누적되는 방식입니다.
