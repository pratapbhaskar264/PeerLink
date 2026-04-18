# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Manually extract all jars and repackage with correct manifest
RUN mkdir -p /app/fatjar && \
    cd /app/fatjar && \
    find /app/target -name "*.jar" -not -name "original-*" | head -1 | xargs -I{} jar xf {} && \
    echo "Main-Class: p2p.App" > manifest.txt && \
    jar cfm /app/app.jar manifest.txt -C /app/fatjar .

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]