FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -U
RUN ls -la target/

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV CACHEBUST=2
COPY --from=build /app/target/peerlink-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]