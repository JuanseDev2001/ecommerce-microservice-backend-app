# ✅ Checklist de Despliegue en Azure VM

## 📋 Preparación (Antes de crear la VM)

- [ ] Cuenta de Azure activa
- [ ] Acceso al Portal de Azure
- [ ] Cliente SSH instalado (Linux/Mac/Windows Terminal)
- [ ] Conocer la IP pública que usarás para acceder

---

## 🖥️ Creación de la VM en Azure

### Configuración Básica
- [ ] Crear recurso → Máquina Virtual
- [ ] Nombre: `vm-ecommerce-app`
- [ ] Región: Seleccionar la más cercana
- [ ] Imagen: `Ubuntu Server 22.04 LTS - x64 Gen2`
- [ ] Tamaño: Mínimo `Standard_D4s_v3` (4 vCPUs, 16GB RAM)
- [ ] Recomendado: `Standard_D8s_v3` (8 vCPUs, 32GB RAM)

### Autenticación
- [ ] Tipo: `Clave pública SSH`
- [ ] Usuario: `azureuser`
- [ ] Generar nuevo par de claves: `vm-ecommerce-key`
- [ ] **IMPORTANTE**: Descargar y guardar el archivo `.pem`

### Discos
- [ ] Tipo: `SSD Premium`
- [ ] Tamaño: Mínimo 100GB, recomendado 200GB

### Redes
- [ ] Red virtual: Crear nueva `vnet-ecommerce`
- [ ] IP pública: Crear nueva `pip-ecommerce-vm`
- [ ] NSG: Crear nuevo `nsg-ecommerce-vm`

### Puertos Iniciales
- [ ] SSH (22)
- [ ] HTTP (80)
- [ ] HTTPS (443)

### Finalizar
- [ ] Revisar configuración
- [ ] Crear VM
- [ ] Esperar 5-10 minutos
- [ ] Copiar IP pública de la VM

---

## 🔐 Configuración de Seguridad (NSG)

### Agregar Reglas de Puerto en Azure Portal

**Opción 1: Regla consolidada (más simple)**
- [ ] Ir a NSG → Reglas de entrada → Agregar
- [ ] Puertos: `3000,5000,5601,8080,8300-8900,9090,9093,9200,9296,9411,8761,16686`
- [ ] Protocolo: TCP
- [ ] Acción: Permitir
- [ ] Nombre: `Allow-Microservices-All`

**Opción 2: Reglas individuales (más seguro)**
- [ ] Eureka: 8761
- [ ] Config Server: 9296
- [ ] API Gateway: 8080
- [ ] Proxy Client: 8900
- [ ] Order Service: 8300
- [ ] Payment Service: 8400
- [ ] Product Service: 8500
- [ ] Shipping Service: 8600
- [ ] User Service: 8700
- [ ] Favourite Service: 8800
- [ ] Prometheus: 9090
- [ ] Grafana: 3000
- [ ] Kibana: 5601
- [ ] Jaeger: 16686
- [ ] Elasticsearch: 9200
- [ ] Logstash: 5000
- [ ] Alertmanager: 9093
- [ ] Zipkin: 9411

---

## 🔌 Conexión Inicial a la VM

### Linux/Mac
```bash
# Configurar permisos de la clave
chmod 400 ~/.ssh/vm-ecommerce-key.pem

# Conectar por SSH
ssh -i ~/.ssh/vm-ecommerce-key.pem azureuser@<IP_PUBLICA>
```

### Windows
- [ ] Usar PuTTY, Windows Terminal con WSL, o Azure Cloud Shell
- [ ] Convertir `.pem` a `.ppk` si usas PuTTY

### Verificación
- [ ] Conexión SSH exitosa
- [ ] Prompt muestra: `azureuser@vm-ecommerce-app:~$`

---

## ⚙️ Configuración Automática de la VM

### Opción A: Script Automático (Recomendado)

```bash
# Descargar el script de configuración
cd ~
git clone https://github.com/JuanseDev2001/ecommerce-microservice-backend-app.git ~/projects/ecommerce-microservice-backend-app

# O si ya tienes el proyecto localmente, súbelo con scp:
# scp -i ~/.ssh/vm-ecommerce-key.pem -r ./ecommerce-microservice-backend-app azureuser@<IP>:~/projects/

# Ejecutar script de configuración
cd ~/projects/ecommerce-microservice-backend-app
chmod +x scripts/azure-vm-setup.sh
./scripts/azure-vm-setup.sh
```

