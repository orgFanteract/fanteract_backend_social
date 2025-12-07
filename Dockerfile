# =========================
# 1단계: 빌드 스테이지
# =========================
FROM eclipse-temurin:21-jdk AS builder

# 작업 디렉토리
WORKDIR /app

# Gradle Wrapper 및 설정 파일만 먼저 복사 (캐시 최적화)
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 전체 소스 복사
COPY . .

# Spring Boot JAR 빌드 (테스트 제외)
RUN ./gradlew clean bootJar -x test


# =========================
# 2단계: 실행 스테이지
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 기본 환경 설정
ENV SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Seoul

# Account 서비스 실행 포트
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]