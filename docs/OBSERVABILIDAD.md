# Observabilidad
## E-Commerce Microservices Backend Application

**Proyecto**: Ingeniería de Software V  
**Fecha**: Noviembre 2025  
**Versión**: 1.0

---

##  Tabla de Contenidos

1. [Introducción](#introducción)
2. [Stack de Observabilidad](#stack-de-observabilidad)
3. [Configuración y Deployment](#configuración-y-deployment)
4. [Métricas](#métricas)
5. [Logs](#logs)
6. [Traces](#traces)
7. [Dashboards](#dashboards)
8. [Testing](#testing)

---

##  Introducción

Este documento describe la implementación completa del stack de observabilidad para el sistema de microservicios e-commerce, siguiendo las mejores prácticas de la industria con los tres pilares fundamentales: **Métricas, Logs y Traces**.

### Objetivos

-  Visibilidad completa del sistema
-  Detección temprana de problemas
-  Análisis de performance
-  Debugging distribuido
-  Alerting proactivo

---

## ️ Stack de Observabilidad

### Componentes Principales

| Componente | Puerto | Propósito |
|------------|--------|-----------|
| **Prometheus** | 9090 | Recolección y almacenamiento de métricas |
| **Grafana** | 3000 | Visualización de métricas y dashboards |
| **Elasticsearch** | 9200 | Almacenamiento de logs |
| **Kibana** | 5601 | Visualización y análisis de logs |
| **Logstash** | 5000 | Procesamiento y agregación de logs |
| **Jaeger** | 16686 | Distributed tracing |
| **Alertmanager** | 9093 | Gestión de alertas |

### Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                    Microservicios                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │  Order   │  │ Product  │  │ Payment  │   ...       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘             │
│       │             │              │                    │
└───────┼─────────────┼──────────────┼────────────────────┘
        │             │              │
        ├─────────────┼──────────────┼──→ Metrics → Prometheus → Grafana
        │             │              │
        ├─────────────┼──────────────┼──→ Logs → Logstash → Elasticsearch → Kibana
        │             │              │
        └─────────────┴──────────────┴──→ Traces → Jaeger
```

---

## ️ Configuración y Deployment

### Docker Compose

El stack completo se despliega con:

```bash
docker compose up -d
```

**Servicios incluidos en `compose.yml`**:
- Microservicios (Order, Product, Payment, User, Shipping, Favourite)
- Service Discovery (Eureka)
- API Gateway
- Cloud Config
- Observabilidad Stack (Prometheus, Grafana, ELK, Jaeger)

### Configuración de Microservicios

Cada microservicio está configurado para exportar métricas, logs y traces:

```yaml
# application.yml
spring:
  zipkin:
    base-url: http://jaeger:9411/
  
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,loggers
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

---

##  Métricas

### Prometheus

**Acceso**: `http://localhost:9091`

#### Configuración

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'spring-actuator'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'order-service:8300'
        - 'product-service:8500'
        - 'payment-service:8400'
```

#### Métricas Exportadas

**Total por servicio**: ~342 líneas

**Categorías**:

1. **JVM Metrics**
```promql
jvm_memory_used_bytes{area="heap"}
jvm_memory_max_bytes{area="heap"}
jvm_threads_live_threads
jvm_gc_pause_seconds_count
```

2. **HTTP Metrics**
```promql
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_active_seconds_active_count
```

3. **Resilience4j Metrics** (15 métricas)
```promql
# Bulkhead
resilience4j_bulkhead_available_concurrent_calls
resilience4j_bulkhead_max_allowed_concurrent_calls
resilience4j_bulkhead_concurrent_calls

# Circuit Breaker
resilience4j_circuitbreaker_state
resilience4j_circuitbreaker_failure_rate
resilience4j_circuitbreaker_buffered_calls

# Retry
resilience4j_retry_calls
```

4. **Database Metrics**
```promql
hikaricp_connections_active
hikaricp_connections_idle
hikaricp_connections_pending
```

5. **System Metrics**
```promql
system_cpu_usage
system_cpu_count
process_uptime_seconds
```

#### Queries Útiles

**Request Rate**:
```promql
rate(http_server_requests_seconds_count{application="ORDER-SERVICE"}[1m])
```

**Memory Usage %**:
```promql
100 * (jvm_memory_used_bytes{area="heap",application="ORDER-SERVICE"} 
/ jvm_memory_max_bytes{area="heap",application="ORDER-SERVICE"})
```

**Circuit Breaker State**:
```promql
resilience4j_circuitbreaker_state{name="orderService"}
```

**p95 Request Duration**:
```promql
histogram_quantile(0.95, 
  rate(http_server_requests_seconds_bucket{application="ORDER-SERVICE"}[5m]))
```

**Error Rate**:
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[1m])
```

### Grafana

**Acceso**: `http://localhost:3000`  
**Credenciales**: admin / admin

#### Configuración de Data Source

1. Click en ️ (Settings) → Data Sources
2. Add data source → Prometheus
3. URL: `http://prometheus:9090`
4. Save & Test

#### Dashboards Recomendados

**Dashboard 1: JVM Overview**
- Import ID: `4701` (JVM Micrometer)
- Muestra: Memory, CPU, Threads, GC

**Dashboard 2: E-Commerce Business Metrics**

Paneles sugeridos:

1. **HTTP Request Rate**
```promql
sum(rate(http_server_requests_seconds_count[1m])) by (uri)
```

2. **Active Requests**
```promql
http_server_requests_active_seconds_active_count
```

3. **Memory Usage**
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

4. **Circuit Breaker State**
```promql
resilience4j_circuitbreaker_state
```

5. **Database Connections**
```promql
hikaricp_connections_active
hikaricp_connections_idle
```

6. **Service Health**
```promql
up{job="spring-actuator"}
```

---

##  Logs

### Stack ELK

**Componentes**:
- Elasticsearch (storage)
- Logstash (processing)
- Kibana (visualization)

### Elasticsearch

**Acceso**: `http://localhost:9200`

**Configuración**:
```yaml
# compose.yml
elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:8.11.1
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false
  ports:
    - "9200:9200"
```

### Logstash

**Puerto**: 5000 (input), 9600 (monitoring)

**Pipeline**:
```ruby
# logstash/pipeline/logstash.conf
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  # Parse and enrich logs
  if [logger_name] =~ "com.selimhorri" {
    mutate {
      add_tag => ["app_log"]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "logstash-%{+YYYY.MM.dd}"
  }
}
```

### Kibana

**Acceso**: `http://localhost:5601`

#### Configuración Inicial

1. Abrir Kibana
2. Menu ☰ → Discover
3. Create data view:
   - Name: `logstash-*`
   - Timestamp field: `@timestamp`
   - Create

#### Queries Útiles

**Error Logs**:
```
level: ERROR
```

**Logs de un servicio**:
```
application: "ORDER-SERVICE"
```

**Resilience Events**:
```
message: *bulkhead* OR message: *retry* OR message: *circuit*
```

**Exception Stack Traces**:
```
exception: *
```

### Logging en Microservicios

**Configuración**:
```yaml
# application.yml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  level:
    root: INFO
    com.selimhorri.app: DEBUG
    org.springframework.cloud: DEBUG
```

**Logback con Logstash**:
```xml
<!-- logback-spring.xml -->
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>spanId</includeMdcKeyName>
    </encoder>
</appender>
```

---

##  Traces

### Jaeger

**Acceso**: `http://localhost:16686`

**Propósito**: Distributed tracing para rastrear requests a través de múltiples servicios.

#### Configuración

```yaml
# application.yml
spring:
  zipkin:
    base-url: http://jaeger:9411/
  sleuth:
    sampler:
      probability: 1.0  # 100% sampling en dev
```

#### Docker Compose

```yaml
jaeger:
  image: jaegertracing/all-in-one:latest
  ports:
    - "16686:16686"  # UI
    - "9411:9411"    # Zipkin compatible endpoint
```

#### Uso

1. Abrir UI: `http://localhost:16686`
2. Seleccionar servicio: "order-service"
3. Click "Find Traces"
4. Ver detalles de traces específicos

**Información visualizada**:
- Duración total del request
- Latencia por servicio
- Dependencias entre servicios
- Errores y excepciones
- Tags y metadata

#### Análisis de Performance

**Identificar servicios lentos**:
- Ver el timeline de cada span
- Comparar duraciones
- Detectar cuellos de botella

**Debugging de errores**:
- Filtrar por error tags
- Ver stack traces
- Analizar flow del request

---

##  Dashboards

### Dashboard Principal - Service Health

**Panels**:

1. **Service Uptime**
```promql
up{job="spring-actuator"}
```

2. **Request Rate (requests/sec)**
```promql
sum(rate(http_server_requests_seconds_count[1m])) by (application)
```

3. **Error Rate**
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) by (application)
```

4. **Response Time p95**
```promql
histogram_quantile(0.95, 
  sum(rate(http_server_requests_seconds_bucket[5m])) by (le, application))
```

5. **JVM Memory Usage**
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

6. **Active Database Connections**
```promql
hikaricp_connections_active
```

### Dashboard Resilience Patterns

**Panels**:

1. **Circuit Breaker State**
```promql
resilience4j_circuitbreaker_state
```

2. **Bulkhead Concurrent Calls**
```promql
resilience4j_bulkhead_concurrent_calls
```

3. **Retry Attempts Rate**
```promql
rate(resilience4j_retry_calls_total[1m])
```

4. **Bulkhead Rejected Calls**
```promql
rate(resilience4j_bulkhead_rejected_calls_total[1m])
```

---

## 🧪 Testing

### Script de Verificación

```powershell
# test-observability.ps1

$baseUrl = "http://localhost:8300/order-service"

# 1. Verificar Prometheus
Write-Host "Testing Prometheus..." -ForegroundColor Cyan
$prom = Invoke-RestMethod -Uri "http://localhost:9091/api/v1/targets"
Write-Host "  Targets: $($prom.data.activeTargets.Count)" -ForegroundColor Green

# 2. Verificar métricas
Write-Host "`nTesting Metrics..." -ForegroundColor Cyan
$metrics = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics"
Write-Host "  Total metrics: $($metrics.names.Count)" -ForegroundColor Green
$resilience = $metrics.names | Where-Object {$_ -like "*resilience4j*"}
Write-Host "  Resilience4j: $($resilience.Count)" -ForegroundColor Green

# 3. Verificar Grafana
Write-Host "`nTesting Grafana..." -ForegroundColor Cyan
try {
    $grafana = Invoke-RestMethod -Uri "http://localhost:3000/api/health"
    Write-Host "  Status: $($grafana.database)" -ForegroundColor Green
} catch {
    Write-Host "  Grafana not ready" -ForegroundColor Yellow
}

# 4. Verificar Elasticsearch
Write-Host "`nTesting Elasticsearch..." -ForegroundColor Cyan
try {
    $es = Invoke-RestMethod -Uri "http://localhost:9200/_cluster/health"
    Write-Host "  Status: $($es.status)" -ForegroundColor Green
} catch {
    Write-Host "  Elasticsearch not ready" -ForegroundColor Yellow
}

# 5. Verificar Kibana
Write-Host "`nTesting Kibana..." -ForegroundColor Cyan
try {
    $kibana = Invoke-RestMethod -Uri "http://localhost:5601/api/status"
    Write-Host "  Overall: $($kibana.status.overall.level)" -ForegroundColor Green
} catch {
    Write-Host "  Kibana not ready" -ForegroundColor Yellow
}

# 6. Verificar Jaeger
Write-Host "`nTesting Jaeger..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri "http://localhost:16686" -UseBasicParsing | Out-Null
    Write-Host "  UI accessible" -ForegroundColor Green
} catch {
    Write-Host "  Jaeger not ready" -ForegroundColor Yellow
}

Write-Host "`n✓ Observability stack verification complete!" -ForegroundColor Green
```

### Generar Tráfico

```powershell
# generate-traffic.ps1

Write-Host "Generating traffic..." -ForegroundColor Cyan

for ($i=1; $i -le 100; $i++) {
    Write-Progress -Activity "Generating load" -Status "$i/100" -PercentComplete $i
    
    # Health checks
    Invoke-WebRequest -Uri "http://localhost:8300/order-service/actuator/health" -UseBasicParsing | Out-Null
    Invoke-WebRequest -Uri "http://localhost:8500/product-service/actuator/health" -UseBasicParsing | Out-Null
    Invoke-WebRequest -Uri "http://localhost:8400/payment-service/actuator/health" -UseBasicParsing | Out-Null
    
    # Metrics
    Invoke-WebRequest -Uri "http://localhost:8300/order-service/actuator/prometheus" -UseBasicParsing | Out-Null
    
    Start-Sleep -Milliseconds 100
}

Write-Host "✓ Traffic generated - Check Grafana dashboards!" -ForegroundColor Green
```

---

##  Alerting

### Prometheus Alertmanager

**Acceso**: `http://localhost:9093`

**Configuración de Alertas**:

```yaml
# prometheus/alerts.yml
groups:
  - name: service_health
    interval: 30s
    rules:
      - alert: ServiceDown
        expr: up{job="spring-actuator"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.instance }} is down"
          
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate on {{ $labels.application }}"
          
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage on {{ $labels.application }}"
```

---

##  Métricas de Resumen

### Cobertura del Stack

-  **Métricas**: Prometheus + Grafana
  - 342 métricas por servicio
  - 15 métricas de Resilience4j
  - Dashboards configurables
  
-  **Logs**: ELK Stack
  - Logstash processing
  - Elasticsearch storage
  - Kibana visualization
  
-  **Traces**: Jaeger
  - Distributed tracing
  - Service dependencies
  - Performance analysis

### Accesos Rápidos

| Componente | URL | Credenciales |
|------------|-----|--------------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9091 | - |
| Kibana | http://localhost:5601 | - |
| Jaeger | http://localhost:16686 | - |
| Elasticsearch | http://localhost:9200 | - |
| Alertmanager | http://localhost:9093 | - |

---

## 📚 Referencias

### Documentación
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Elastic Stack](https://www.elastic.co/guide/index.html)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

### Best Practices
- [Google SRE Book](https://sre.google/sre-book/table-of-contents/)
- [The Twelve-Factor App](https://12factor.net/)
- [Microservices Observability](https://www.nginx.com/blog/microservices-reference-architecture-nginx-monitoring-logging/)

---

**Fecha de última actualización**: Noviembre 30, 2025  
**Versión del documento**: 1.0  
**Proyecto**: E-Commerce Microservices Backend Application
