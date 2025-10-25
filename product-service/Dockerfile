FROM openjdk:11
WORKDIR /app
COPY target/product-service-v0.1.0.jar app.jar
EXPOSE 8500
ENTRYPOINT ["java", "-jar", "app.jar"]