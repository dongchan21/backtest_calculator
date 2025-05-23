# backend/Dockerfile
FROM eclipse-temurin:20-jdk
ARG JAR_FILE=build/libs/portfolio-backtest-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