**Checklist del script**:
- [ ] Sistema actualizado
- [ ] Docker instalado
- [ ] Docker Compose instalado
- [ ] Java 11 instalado
- [ ] Maven instalado
- [ ] Swap configurado (8GB)
- [ ] Firewall (UFW) configurado
- [ ] Límites de sistema ajustados
- [ ] Directorios de datos creados

**IMPORTANTE**:
- [ ] Cerrar sesión SSH: `exit`
- [ ] Volver a conectar por SSH

---

### Opción B: Configuración Manual

Si prefieres hacerlo paso a paso, sigue la guía completa en:
`docs/azure-vm-deployment-guide.md`

---

## 🚀 Despliegue de la Aplicación

### Preparación
```bash
cd ~/projects/ecommerce-microservice-backend-app
```

### Verificar archivos
- [ ] `compose.yml` existe
- [ ] Directorio `prometheus/` existe
- [ ] Directorio `elk/` existe
- [ ] Scripts en `scripts/` tienen permisos de ejecución

### Despliegue Gradual (Recomendado)
```bash
./scripts/deploy-gradual.sh
```

**El script hará**:
- [ ] Fase 1: Infraestructura (Zipkin, Eureka, Config Server)
- [ ] Fase 2: Gateway (API Gateway, Proxy Client)
- [ ] Fase 3: Microservicios (6 servicios de negocio)
- [ ] Fase 4: Observabilidad (Prometheus, Grafana, ELK, Jaeger)

**Tiempo estimado**: 10-15 minutos

### Despliegue Rápido (Alternativa)
```bash
docker-compose -f compose.yml up -d
```

---

## ✅ Verificación del Despliegue

### Ejecutar script de verificación
```bash
./scripts/check-services.sh
```

**Debe mostrar**:
- [ ] Todos los contenedores corriendo
- [ ] Todos los servicios HTTP respondiendo
- [ ] Servicios registrados en Eureka
- [ ] Uso de recursos aceptable

### Verificación manual

**Contenedores Docker**:
```bash
docker ps
```
- [ ] 18+ contenedores corriendo

**Servicios en Eureka**:
```bash
curl http://localhost:8761
```
- [ ] Eureka UI accesible
- [ ] 9 servicios registrados

**API Gateway**:
```bash
curl http://localhost:8080/actuator/health
```
- [ ] Respuesta: `{"status":"UP"}`

---

## 🌐 Acceso desde el Navegador

Reemplaza `<IP_PUBLICA>` con la IP de tu VM.

### Infraestructura
- [ ] Eureka: `http://<IP_PUBLICA>:8761`
- [ ] Config Server: `http://<IP_PUBLICA>:9296/actuator/health`
- [ ] API Gateway: `http://<IP_PUBLICA>:8080/actuator/health`

### Microservicios (Swagger UI)
- [ ] User Service: `http://<IP_PUBLICA>:8700/swagger-ui.html`
- [ ] Product Service: `http://<IP_PUBLICA>:8500/swagger-ui.html`
- [ ] Order Service: `http://<IP_PUBLICA>:8300/swagger-ui.html`
- [ ] Payment Service: `http://<IP_PUBLICA>:8400/swagger-ui.html`
- [ ] Shipping Service: `http://<IP_PUBLICA>:8600/swagger-ui.html`
- [ ] Favourite Service: `http://<IP_PUBLICA>:8800/swagger-ui.html`
- [ ] Proxy Client: `http://<IP_PUBLICA>:8900/swagger-ui.html`

### Observabilidad
- [ ] Prometheus: `http://<IP_PUBLICA>:9090`
- [ ] Grafana: `http://<IP_PUBLICA>:3000` (admin/admin)
- [ ] Kibana: `http://<IP_PUBLICA>:5601`
- [ ] Jaeger: `http://<IP_PUBLICA>:16686`
- [ ] Zipkin: `http://<IP_PUBLICA>:9411`

---

## 📊 Configuración de Grafana

- [ ] Acceder a `http://<IP_PUBLICA>:3000`
- [ ] Login: `admin` / `admin`
- [ ] Cambiar contraseña
- [ ] Agregar Data Source → Prometheus
  - URL: `http://prometheus:9090`
  - Save & Test
- [ ] Importar dashboards:
  - Dashboard ID `3662` (Prometheus Stats)
  - Dashboard ID `1860` (Node Exporter)
  - Dashboard ID `11074` (Spring Boot)

---

## 🧪 Ejecutar Tests

