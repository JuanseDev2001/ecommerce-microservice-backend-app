#!/bin/bash

# Script para gestionar versiones automáticamente
# Uso: ./version-manager.sh [major|minor|patch|auto]

set -e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Obtener la versión actual desde Git tags
get_current_version() {
    local latest_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "v-0.0.0")
    # Remover 'v-' del inicio (mantener formato con guión)
    echo "${latest_tag#v-}"
}

# Incrementar versión según tipo
increment_version() {
    local version=$1
    local type=$2
    
    IFS='.' read -r -a parts <<< "$version"
    local major="${parts[0]:-0}"
    local minor="${parts[1]:-0}"
    local patch="${parts[2]:-0}"
    
    case "$type" in
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch|auto)
            patch=$((patch + 1))
            ;;
        *)
            echo "Tipo inválido. Usa: major, minor, patch, o auto"
            exit 1
            ;;
    esac
    
    echo "${major}.${minor}.${patch}"
}

# Analizar commits para determinar tipo de versión (Conventional Commits)
analyze_commits() {
    local last_tag=$(git describe --tags --abbrev=0 2>/dev/null || git rev-list --max-parents=0 HEAD)
    local commits=$(git log ${last_tag}..HEAD --pretty=format:"%s" 2>/dev/null || echo "")
    
    # Buscar breaking changes (major)
    if echo "$commits" | grep -qi "BREAKING CHANGE\|!:"; then
        echo "major"
        return
    fi
    
    # Buscar nuevas features (minor)
    if echo "$commits" | grep -qi "^feat"; then
        echo "minor"
        return
    fi
    
    # Por defecto, patch
    echo "patch"
}

# Main
TYPE=${1:-auto}

if [ "$TYPE" = "auto" ]; then
    TYPE=$(analyze_commits)
    echo -e "${YELLOW}Analizando commits... Tipo detectado: ${TYPE}${NC}"
fi

CURRENT_VERSION=$(get_current_version)
NEW_VERSION=$(increment_version "$CURRENT_VERSION" "$TYPE")

echo -e "${BLUE}Versión actual: ${CURRENT_VERSION}${NC}"
echo -e "${GREEN}Nueva versión: ${NEW_VERSION}${NC}"
echo ""
echo -e "${YELLOW}¿Deseas crear el tag v${NEW_VERSION}? (y/n)${NC}"
read -r response

if [[ "$response" =~ ^[Yy]$ ]]; then
    # Crear tag con formato v-X.Y.Z (con guión)
    git tag -a "v-${NEW_VERSION}" -m "Release v-${NEW_VERSION}"
    echo -e "${GREEN}Tag v-${NEW_VERSION} creado${NC}"
    echo -e "${BLUE}Para pushear el tag: git push origin v-${NEW_VERSION}${NC}"
    echo ""
    echo -e "${YELLOW}¿Deseas generar las release notes ahora? (y/n)${NC}"
    read -r gen_notes
    
    if [[ "$gen_notes" =~ ^[Yy]$ ]]; then
        # Corregir ruta cuando se ejecuta desde release-notes/
        if [ -f "./generate-release-notes.sh" ]; then
            ./generate-release-notes.sh "${NEW_VERSION}" "production"
        else
            ../release-notes/generate-release-notes.sh "${NEW_VERSION}" "production"
        fi
    fi
else
    echo -e "${YELLOW}Tag no creado${NC}"
fi
