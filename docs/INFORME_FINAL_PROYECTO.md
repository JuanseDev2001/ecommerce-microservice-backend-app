# Informe Final del Proyecto

## E-Commerce Microservices Backend Application

---

**Curso:** Ingeniería de Software V (IngeSoft)  
**Proyecto:** Sprint Software - Sistema E-Commerce con Microservicios  
**Github:** https://github.com/JuanseDev2001/ecommerce-microservice-backend-app.git
**Estado del Sprint:** Completado  
**Fecha:** Diciembre 2025  
**Versión:** 1.0

---

### Integrantes del Equipo

Juan Sebastian Gonzalez - A00371810
Jhonathan Castaño - A00375798

---

## Tabla de Contenidos

1. [Introducción](#-introducción)
2. [Desarrollo del Proyecto - Módulos Completados](#-desarrollo-del-proyecto---módulos-completados)
   - [Metodología Ágil y Branching](#1-metodología-ágil-y-branching)
   - [Patrones de Diseño](#2-patrones-de-diseño)
   - [CI/CD Avanzado](#3-cicd-avanzado)
   - [Observabilidad y Monitoreo](#4-observabilidad-y-monitoreo)
3. [Módulos en Fase de Validación](#-módulos-en-fase-de-validación-testing)
   - [Infraestructura como Código (Terraform)](#1-infraestructura-como-código-terraform)
   - [Pruebas Completas](#2-pruebas-completas)
   - [Change Management](#3-change-management)
   - [Documentación](#4-documentación)
4. [Trabajo Futuro](#-trabajo-futuro-pendientes)
5. [Conclusión](#-conclusión)

---

## Introducción

Este proyecto consiste en el desarrollo y mejora de un **sistema de comercio electrónico basado en microservicios** utilizando tecnologías modernas de cloud-native y prácticas de DevOps.

### Objetivo Principal

Implementar un ecosistema completo de microservicios que incluya:
- **Patrones de diseño** para resiliencia y configurabilidad
- **Pipeline CI/CD** automatizado con múltiples ambientes
- **Observabilidad completa** (métricas, logs, traces)
- **Infraestructura como código** reproducible
- **Suite de pruebas** integral (unitarias, integración, E2E, rendimiento)

### Arquitectura del Sistema

El sistema está compuesto por **10 microservicios**:

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| `api-gateway` | 8080 | Punto de entrada único |
| `service-discovery` | 8761 | Registro de servicios (Eureka) |
| `cloud-config` | 9296 | Configuración centralizada |
| `proxy-client` | 8900 | Autenticación y agregación |
| `user-service` | 8700 | Gestión de usuarios |
| `product-service` | 8500 | Catálogo de productos |
| `order-service` | 8300 | Gestión de órdenes |
| `payment-service` | 8400 | Procesamiento de pagos |
| `shipping-service` | 8600 | Envíos |
| `favourite-service` | 8800 | Lista de favoritos |

---

## Desarrollo del Proyecto - Módulos Completados

### 1. Metodología Ágil y Branching

#### Metodología Implementada

Se utilizó una combinación de **Scrum** y **Kanban** para la gestión del proyecto:

- **Sprint único** de 1 semana (debido al tiempo disponible solo se alcanzó a completar 1 sprint)
- **Tablero Kanban** en **Trello** para visualización del flujo de trabajo
- **Historias de Usuario** documentadas en cada tarjeta
- **Sincronización del equipo** mediante comunicación continua

#### Estrategia de Branching (GitFlow)

```
main (producción)
  │
  ├── dev (desarrollo e integración)
  │     │
  │     ├── feature/patrones-diseño
  │     ├── feature/cicd-avanzado
  │     ├── feature/observabilidad
  │     └── feature/terraform
  │
  ├── stage (pre-producción)
  │
  └── master/main(prod)
```

**Ramas principales:**
- `main`: Código en producción, protegida
- `dev`: Integración de features, builds automáticos
- `stage`: Pre-producción para validación final

**Flujo de trabajo:**
1. Crear rama `feature/*` desde `dev`
2. Desarrollar y hacer commits con **Conventional Commits**
3. Abrir **Pull Request** hacia `dev`
4. Pipeline CI valida automáticamente (tests, SonarQube, Trivy)
5. Code review y merge
6. Promoción a `stage` → `main` según validaciones

#### Herramientas Utilizadas

- **GitHub Projects**: Tablero Kanban
- **GitHub Actions + Jenkins**: Automatización CI/CD
- **Conventional Commits**: Estandarización de mensajes

---

### 2. Patrones de Diseño

#### Patrones Existentes Identificados (10)

| # | Patrón | Ubicación | Propósito |
|---|--------|-----------|-----------|
| 1 | API Gateway | `api-gateway/` | Punto de entrada único |
| 2 | Service Registry | `service-discovery/` | Descubrimiento dinámico |
| 3 | Externalized Config | `cloud-config/` | Configuración centralizada |
| 4 | Database per Service | Todos los servicios | Independencia de datos |
| 5 | Circuit Breaker | `proxy-client/` | Prevención de fallos en cascada |
| 6 | Distributed Tracing | Zipkin/Jaeger | Rastreo de requests |
| 7 | Health Check | Spring Actuator | Monitoreo de salud |
| 8 | Aggregator | `proxy-client/` | Composición de servicios |
| 9 | Repository | Spring Data JPA | Abstracción de datos |
| 10 | Observability | Prometheus/Grafana | Visibilidad del sistema |

#### Patrones Implementados (4 Nuevos)

##### 2.1 Bulkhead Pattern (Resiliencia)

**Ubicación:** `order-service/src/main/java/com/selimhorri/app/resilience/`

**Propósito:** Aislar recursos para diferentes operaciones, previniendo que una sobrecarga afecte a todo el sistema.

**Implementación:**
- **Bulkhead Semafórico**: Limita llamadas concurrentes (10 máx)
- **Bulkhead Thread Pool**: Pool dedicado de threads (5 threads, queue de 20)

**Configuración:**
```yaml
resilience4j:
  bulkhead:
    instances:
      orderProcessing:
        max-concurrent-calls: 10
        max-wait-duration: 0ms
```

**Beneficios:**
- Aislamiento de recursos por operación
- Degradación elegante con fallbacks
- Métricas granulares

---

##### 2.2 Feature Toggle Pattern (Configuración)

**Ubicación:** `order-service/src/main/java/com/selimhorri/app/config/`

**Propósito:** Habilitar/deshabilitar funcionalidades dinámicamente sin redeploy.

**Características:**
- Activación en runtime
- Rollout porcentual (0-100%)
- Habilitación por usuario específico
- Habilitación por ambiente (dev, stage, prod)
- API REST para gestión

**API Endpoints:**
```
GET  /actuator/features                         # Listar features
POST /actuator/features/{name}/enable          # Activar
POST /actuator/features/{name}/disable         # Desactivar
PUT  /actuator/features/{name}/rollout?pct=50  # Rollout gradual
```

**Casos de Uso:**
- Canary releases
- A/B testing
- Kill switches para rollback instantáneo

---

##### 2.3 Retry Pattern con Exponential Backoff (Resiliencia)

**Ubicación:** `order-service/src/main/java/com/selimhorri/app/resilience/`

**Propósito:** Reintentar operaciones fallidas con delays exponencialmente crecientes.

**Configuración:**
```yaml
resilience4j:
  retry:
    instances:
      orderProcessing:
        max-attempts: 3
        wait-duration: 1000ms
        exponential-backoff-multiplier: 2
```

**Beneficios:**
- Manejo automático de fallos transitorios
- Prevención de thundering herd
- Fallback para degradación elegante

---

##### 2.4 Resilience Event Listeners (Observabilidad)

**Ubicación:** `order-service/src/main/java/com/selimhorri/app/config/ResilienceEventListener.java`

**Propósito:** Capturar y registrar eventos de resiliencia para debugging y alerting.

**Eventos Capturados:**
- Circuit Breaker: Transiciones de estado, errores
- Bulkhead: Llamadas rechazadas
- Retry: Intentos, éxitos, fallos

---

### 3. CI/CD Avanzado

#### Pipeline de Jenkins

**Ubicación:** `jenkins/dev/Jenkinsfile.dev`

El pipeline implementa las siguientes etapas:

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│  Checkout   │───▶│  Unit Tests  │───▶│    Build    │
└─────────────┘    └──────────────┘    └─────────────┘
                                              │
                                              ▼
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│   Deploy    │◀───│  Trivy Scan  │◀───│  SonarQube  │
└─────────────┘    └──────────────┘    └─────────────┘
```

#### Etapas del Pipeline

| Etapa | Herramienta | Descripción |
|-------|-------------|-------------|
| Checkout | Git | Clona el código desde GitHub |
| Test | Maven | Ejecuta tests unitarios de cada servicio |
| Build | Maven | Compila y empaqueta los JARs |
| SonarQube | SonarQube | Análisis de calidad de código |
| Docker Build | Docker | Construye imágenes de contenedores |
| Trivy Scan | Trivy | Escaneo de vulnerabilidades en imágenes |
| Push | DockerHub | Sube imágenes al registry |

#### Ambientes Separados

```
┌─────────────────────────────────────────────────────┐
│                    AMBIENTES                        │
├─────────────┬─────────────────┬────────────────────┤
│     DEV     │     STAGE       │      PROD          │
├─────────────┼─────────────────┼────────────────────┤
│ :dev tag    │ :stage tag      │ :0.1.0 tag         │
│ Auto-deploy │ Manual approval │ Release notes      │
│ All tests   │ E2E + Perf      │ Smoke tests        │
└─────────────┴─────────────────┴────────────────────┘
```

#### Análisis de Código con SonarQube

**Métricas analizadas:**
- Code coverage
- Code smells
- Bugs potenciales
- Vulnerabilidades de seguridad
- Duplicación de código
- Deuda técnica

#### Escaneo de Seguridad con Trivy

```bash
trivy image --severity HIGH,CRITICAL ${IMAGE_NAME}
```

**Configuración:**
- Escanea vulnerabilidades HIGH y CRITICAL
- Reporta pero no bloquea (configurable)
- Integrado en el pipeline de CI

#### Notificaciones

- **Éxito**: Mensaje de confirmación en consola
- **Fallo**: Log detallado del error
- Integración con GitHub Status Checks

---

### 4. Observabilidad y Monitoreo

#### Stack Implementado

| Componente | Puerto | Función |
|------------|--------|---------|
| **Prometheus** | 9090 | Recolección de métricas |
| **Grafana** | 3000 | Dashboards y visualización |
| **Elasticsearch** | 9200 | Almacenamiento de logs |
| **Logstash** | 5000 | Procesamiento de logs |
| **Kibana** | 5601 | Visualización de logs |
| **Jaeger** | 16686 | Distributed tracing |
| **Alertmanager** | 9093 | Gestión de alertas |

#### Métricas (Prometheus + Grafana)

**Métricas exportadas por servicio:** ~342 líneas

**Categorías:**
- **JVM**: Memoria, threads, GC
- **HTTP**: Requests, latencia, errores
- **Resilience4j**: Circuit breaker, bulkhead, retry (15 métricas)
- **Database**: Conexiones HikariCP
- **Sistema**: CPU, uptime

**Queries útiles:**
```promql
# Request rate
rate(http_server_requests_seconds_count[1m])

# Memory usage
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[1m])
```

#### Logs (ELK Stack)

**Flujo:**
```
Microservicio → Logstash → Elasticsearch → Kibana
```

**Configuración de logs:**
```yaml
logging:
  level:
    root: INFO
    com.selimhorri.app: DEBUG
```

#### Traces (Jaeger)

**Configuración:**
```yaml
spring:
  zipkin:
    base-url: http://jaeger:9411/
  sleuth:
    sampler:
      probability: 1.0  # 100% en dev
```

**Beneficios:**
- Visualización end-to-end de requests
- Análisis de latencia por servicio
- Identificación de cuellos de botella

#### Health Checks

**Endpoints expuestos:**
```
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
```

---

## Módulos en Fase de Validación (Testing)

### 1. Infraestructura como Código (Terraform)

**Ubicación:** `infra/`

#### Estructura Modular

```
infra/
├── modules/
│   ├── artifact-registry/   # Registry de imágenes
│   ├── gke-cluster/         # Cluster Kubernetes
│   ├── iam/                 # Permisos y roles
│   ├── network/             # VPC y subnets
│   └── digitalocean-jenkins/# Jenkins en DigitalOcean
├── envs/
│   └── dev/
│       └── terraform.tfvars # Variables por ambiente
├── backend-local.tf         # Estado local (dev)
├── variables.tf             # Definición de variables
└── provider.tf              # Configuración de providers
```

#### Configuración Multi-Ambiente

**Variables por ambiente:**
```hcl
# envs/dev/terraform.tfvars
environment = "dev"
project_id  = "ecommerce-dev"
region      = "us-central1"
```

#### Recursos Gestionados

- **GKE Cluster**: Kubernetes administrado
- **VPC Network**: Red privada aislada
- **IAM**: Service accounts y permisos
- **Artifact Registry**: Almacenamiento de imágenes Docker

---

### 2. Pruebas Completas

**Ubicación:** `globaltests/`

#### Tipos de Pruebas

| Tipo | Ubicación | Herramienta |
|------|-----------|-------------|
| Unitarias | `*/src/test/` | JUnit 5 + Mockito |
| Integración | `globaltests/integration/` | Spring Test |
| E2E | `globaltests/e2e/` | RestTemplate + JUnit |
| Rendimiento | `globaltests/rendimiento/` | Locust |

#### Pruebas Unitarias

Cada microservicio incluye tests unitarios:
```bash
mvn clean test
```

#### Pruebas de Integración

**Tests implementados:**
- `ApiGatewayIntegracionGlobalTests.java`
- `ProxyClientIntegracionGlobalTests.java`

Validan la comunicación entre servicios a través del API Gateway.

#### Pruebas E2E (End-to-End)

**Tests implementados:**
- `CloudConfigE2ETest.java` - Configuración centralizada
- `UserServiceE2ETest.java` - CRUD de usuarios
- `ProductServiceE2ETest.java` - Catálogo de productos
- `OrderServiceE2ETest.java` - Gestión de órdenes
- `PaymentServiceE2ETest.java` - Procesamiento de pagos
- `FavouriteServiceE2ETest.java` - Lista de favoritos
- `ShippingServiceE2ETest.java` - Envíos
- `SystemE2ESmokeTest.java` - Health checks de todos los servicios

#### Pruebas de Rendimiento (Locust)

**Archivos:**
- `user_locustfile.py`
- `product_locustfile.py`
- `order_locustfile.py`
- `payment_locustfile.py`
- `favourite_locustfile.py`

**Umbrales de validación:**
| Métrica | Umbral |
|---------|--------|
| Tiempo de respuesta promedio | ≤ 1000 ms |
| Tasa de errores | ≤ 1.0% |
| Throughput mínimo | ≥ 10 req/s |
| Percentil 95 | ≤ 2000 ms |

**Ejecución:**
```bash
# PowerShell (local)
.\run_tests.ps1

# Bash (Docker/Jenkins)
./run_tests.sh
```

---

### 3. Change Management

**Ubicación:** `release-notes/`

#### Release Notes Automáticas

**Script:** `generate-release-notes.sh`

**Funcionalidades:**
- Auto-detección de versión (Git tags, BUILD_NUMBER)
- Generación automática de changelog
- Lista de servicios desplegados
- Commits recientes incluidos
- Enlaces a builds de Jenkins

**Uso:**
```bash
# Auto-detectar todo
./release-notes/generate-release-notes.sh

# Especificar ambiente
./release-notes/generate-release-notes.sh production order-service user-service
```

#### Version Manager

**Script:** `version-manager.sh`

**Versionado Semántico Automático:**
- `major`: Cambios incompatibles (1.0.0 → 2.0.0)
- `minor`: Nuevas features (1.0.0 → 1.1.0)
- `patch`: Bug fixes (1.0.0 → 1.0.1)
- `auto`: Analiza commits y decide

**Conventional Commits:**
```
feat: add payment method    → minor
fix: resolve memory leak    → patch
BREAKING CHANGE: new API    → major
```

---

### 4. Documentación

**Ubicación:** `docs/`

| Documento | Descripción |
|-----------|-------------|
| `azure-architecture.md` | Arquitectura de despliegue en Azure |
| `DockerImages.md` | Gestión de imágenes Docker |
| `GUIA_JENKINS.md` | Guía de configuración de Jenkins |
| `GUIA_KUBERNETES.md` | Guía para correr el proyecto con Minikube |
| `INFORME_FINAL_PROYECTO.md` | Este informe |
| `OBSERVABILIDAD.md` | Stack de monitoreo completo |
| `PATRONES_DISEÑO.md` | Patrones existentes e implementados |
| `PipelineLogic.md` | Lógica de pipelines |
| `QUICKSTART-AZURE.md` | Guía rápida de despliegue en Azure VM |
| `README.md` | Documentación principal |

---

## Conclusión

### Logros del Sprint

| Objetivo | Estado | Detalle |
|----------|--------|---------|
| Metodología Ágil | 100% | GitFlow + Kanban + GitHub Projects |
| Patrones de Diseño | 100% | 10 existentes + 4 nuevos implementados |
| CI/CD Avanzado | 100% | Jenkins + SonarQube + Trivy + Multi-ambiente |
| Observabilidad | 100% | Prometheus + Grafana + ELK + Jaeger |
| Terraform | 90% | Módulos listos, validación en curso |
| Pruebas | 85% | Unitarias, E2E, Rendimiento implementadas |
| Documentación | 100% | Docs completos |

### Métricas Finales

- **Microservicios**: 10 servicios desplegables
- **Patrones de Diseño**: 14 patrones (10 existentes + 4 nuevos)
- **Métricas de Observabilidad**: ~342 por servicio
- **Tests E2E**: 8 suites de pruebas
- **Pruebas de Rendimiento**: 5 scripts Locust
- **Documentación**: 10+ documentos técnicos

### Lecciones Aprendidas

1. **Infraestructura como Código** acelera la reproducibilidad
2. **Observabilidad** es crítica para debugging distribuido
3. **Patrones de resiliencia** previenen fallos en cascada
4. **CI/CD automatizado** reduce errores humanos
5. **Feature Toggles** permiten releases más seguros

---

**Proyecto:** E-Commerce Microservices Backend Application  
**Versión del Informe:** 1.0  
**Última Actualización:** Diciembre 2025  
**Estado:** Sprint Completado

---

> *"La arquitectura de microservicios no es solo sobre tecnología, es sobre habilitar a los equipos para entregar valor de forma independiente y rápida."*
