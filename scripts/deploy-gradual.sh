#!/bin/bash

###############################################################################
# Script de Despliegue Gradual
# Inicia los servicios en el orden correcto con verificaciones
###############################################################################

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Función para esperar que un servicio esté listo
wait_for_service() {
    local name=$1
    local url=$2
    local max_wait=${3:-300}  # 5 minutos por defecto
    local elapsed=0
    
    echo -n "Esperando a que $name esté listo..."
    
    while [ $elapsed -lt $max_wait ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo -e " ${GREEN}✓ Listo${NC} (${elapsed}s)"
            return 0
        fi
        echo -n "."
        sleep 5
        elapsed=$((elapsed + 5))
    done
    
    echo -e " ${RED}✗ Timeout${NC}"
    return 1
}

# Banner
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║   E-Commerce Microservices - Despliegue Gradual           ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Verificar que estamos en el directorio correcto
if [ ! -f "compose.yml" ]; then
    echo -e "${RED}Error: No se encontró compose.yml${NC}"
    echo "Por favor, ejecuta este script desde el directorio del proyecto"
    exit 1
fi

# Preguntar si detener servicios existentes
echo -e "${YELLOW}¿Deseas detener los servicios existentes primero? (s/n)${NC}"
read -r response
if [[ "$response" =~ ^[Ss]$ ]]; then
    echo -e "\n${BLUE}Deteniendo servicios existentes...${NC}"
    docker-compose -f compose.yml down
    echo -e "${GREEN}Servicios detenidos${NC}"
    sleep 5
fi

# Fase 1: Infraestructura Base
echo -e "\n${YELLOW}=== Fase 1: Infraestructura Base ===${NC}\n"

echo "Iniciando Zipkin..."
docker-compose -f compose.yml up -d zipkin
wait_for_service "Zipkin" "http://localhost:9411" 60

echo -e "\nIniciando Service Discovery (Eureka)..."
docker-compose -f compose.yml up -d service-discovery-container
wait_for_service "Eureka" "http://localhost:8761" 120

echo -e "\nIniciando Cloud Config Server..."
docker-compose -f compose.yml up -d cloud-config-container
wait_for_service "Config Server" "http://localhost:9296/actuator/health" 120

echo -e "${GREEN}✓ Infraestructura base lista${NC}"
sleep 10

# Fase 2: API Gateway y Proxy
echo -e "\n${YELLOW}=== Fase 2: API Gateway y Proxy ===${NC}\n"

echo "Iniciando API Gateway..."
docker-compose -f compose.yml up -d api-gateway-container
wait_for_service "API Gateway" "http://localhost:8080/actuator/health" 120

echo -e "\nIniciando Proxy Client..."
docker-compose -f compose.yml up -d proxy-client-container
wait_for_service "Proxy Client" "http://localhost:8900/actuator/health" 120

echo -e "${GREEN}✓ Gateway y Proxy listos${NC}"
sleep 10

# Fase 3: Microservicios de Negocio
echo -e "\n${YELLOW}=== Fase 3: Microservicios de Negocio ===${NC}\n"

services=(
    "user-service-container:8700"
    "product-service-container:8500"
    "order-service-container:8300"
    "payment-service-container:8400"
    "shipping-service-container:8600"
    "favourite-service-container:8800"
)

for service_info in "${services[@]}"; do
    IFS=':' read -r service port <<< "$service_info"
    service_name=$(echo "$service" | sed 's/-container//' | sed 's/-/ /g' | awk '{for(i=1;i<=NF;i++)sub(/./,toupper(substr($i,1,1)),$i)}1')
    
    echo -e "\nIniciando $service_name..."
    docker-compose -f compose.yml up -d "$service"
    wait_for_service "$service_name" "http://localhost:$port/actuator/health" 120
done

echo -e "${GREEN}✓ Microservicios de negocio listos${NC}"
sleep 10

# Fase 4: Stack de Observabilidad
echo -e "\n${YELLOW}=== Fase 4: Stack de Observabilidad ===${NC}\n"

echo "Iniciando Elasticsearch..."
docker-compose -f compose.yml up -d elasticsearch
wait_for_service "Elasticsearch" "http://localhost:9200/_cluster/health" 180

echo -e "\nIniciando Logstash..."
docker-compose -f compose.yml up -d logstash
sleep 30  # Logstash tarda en iniciar

echo -e "\nIniciando Kibana..."
docker-compose -f compose.yml up -d kibana
wait_for_service "Kibana" "http://localhost:5601/api/status" 180

echo -e "\nIniciando Prometheus..."
docker-compose -f compose.yml up -d prometheus
wait_for_service "Prometheus" "http://localhost:9090/-/healthy" 60

echo -e "\nIniciando Alertmanager..."
docker-compose -f compose.yml up -d alertmanager
wait_for_service "Alertmanager" "http://localhost:9093/-/healthy" 60

echo -e "\nIniciando Grafana..."
docker-compose -f compose.yml up -d grafana
wait_for_service "Grafana" "http://localhost:3000/api/health" 120

echo -e "\nIniciando Jaeger..."
docker-compose -f compose.yml up -d jaeger
wait_for_service "Jaeger" "http://localhost:16686" 60

echo -e "${GREEN}✓ Stack de observabilidad listo${NC}"

# Resumen final
echo -e "\n${YELLOW}=== Resumen del Despliegue ===${NC}\n"

echo "Estado de los contenedores:"
docker-compose -f compose.yml ps

echo -e "\n${BLUE}Servicios disponibles:${NC}"
echo ""
echo "Infraestructura:"
echo "  • Eureka:        http://localhost:8761"
echo "  • Config Server: http://localhost:9296"
echo "  • API Gateway:   http://localhost:8080"
echo "  • Zipkin:        http://localhost:9411"
echo ""
echo "Microservicios:"
echo "  • User Service:      http://localhost:8700/swagger-ui.html"
echo "  • Product Service:   http://localhost:8500/swagger-ui.html"
echo "  • Order Service:     http://localhost:8300/swagger-ui.html"
echo "  • Payment Service:   http://localhost:8400/swagger-ui.html"
echo "  • Shipping Service:  http://localhost:8600/swagger-ui.html"
echo "  • Favourite Service: http://localhost:8800/swagger-ui.html"
echo "  • Proxy Client:      http://localhost:8900/swagger-ui.html"
echo ""
echo "Observabilidad:"
echo "  • Prometheus:    http://localhost:9090"
echo "  • Grafana:       http://localhost:3000 (admin/admin)"
echo "  • Kibana:        http://localhost:5601"
echo "  • Jaeger:        http://localhost:16686"
echo ""

# Verificar servicios registrados en Eureka
echo -e "${BLUE}Servicios registrados en Eureka:${NC}"
sleep 5  # Dar tiempo para que se registren
if curl -s http://localhost:8761/eureka/apps > /dev/null 2>&1; then
    registered=$(curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort -u | wc -l)
    echo "  Total: $registered servicios"
    curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort -u | while read service; do
        echo "    • $service"
    done
else
    echo -e "  ${RED}No se pudo conectar a Eureka${NC}"
fi

echo -e "\n${GREEN}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║          ✓ Despliegue completado exitosamente             ║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════════╝${NC}"

echo -e "\n${YELLOW}Próximos pasos:${NC}"
echo "  1. Verifica el estado: ./scripts/check-services.sh"
echo "  2. Ejecuta los tests: ./test-em-all.sh"
echo "  3. Accede a Grafana y configura los dashboards"
echo "  4. Revisa los logs: docker-compose -f compose.yml logs -f"
echo ""