```bash
cd ~/projects/ecommerce-microservice-backend-app
chmod +x test-em-all.sh
./test-em-all.sh
```

- [ ] Todos los tests pasan
- [ ] No hay errores críticos

---

## 💾 Configurar Backups

### Backup manual
```bash
./scripts/backup.sh
```

### Backup automático (diario a las 2 AM)
```bash
(crontab -l 2>/dev/null; echo "0 2 * * * $HOME/projects/ecommerce-microservice-backend-app/scripts/backup.sh") | crontab -
```

- [ ] Backup manual ejecutado exitosamente
- [ ] Cron job configurado
- [ ] Verificar: `crontab -l`

---

## 🔄 Configurar Auto-inicio

```bash
sudo nano /etc/systemd/system/ecommerce-microservices.service
```

Copiar contenido del archivo de la guía, luego:

```bash
sudo systemctl daemon-reload
sudo systemctl enable ecommerce-microservices.service
sudo systemctl start ecommerce-microservices.service
sudo systemctl status ecommerce-microservices.service
```

- [ ] Servicio creado
- [ ] Servicio habilitado
- [ ] Servicio iniciado
- [ ] Estado: `active (running)`

---

## 📈 Monitoreo Continuo

### Comandos útiles

**Ver logs en tiempo real**:
```bash
docker-compose -f compose.yml logs -f
```

**Ver logs de un servicio específico**:
```bash
docker-compose -f compose.yml logs -f <servicio>
```

**Ver uso de recursos**:
```bash
docker stats
htop
```

**Ver estado de servicios**:
```bash
./scripts/check-services.sh
```

---

## 🛠️ Troubleshooting Común

### Problema: Servicio no inicia
```bash
# Ver logs
docker-compose -f compose.yml logs <servicio>

# Reiniciar servicio
docker-compose -f compose.yml restart <servicio>
```

### Problema: Elasticsearch no inicia
```bash
# Verificar límites
sysctl vm.max_map_count

# Ajustar si es necesario
sudo sysctl -w vm.max_map_count=262144
```

### Problema: Memoria insuficiente
```bash
# Ver memoria
free -h

# Limpiar recursos Docker
docker system prune -a

# Reiniciar servicios pesados
docker-compose -f compose.yml restart elasticsearch logstash kibana
```

### Problema: No se puede acceder desde navegador
- [ ] Verificar NSG en Azure Portal
- [ ] Verificar UFW: `sudo ufw status`
- [ ] Verificar que el servicio esté corriendo: `docker ps`
- [ ] Verificar puerto: `sudo netstat -tlnp | grep <puerto>`

---

## ✨ Optimizaciones Opcionales

### Configurar dominio personalizado
- [ ] Comprar dominio
- [ ] Configurar DNS → IP pública de la VM
- [ ] Instalar Nginx como reverse proxy
- [ ] Configurar SSL con Let's Encrypt

### Mejorar seguridad
- [ ] Configurar Azure Key Vault
- [ ] Implementar OAuth2/JWT
- [ ] Configurar WAF
- [ ] Restringir NSG a IPs específicas

### Escalar
- [ ] Configurar Azure Load Balancer
- [ ] Crear VM Scale Set
- [ ] Migrar a Azure Kubernetes Service (AKS)

---

## 📚 Recursos

- [ ] Guía completa: `docs/azure-vm-deployment-guide.md`
- [ ] README de scripts: `scripts/README.md`
- [ ] Documentación del proyecto: `README.md`

---

## ✅ Checklist Final de Verificación

- [ ] VM creada y accesible
- [ ] Todos los puertos configurados
- [ ] Software instalado (Docker, Java, Maven)
- [ ] Proyecto clonado
- [ ] Servicios desplegados
- [ ] Todos los contenedores corriendo
- [ ] Eureka muestra todos los servicios
- [ ] API Gateway responde
- [ ] Prometheus recolectando métricas
- [ ] Grafana configurado
- [ ] Tests pasan correctamente
- [ ] Backups configurados
- [ ] Auto-inicio configurado
- [ ] Acceso desde navegador funciona

---

## 🎉 ¡Felicidades!

Si todos los items están marcados, tu aplicación de microservicios está corriendo exitosamente en Azure.

**Próximos pasos sugeridos**:
1. Explorar los dashboards de Grafana
2. Revisar traces en Jaeger
3. Probar los endpoints con Swagger UI
4. Configurar alertas en Prometheus
5. Personalizar dashboards de Kibana

---

**Fecha de creación**: 2025-11-30  
**Versión**: 1.0
