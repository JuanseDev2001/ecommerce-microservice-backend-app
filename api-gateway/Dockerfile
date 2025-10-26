FROM openjdk:11
WORKDIR /app
COPY target/api-gateway-v0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]