FROM gradle:8.5-jdk17 AS builder

WORKDIR /workspace
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle --no-daemon dependencies


COPY . .
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:17-jre

ARG DB_PASSWORD
ENV DB_PASSWORD=${DB_PASSWORD}

ARG JAR_FILE=/workspace/build/libs/*.jar
COPY --from=builder ${JAR_FILE} /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]

