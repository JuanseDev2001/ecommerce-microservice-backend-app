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

#if theres no image in local
minikube image load ecommerce-microservice-backend-app-jenkins:latest

kubectl apply -f K8/jenkins/jenkins.yaml

kubectl rollout restart deployment <deployment-name>

#check new pod is up with update jenkins image
kubectl get pods

#run command

minikube service jenkins


#Check docker socket in kuber pod in case of build not functioning

kubectl get pods

kubectl exec -it [id de pod] -- ls -l /var/run/docker.sock

minikube ssh "sudo chgrp 102 /var/run/docker.sock"

#Also dont forget to make namespaces for stage and prod so when you run kubernet pods in stage you dont take down prod

kubectl create namespace stage
kubectl create namespace prod

#And give jenkins permission to access and use this namespaces

kubectl create rolebinding jenkins-prod-admin `
  --clusterrole=admin `
  --serviceaccount=default:jenkins `
  --namespace=prod


kubectl exec -it jenkins-c5ff74544-4tk4m -- cat /var/jenkins_home/secrets/initialAdminPassword

