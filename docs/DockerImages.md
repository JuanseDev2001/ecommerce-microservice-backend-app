#To rebuild docker images you need to be in root and create jar files

mvn clean package -DskipTests

#The to build and upload the docker images you need to be in root and use the command

docker build -f payment-service/Dockerfile -t juanse201/payment-service-ecommerce-boot:0.1.0 .

#thats an example

docker build -f {microservice name}/Dockerfile -t {dockerHubUser}/{ImageName} .