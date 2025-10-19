# Spring Batch Schema for MySQL

이 디렉터리에는 Spring Batch 메타데이터 테이블을 생성하는 SQL 스크립트가 포함되어 있습니다.

## 파일 설명

### local-was-schema-mysql.sql
MySQL/MariaDB용 Spring Batch 메타데이터 테이블 생성 스크립트

## 테이블 구조

### 핵심 테이블

1. **BATCH_JOB_INSTANCE**
   - Job의 고유한 인스턴스를 저장
   - Job 이름과 파라미터로 식별

2. **BATCH_JOB_EXECUTION**
   - Job 실행 정보 저장
   - 실행 상태, 시작/종료 시간, 종료 코드 등

3. **BATCH_JOB_EXECUTION_PARAMS**
   - Job 실행 시 전달된 파라미터 저장

4. **BATCH_STEP_EXECUTION**
   - Step 실행 정보 저장
   - 읽기/쓰기/스킵 카운트 등 상세 메트릭

5. **BATCH_JOB_EXECUTION_CONTEXT**
   - Job 실행 컨텍스트(상태) 저장

6. **BATCH_STEP_EXECUTION_CONTEXT**
   - Step 실행 컨텍스트(상태) 저장

### 시퀀스 테이블

- **BATCH_JOB_SEQ**: Job Instance ID 생성
- **BATCH_JOB_EXECUTION_SEQ**: Job Execution ID 생성
- **BATCH_STEP_EXECUTION_SEQ**: Step Execution ID 생성

## 사용 방법

### 1. 수동 실행

```bash
# MySQL 접속 후 실행
mysql -u username -p database_name < schema/batch/local-was-schema-mysql.sql
```

### 2. Spring Boot 자동 실행

`application.yml` 또는 `application-{profile}.yml`에 다음 설정 추가:

```yaml
spring:
  batch:
    jdbc:
      initialize-schema: always  # 또는 embedded (H2 등 임베디드 DB만)
```

**주의**: `initialize-schema: always`는 개발 환경에서만 사용하고, 운영 환경에서는 수동으로 스크립트를 실행하는 것을 권장합니다.

### 3. 운영 환경 권장 방식

운영 환경에서는 다음 단계를 따르세요:

1. `application-prod.yml`:
   ```yaml
   spring:
     batch:
       jdbc:
         initialize-schema: never  # 자동 초기화 비활성화
   ```

2. 배포 전 수동으로 스크립트 실행:
   ```bash
   mysql -u prod_user -p prod_database < schema/batch/local-was-schema-mysql.sql
   ```

## 테이블 삭제

테이블을 삭제하고 재생성하려면:

```sql
-- 외래 키 제약조건 때문에 순서대로 삭제해야 함
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_SEQ;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_SEQ;
DROP TABLE IF EXISTS BATCH_JOB_SEQ;
```

## 데이터 확인

### Job 실행 이력 조회

```sql
-- 최근 실행된 Job 목록
SELECT
    ji.JOB_INSTANCE_ID,
    ji.JOB_NAME,
    je.JOB_EXECUTION_ID,
    je.STATUS,
    je.START_TIME,
    je.END_TIME,
    TIMESTAMPDIFF(SECOND, je.START_TIME, je.END_TIME) AS DURATION_SECONDS
FROM BATCH_JOB_INSTANCE ji
JOIN BATCH_JOB_EXECUTION je ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
ORDER BY je.START_TIME DESC
LIMIT 10;
```

### Step 실행 상세 조회

```sql
-- 특정 Job Execution의 Step 상세 정보
SELECT
    se.STEP_NAME,
    se.STATUS,
    se.READ_COUNT,
    se.WRITE_COUNT,
    se.COMMIT_COUNT,
    se.ROLLBACK_COUNT,
    se.READ_SKIP_COUNT,
    se.WRITE_SKIP_COUNT,
    se.PROCESS_SKIP_COUNT,
    TIMESTAMPDIFF(SECOND, se.START_TIME, se.END_TIME) AS DURATION_SECONDS
FROM BATCH_STEP_EXECUTION se
WHERE se.JOB_EXECUTION_ID = ?;  -- Job Execution ID 입력
```

### 실패한 Job 조회

```sql
-- 실패한 Job 목록
SELECT
    ji.JOB_NAME,
    je.JOB_EXECUTION_ID,
    je.STATUS,
    je.EXIT_CODE,
    je.EXIT_MESSAGE,
    je.START_TIME,
    je.END_TIME
FROM BATCH_JOB_INSTANCE ji
JOIN BATCH_JOB_EXECUTION je ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
WHERE je.STATUS IN ('FAILED', 'ABANDONED')
ORDER BY je.START_TIME DESC;
```

### Job Parameters 조회

```sql
-- 특정 Job Execution의 파라미터 확인
SELECT
    PARAMETER_NAME,
    PARAMETER_TYPE,
    PARAMETER_VALUE,
    IDENTIFYING
FROM BATCH_JOB_EXECUTION_PARAMS
WHERE JOB_EXECUTION_ID = ?;  -- Job Execution ID 입력
```

## 데이터 정리

오래된 배치 실행 이력을 삭제하려면:

```sql
-- 30일 이전의 완료된 Job 데이터 삭제
DELETE se FROM BATCH_STEP_EXECUTION se
WHERE se.JOB_EXECUTION_ID IN (
    SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION
    WHERE STATUS = 'COMPLETED'
    AND END_TIME < DATE_SUB(NOW(), INTERVAL 30 DAY)
);

DELETE jep FROM BATCH_JOB_EXECUTION_PARAMS jep
WHERE jep.JOB_EXECUTION_ID IN (
    SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION
    WHERE STATUS = 'COMPLETED'
    AND END_TIME < DATE_SUB(NOW(), INTERVAL 30 DAY)
);

DELETE FROM BATCH_JOB_EXECUTION
WHERE STATUS = 'COMPLETED'
AND END_TIME < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## 참고 사항

- Spring Batch 버전: 5.x (Spring Boot 3.x)
- MySQL 버전: 5.7 이상 또는 MariaDB 10.3 이상
- 문자셋: UTF8MB4 (이모지 지원)
- 엔진: InnoDB (트랜잭션 지원)

## 트러블슈팅

### 테이블이 자동 생성되지 않는 경우

1. `spring.batch.jdbc.initialize-schema` 설정 확인
2. 데이터소스 설정 확인
3. 로그에서 에러 메시지 확인

### 시퀀스 값이 중복되는 경우

```sql
-- 시퀀스 초기화
UPDATE BATCH_JOB_SEQ SET ID = (SELECT COALESCE(MAX(JOB_INSTANCE_ID), 0) FROM BATCH_JOB_INSTANCE);
UPDATE BATCH_JOB_EXECUTION_SEQ SET ID = (SELECT COALESCE(MAX(JOB_EXECUTION_ID), 0) FROM BATCH_JOB_EXECUTION);
UPDATE BATCH_STEP_EXECUTION_SEQ SET ID = (SELECT COALESCE(MAX(STEP_EXECUTION_ID), 0) FROM BATCH_STEP_EXECUTION);
```