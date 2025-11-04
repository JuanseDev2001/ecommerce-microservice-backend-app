# Release Notes Generator

Script automatizado para generar release notes en deployments de producción y staging con **versionado automático**.

## Descripción

Este script genera automáticamente documentación de release notes que incluye:
- Información de versión y fecha
- Lista de servicios desplegados
- Commits recientes
- Detalles de infraestructura
- Enlaces a builds de Jenkins y commits de GitHub

## Uso

### Sintaxis Básica
```bash
./generate-release-notes.sh [version] [environment] [service1] [service2] ...
```

### Auto-detección de Versión

El script ahora detecta automáticamente la versión si no se especifica:

**Prioridad de detección:**
1. Variable `BUILD_NUMBER` (Jenkins)
2. Último tag de Git (ej: `v1.2.3`)
3. Número de commits (ej: `0.0.152`)
4. Timestamp (fallback)

### Ejemplos

**Con auto-detección de versión:**
```bash
# Detecta versión automáticamente, ambiente por defecto (production)
./release-notes/generate-release-notes.sh

# Solo especifica ambiente, versión auto-detectada
./release-notes/generate-release-notes.sh production

# Ambiente + servicios, versión auto-detectada
./release-notes/generate-release-notes.sh staging order-service user-service
```

**Con versión manual:**
```bash
# Versión específica
./release-notes/generate-release-notes.sh "1.0.0" "production"

# Versión + servicios
./release-notes/generate-release-notes.sh "2.5.0" "production" \
    order-service user-service payment-service
```

**Desde Jenkins (Production):**
```bash
# Simple - usa BUILD_NUMBER automáticamente
./release-notes/generate-release-notes.sh production \
    order-service user-service payment-service

# O incluso más simple - todo se auto-detecta
./release-notes/generate-release-notes.sh
```

**Desde Jenkins (Staging):**
```bash
./release-notes/generate-release-notes.sh staging \
    order-service user-service payment-service
```

**Manual:**
```bash
./release-notes/generate-release-notes.sh "1.0.0" "production" \
    "service1" "service2"
```

## Gestión de Versiones con Git Tags

### Version Manager Script

Incluye un script adicional para gestionar versiones semánticas automáticamente:

```bash
./release-notes/version-manager.sh [major|minor|patch|auto]
```

**Tipos de versión:**
- `major`: 1.0.0 → 2.0.0 (cambios incompatibles)
- `minor`: 1.0.0 → 1.1.0 (nuevas features)
- `patch`: 1.0.0 → 1.0.1 (bug fixes)
- `auto`: Analiza commits y decide automáticamente

**Análisis automático de commits (Conventional Commits):**
- `feat:` → incrementa **minor**
- `fix:` → incrementa **patch**
- `BREAKING CHANGE` o `!:` → incrementa **major**

**Ejemplo de uso:**
```bash
# Auto-detectar tipo de versión según commits
./release-notes/version-manager.sh auto

# Incrementar versión minor manualmente
./release-notes/version-manager.sh minor

# Esto creará el tag y opcionalmente las release notes
```

### Workflow Recomendado

```bash
# 1. Hacer commits con Conventional Commits
git commit -m "feat: add new payment method"
git commit -m "fix: resolve memory leak"

# 2. Cuando estés listo para release
./release-notes/version-manager.sh auto

# 3. El script:
#    - Analiza commits
#    - Sugiere nueva versión
#    - Crea el tag
#    - Opcionalmente genera release notes

# 4. Push del tag
git push origin v1.2.0
```

## Estructura de Archivos

Los release notes se guardan en:
```
release-notes/
├── generate-release-notes.sh          # Script principal
├── README.md                          # Esta documentación
├── RELEASE-production-v1.md           # Release notes de producción
├── RELEASE-staging-v2.md              # Release notes de staging
└── ...
```

## Integración con Jenkins

### En Jenkinsfile
```groovy
stage('Generate Release Notes') {
    steps {
        sh """
            chmod +x release-notes/generate-release-notes.sh
            ./release-notes/generate-release-notes.sh \
                "${BUILD_NUMBER}" \
                "production" \
                "${S1}" "${S2}" "${S3}" "${S4}" \
                "${S5}" "${S6}" "${S7}" "${S8}"
        """
        archiveArtifacts artifacts: "release-notes/RELEASE-*.md", fingerprint: true
    }
}
```

## Formato de Release Notes

El script genera un archivo Markdown con las siguientes secciones:

1. **Información General**: Versión, fecha, commit, build
2. **Servicios Desplegados**: Lista de microservicios
3. **Imágenes Docker**: Información del registry
4. **Últimos Cambios**: Top 10 commits
5. **Infraestructura**: Detalles de K8s y CI/CD
6. **Estado del Deployment**: Checklist de validaciones
7. **Validaciones**: Tests y health checks
8. **Notas Adicionales**: Enlaces útiles

## Variables de Entorno

El script utiliza automáticamente las siguientes variables si están disponibles:

| Variable | Descripción | Default |
|----------|-------------|---------|
| `BUILD_NUMBER` | Número de build de Jenkins | "N/A" |
| `JOB_NAME` | Nombre del job de Jenkins | "Manual" |
| `BUILD_URL` | URL del build en Jenkins | "N/A" |

## Características

- Detecta automáticamente si las release notes ya existen
- Obtiene información de Git (hash, branch, commits)
- Integración automática con Jenkins
- Output colorizado para mejor legibilidad
- Manejo de errores robusto
- Compatible con diferentes ambientes

## Comportamiento

### Si las Release Notes YA existen:
- Muestra un warning
- Imprime el contenido existente
- Sale con código 0 (éxito)

### Si NO existen:
- Crea el archivo nuevo
- Imprime el contenido generado
- Sale con código 0 (éxito)

## Notas

- El script es idempotente: ejecutarlo múltiples veces no sobrescribe archivos existentes
- Requiere Git para obtener información de commits
- Compatible con bash en Linux/Unix y Git Bash en Windows
- Los archivos generados usan formato Markdown estándar

## Troubleshooting

**Error: "Permission denied"**
```bash
chmod +x release-notes/generate-release-notes.sh
```

**Error: "git: command not found"**
- Asegúrate de que Git esté instalado y en el PATH
- El script continuará con valores por defecto si Git no está disponible

**Los colores no se muestran correctamente**
- Normal en algunos entornos de Jenkins
- No afecta la funcionalidad del script

## Licencia

Este script es parte del proyecto ecommerce-microservice-backend-app.
