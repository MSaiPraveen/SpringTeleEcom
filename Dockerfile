# ---------- BUILD STAGE ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1) Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# 2) Copy the entire source and build the JAR
COPY src ./src
RUN mvn -e -X clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Non-root user (security best practice)
RUN useradd -ms /bin/bash appuser
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
