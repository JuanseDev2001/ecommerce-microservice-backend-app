# 🚀 Inicio Rápido - Despliegue en Azure VM

Esta es la guía de inicio rápido para desplegar el proyecto de microservicios de e-commerce en una VM de Azure.

---

## ⚡ Resumen de 5 Minutos

### 1️⃣ Crear VM en Azure Portal

```
Nombre: vm-ecommerce-app
Imagen: Ubuntu 22.04 LTS
Tamaño: Standard_D8s_v3 (8 vCPUs, 32GB RAM)
Disco: 200GB SSD Premium
SSH: Generar nuevo par de claves
```

**Puertos a abrir en NSG**: 22, 80, 443, 3000, 5000, 5601, 8080, 8300-8900, 9090, 9093, 9200, 9296, 9411, 8761, 16686

### 2️⃣ Conectar a la VM

```bash
chmod 400 ~/.ssh/vm-ecommerce-key.pem
ssh -i ~/.ssh/vm-ecommerce-key.pem azureuser@<IP_PUBLICA>
```

### 3️⃣ Configurar VM (una sola vez)

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

### 4️⃣ Desplegar Aplicación

```bash
cd ~/projects/ecommerce-microservice-backend-app
./scripts/deploy-gradual.sh
```

**Espera**: 10-15 minutos mientras se despliegan todos los servicios.

### 5️⃣ Verificar

```bash
./scripts/check-services.sh
```

### 6️⃣ Acceder desde el Navegador

Reemplaza `<IP_PUBLICA>` con la IP de tu VM:

- **Eureka**: http://\<IP_PUBLICA\>:8761
- **API Gateway**: http://\<IP_PUBLICA\>:8080/actuator/health
- **Grafana**: http://\<IP_PUBLICA\>:3000 (admin/admin)
- **Prometheus**: http://\<IP_PUBLICA\>:9090
- **Jaeger**: http://\<IP_PUBLICA\>:16686
- **Kibana**: http://\<IP_PUBLICA\>:5601

---

## 📋 Comandos Esenciales

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

### Mantenimiento

```bash
# Hacer backup
./scripts/backup.sh

# Limpiar recursos de Docker
docker system prune -a

# Ver logs del sistema
sudo journalctl -xe
```

---

## 🎯 Servicios y Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| **Infraestructura** |
| Eureka | 8761 | http://\<IP\>:8761 |
| Config Server | 9296 | http://\<IP\>:9296 |
| API Gateway | 8080 | http://\<IP\>:8080 |
| Zipkin | 9411 | http://\<IP\>:9411 |
| **Microservicios** |
| Proxy Client | 8900 | http://\<IP\>:8900/swagger-ui.html |
| User Service | 8700 | http://\<IP\>:8700/swagger-ui.html |
| Product Service | 8500 | http://\<IP\>:8500/swagger-ui.html |
| Order Service | 8300 | http://\<IP\>:8300/swagger-ui.html |
| Payment Service | 8400 | http://\<IP\>:8400/swagger-ui.html |
| Shipping Service | 8600 | http://\<IP\>:8600/swagger-ui.html |
| Favourite Service | 8800 | http://\<IP\>:8800/swagger-ui.html |
| **Observabilidad** |
| Prometheus | 9090 | http://\<IP\>:9090 |
| Grafana | 3000 | http://\<IP\>:3000 |
| Alertmanager | 9093 | http://\<IP\>:9093 |
| Jaeger | 16686 | http://\<IP\>:16686 |
| Elasticsearch | 9200 | http://\<IP\>:9200 |
| Logstash | 5000 | - |
| Kibana | 5601 | http://\<IP\>:5601 |

---

## 🔧 Solución Rápida de Problemas

### Servicio no responde

```bash
# 1. Ver si el contenedor está corriendo
docker ps | grep <servicio>

# 2. Ver logs del servicio
docker-compose -f compose.yml logs <servicio>

# 3. Reiniciar el servicio
docker-compose -f compose.yml restart <servicio>
```

### Elasticsearch no inicia

```bash
sudo sysctl -w vm.max_map_count=262144
docker-compose -f compose.yml restart elasticsearch
```

### Memoria insuficiente

```bash
# Limpiar recursos
docker system prune -a

# Reiniciar servicios pesados
docker-compose -f compose.yml restart elasticsearch logstash kibana
```

### No puedo acceder desde el navegador

1. Verifica NSG en Azure Portal: VM → Networking → Inbound port rules
2. Verifica firewall: `sudo ufw status`
3. Verifica que el servicio esté corriendo: `docker ps`

---

## 📚 Documentación Completa

- **Guía Detallada**: `docs/azure-vm-deployment-guide.md`
- **Checklist Completo**: `docs/azure-deployment-checklist.md`
- **Scripts**: `scripts/README.md`

---

## 🆘 Ayuda Rápida

```bash
# Ver todos los contenedores
docker ps -a

# Ver servicios en Eureka
curl http://localhost:8761/eureka/apps | grep "<name>"

# Verificar salud del API Gateway
curl http://localhost:8080/actuator/health

# Ver uso de recursos en tiempo real
htop

# Ver conexiones de red
sudo netstat -tulpn
```

---

## 💡 Tips

1. **Primera vez**: Usa el script `deploy-gradual.sh` para un despliegue ordenado
2. **Monitoreo**: Configura Grafana con los dashboards recomendados
3. **Backups**: Configura el cron job para backups automáticos
4. **Seguridad**: Cambia la contraseña de Grafana en el primer login
5. **Logs**: Usa `docker-compose logs -f` para debugging en tiempo real

---

## ✅ Verificación Rápida

Ejecuta estos comandos para verificar que todo funciona:

```bash
# 1. Todos los contenedores corriendo
docker ps | wc -l
# Debe mostrar ~19 (18 contenedores + header)

# 2. Servicios en Eureka
curl -s http://localhost:8761/eureka/apps | grep -o "<name>[^<]*</name>" | wc -l
# Debe mostrar 9

# 3. API Gateway saludable
curl -s http://localhost:8080/actuator/health | jq .status
# Debe mostrar "UP"

# 4. Prometheus recolectando métricas
curl -s http://localhost:9090/-/healthy
# Debe mostrar "Prometheus is Healthy."
```

---

**¿Necesitas más ayuda?** Consulta la guía completa en `docs/azure-vm-deployment-guide.md`

---

**Última actualización**: 2025-11-30
