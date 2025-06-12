# Use lightweight Java base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR into the container
COPY target/demo2-0.0.1-SNAPSHOT.jar task.jar

# Expose the port your service runs on
EXPOSE 8082  # change for each service

# Start the application
ENTRYPOINT ["java", "-jar", "task.jar"] 
