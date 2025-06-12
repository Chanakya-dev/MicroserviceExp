# Stage 1: Build
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

# Copy source code and pom.xml
COPY pom.xml .
COPY src ./src

# Build the jar file
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy jar from the build stage
COPY --from=build /app/target/svs-0.0.1-SNAPSHOT.jar server.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "server.jar"]
