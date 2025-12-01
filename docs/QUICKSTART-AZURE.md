# Inicio Rápido - Despliegue en Azure VM

Esta es la guía de inicio rápido para desplegar el proyecto de microservicios de e-commerce en una VM de Azure.

---

## ⚡ Resumen de 5 Minutos

### 1 Crear VM en Azure Portal

```
Nombre: vm-ecommerce-app
Imagen: Ubuntu 22.04 LTS
Tamaño: Standard_D8s_v3 (8 vCPUs, 32GB RAM)
Disco: 200GB SSD Premium
SSH: Generar nuevo par de claves
```

**Puertos a abrir en NSG**: 22, 80, 443, 3000, 5000, 5601, 8080, 8300-8900, 9090, 9093, 9200, 9296, 9411, 8761, 16686

### 2 Conectar a la VM

```bash
chmod 400 ~/.ssh/vm-ecommerce-key.pem
ssh -i ~/.ssh/vm-ecommerce-key.pem azureuser@<IP_PUBLICA>
```

### 3 Configurar VM (una sola vez)

```bash
# Clonar repositorio
git clone https://github.com/JuanseDev2001/ecommerce-microservice-backend-app.git ~/projects/ecommerce-microservice-backend-app
cd ~/projects/ecommerce-microservice-backend-app

# Ejecutar configuración automática
chmod +x scripts/azure-vm-setup.sh
./scripts/azure-vm-setup.sh

# IMPORTANTE: Cerrar sesión y volver a conectar
exit
ssh -i ~/.ssh/vm-ecommerce-key.pem azureuser@<IP_PUBLICA>
```

### 4 Desplegar Aplicación

```bash
cd ~/projects/ecommerce-microservice-backend-app
./scripts/deploy-gradual.sh
```

**Espera**: 10-15 minutos mientras se despliegan todos los servicios.

### 5 Verificar

```bash
./scripts/check-services.sh
```

### 6 Acceder desde el Navegador

Reemplaza `<IP_PUBLICA>` con la IP de tu VM:

- **Eureka**: http://\<IP_PUBLICA\>:8761
- **API Gateway**: http://\<IP_PUBLICA\>:8080/actuator/health
- **Grafana**: http://\<IP_PUBLICA\>:3000 (admin/admin)
- **Prometheus**: http://\<IP_PUBLICA\>:9090
- **Jaeger**: http://\<IP_PUBLICA\>:16686
- **Kibana**: http://\<IP_PUBLICA\>:5601

---

## Comandos Esenciales

### Gestión de Servicios

```bash
# Ver estado de todos los servicios
docker ps

# Ver logs de todos los servicios
docker-compose -f compose.yml logs -f

# Ver logs de un servicio específico
docker-compose -f compose.yml logs -f <servicio>

# Reiniciar un servicio
docker-compose -f compose.yml restart <servicio>

# Reiniciar todos los servicios
docker-compose -f compose.yml restart

# Detener todos los servicios
docker-compose -f compose.yml down

# Iniciar todos los servicios
docker-compose -f compose.yml up -d
```

### Monitoreo

```bash
# Verificar estado de servicios
./scripts/check-services.sh

# Ver uso de recursos
docker stats

# Ver memoria del sistema
free -h

# Ver uso de disco
df -h
```