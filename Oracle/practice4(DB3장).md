### 1. loan 테이블을 조회하여 1000번 지점의 대출 내역을 대출 코드별로 합쳐서 대출일자, 대출구분코드, 대출건수, 대출총액, 코드별 누적대출금액을 출력
```
SELECT L_DATE AS "대출일자",
    L_CODE AS "대출종목코드",
    L_QTY AS "대출건수",
    L_TOTAL AS "대출총액",
    SUM(L_TOTAL) OVER(PARTITION BY L_CODE ORDER BY L_TOTAL) AS "누적대출금액"
FROM loan
WHERE L_STORE = 1000;
```
**SELECT L_DATE AS "대출일자", L_CODE AS "대출종목코드", L_QTY AS "대출건수", L_TOTAL AS "대출총액"**: loan 테이블에서 대출일자(L_DATE), 대출종목코드(L_CODE), 대출건수(L_QTY), 대출총액(L_TOTAL)을 선택합니다. 각 컬럼에는 한글 별칭이 부여됩니다.

**SUM(L_TOTAL) OVER(PARTITION BY L_CODE ORDER BY L_TOTAL)**: PARTITION BY L_CODE는 대출종목코드(L_CODE)별로 그룹을 나누어 각 대출종목코드에 대한 누적 대출 금액을 계산합니다. ORDER BY L_TOTAL은 각 대출종목코드 그룹 내에서 대출액(L_TOTAL)을 기준으로 오름차순으로 정렬하여 누적합을 계산합니다. 즉, 대출 종목 내에서 대출액이 증가하는 순서대로 누적합을 계산합니다.

**WHERE L_STORE = 1000**: L_STORE = 1000 조건으로, 대출 지점이 1000인 데이터만 필터링합니다.

### 2. professor 테이블에서 각 교수들의 급여를 구하고 각 교수의 급여액이 전체 교수의 급여 합계에서 차지하는 비율을 출력
```
SELECT deptno, name, pay, 
    SUM(pay) OVER() AS "TOTAL PAY",
    ROUND(SUM(pay) / SUM(pay) OVER() * 100, 2) AS "RATIO %"
FROM professor
GROUP BY deptno, name, pay
ORDER BY 5 DESC;
```
**SELECT deptno, name, pay**: professor 테이블에서 부서 번호(deptno), 교수 이름(name), 급여(pay)를 선택합니다.

**SUM(pay) OVER()**: OVER() 절에 PARTITION BY가 없으므로 전체 데이터에 대해 급여(pay)의 총합을 계산합니다. 즉, 모든 교수의 급여 합계를 TOTAL PAY라는 별칭으로 계산합니다.

**ROUND(SUM(pay) / SUM(pay) OVER() * 100, 2) AS "RATIO %"**: 각 교수의 급여가 전체 급여에서 차지하는 비율을 계산합니다. 급여를 전체 급여 합계로 나누고 100을 곱하여 퍼센트로 계산합니다. ROUND() 함수는 소수점 두 자리로 반올림합니다.

**GROUP BY deptno, name, pay**: GROUP BY는 부서 번호(deptno), 교수 이름(name), 급여(pay)별로 그룹화합니다. 이 경우, 각 교수별로 급여 비율을 계산합니다.

**ORDER BY 5 DESC**: ORDER BY 5는 SELECT 절에서 다섯 번째 컬럼인 RATIO %로 결과를 내림차순 정렬합니다. 즉, 급여 비율이 높은 순서대로 결과를 출력합니다.

### 3.  professor 테이블을 조회하여 학과번호 , 교수명 , 급여 , 학과별 급여 합계를 구하고 각 교수의 급여가 해당 학과별 급여 합계에서 차지하는 비율을 출력
```
SELECT deptno, name, pay, 
    SUM(pay) OVER(PARTITION BY deptno) AS "TOTAL PAY",
    ROUND(SUM(pay) / SUM(pay) OVER(PARTITION BY deptno) * 100, 2) AS "RATIO %"
FROM professor
GROUP BY deptno, name, pay
ORDER BY 1;
```
**SELECT deptno, name, pay**: professor 테이블에서 부서 번호(deptno), 교수 이름(name), 급여(pay)를 선택합니다.

**SUM(pay) OVER(PARTITION BY deptno)**: PARTITION BY deptno는 각 부서별로 그룹을 나누어, 부서 내에서 급여의 합계(TOTAL PAY)를 계산합니다. 즉, 각 부서 내 교수들의 급여 합계가 계산됩니다.

**ROUND(SUM(pay) / SUM(pay) OVER(PARTITION BY deptno) * 100, 2) AS "RATIO %"**: 각 교수의 급여가 속한 부서 내에서 차지하는 비율을 계산합니다. 각 교수의 급여를 해당 부서 내 급여 합계로 나누고 100을 곱하여 비율을 계산합니다. 이 값은 소수점 두 자리까지 반올림됩니다.

**GROUP BY deptno, name, pay**: GROUP BY는 부서 번호(deptno), 교수 이름(name), 급여(pay)별로 그룹화하여 결과를 출력합니다. 하지만 SUM() 같은 집계 함수가 OVER() 절과 함께 사용되므로 GROUP BY는 SUM()을 적용한 값이 각 행에 대해 계산됩니다.

**ORDER BY 1**: ORDER BY 1은 SELECT 절에서 첫 번째 컬럼인 deptno(부서 번호) 기준으로 결과를 오름차순 정렬합니다.
