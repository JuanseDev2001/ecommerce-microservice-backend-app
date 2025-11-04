#!/bin/bash

# Script para generar Release Notes automáticamente
# Uso: ./generate-release-notes.sh [version] [environment] [services...]
# Si no se proporciona version, se auto-detecta desde Git

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para auto-detectar versión
get_version() {
    # Prioridad 1: Variable de entorno BUILD_NUMBER (Jenkins)
    if [ -n "$BUILD_NUMBER" ]; then
        echo "$BUILD_NUMBER"
        return
    fi
    
    # Prioridad 2: Último tag de Git
    local git_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
    if [ -n "$git_tag" ]; then
        # Remover 'v' si existe al inicio
        echo "${git_tag#v}"
        return
    fi
    
    # Prioridad 3: Contar commits desde el inicio
    local commit_count=$(git rev-list --count HEAD 2>/dev/null || echo "0")
    if [ "$commit_count" != "0" ]; then
        echo "0.0.${commit_count}"
        return
    fi
    
    # Fallback: usar timestamp
    echo "$(date +%Y%m%d-%H%M%S)"
}

# Parsear argumentos (ahora todos son opcionales)
if [ $# -eq 0 ]; then
    # Sin argumentos: auto-detectar todo
    VERSION=$(get_version)
    ENVIRONMENT=${ENVIRONMENT:-"production"}
    SERVICES=()
elif [ $# -eq 1 ]; then
    # Solo ambiente o solo versión
    if [[ "$1" =~ ^(production|staging|prod|stage|dev|development)$ ]]; then
        VERSION=$(get_version)
        ENVIRONMENT="$1"
        SERVICES=()
    else
        VERSION="$1"
        ENVIRONMENT=${ENVIRONMENT:-"production"}
        SERVICES=()
    fi
else
    # Argumentos completos
    VERSION=${1:-$(get_version)}
    ENVIRONMENT=${2:-"production"}
    shift 2
    SERVICES=("$@")
fi

RELEASE_DIR="release-notes"
RELEASE_FILE="${RELEASE_DIR}/RELEASE-${ENVIRONMENT}-v${VERSION}.md"
DATE=$(date +%Y-%m-%d)
TIME=$(date +%H:%M:%S)
GIT_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "N/A")
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "N/A")
GIT_LOG=$(git log --pretty=format:"- %s (%an)" -10 2>/dev/null || echo "No git history available")

# Crear directorio si no existe
mkdir -p "${RELEASE_DIR}"

# Verificar si el archivo ya existe
if [ -f "${RELEASE_FILE}" ]; then
    echo -e "${YELLOW}Release Notes para ${ENVIRONMENT} v${VERSION} ya existen${NC}"
    echo -e "${BLUE}Archivo: ${RELEASE_FILE}${NC}"
    cat "${RELEASE_FILE}"
    exit 0
fi

echo -e "${GREEN}Generando Release Notes para ${ENVIRONMENT} v${VERSION}...${NC}"

# Generar lista de servicios
SERVICES_LIST=""
if [ ${#SERVICES[@]} -gt 0 ]; then
    for service in "${SERVICES[@]}"; do
        SERVICES_LIST="${SERVICES_LIST}- ${service}\n"
    done
else
    SERVICES_LIST="- No services specified\n"
fi

# Obtener información adicional de Jenkins si está disponible
BUILD_NUMBER=${BUILD_NUMBER:-"N/A"}
JOB_NAME=${JOB_NAME:-"Manual"}
BUILD_URL=${BUILD_URL:-"N/A"}

# Crear el archivo de release notes
cat > "${RELEASE_FILE}" << EOF
# Release Notes - ${ENVIRONMENT^} v${VERSION}

## Información General
- **Fecha de Release**: ${DATE} ${TIME}
- **Versión**: v${VERSION}
- **Entorno**: ${ENVIRONMENT}
- **Build Number**: ${BUILD_NUMBER}
- **Commit Hash**: \`${GIT_HASH}\`
- **Branch**: \`${GIT_BRANCH}\`
- **Jenkins Job**: ${JOB_NAME}
- **Build URL**: ${BUILD_URL}

## Servicios Desplegados
$(echo -e "${SERVICES_LIST}")

## Imágenes Docker
Todas las imágenes fueron desplegadas desde Docker Hub con el tag correspondiente al ambiente ${ENVIRONMENT}.

## Últimos Cambios (Top 10 Commits)
${GIT_LOG}

## Infraestructura
- **Namespace Kubernetes**: ${ENVIRONMENT}
- **Orchestrator**: Kubernetes
- **Container Registry**: Docker Hub
- **CI/CD**: Jenkins

## Estado del Deployment
- Pre-cleanup ejecutado
- Imágenes Docker verificadas
- Manifiestos Kubernetes aplicados
- Servicios y Pods verificados
- Health checks completados

## Validaciones
- Build exitoso en Jenkins
- Tests unitarios
- Tests de integración
- Deployment verificado

## Notas Adicionales
Este release fue generado automáticamente por el pipeline de Jenkins.

### Enlaces Útiles
- [Jenkins Build #${BUILD_NUMBER}](${BUILD_URL})
- [GitHub Commit](https://github.com/JuanseDev2001/ecommerce-microservice-backend-app/commit/${GIT_HASH})

---
*Generado automáticamente el ${DATE} ${TIME}*
EOF

echo -e "${GREEN}Release Notes creadas exitosamente${NC}"
echo -e "${BLUE}Archivo: ${RELEASE_FILE}${NC}"
echo ""
cat "${RELEASE_FILE}"

exit 0
