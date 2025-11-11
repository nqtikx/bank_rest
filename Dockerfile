FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080


ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/bankcards
ENV SPRING_DATASOURCE_USERNAME=bankuser
ENV SPRING_DATASOURCE_PASSWORD=bankpass
ENV JWT_SECRET=myamyamyamya123!@#supersecretkey2025

ENTRYPOINT ["java", "-jar", "app.jar"]
