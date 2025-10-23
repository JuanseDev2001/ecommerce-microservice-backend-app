
FROM openjdk:11
WORKDIR /app
COPY target/service-discovery-v0.1.0.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]


