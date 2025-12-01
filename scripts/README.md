# Scripts de Utilidad para Azure VM

Este directorio contiene scripts útiles para el despliegue y mantenimiento de la aplicación de microservicios en Azure VM.

## 📋 Scripts Disponibles

### 1. `azure-vm-setup.sh`
**Propósito**: Configuración automática inicial de la VM de Azure.

**Qué hace**:
- Actualiza el sistema Ubuntu
- Instala Docker y Docker Compose
- Instala Java 11 y Maven
- Configura límites del sistema para Elasticsearch
- Crea swap de 8GB
- Configura el firewall (UFW)
- Crea directorios para datos persistentes

**Uso**:
```bash
# En la VM de Azure (después de conectarte por SSH)
wget https://raw.githubusercontent.com/TU_USUARIO/TU_REPO/main/scripts/azure-vm-setup.sh
chmod +x azure-vm-setup.sh
./azure-vm-setup.sh
```

**Nota**: Después de ejecutar este script, debes cerrar sesión y volver a conectarte para que los cambios de grupo de Docker tengan efecto.

---

### 2. `deploy-gradual.sh`
**Propósito**: Despliegue gradual y ordenado de todos los servicios.

**Qué hace**:
- Inicia servicios en el orden correcto (infraestructura → gateway → microservicios → observabilidad)
- Espera a que cada servicio esté listo antes de continuar
- Verifica la salud de cada servicio
- Muestra un resumen completo al final

**Uso**:
```bash
cd ~/projects/ecommerce-microservice-backend-app
chmod +x scripts/deploy-gradual.sh
./scripts/deploy-gradual.sh
```

**Fases del despliegue**:
1. **Infraestructura Base**: Zipkin, Eureka, Config Server
2. **Gateway y Proxy**: API Gateway, Proxy Client
3. **Microservicios**: User, Product, Order, Payment, Shipping, Favourite
4. **Observabilidad**: Elasticsearch, Logstash, Kibana, Prometheus, Grafana, Jaeger

---

### 3. `check-services.sh`
**Propósito**: Verificación del estado de todos los servicios.

**Qué hace**:
- Verifica que todos los contenedores Docker estén corriendo
- Comprueba que todos los endpoints HTTP respondan
- Lista servicios registrados en Eureka
- Muestra uso de recursos (CPU, memoria, disco)
- Genera un resumen del estado general

**Uso**:
```bash
cd ~/projects/ecommerce-microservice-backend-app
chmod +x scripts/check-services.sh
./scripts/check-services.sh
```

**Código de salida**:
- `0`: Todos los servicios funcionan correctamente
- `1`: Algunos servicios tienen problemas

---

### 4. `backup.sh`
**Propósito**: Backup de volúmenes Docker y configuraciones.

**Qué hace**:
- Respalda volúmenes de Docker (Jenkins, Grafana, Elasticsearch)
- Respalda archivos de configuración (compose.yml, prometheus, elk, etc.)
- Respalda scripts personalizados
- Limpia backups antiguos (>7 días)
- Genera reporte de backups creados

**Uso**:
```bash
cd ~/projects/ecommerce-microservice-backend-app
chmod +x scripts/backup.sh
./scripts/backup.sh
```

**Configurar backup automático** (diario a las 2 AM):
```bash
(crontab -l 2>/dev/null; echo "0 2 * * * /home/azureuser/projects/ecommerce-microservice-backend-app/scripts/backup.sh") | crontab -
```

**Restaurar un backup**:
```bash
# Para volúmenes Docker
docker run --rm \
  -v <nombre-volumen>:/data \
  -v ~/backups:/backup \
  ubuntu tar xzf /backup/<archivo-backup>.tar.gz -C /

# Para configuraciones
cd ~/projects/ecommerce-microservice-backend-app
tar xzf ~/backups/config-<fecha>.tar.gz
```

---

## 🚀 Flujo de Trabajo Recomendado

### Primera vez (VM nueva)

