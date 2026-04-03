# Build stage
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage (rất nhẹ)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JVM flags tối ưu cho Render (512MB hoặc ít hơn)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=65.0", \
    "-XX:+UseG1GC", \
    "-Xms128m", \
    "-jar", \
    "app.jar"]