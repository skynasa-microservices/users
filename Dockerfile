# Stage 1: Build the devices service
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven configuration first
COPY pom.xml ./pom.xml

# Copy the shared common-package JAR from the common-package image
COPY --from=common-package:latest /shared-libs/common-package-1.0.0.jar libs/

# Install the common-package JAR into the local Maven repository
RUN mvn install:install-file \
  -Dfile=/app/libs/common-package-1.0.0.jar \
  -DgroupId=com.skynasa.tracking \
  -DartifactId=common-package \
  -Dversion=1.0.0 \
  -Dpackaging=jar

# Resolve dependencies offline
RUN mvn dependency:go-offline

# Copy the source code after dependencies are resolved
COPY src ./src

# Build the application
RUN mvn clean install -DskipTests

# Stage 2: Runtime environment for the devices service
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar users.jar

# Define the entry point for the application
ENTRYPOINT ["java", "-jar", "users.jar"]