```bash
# 1. Configurar la VM
./azure-vm-setup.sh

# 2. Cerrar sesión y volver a conectar
exit
ssh -i ~/.ssh/vm-ecommerce-key.pem azureuser@<IP_PUBLICA>

# 3. Clonar el repositorio
cd ~/projects
git clone <URL_DEL_REPO>
cd ecommerce-microservice-backend-app

# 4. Dar permisos a los scripts
chmod +x scripts/*.sh

# 5. Desplegar servicios
./scripts/deploy-gradual.sh

# 6. Verificar estado
./scripts/check-services.sh
```

### Mantenimiento regular

```bash
# Verificar estado de servicios
./scripts/check-services.sh

# Realizar backup manual
./scripts/backup.sh

# Reiniciar servicios si es necesario
docker-compose -f compose.yml restart <servicio>

# Ver logs
docker-compose -f compose.yml logs -f <servicio>
```

### Actualización de servicios

```bash
# 1. Hacer backup
./scripts/backup.sh

# 2. Detener servicios
docker-compose -f compose.yml down

# 3. Actualizar código
git pull

# 4. Reconstruir imágenes (si es necesario)
docker-compose -f compose.yml build

# 5. Desplegar nuevamente
./scripts/deploy-gradual.sh

# 6. Verificar
./scripts/check-services.sh
```

---

## 🔧 Troubleshooting

### Script falla con "Permission denied"
```bash
chmod +x scripts/<nombre-script>.sh
```

### Servicios no inician correctamente
```bash
# Ver logs
docker-compose -f compose.yml logs <servicio>

# Verificar recursos
docker stats
free -h
df -h

# Reiniciar servicio específico
docker-compose -f compose.yml restart <servicio>
```

### Elasticsearch no inicia
```bash
# Verificar límites de memoria virtual
sysctl vm.max_map_count

# Si es menor a 262144, ejecutar:
sudo sysctl -w vm.max_map_count=262144
```

### Problemas de memoria
```bash
# Verificar swap
free -h

# Limpiar recursos de Docker
docker system prune -a --volumes

# Reiniciar servicios de observabilidad
docker-compose -f compose.yml restart elasticsearch logstash kibana
```

---

## 📊 Monitoreo

### Ver uso de recursos en tiempo real
```bash
# CPU y memoria por contenedor
docker stats

# Uso general del sistema
htop

# Uso de disco
df -h

# Uso de red
sudo iftop
```

### Acceder a servicios de monitoreo

Desde tu navegador (reemplaza `<IP_PUBLICA>` con la IP de tu VM):

- **Prometheus**: `http://<IP_PUBLICA>:9090`
- **Grafana**: `http://<IP_PUBLICA>:3000` (admin/admin)
- **Kibana**: `http://<IP_PUBLICA>:5601`
- **Jaeger**: `http://<IP_PUBLICA>:16686`
- **Eureka**: `http://<IP_PUBLICA>:8761`

---

## 🔒 Seguridad

### Recomendaciones

1. **Cambiar contraseñas por defecto**:
   - Grafana: admin/admin → cambiar en primer login
   - Elasticsearch: configurar autenticación si se expone públicamente

2. **Restringir acceso por IP** (en Azure NSG):
   - Permitir solo IPs conocidas para servicios de administración
   - Exponer solo API Gateway públicamente

3. **Usar HTTPS**:
   - Configurar certificados SSL/TLS
   - Usar Let's Encrypt para certificados gratuitos

4. **Actualizar regularmente**:
   ```bash
   sudo apt update && sudo apt upgrade -y
   docker-compose pull
   ```

---

## 📝 Notas Adicionales

- Todos los scripts están diseñados para Ubuntu 22.04 LTS
- Los backups se almacenan en `~/backups`
- Los logs de Docker se rotan automáticamente (max 10MB, 3 archivos)
- El swap configurado es de 8GB
- Los volúmenes persistentes se crean automáticamente

---

## 🆘 Soporte

Si encuentras problemas:

1. Revisa los logs: `docker-compose logs <servicio>`
2. Ejecuta el script de verificación: `./scripts/check-services.sh`
3. Consulta la guía completa: `docs/azure-vm-deployment-guide.md`
4. Abre un issue en el repositorio

---

**Última actualización**: 2025-11-30
