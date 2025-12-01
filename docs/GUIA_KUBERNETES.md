# Guía de Kubernetes (Minikube)

## E-Commerce Microservices Backend Application

Guía rápida para correr el proyecto con Kubernetes usando Minikube.

---

## Requisitos Previos

- **Docker Desktop** instalado y corriendo
- **Minikube** instalado
- **kubectl** instalado
- **Maven** para compilar el proyecto

---

## Inicio Rápido

### 1. Iniciar Minikube

```bash
minikube start
```

### 2. Compilar el Proyecto

```bash
mvn clean package -DskipTests
```

---

## Construir y Cargar Imágenes Docker

### Microservicios

```bash
# Construir imágenes
docker build -f order-service/Dockerfile -t juanse201/order-service-ecommerce-boot:0.1.0 .
docker build -f user-service/Dockerfile -t juanse201/user-service-ecommerce-boot:0.1.0 .
docker build -f payment-service/Dockerfile -t juanse201/payment-service-ecommerce-boot:0.1.0 .
docker build -f product-service/Dockerfile -t juanse201/product-service-ecommerce-boot:0.1.0 .
docker build -f favourite-service/Dockerfile -t juanse201/favourite-service-ecommerce-boot:0.1.0 .
docker build -f shipping-service/Dockerfile -t juanse201/shipping-service-ecommerce-boot:0.1.0 .
docker build -f cloud-config/Dockerfile -t juanse201/cloud-config-ecommerce-boot:0.1.0 .
docker build -f service-discovery/Dockerfile -t juanse201/service-discovery-ecommerce-boot:0.1.0 .
docker build -f api-gateway/Dockerfile -t juanse201/api-gateway-ecommerce-boot:0.1.0 .
docker build -f proxy-client/Dockerfile -t juanse201/proxy-client-ecommerce-boot:0.1.0 .

# Cargar en Minikube
minikube image load juanse201/order-service-ecommerce-boot:0.1.0
minikube image load juanse201/user-service-ecommerce-boot:0.1.0
# ... repetir para cada servicio
```

---

## Jenkins en Kubernetes

### Primera Vez - Construir y Desplegar

```bash
# 1. Construir imagen de Jenkins con tag único
docker build -t ecommerce-jenkins:v1 ./jenkins

# 2. Cargar imagen en Minikube
minikube image load ecommerce-jenkins:v1

# 3. Aplicar configuración (asegúrate de que el YAML use el mismo tag)
kubectl apply -f k8/jenkins/jenkins.yaml

# 4. Verificar que el pod esté corriendo
kubectl get pods

# 5. Exponer servicio
minikube service jenkins
```

### Actualizar Imagen de Jenkins (IMPORTANTE)

> **Nota:** Minikube cachea las imágenes. Para actualizar Jenkins, DEBES usar un tag diferente.

```bash
# 1. Construir con NUEVO tag (incrementar versión)
docker build -t ecommerce-jenkins:v2 ./jenkins

# 2. Cargar la nueva imagen
minikube image load ecommerce-jenkins:v2

# 3. Actualizar el deployment con la nueva imagen
kubectl set image deployment/jenkins jenkins=ecommerce-jenkins:v2

# 4. Reiniciar el deployment
kubectl rollout restart deployment jenkins

# 5. Verificar el nuevo pod
kubectl get pods -w
```

**Tags sugeridos:**
- `ecommerce-jenkins:v1`, `ecommerce-jenkins:v2`, `ecommerce-jenkins:v3`...
- O usar timestamp: `ecommerce-jenkins:20251201`

---

## Namespaces (Ambientes Separados)

```bash
# Crear namespaces
kubectl create namespace dev
kubectl create namespace stage
kubectl create namespace prod

# Dar permisos a Jenkins para cada namespace
kubectl create rolebinding jenkins-stage-admin \
  --clusterrole=admin \
  --serviceaccount=default:jenkins \
  --namespace=stage

kubectl create rolebinding jenkins-prod-admin \
  --clusterrole=admin \
  --serviceaccount=default:jenkins \
  --namespace=prod
```

---

## Configurar Docker Socket

Si Jenkins no puede construir imágenes:

```bash
# Verificar acceso al socket
kubectl exec -it <pod-jenkins> -- ls -l /var/run/docker.sock

# Dar permisos al grupo docker (102 es el GID de docker en Jenkins)
minikube ssh "sudo chgrp 102 /var/run/docker.sock"
```

---

## Comandos Útiles

### Ver Estado

```bash
# Ver todos los pods
kubectl get pods

# Ver pods en un namespace específico
kubectl get pods -n stage

# Ver logs de un pod
kubectl logs <pod-name>

# Ver logs en tiempo real
kubectl logs -f <pod-name>
```

### Gestionar Deployments

```bash
# Listar deployments
kubectl get deployments

# Escalar un deployment
kubectl scale deployment <name> --replicas=3

# Reiniciar un deployment
kubectl rollout restart deployment <name>

# Ver historial de rollouts
kubectl rollout history deployment <name>
```

### Acceder a Servicios

```bash
# Exponer servicio en el navegador
minikube service <service-name>

# Ver todos los servicios
kubectl get services
```

### Debugging

```bash
# Ejecutar comando dentro de un pod
kubectl exec -it <pod-name> -- bash

# Ver descripción detallada de un pod
kubectl describe pod <pod-name>

# Ver eventos del cluster
kubectl get events --sort-by=.metadata.creationTimestamp
```

---

## Obtener Password de Jenkins

```bash
kubectl exec -it <pod-jenkins> -- cat /var/jenkins_home/secrets/initialAdminPassword
```

---

## Limpieza

```bash
# Eliminar un deployment
kubectl delete deployment <name>

# Eliminar todos los recursos de un archivo
kubectl delete -f k8/jenkins/jenkins.yaml

# Detener Minikube
minikube stop

# Eliminar cluster de Minikube
minikube delete
```

---

## Resumen de Flujo

```
1. minikube start
2. mvn clean package -DskipTests
3. docker build -t <image>:<tag> .
4. minikube image load <image>:<tag>
5. kubectl apply -f <archivo>.yaml
6. minikube service <service-name>
```

**Para actualizar una imagen:**
```
1. docker build -t <image>:<NUEVO-tag> .
2. minikube image load <image>:<NUEVO-tag>
3. kubectl set image deployment/<name> <container>=<image>:<NUEVO-tag>
4. kubectl rollout restart deployment <name>
```

---

## Tips

1. **Siempre usa tags únicos** al actualizar imágenes (no uses `:latest`)
2. **Verifica el pod nuevo** con `kubectl get pods -w` después de actualizar
3. **Revisa logs** si algo falla: `kubectl logs <pod-name>`
4. **Usa namespaces** para separar ambientes (dev, stage, prod)
5. **Docker socket**: Si Jenkins no puede buildear, corre el comando de `chgrp`

---

**Última actualización:** Diciembre 2025
