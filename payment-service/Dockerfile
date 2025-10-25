FROM openjdk:11
WORKDIR /app
COPY target/payment-service-v0.1.0.jar app.jar
EXPOSE 8400
ENTRYPOINT ["java", "-jar", "app.jar"]