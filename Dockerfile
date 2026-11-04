FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /chatter

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /chatter

COPY --from=build /chatter/target/*.jar chatter.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "chatter.jar"]