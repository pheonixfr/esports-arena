FROM eclipse-temurin:27-jdk AS build

WORKDIR /app

RUN apt-get update && apt-get install -y maven

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:27-jre

WORKDIR /app

COPY --from=build /app/target/esports-arena-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]