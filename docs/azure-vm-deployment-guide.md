# Guía Completa: Despliegue en Azure VM

## Tabla de Contenidos
1. [Requisitos Previos](#requisitos-previos)
2. [Creación de la Máquina Virtual](#creación-de-la-máquina-virtual)
3. [Configuración Inicial de la VM](#configuración-inicial-de-la-vm)
4. [Instalación de Software Necesario](#instalación-de-software-necesario)
5. [Configuración de Puertos y Seguridad](#configuración-de-puertos-y-seguridad)
6. [Despliegue del Proyecto](#despliegue-del-proyecto)
7. [Verificación y Monitoreo](#verificación-y-monitoreo)
8. [Troubleshooting](#troubleshooting)

---

## Requisitos Previos

### Recursos Necesarios para el Proyecto
Basado en el análisis del proyecto, necesitarás:

- **Microservicios**: 9 servicios principales
- **Infraestructura**: Eureka, Config Server, API Gateway, Zipkin
- **Observabilidad**: Prometheus, Grafana, ELK Stack (Elasticsearch, Logstash, Kibana), Jaeger
- **CI/CD**: Jenkins (opcional)

### Especificaciones Recomendadas de la VM
- **CPU**: Mínimo 8 vCPUs (recomendado: 16 vCPUs)
- **RAM**: Mínimo 16 GB (recomendado: 32 GB)
- **Disco**: Mínimo 100 GB SSD (recomendado: 200 GB)
- **Sistema Operativo**: Ubuntu 22.04 LTS

---

## Creación de la Máquina Virtual

### Paso 1: Acceder al Portal de Azure

1. Ingresa a [Azure Portal](https://portal.azure.com)
2. Inicia sesión con tu cuenta de Azure

### Paso 2: Crear una Nueva Máquina Virtual

#### 2.1 Iniciar la Creación
1. En el portal de Azure, haz clic en **"Crear un recurso"**
2. Busca **"Máquina virtual"** o **"Virtual Machine"**
3. Haz clic en **"Crear"**

#### 2.2 Configuración Básica (Pestaña "Basics")

**Detalles del Proyecto:**
- **Suscripción**: Selecciona tu suscripción de Azure
- **Grupo de recursos**: 
  - Opción 1: Crear nuevo → Nombre: `rg-ecommerce-microservices`
  - Opción 2: Usar uno existente

**Detalles de la Instancia:**
- **Nombre de la máquina virtual**: `vm-ecommerce-app`
- **Región**: Selecciona la más cercana (ej: `East US`, `West Europe`, `Brazil South`)
- **Opciones de disponibilidad**: `No se requiere redundancia de infraestructura`
- **Tipo de seguridad**: `Standard`
- **Imagen**: `Ubuntu Server 22.04 LTS - x64 Gen2`
- **Tamaño**: 
  - Haz clic en "Ver todos los tamaños"
  - **Recomendado**: `Standard_D8s_v3` (8 vCPUs, 32 GB RAM)
  - **Mínimo**: `Standard_D4s_v3` (4 vCPUs, 16 GB RAM)
  - **Óptimo**: `Standard_D16s_v3` (16 vCPUs, 64 GB RAM)

**Cuenta de Administrador:**
- **Tipo de autenticación**: `Clave pública SSH`
- **Nombre de usuario**: `azureuser` (o el que prefieras)
- **Origen de clave pública SSH**: 
  - Opción 1: `Generar nuevo par de claves` → Nombre: `vm-ecommerce-key`
  - Opción 2: `Usar clave pública existente` (si ya tienes una)

**Reglas de Puerto de Entrada:**
- Selecciona: `Permitir los puertos seleccionados`
- **Puertos de entrada públicos**: 
  - `SSH (22)`
  - `HTTP (80)`
  - `HTTPS (443)`

#### 2.3 Configuración de Discos (Pestaña "Disks")

- **Tipo de disco del SO**: `SSD Premium` (mejor rendimiento)
- **Tamaño del disco**: `200 GB` (mínimo 100 GB)
- **Cifrado**: Dejar por defecto
- **Discos de datos**: (Opcional) Agregar un disco adicional de 100 GB para datos

#### 2.4 Configuración de Redes (Pestaña "Networking")

**Interfaz de Red:**
- **Red virtual**: Crear nueva → `vnet-ecommerce`
- **Subred**: `default (10.0.0.0/24)`
- **IP pública**: Crear nueva → `pip-ecommerce-vm`
- **Grupo de seguridad de red NIC**: `Avanzado`
- **Configurar grupo de seguridad de red**: Crear nuevo → `nsg-ecommerce-vm`

**Equilibrio de Carga:**
- Dejar sin configurar por ahora

#### 2.5 Configuración de Administración (Pestaña "Management")

- **Identidad**: Dejar por defecto
- **Apagado automático**: 
  - Habilitar si deseas (opcional)
  - Configurar hora: `11:00 PM` en tu zona horaria
- **Copia de seguridad**: Habilitar (recomendado)
- **Diagnósticos de arranque**: Habilitar

#### 2.6 Configuración Avanzada (Pestaña "Advanced")

- **Extensiones**: Ninguna por ahora
- **Datos personalizados**: Dejar vacío
- **Cloud init**: Dejar vacío

#### 2.7 Etiquetas (Pestaña "Tags")

Agregar etiquetas para organización (opcional):
- `Environment`: `Production` o `Development`
- `Project`: `ecommerce-microservices`
- `Owner`: Tu nombre o equipo

#### 2.8 Revisar y Crear

1. Haz clic en **"Revisar y crear"**
2. Azure validará la configuración
3. Revisa el resumen y el costo estimado
4. Haz clic en **"Crear"**

#### 2.9 Descargar la Clave SSH

Si seleccionaste "Generar nuevo par de claves":
1. Se abrirá un diálogo para descargar la clave privada
2. **IMPORTANTE**: Descarga y guarda el archivo `.pem` en un lugar seguro
3. No podrás descargarlo nuevamente
4. En Linux/Mac, guárdalo en `~/.ssh/vm-ecommerce-key.pem`

**Espera 5-10 minutos** mientras Azure crea la VM.

---

## Configuración Inicial de la VM

### Paso 3: Conectarse a la VM

#### 3.1 Obtener la IP Pública

1. Ve a **"Máquinas virtuales"** en el portal de Azure
2. Selecciona tu VM `vm-ecommerce-app`
3. En la página de información general, copia la **"Dirección IP pública"**

#### 3.2 Configurar Permisos de la Clave SSH (Linux/Mac)

```bash
# Cambiar permisos de la clave
chmod 400 ~/.ssh/vm-ecommerce-key.pem
```

#### 3.3 Conectarse por SSH

```bash
# Reemplaza <IP_PUBLICA> con la IP de tu VM
ssh -i ~/.ssh/vm-ecommerce-key.pem azureuser@<IP_PUBLICA>
```

**Para Windows:**
- Usa **PuTTY** o **Windows Terminal** con WSL
- O usa **Azure Cloud Shell** desde el portal

#### 3.4 Verificar Conexión

Una vez conectado, deberías ver el prompt:
```bash
azureuser@vm-ecommerce-app:~$
```

---

## Instalación de Software Necesario

### Paso 4: Actualizar el Sistema

```bash
# Actualizar lista de paquetes
sudo apt update

# Actualizar paquetes instalados
sudo apt upgrade -y

# Instalar utilidades básicas
sudo apt install -y curl wget git vim net-tools htop
```

### Paso 5: Instalar Docker

```bash
# Instalar dependencias
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Agregar clave GPG de Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Agregar repositorio de Docker
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Actualizar lista de paquetes
sudo apt update

# Instalar Docker
sudo apt install -y docker-ce docker-ce-cli containerd.io

# Verificar instalación
docker --version

# Agregar usuario al grupo docker (para no usar sudo)
sudo usermod -aG docker $USER

# Aplicar cambios de grupo (o cerrar sesión y volver a conectar)
newgrp docker

# Verificar que funciona sin sudo
docker ps
```

### Paso 6: Instalar Docker Compose

```bash
# Descargar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Dar permisos de ejecución
sudo chmod +x /usr/local/bin/docker-compose

# Verificar instalación
docker-compose --version
```

### Paso 7: Instalar Java 11 (para compilación local si es necesario)

```bash
# Instalar OpenJDK 11
sudo apt install -y openjdk-11-jdk

# Verificar instalación
java -version
javac -version

# Configurar JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc
```

### Paso 8: Instalar Maven (opcional, para compilación)

```bash
# Instalar Maven
sudo apt install -y maven

# Verificar instalación
mvn -version
```

### Paso 9: Instalar utilidades adicionales

```bash
# Instalar jq (para procesar JSON)
sudo apt install -y jq

# Instalar netstat y otras herramientas de red
sudo apt install -y net-tools

# Instalar herramientas de monitoreo
sudo apt install -y htop iotop
```

---

## Configuración de Puertos y Seguridad

### Paso 10: Configurar el Grupo de Seguridad de Red (NSG)

Necesitas abrir los siguientes puertos para acceder a los servicios:

#### 10.1 Puertos Necesarios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| SSH | 22 | Acceso remoto |
| HTTP | 80 | Acceso web general |
| HTTPS | 443 | Acceso web seguro |
| API Gateway | 8080 | Gateway principal |
| Eureka | 8761 | Service Discovery |
| Config Server | 9296 | Configuración centralizada |
| Proxy Client | 8900 | Cliente proxy |
| Order Service | 8300 | Servicio de órdenes |
| Payment Service | 8400 | Servicio de pagos |
| Product Service | 8500 | Servicio de productos |
| Shipping Service | 8600 | Servicio de envíos |
| User Service | 8700 | Servicio de usuarios |
| Favourite Service | 8800 | Servicio de favoritos |
| Prometheus | 9090 | Métricas |
| Grafana | 3000 | Dashboards |
| Alertmanager | 9093 | Alertas |
| Zipkin | 9411 | Tracing |
| Jaeger UI | 16686 | Tracing UI |
| Elasticsearch | 9200 | Búsqueda y logs |
| Logstash | 5000 | Ingesta de logs |
| Kibana | 5601 | Visualización de logs |
| Jenkins | 8080 | CI/CD (si se usa) |

#### 10.2 Agregar Reglas de Entrada en Azure

1. Ve al portal de Azure
2. Navega a **"Grupos de seguridad de red"**
3. Selecciona `nsg-ecommerce-vm`
4. Haz clic en **"Reglas de seguridad de entrada"**
5. Haz clic en **"+ Agregar"**

**Crear regla para cada servicio:**

**Ejemplo: Regla para API Gateway**
- **Origen**: `Any` (o tu IP específica para mayor seguridad)
- **Rangos de puertos de origen**: `*`
- **Destino**: `Any`
- **Rangos de puertos de destino**: `8080`
- **Protocolo**: `TCP`
- **Acción**: `Permitir`
- **Prioridad**: `1000` (incrementar para cada regla)
- **Nombre**: `Allow-API-Gateway`

**Regla consolidada para todos los servicios (más simple):**
- **Origen**: `Any` (o tu IP)
- **Rangos de puertos de origen**: `*`
- **Destino**: `Any`
- **Rangos de puertos de destino**: `3000,5000,5601,8080,8300-8900,9090,9093,9200,9296,9411,9761,16686`
- **Protocolo**: `TCP`
- **Acción**: `Permitir`
- **Prioridad**: `1000`
- **Nombre**: `Allow-Microservices-All`

**⚠️ Nota de Seguridad:**
Para producción, es mejor:
- Usar un balanceador de carga
- Exponer solo el API Gateway (8080) y servicios de monitoreo
- Usar VPN o Azure Bastion para acceso administrativo
- Restringir el origen a IPs específicas

#### 10.3 Configurar Firewall en la VM (UFW)

```bash
# Habilitar UFW
sudo ufw enable

# Permitir SSH (importante, no te bloquees)
sudo ufw allow 22/tcp

# Permitir HTTP y HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Permitir puertos de microservicios
sudo ufw allow 3000/tcp    # Grafana
sudo ufw allow 5000/tcp    # Logstash
sudo ufw allow 5601/tcp    # Kibana
sudo ufw allow 8080/tcp    # API Gateway
sudo ufw allow 8300/tcp    # Order Service
sudo ufw allow 8400/tcp    # Payment Service
sudo ufw allow 8500/tcp    # Product Service
sudo ufw allow 8600/tcp    # Shipping Service
sudo ufw allow 8700/tcp    # User Service
sudo ufw allow 8800/tcp    # Favourite Service
sudo ufw allow 8900/tcp    # Proxy Client
sudo ufw allow 9090/tcp    # Prometheus
sudo ufw allow 9093/tcp    # Alertmanager
sudo ufw allow 9200/tcp    # Elasticsearch
sudo ufw allow 9296/tcp    # Config Server
sudo ufw allow 9411/tcp    # Zipkin/Jaeger
sudo ufw allow 8761/tcp    # Eureka
sudo ufw allow 16686/tcp   # Jaeger UI

# Verificar reglas
sudo ufw status numbered

# Recargar firewall
sudo ufw reload
```

---

## Despliegue del Proyecto

### Paso 11: Clonar el Repositorio

```bash
# Crear directorio para proyectos
mkdir -p ~/projects
cd ~/projects

# Clonar el repositorio
git clone https://github.com/JuanseDev2001/ecommerce-microservice-backend-app.git

# Entrar al directorio
cd ecommerce-microservice-backend-app

# Verificar contenido
ls -la
```

### Paso 12: Configurar Variables de Entorno

```bash
# Crear archivo de variables de entorno
cat > .env << 'EOF'
# Configuración de la aplicación
SPRING_PROFILES_ACTIVE=dev

# Configuración de Docker
COMPOSE_PROJECT_NAME=ecommerce-microservices

# Configuración de recursos
JAVA_OPTS=-Xmx512m -Xms256m
ES_JAVA_OPTS=-Xms512m -Xmx512m
LS_JAVA_OPTS=-Xmx512m -Xms512m

# IP pública de la VM (reemplazar con tu IP)
PUBLIC_IP=<TU_IP_PUBLICA>
EOF

# Editar el archivo y reemplazar <TU_IP_PUBLICA>
nano .env
```

### Paso 13: Revisar y Ajustar Docker Compose

```bash
# Ver el archivo compose.yml
cat compose.yml

# Si necesitas hacer ajustes, edítalo
nano compose.yml
```

**Ajustes recomendados para VM con recursos limitados:**

```yaml
# En cada servicio de Java, agregar límites de memoria:
environment:
  - JAVA_OPTS=-Xmx512m -Xms256m
deploy:
  resources:
    limits:
      memory: 1G
    reservations:
      memory: 512M
```

### Paso 14: Descargar las Imágenes Docker

```bash
# Descargar todas las imágenes (esto puede tomar 10-20 minutos)
docker-compose -f compose.yml pull

# Verificar imágenes descargadas
docker images
```

### Paso 15: Iniciar los Servicios

#### Opción 1: Iniciar todo de una vez

```bash
# Iniciar todos los servicios
docker-compose -f compose.yml up -d

# Ver logs en tiempo real
docker-compose -f compose.yml logs -f
```

#### Opción 2: Iniciar por etapas (recomendado para primera vez)

```bash
# Paso 1: Iniciar infraestructura base
docker-compose -f compose.yml up -d zipkin service-discovery-container cloud-config-container

# Esperar 30 segundos
sleep 30

# Verificar que estén corriendo
docker-compose -f compose.yml ps

# Paso 2: Iniciar API Gateway y Proxy
docker-compose -f compose.yml up -d api-gateway-container proxy-client-container

# Esperar 30 segundos
sleep 30

# Paso 3: Iniciar microservicios de negocio
docker-compose -f compose.yml up -d order-service-container payment-service-container product-service-container shipping-service-container user-service-container favourite-service-container

# Esperar 30 segundos
sleep 30

# Paso 4: Iniciar stack de observabilidad
docker-compose -f compose.yml up -d prometheus alertmanager grafana jaeger elasticsearch logstash kibana

# Ver todos los contenedores
docker-compose -f compose.yml ps
```

### Paso 16: Verificar el Estado de los Servicios

```bash
# Ver todos los contenedores corriendo
docker ps

# Ver logs de un servicio específico
docker-compose -f compose.yml logs api-gateway-container

# Ver logs de todos los servicios
docker-compose -f compose.yml logs

# Ver uso de recursos
docker stats

# Verificar salud de los contenedores
docker-compose -f compose.yml ps
```

### Paso 17: Esperar a que los Servicios Estén Listos

```bash
# Crear script de verificación
cat > check-services.sh << 'EOF'
#!/bin/bash

echo "Verificando servicios..."

# Función para verificar un servicio
check_service() {
    local name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo -n "Verificando $name... "
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo "✓ OK"
            return 0
        fi
        sleep 10
        attempt=$((attempt + 1))
    done
    
    echo "✗ TIMEOUT"
    return 1
}

# Verificar servicios principales
check_service "Eureka" "http://localhost:8761"
check_service "Config Server" "http://localhost:9296/actuator/health"
check_service "API Gateway" "http://localhost:8080/actuator/health"
check_service "Prometheus" "http://localhost:9090/-/healthy"
check_service "Grafana" "http://localhost:3000/api/health"
check_service "Kibana" "http://localhost:5601/api/status"
check_service "Jaeger" "http://localhost:16686"

echo ""
echo "Verificación completada!"
EOF

# Dar permisos de ejecución
chmod +x check-services.sh

# Ejecutar verificación
./check-services.sh
```

---

## Verificación y Monitoreo

### Paso 18: Acceder a los Servicios

Desde tu navegador local, accede a los siguientes URLs (reemplaza `<IP_PUBLICA>` con la IP de tu VM):

#### Servicios de Infraestructura
- **Eureka (Service Discovery)**: `http://<IP_PUBLICA>:8761`
- **API Gateway Health**: `http://<IP_PUBLICA>:8080/actuator/health`
- **Swagger UI (Proxy Client)**: `http://<IP_PUBLICA>:8900/swagger-ui.html`

#### Servicios de Observabilidad
- **Prometheus**: `http://<IP_PUBLICA>:9090`
- **Grafana**: `http://<IP_PUBLICA>:3000` (usuario: `admin`, contraseña: `admin`)
- **Kibana**: `http://<IP_PUBLICA>:5601`
- **Jaeger**: `http://<IP_PUBLICA>:16686`
- **Zipkin**: `http://<IP_PUBLICA>:9411`

#### Microservicios
- **User Service**: `http://<IP_PUBLICA>:8700/swagger-ui.html`
- **Product Service**: `http://<IP_PUBLICA>:8500/swagger-ui.html`
- **Order Service**: `http://<IP_PUBLICA>:8300/swagger-ui.html`
- **Payment Service**: `http://<IP_PUBLICA>:8400/swagger-ui.html`
- **Shipping Service**: `http://<IP_PUBLICA>:8600/swagger-ui.html`
- **Favourite Service**: `http://<IP_PUBLICA>:8800/swagger-ui.html`

### Paso 19: Configurar Grafana

1. Accede a Grafana: `http://<IP_PUBLICA>:3000`
2. Login con `admin` / `admin`
3. Cambia la contraseña cuando se solicite
4. Agregar Prometheus como Data Source:
   - Ve a **Configuration** → **Data Sources**
   - Click **Add data source**
   - Selecciona **Prometheus**
   - URL: `http://prometheus:9090`
   - Click **Save & Test**

5. Importar dashboards:
   - Ve a **Create** → **Import**
   - Usa estos IDs de dashboards públicos:
     - `3662` - Prometheus 2.0 Stats
     - `1860` - Node Exporter Full
     - `11074` - Spring Boot Statistics

### Paso 20: Ejecutar Tests de Integración

```bash
# Dar permisos de ejecución al script de tests
chmod +x test-em-all.sh

# Ejecutar tests
./test-em-all.sh

# O si quieres iniciar, probar y detener:
./test-em-all.sh start stop
```

### Paso 21: Monitorear Recursos de la VM

```bash
# Ver uso de CPU y memoria
htop

# Ver uso de disco
df -h

# Ver uso de red
sudo iftop

# Ver logs del sistema
sudo journalctl -f

# Ver estadísticas de Docker
docker stats
```

---

## Configuración de Persistencia y Backups

### Paso 22: Configurar Volúmenes Persistentes

```bash
# Crear directorios para datos persistentes
sudo mkdir -p /data/grafana
sudo mkdir -p /data/prometheus
sudo mkdir -p /data/elasticsearch
sudo mkdir -p /data/jenkins

# Cambiar permisos
sudo chown -R 472:472 /data/grafana  # UID de Grafana
sudo chown -R 65534:65534 /data/prometheus  # UID de Prometheus
sudo chown -R 1000:1000 /data/elasticsearch  # UID de Elasticsearch
sudo chown -R 1000:1000 /data/jenkins  # UID de Jenkins
```

### Paso 23: Script de Backup

```bash
# Crear script de backup
cat > ~/backup-ecommerce.sh << 'EOF'
#!/bin/bash

BACKUP_DIR="/home/azureuser/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/ecommerce-backup-$DATE.tar.gz"

# Crear directorio de backups
mkdir -p $BACKUP_DIR

echo "Iniciando backup..."

# Backup de volúmenes de Docker
docker run --rm \
  -v jenkins_home:/data/jenkins \
  -v grafana-storage:/data/grafana \
  -v elasticsearch-data:/data/elasticsearch \
  -v $BACKUP_DIR:/backup \
  ubuntu tar czf /backup/docker-volumes-$DATE.tar.gz /data

# Backup de configuraciones
cd ~/projects/ecommerce-microservice-backend-app
tar czf $BACKUP_DIR/config-$DATE.tar.gz \
  compose.yml \
  prometheus/ \
  elk/ \
  .env

echo "Backup completado: $BACKUP_FILE"

# Limpiar backups antiguos (mantener últimos 7 días)
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "Backups antiguos eliminados"
EOF

# Dar permisos de ejecución
chmod +x ~/backup-ecommerce.sh

# Agregar a crontab para ejecutar diariamente a las 2 AM
(crontab -l 2>/dev/null; echo "0 2 * * * /home/azureuser/backup-ecommerce.sh") | crontab -
```

---

## Optimización y Mejores Prácticas

### Paso 24: Configurar Swap (si la RAM es limitada)

```bash
# Verificar swap actual
free -h

# Crear archivo de swap de 8GB
sudo fallocate -l 8G /swapfile

# Configurar permisos
sudo chmod 600 /swapfile

# Crear swap
sudo mkswap /swapfile

# Activar swap
sudo swapon /swapfile

# Hacer permanente
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verificar
free -h
```

### Paso 25: Configurar Límites de Docker

```bash
# Editar daemon.json de Docker
sudo nano /etc/docker/daemon.json
```

Agregar:
```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "default-ulimits": {
    "nofile": {
      "Name": "nofile",
      "Hard": 64000,
      "Soft": 64000
    }
  }
}
```

```bash
# Reiniciar Docker
sudo systemctl restart docker
```

### Paso 26: Configurar Auto-inicio de Servicios

```bash
# Crear servicio systemd
sudo nano /etc/systemd/system/ecommerce-microservices.service
```

Contenido:
```ini
[Unit]
Description=E-Commerce Microservices
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/azureuser/projects/ecommerce-microservice-backend-app
ExecStart=/usr/local/bin/docker-compose -f compose.yml up -d
ExecStop=/usr/local/bin/docker-compose -f compose.yml down
User=azureuser

[Install]
WantedBy=multi-user.target
```

```bash
# Recargar systemd
sudo systemctl daemon-reload

# Habilitar servicio
sudo systemctl enable ecommerce-microservices.service

# Iniciar servicio
sudo systemctl start ecommerce-microservices.service

# Verificar estado
sudo systemctl status ecommerce-microservices.service
```

---

## Troubleshooting

### Problemas Comunes y Soluciones

#### 1. Contenedores que no inician

```bash
# Ver logs del contenedor
docker-compose -f compose.yml logs <nombre-servicio>

# Ver últimas 100 líneas
docker-compose -f compose.yml logs --tail=100 <nombre-servicio>

# Reiniciar un servicio específico
docker-compose -f compose.yml restart <nombre-servicio>
```

#### 2. Problemas de memoria

```bash
# Ver uso de memoria
free -h
docker stats

# Limpiar recursos de Docker
docker system prune -a --volumes

# Reiniciar servicios pesados uno por uno
docker-compose -f compose.yml restart elasticsearch
```

#### 3. Servicios no se registran en Eureka

```bash
# Verificar que Eureka esté corriendo
curl http://localhost:8761

# Ver logs de Eureka
docker-compose -f compose.yml logs service-discovery-container

# Reiniciar servicios en orden
docker-compose -f compose.yml restart service-discovery-container
sleep 30
docker-compose -f compose.yml restart api-gateway-container
```

#### 4. No se puede acceder desde el navegador

```bash
# Verificar que el puerto esté abierto en la VM
sudo netstat -tlnp | grep <puerto>

# Verificar firewall
sudo ufw status

# Verificar NSG en Azure Portal
# Ir a: VM → Networking → Inbound port rules
```

#### 5. Elasticsearch no inicia

```bash
# Aumentar límites de memoria virtual
sudo sysctl -w vm.max_map_count=262144

# Hacer permanente
echo 'vm.max_map_count=262144' | sudo tee -a /etc/sysctl.conf

# Reiniciar Elasticsearch
docker-compose -f compose.yml restart elasticsearch
```

#### 6. Disco lleno

```bash
# Ver uso de disco
df -h

# Limpiar logs de Docker
sudo sh -c "truncate -s 0 /var/lib/docker/containers/*/*-json.log"

# Limpiar imágenes no usadas
docker image prune -a

# Limpiar volúmenes no usados
docker volume prune
```

### Comandos Útiles de Diagnóstico

```bash
# Ver todos los contenedores (incluso detenidos)
docker ps -a

# Ver uso de recursos en tiempo real
docker stats

# Ver redes de Docker
docker network ls

# Inspeccionar un contenedor
docker inspect <container-id>

# Ver logs del sistema
sudo journalctl -xe

# Ver procesos que usan más CPU
top

# Ver conexiones de red
sudo netstat -tulpn

# Verificar DNS
nslookup google.com

# Test de conectividad
ping 8.8.8.8
```

---

## Scripts de Utilidad

### Script de Reinicio Completo

```bash
cat > ~/restart-all.sh << 'EOF'
#!/bin/bash

echo "Deteniendo todos los servicios..."
cd ~/projects/ecommerce-microservice-backend-app
docker-compose -f compose.yml down

echo "Esperando 10 segundos..."
sleep 10

echo "Limpiando recursos..."
docker system prune -f

echo "Iniciando servicios..."
docker-compose -f compose.yml up -d

echo "Esperando a que los servicios estén listos..."
sleep 60

echo "Verificando estado..."
docker-compose -f compose.yml ps

echo "Listo!"
EOF

chmod +x ~/restart-all.sh
```

### Script de Monitoreo

```bash
cat > ~/monitor.sh << 'EOF'
#!/bin/bash

echo "=== Estado de Contenedores ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "=== Uso de Recursos ==="
docker stats --no-stream

echo ""
echo "=== Uso de Disco ==="
df -h | grep -E "Filesystem|/dev/sda"

echo ""
echo "=== Memoria del Sistema ==="
free -h

echo ""
echo "=== Servicios en Eureka ==="
curl -s http://localhost:8761/eureka/apps | grep "<app>" | sed 's/<app>//g' | sed 's/<\/app>//g'
EOF

chmod +x ~/monitor.sh
```

---

## Checklist Final

### ✅ Verificación de Despliegue

- [ ] VM creada y accesible por SSH
- [ ] Docker y Docker Compose instalados
- [ ] Puertos configurados en NSG y UFW
- [ ] Repositorio clonado
- [ ] Todos los contenedores corriendo (`docker ps`)
- [ ] Eureka accesible y muestra todos los servicios
- [ ] API Gateway responde en puerto 8080
- [ ] Prometheus recolectando métricas
- [ ] Grafana accesible y configurado
- [ ] Kibana accesible
- [ ] Jaeger accesible
- [ ] Tests de integración pasan (`./test-em-all.sh`)
- [ ] Backups configurados
- [ ] Auto-inicio configurado
- [ ] Monitoreo funcionando

---

## Próximos Pasos

1. **Configurar un dominio personalizado**
   - Comprar un dominio
   - Configurar DNS apuntando a la IP pública
   - Configurar Nginx como reverse proxy
   - Instalar certificados SSL con Let's Encrypt

2. **Implementar CI/CD**
   - Configurar Jenkins
   - Crear pipelines de despliegue
   - Automatizar builds y tests

3. **Mejorar seguridad**
   - Configurar Azure Key Vault para secretos
   - Implementar autenticación OAuth2
   - Configurar WAF (Web Application Firewall)

4. **Escalar horizontalmente**
   - Configurar Azure Load Balancer
   - Crear VM Scale Sets
   - Migrar a Azure Kubernetes Service (AKS)

---

## Recursos Adicionales

- [Documentación de Azure VMs](https://docs.microsoft.com/azure/virtual-machines/)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

---

## Soporte

Si encuentras problemas:
1. Revisa la sección de [Troubleshooting](#troubleshooting)
2. Verifica los logs: `docker-compose logs`
3. Consulta la documentación del proyecto
4. Abre un issue en el repositorio de GitHub

---

**¡Felicidades! Tu aplicación de microservicios está corriendo en Azure** 🎉
