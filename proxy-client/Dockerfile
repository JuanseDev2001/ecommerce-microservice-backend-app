FROM openjdk:11
WORKDIR /app
COPY target/proxy-client-v0.1.0.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]