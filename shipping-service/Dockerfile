
FROM openjdk:11
WORKDIR /app
COPY target/shipping-service-v0.1.0.jar app.jar
EXPOSE 8600
ENTRYPOINT ["java", "-jar", "app.jar"]


