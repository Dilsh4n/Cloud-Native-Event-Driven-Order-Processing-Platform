# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]