#!/bin/bash

###############################################################################
# Script de Backup para E-Commerce Microservices
# Realiza backup de volúmenes Docker y configuraciones
###############################################################################

# Configuración
BACKUP_DIR="${HOME}/backups"
DATE=$(date +%Y%m%d_%H%M%S)
PROJECT_DIR="${HOME}/projects/ecommerce-microservice-backend-app"

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Crear directorio de backups
mkdir -p "$BACKUP_DIR"

echo -e "${GREEN}=== Iniciando Backup ===${NC}"
echo "Fecha: $(date)"
echo "Directorio de backup: $BACKUP_DIR"
echo ""

# 1. Backup de volúmenes de Docker
echo -e "${YELLOW}Realizando backup de volúmenes Docker...${NC}"

volumes=("jenkins_home" "grafana-storage" "elasticsearch-data")

for volume in "${volumes[@]}"; do
    if docker volume inspect "$volume" > /dev/null 2>&1; then
        echo "  • Backup de $volume..."
        docker run --rm \
            -v "${volume}:/data" \
            -v "${BACKUP_DIR}:/backup" \
            ubuntu tar czf "/backup/${volume}-${DATE}.tar.gz" /data
        
        if [ $? -eq 0 ]; then
            echo -e "    ${GREEN}✓ Completado${NC}"
        else
            echo -e "    ${RED}✗ Error${NC}"
        fi
    else
        echo "  • Volumen $volume no existe, saltando..."
    fi
done

# 2. Backup de configuraciones
echo -e "\n${YELLOW}Realizando backup de configuraciones...${NC}"

if [ -d "$PROJECT_DIR" ]; then
    cd "$PROJECT_DIR" || exit
    
    tar czf "${BACKUP_DIR}/config-${DATE}.tar.gz" \
        compose.yml \
        docker-compose.observability.yml \
        prometheus/ \
        elk/ \
        .env 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}✓ Configuraciones respaldadas${NC}"
    else
        echo -e "  ${RED}✗ Error al respaldar configuraciones${NC}"
    fi
else
    echo -e "  ${RED}✗ Directorio del proyecto no encontrado${NC}"
fi

# 3. Backup de scripts personalizados
echo -e "\n${YELLOW}Realizando backup de scripts...${NC}"

if [ -d "${PROJECT_DIR}/scripts" ]; then
    tar czf "${BACKUP_DIR}/scripts-${DATE}.tar.gz" -C "$PROJECT_DIR" scripts/
    echo -e "  ${GREEN}✓ Scripts respaldados${NC}"
fi

# 4. Listar backups
echo -e "\n${YELLOW}Backups creados:${NC}"
ls -lh "$BACKUP_DIR" | grep "$DATE"

# 5. Calcular tamaño total
total_size=$(du -sh "$BACKUP_DIR" | cut -f1)
echo -e "\nTamaño total de backups: ${GREEN}$total_size${NC}"

# 6. Limpiar backups antiguos (mantener últimos 7 días)
echo -e "\n${YELLOW}Limpiando backups antiguos (>7 días)...${NC}"
old_backups=$(find "$BACKUP_DIR" -name "*.tar.gz" -mtime +7)

if [ -n "$old_backups" ]; then
    echo "$old_backups" | while read file; do
        echo "  • Eliminando: $(basename "$file")"
        rm -f "$file"
    done
    echo -e "${GREEN}✓ Backups antiguos eliminados${NC}"
else
    echo "  No hay backups antiguos para eliminar"
fi

# 7. Resumen
echo -e "\n${GREEN}=== Backup Completado ===${NC}"
echo "Archivos de backup:"
ls -1 "$BACKUP_DIR" | grep "$DATE" | while read file; do
    size=$(du -h "${BACKUP_DIR}/${file}" | cut -f1)
    echo "  • $file ($size)"
done

echo -e "\n${YELLOW}Para restaurar un backup:${NC}"
echo "  docker run --rm -v <volume>:/data -v ${BACKUP_DIR}:/backup ubuntu tar xzf /backup/<archivo>.tar.gz -C /"
