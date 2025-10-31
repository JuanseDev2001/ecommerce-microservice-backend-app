#get images from docker hub named juanse201/{SERVICE_NAME}

#after getting them gotta run them dont know if these commands are correct
minkube start
minikube image load {docker image name}
kubectl apply -f K8/{SERVICE_NAME}/{SERVICE_NAME}.yaml
minikube service {SERVICE_NAME}
#problem is that dont know in the case if theres already a image how to get multiple pods with diferent images or how to run them at the same time cause minikube service command think only runs one


#globaltests dir you can do cd globaltests or immedietly place it there

#run order e2e test after services have been up
mvn test -Dtest=OrderServiceE2ETest

#If it passes then find docker image in dockerhub and duplicate it with a extra name or tag like :stage and push it to dockerhub it should be seperate.



docker build -t juanse201/order-service:dev -f order-service/Dockerfile .
docker build -t juanse201/api-gateway:dev -f api-gateway/Dockerfile .