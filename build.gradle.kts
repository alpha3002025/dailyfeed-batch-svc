plugins {
    java
    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.google.cloud.tools.jib") version "3.4.0"
}

allprojects {
    group = "click.dailyfeed"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.google.cloud.tools.jib")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

project(":dailyfeed-batch") {
    // Spring Boot main class 설정
    tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
        mainClass.set("click.dailyfeed.batch.DailyfeedBatchApplication")
    }

    jib {
        // Base 이미지 설정 (Java 17 기반)
        from {
            image = "eclipse-temurin:17-jre-alpine"
        }

        // 타겟 이미지 설정
        to {
            val imageVersion = System.getenv("IMAGE_VERSION") ?: "beta-20251015-0001"
            tags = setOf(imageVersion)
            image = "ghcr.io/alpha3002025/dailyfeed-batch-svc"

            // GitHub Container Registry 인증 (환경변수에서 가져오기)
            auth {
                username = System.getenv("GITHUB_USERNAME") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }

        // Docker 실행 파일 경로 명시
        dockerClient {
            executable = "/usr/local/bin/docker"
        }

        // 컨테이너 설정
        container {
            // Main class 명시
            mainClass = "click.dailyfeed.batch.DailyfeedBatchApplication"
            // JVM 옵션
            jvmFlags = listOf(
                "-XX:+UseContainerSupport",
                "-XX:+UseG1GC",
                "-Dfile.encoding=UTF-8"
            )

            // 레이블
            labels = mapOf(
                "maintainer" to "alpha300uk@gmail.com",
                "version" to project.version.toString(),
                "description" to "DailyFeed Batch Service"
            )

            // 작업 디렉토리
            workingDirectory = "/app"

            // 사용자 설정 (보안을 위해 non-root 사용자 사용)
            user = "1000:1000"

            // 생성 시간 설정 (재현 가능한 빌드를 위해)
            creationTime = "USE_CURRENT_TIMESTAMP"
        }
    }
}
