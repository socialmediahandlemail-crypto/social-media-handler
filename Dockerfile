# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the pom.xml first to cache dependencies (Faster builds)
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application (Skip tests to speed up deployment)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port Render expects (usually 8080, but we map it)
EXPOSE 8082

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]