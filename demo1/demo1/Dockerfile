# Use lightweight Java base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR into the container
COPY demo1-0.0.1-SNAPSHOT.jar user.jar

# Expose the port your service runs on
EXPOSE 8081

# Run with prod profile
ENTRYPOINT ["java", "-jar", "user.jar", "--spring.profiles.active=prod"]
