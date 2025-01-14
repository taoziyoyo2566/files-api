# Step 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy Maven's dependency files first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean install -DskipTests

# Step 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /home

# Copy only the built jar file from the builder stage
#COPY --from=builder /app/target/copy-0.0.1-SNAPSHOT.jar app.jar

# 安装 curl
RUN apk update && apk add --no-cache curl

ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar
# Expose the application's default port
EXPOSE 8080

# Use a lightweight entrypoint with Java memory settings
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:InitialRAMPercentage=80.0", "-XX:MinRAMPercentage=80.0", "-XX:MaxRAMPercentage=90.0", "-jar", "app.jar"]