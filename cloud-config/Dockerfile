
FROM openjdk:11
WORKDIR /app
COPY target/cloud-config-v0.1.0.jar cloud-config.jar
EXPOSE 9296
ENTRYPOINT ["java", "-jar", "cloud-config.jar"]


