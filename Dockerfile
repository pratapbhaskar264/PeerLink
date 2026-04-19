# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Manually repackage with all dependencies
RUN mkdir -p /app/fatjar && \
    cd /app/fatjar && \
    find /root/.m2 -name "commons-io-2.15.1.jar" | xargs -I{} jar xf {} && \
    find /root/.m2 -name "commons-fileupload-1.5.jar" | xargs -I{} jar xf {} && \
    jar xf /app/target/*.jar && \
    echo "Main-Class: p2p.App" > manifest.txt && \
    jar cfm /app/app.jar manifest.txt -C /app/fatjar .

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]