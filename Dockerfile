
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 1. Create a non-root system user for security
RUN addgroup -S peenggroup && adduser -S peenguser -G peenggroup

# 2. Copy the built JAR from Stage 1 builder
COPY --chown=peenguser:peenggroup --from=builder /app/target/*.jar app.jar

USER peenguser:peenggroup

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]