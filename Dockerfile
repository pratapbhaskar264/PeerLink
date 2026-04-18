# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Debug — print what jars were created (check render logs)
RUN ls -la target/

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the shaded jar, NOT the original-* one
COPY --from=build /app/target/peerlink-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]