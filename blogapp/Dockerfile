# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for better caching
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Copy startup script
COPY start.sh .
RUN chmod +x start.sh

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port that Render will use
EXPOSE $PORT

# Run the application using the startup script
CMD ["./start.sh"]