FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

RUN chmod +x mvnw

FROM openjdk:21-jdk

WORKDIR /app

COPY --from=builder /app /app

RUN chmod +x /app/mvnw

ENTRYPOINT ["./mvnw", "clean", "package", "spring-boot:run", "-D", "skipTests"]
