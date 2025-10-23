
FROM openjdk:11
WORKDIR /app
COPY target/order-service-v0.1.0.jar order-service.jar
EXPOSE 8300
ENTRYPOINT ["java", "-jar", "order-service.jar"]


