# Use the official OpenJDK 11 base image
FROM adoptopenjdk:11-jre-hotspot

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/web-crawler-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port that your Spring Boot application will listen on
EXPOSE 8080

# Command to run your Spring Boot application
CMD ["java", "-jar", "app.jar"]