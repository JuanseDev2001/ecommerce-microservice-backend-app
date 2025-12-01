#!/bin/bash

###############################################################################
# Script de Configuración Automática para Azure VM
# Este script instala y configura todo lo necesario para correr el proyecto
###############################################################################

set -e  # Salir si hay algún error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir mensajes
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Verificar que se ejecuta en Ubuntu
if [ ! -f /etc/lsb-release ]; then
    print_error "Este script está diseñado para Ubuntu"
    exit 1
fi

print_message "=== Iniciando configuración de Azure VM para E-Commerce Microservices ==="

# 1. Actualizar el sistema
print_message "Actualizando el sistema..."
sudo apt update
sudo apt upgrade -y

# 2. Instalar utilidades básicas
print_message "Instalando utilidades básicas..."
sudo apt install -y \
    curl \
    wget \
    git \
    vim \
    nano \
    net-tools \
    htop \
    iotop \
    jq \
    unzip \
    apt-transport-https \
    ca-certificates \
    software-properties-common

# 3. Instalar Docker
print_message "Instalando Docker..."
if ! command -v docker &> /dev/null; then
    # Agregar clave GPG de Docker
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    
    # Agregar repositorio
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # Instalar Docker
    sudo apt update
    sudo apt install -y docker-ce docker-ce-cli containerd.io
    
    # Agregar usuario al grupo docker
    sudo usermod -aG docker $USER
    
    print_message "Docker instalado correctamente"
else
    print_warning "Docker ya está instalado"
fi

# 4. Instalar Docker Compose
print_message "Instalando Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    print_message "Docker Compose instalado correctamente"
else
    print_warning "Docker Compose ya está instalado"
fi

# 5. Instalar Java 11
print_message "Instalando Java 11..."
if ! command -v java &> /dev/null; then
    sudo apt install -y openjdk-11-jdk
    
    # Configurar JAVA_HOME
    echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' >> ~/.bashrc
    echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
    
    print_message "Java 11 instalado correctamente"
else
    print_warning "Java ya está instalado"
fi

# 6. Instalar Maven
print_message "Instalando Maven..."
if ! command -v mvn &> /dev/null; then
    sudo apt install -y maven
    print_message "Maven instalado correctamente"
else
    print_warning "Maven ya está instalado"
fi

# 7. Configurar límites del sistema para Elasticsearch
print_message "Configurando límites del sistema..."
sudo sysctl -w vm.max_map_count=262144
echo 'vm.max_map_count=262144' | sudo tee -a /etc/sysctl.conf

# 8. Configurar Swap (8GB)
print_message "Configurando Swap..."
if [ ! -f /swapfile ]; then
    sudo fallocate -l 8G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    print_message "Swap configurado correctamente"
else
    print_warning "Swap ya está configurado"
fi

# 9. Configurar Docker daemon
print_message "Configurando Docker daemon..."
sudo mkdir -p /etc/docker
cat << EOF | sudo tee /etc/docker/daemon.json
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
EOF

sudo systemctl restart docker

# 10. Configurar firewall (UFW)
print_message "Configurando firewall..."
sudo ufw --force enable
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 3000/tcp  # Grafana
sudo ufw allow 5000/tcp  # Logstash
sudo ufw allow 5601/tcp  # Kibana
sudo ufw allow 8080/tcp  # API Gateway
sudo ufw allow 8300/tcp  # Order Service
sudo ufw allow 8400/tcp  # Payment Service
sudo ufw allow 8500/tcp  # Product Service
sudo ufw allow 8600/tcp  # Shipping Service
sudo ufw allow 8700/tcp  # User Service
sudo ufw allow 8800/tcp  # Favourite Service
sudo ufw allow 8900/tcp  # Proxy Client
sudo ufw allow 9090/tcp  # Prometheus
sudo ufw allow 9093/tcp  # Alertmanager
sudo ufw allow 9200/tcp  # Elasticsearch
sudo ufw allow 9296/tcp  # Config Server
sudo ufw allow 9411/tcp  # Zipkin
sudo ufw allow 8761/tcp  # Eureka
sudo ufw allow 16686/tcp # Jaeger UI
sudo ufw reload

# 11. Crear directorios para datos persistentes
print_message "Creando directorios para datos persistentes..."
sudo mkdir -p /data/{grafana,prometheus,elasticsearch,jenkins}
sudo chown -R 472:472 /data/grafana
sudo chown -R 65534:65534 /data/prometheus
sudo chown -R 1000:1000 /data/elasticsearch
sudo chown -R 1000:1000 /data/jenkins

# 12. Crear directorio de proyectos
print_message "Creando directorio de proyectos..."
mkdir -p ~/projects
mkdir -p ~/backups

print_message "=== Configuración completada ==="
print_message ""
print_message "Versiones instaladas:"
echo "  - Docker: $(docker --version)"
echo "  - Docker Compose: $(docker-compose --version)"
echo "  - Java: $(java -version 2>&1 | head -n 1)"
echo "  - Maven: $(mvn -version | head -n 1)"
print_message ""
print_warning "IMPORTANTE: Cierra la sesión SSH y vuelve a conectarte para que los cambios de grupo de Docker tengan efecto"
print_message ""
print_message "Próximos pasos:"
print_message "  1. Cerrar sesión: exit"
print_message "  2. Volver a conectar por SSH"
print_message "  3. Clonar el repositorio: cd ~/projects && git clone <repo-url>"
print_message "  4. Ejecutar: cd ecommerce-microservice-backend-app && docker-compose -f compose.yml up -d"
