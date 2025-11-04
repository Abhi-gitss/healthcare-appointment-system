# ===============================
# STEP 1: Build the application
# ===============================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the pom.xml first (for caching dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the code
COPY src ./src

# Package the Spring Boot application
RUN mvn clean package -DskipTests

# ===============================
# STEP 2: Run the application
# ===============================
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 for Render
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
