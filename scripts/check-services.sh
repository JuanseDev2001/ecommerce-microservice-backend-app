#!/bin/bash

###############################################################################
# Script de Verificación de Servicios
# Verifica que todos los microservicios estén corriendo correctamente
###############################################################################

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Función para verificar un servicio HTTP
check_http_service() {
    local name=$1
    local url=$2
    local max_attempts=${3:-30}
    local attempt=1
    
    echo -n "Verificando $name... "
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ OK${NC}"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}✗ TIMEOUT${NC}"
    return 1
}

# Función para verificar un contenedor Docker
check_container() {
    local container_name=$1
    
    if docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
        local status=$(docker inspect --format='{{.State.Status}}' "$container_name")
        if [ "$status" = "running" ]; then
            echo -e "${GREEN}✓${NC} $container_name: running"
            return 0
        else
            echo -e "${RED}✗${NC} $container_name: $status"
            return 1
        fi
    else
        echo -e "${RED}✗${NC} $container_name: not found"
        return 1
    fi
}

# Banner
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║   E-Commerce Microservices - Verificación de Servicios    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# 1. Verificar contenedores Docker
echo -e "\n${YELLOW}=== Verificando Contenedores Docker ===${NC}\n"

containers=(
    "zipkin"
    "service-discovery-container"
    "cloud-config-container"
    "api-gateway-container"
    "proxy-client-container"
    "order-service-container"
    "payment-service-container"
    "product-service-container"
    "shipping-service-container"
    "user-service-container"
    "favourite-service-container"
    "prometheus"
    "alertmanager"
    "grafana"
    "jaeger"
    "elasticsearch"
    "logstash"
    "kibana"
)

container_failures=0
for container in "${containers[@]}"; do
    if ! check_container "$container"; then
        ((container_failures++))
    fi
done

# 2. Verificar servicios HTTP
echo -e "\n${YELLOW}=== Verificando Servicios HTTP ===${NC}\n"

service_failures=0

# Infraestructura
echo -e "${BLUE}Infraestructura:${NC}"
check_http_service "Eureka" "http://localhost:8761" || ((service_failures++))
check_http_service "Config Server" "http://localhost:9296/actuator/health" || ((service_failures++))
check_http_service "API Gateway" "http://localhost:8080/actuator/health" || ((service_failures++))
check_http_service "Zipkin" "http://localhost:9411" || ((service_failures++))

# Microservicios
echo -e "\n${BLUE}Microservicios:${NC}"
check_http_service "Proxy Client" "http://localhost:8900/actuator/health" || ((service_failures++))
check_http_service "Order Service" "http://localhost:8300/actuator/health" || ((service_failures++))
check_http_service "Payment Service" "http://localhost:8400/actuator/health" || ((service_failures++))
check_http_service "Product Service" "http://localhost:8500/actuator/health" || ((service_failures++))
check_http_service "Shipping Service" "http://localhost:8600/actuator/health" || ((service_failures++))
check_http_service "User Service" "http://localhost:8700/actuator/health" || ((service_failures++))
check_http_service "Favourite Service" "http://localhost:8800/actuator/health" || ((service_failures++))

# Observabilidad
echo -e "\n${BLUE}Observabilidad:${NC}"
check_http_service "Prometheus" "http://localhost:9090/-/healthy" || ((service_failures++))
check_http_service "Alertmanager" "http://localhost:9093/-/healthy" || ((service_failures++))
check_http_service "Grafana" "http://localhost:3000/api/health" || ((service_failures++))
check_http_service "Jaeger" "http://localhost:16686" || ((service_failures++))
check_http_service "Elasticsearch" "http://localhost:9200/_cluster/health" || ((service_failures++))
check_http_service "Kibana" "http://localhost:5601/api/status" || ((service_failures++))

# 3. Verificar servicios registrados en Eureka
echo -e "\n${YELLOW}=== Servicios Registrados en Eureka ===${NC}\n"

if curl -s http://localhost:8761/eureka/apps > /dev/null 2>&1; then
    registered_services=$(curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort -u)
    
    if [ -n "$registered_services" ]; then
        echo "$registered_services" | while read service; do
            echo -e "${GREEN}✓${NC} $service"
        done
    else
        echo -e "${RED}No se encontraron servicios registrados${NC}"
    fi
else
    echo -e "${RED}No se pudo conectar a Eureka${NC}"
fi

# 4. Verificar uso de recursos
echo -e "\n${YELLOW}=== Uso de Recursos ===${NC}\n"

echo -e "${BLUE}Memoria:${NC}"
free -h | grep -E "Mem:|Swap:"

echo -e "\n${BLUE}Disco:${NC}"
df -h | grep -E "Filesystem|/dev/sda1"

echo -e "\n${BLUE}Top 5 Contenedores por Uso de CPU:${NC}"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | head -n 6

# 5. Resumen
echo -e "\n${YELLOW}=== Resumen ===${NC}\n"

total_containers=${#containers[@]}
total_services=17  # Número total de servicios HTTP verificados

echo "Contenedores: $((total_containers - container_failures))/$total_containers corriendo"
echo "Servicios HTTP: $((total_services - service_failures))/$total_services respondiendo"

if [ $container_failures -eq 0 ] && [ $service_failures -eq 0 ]; then
    echo -e "\n${GREEN}✓ Todos los servicios están funcionando correctamente${NC}"
    exit 0
else
    echo -e "\n${RED}✗ Algunos servicios tienen problemas${NC}"
    echo -e "\nPara ver logs de un servicio específico:"
    echo "  docker-compose -f compose.yml logs <nombre-servicio>"
    echo -e "\nPara reiniciar un servicio:"
    echo "  docker-compose -f compose.yml restart <nombre-servicio>"
    exit 1
fi
