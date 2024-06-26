# Use the official Amazon Corretto 17 image
FROM amazoncorretto:17

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the target directory to the container
COPY build/libs/*.jar /app/myapp.jar

# Expose the port the application will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "myapp.jar"]
