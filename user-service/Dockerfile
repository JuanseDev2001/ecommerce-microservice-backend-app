FROM openjdk:11
WORKDIR /app
COPY target/user-service-v0.1.0.jar app.jar
EXPOSE 8700
ENTRYPOINT ["java", "-jar", "app.jar"]