# syntax=docker/dockerfile:1
#빌드스테이지
FROM gradle:8.5-jdk17 AS build

# 인증서 설치 (HTTPS 통신용)
USER root
RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates
USER gradle

WORKDIR /workspace

# Gradle 설정 파일만 먼저 복사 → 의존성 캐시 활용
COPY settings.gradle build.gradle gradle.properties* ./
COPY gradle gradle

# 의존성 미리 다운로드 (캐시 최적화)
RUN gradle --no-daemon dependencies

# 실제 소스 코드 복사 후 빌드
COPY src src
RUN gradle clean bootJar -x test --no-daemon
# plain.jar 제거하고 실행용 jar 하나로 정리
RUN rm -f build/libs/*-plain.jar && mv build/libs/*.jar app.jar


FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /workspace/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app/app.jar"]
