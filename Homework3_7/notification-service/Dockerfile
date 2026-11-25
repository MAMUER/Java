FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/target/notification-service-1.0.0.jar app.jar
RUN mkdir -p /app/logs && chown -R 1001:1001 /app

EXPOSE 8081
USER 1001:1001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]