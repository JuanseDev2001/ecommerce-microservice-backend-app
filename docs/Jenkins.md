Order to run jenkins

root

docker-compose build --no-cache jenkins

#to get image faster
docker-compose up -d jenkins
docker-compose down jenkins

#kubernets / minikube

minkube start

#if any pods are up

kubectl get pods

kubectl delete deployment jenkins

minikube image load ecommerce-microservice-backend-app-jenkins:latest

kubectl apply -f K8/jenkins/jenkins.yaml

#check new pod is up with update jenkins image
kubectl get pods

#run command

minikube service jenkins


#Check docker socket in kuber pod in case of build not functioning

kubectl get pods

kubectl exec -it [id de pod] -- ls -l /var/run/docker.sock

minikube ssh "sudo chgrp 102 /var/run/docker.sock"