# Use lightweight Java base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR into the container
COPY service-0.0.1-SNAPSHOT.jar server.jar

# Expose the port your service runs on
EXPOSE 8761

# Start the application
ENTRYPOINT ["java", "-jar", "server.jar"] 
