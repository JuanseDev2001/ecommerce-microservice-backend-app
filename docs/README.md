# Documentación del Proyecto
## E-Commerce Microservices Backend Application

**Proyecto**: Ingeniería de Software V  
**Versión**: 1.0  
**Fecha**: Noviembre 2025

---

## Estructura de Documentos

Este directorio contiene la documentación técnica del proyecto organizada por tema:

### Documentos Principales

1. **PATRONES_DISEÑO.md**
   - Patrones existentes identificados en la arquitectura
   - Patrones implementados (Bulkhead, Retry, Feature Toggles)
   - Testing y verificación
   - Configuración y ejemplos de código

2. **OBSERVABILIDAD.md**
   - Stack completo de observabilidad
   - Configuración de Prometheus, Grafana, ELK, Jaeger
   - Métricas, logs y traces
   - Dashboards y queries útiles

### Documentos de Infraestructura

3. **Jenkins.md**
   - Configuración de CI/CD
   - Pipelines y automatización

4. **DockerImages.md**
   - Imágenes Docker utilizadas
   - Versionamiento

5. **PipelineLogic.md**
   - Lógica de los pipelines de deployment

---

## Guía de Lectura

### Para entender los Patrones de Diseño:
1. Leer `PATRONES_DISEÑO.md`
2. Revisar el código en `order-service/src/main/java/com/selimhorri/app/resilience/`
3. Probar los endpoints de actuator

### Para configurar Observabilidad:
1. Leer `OBSERVABILIDAD.md`
2. Ejecutar `docker compose up -d`
3. Acceder a Grafana, Prometheus, Kibana y Jaeger
4. Configurar dashboards según los ejemplos

---

## Enlaces Rápidos

**Código Fuente**:
- Bulkhead: `order-service/src/main/java/com/selimhorri/app/resilience/OrderBulkheadService.java`
- Retry: `order-service/src/main/java/com/selimhorri/app/resilience/OrderRetryService.java`
- Feature Toggles: `order-service/src/main/java/com/selimhorri/app/config/FeatureToggleService.java`

**Configuración**:
- Application: `order-service/src/main/resources/application.yml`
- Docker: `compose.yml`
- Prometheus: `prometheus/prometheus.yml`

---

## Contacto

Para preguntas sobre la documentación, contactar al equipo de desarrollo.

**Última actualización**: Noviembre 30, 2025
