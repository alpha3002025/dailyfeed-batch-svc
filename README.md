# DailyFeed Batch Service

Spring Batch 기반의 배치 처리 서비스입니다.

## 프로젝트 구조

```
dailyfeed-batch-svc/
├── dailyfeed-batch/          # Spring Batch 메인 모듈
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── click/dailyfeed/batch/
│   │   │   │       ├── config/          # Batch 설정
│   │   │   │       └── job/             # Batch Job 정의
│   │   │   │           └── sample/      # 샘플 Job
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-local.yml
│   │   │       └── application-prod.yml
│   │   └── test/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 기술 스택

- Java 17
- Spring Boot 3.5.5
- Spring Batch
- Spring Data JPA
- QueryDSL
- MapStruct
- Lombok
- H2 Database (로컬 개발)
- MySQL (프로덕션)

## 빌드 및 실행

### 빌드
```bash
./gradlew clean build
```

### 로컬 실행
```bash
./gradlew :dailyfeed-batch:bootRun
```

### 특정 Job 실행
```bash
./gradlew :dailyfeed-batch:bootRun --args='--job.name=sampleJob'
```

### Job 파라미터와 함께 실행
```bash
./gradlew :dailyfeed-batch:bootRun --args='--job.name=sampleJob requestDate=2025-10-17'
```

## 환경 설정

### 프로파일
- `local`: 로컬 개발 환경 (H2 Database)
- `prod`: 프로덕션 환경 (MySQL)

### 주요 설정

#### application.yml
- `spring.batch.job.enabled: false` - 애플리케이션 시작 시 자동 Job 실행 방지
- `spring.batch.jdbc.initialize-schema: always` - 배치 메타데이터 테이블 자동 생성

## Batch Job 작성 가이드

### 기본 Job 구조

```java
@Configuration
@RequiredArgsConstructor
public class MyJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job myJob() {
        return new JobBuilder("myJob", jobRepository)
                .start(myStep())
                .build();
    }

    @Bean
    public Step myStep() {
        return new StepBuilder("myStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 작업 로직
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
```

### Chunk 기반 Step

```java
@Bean
public Step chunkStep() {
    return new StepBuilder("chunkStep", jobRepository)
            .<InputType, OutputType>chunk(100, transactionManager)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
}
```

## 테스트

```bash
./gradlew test
```

## Docker 이미지 빌드

```bash
./gradlew :dailyfeed-batch:jibDockerBuild
```

## H2 콘솔

로컬 개발 시 H2 콘솔 접근:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:batchdb
- Username: sa
- Password: (비어있음)

## 배치 메타데이터 테이블

Spring Batch는 다음 메타데이터 테이블을 자동으로 생성합니다:
- BATCH_JOB_INSTANCE
- BATCH_JOB_EXECUTION
- BATCH_JOB_EXECUTION_PARAMS
- BATCH_JOB_EXECUTION_CONTEXT
- BATCH_STEP_EXECUTION
- BATCH_STEP_EXECUTION_CONTEXT

## 참고사항

- Job 이름은 고유해야 합니다
- 동일한 파라미터로 성공한 Job은 재실행되지 않습니다
- Job 재실행을 위해서는 파라미터를 변경하거나 메타데이터를 수정해야 합니다
