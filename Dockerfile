# syntax=docker/dockerfile:1

FROM gradle:8.5-jdk17 AS build

USER root
RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates
USER gradle

WORKDIR /workspace

COPY settings.gradle build.gradle gradle.properties* ./
COPY gradle gradle

RUN gradle --no-daemon dependencies

COPY src src
RUN gradle clean bootJar -x test --no-daemon
RUN rm -f build/libs/*-plain.jar && mv build/libs/*.jar app.jar


FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /workspace/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app/app.jar"]
