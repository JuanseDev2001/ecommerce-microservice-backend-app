# Patrones de Diseño
## E-Commerce Microservices Backend Application

**Proyecto**: Ingeniería de Software V  
**Fecha**: Noviembre 2025  
**Versión**: 1.0

---

##  Tabla de Contenidos

1. [Introducción](#introducción)
2. [Patrones Existentes](#patrones-existentes)
3. [Patrones Implementados](#patrones-implementados)
4. [Testing y Verificación](#testing-y-verificación)
5. [Referencias](#referencias)

---

##  Introducción

Este documento describe los patrones de diseño implementados en la arquitectura de microservicios del sistema e-commerce. El objetivo es mejorar la resiliencia, configurabilidad y mantenibilidad del sistema mediante la aplicación de patrones probados en la industria.

### Objetivos Alcanzados

-  Identificación de 10 patrones existentes
-  Implementación de 4 patrones adicionales
-  Documentación completa con ejemplos
-  Testing y verificación funcional

---

## ️ Patrones Existentes

### 1. API Gateway Pattern

**Ubicación**: `api-gateway/`

**Propósito**: Punto de entrada único para todos los clientes, simplificando la comunicación y proporcionando capacidades transversales como autenticación, rate limiting y routing.

**Beneficios**:
- Simplifica la comunicación del cliente
- Centraliza políticas de seguridad
- Facilita el versionado de APIs
- Reduce la complejidad del cliente

**Evidencia**:
```yaml
# api-gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/order-service/**
```

### 2. Service Registry Pattern (Eureka)

**Ubicación**: `service-discovery/`

**Propósito**: Registro y descubrimiento dinámico de servicios, permitiendo que los microservicios se encuentren entre sí sin configuración hardcoded.

**Beneficios**:
- Descubrimiento automático de servicios
- Balanceo de carga dinámico
- Tolerancia a fallos
- Escalabilidad horizontal simplificada

**Evidencia**:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://service-discovery:8761/eureka/
```

### 3. Externalized Configuration Pattern

**Ubicación**: `cloud-config/`

**Propósito**: Configuración centralizada almacenada en Git, permitiendo cambios de configuración sin redeploying.

**Beneficios**:
- Configuración versionada
- Cambios sin redeploy
- Configuración por ambiente
- Auditoría de cambios

### 4. Database per Service Pattern

**Propósito**: Cada microservicio tiene su propia base de datos, garantizando independencia y desacoplamiento.

**Beneficios**:
- Desacoplamiento de servicios
- Escalabilidad independiente
- Flexibilidad tecnológica
- Aislamiento de fallos

### 5. Circuit Breaker Pattern

**Ubicación**: `proxy-client/`, configurado con Resilience4j

**Propósito**: Prevenir fallos en cascada detectando fallos y evitando llamadas a servicios no disponibles.

**Configuración**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      orderService:
        failure-rate-threshold: 50
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 5s
```

### 6. Distributed Tracing Pattern

**Herramientas**: Zipkin, Jaeger, Spring Cloud Sleuth

**Propósito**: Rastrear requests a través de múltiples servicios para debugging y análisis de latencia.

**Beneficios**:
- Visibilidad end-to-end
- Análisis de latencia
- Debugging distribuido
- Identificación de cuellos de botella

### 7. Health Check Pattern

**Implementación**: Spring Boot Actuator

**Propósito**: Exponer endpoints de salud para orquestación y monitoreo.

**Endpoints**:
```
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
```

### 8. Aggregator Pattern

**Ubicación**: `proxy-client/`

**Propósito**: Agregar llamadas a múltiples servicios para reducir el chattiness del cliente.

### 9. Repository Pattern

**Implementación**: Spring Data JPA en todos los servicios

**Propósito**: Abstracción del acceso a datos para facilitar testing y cambios.

### 10. Observability Pattern

**Herramientas**: Prometheus, Grafana, ELK Stack, Jaeger

**Propósito**: Visibilidad completa del sistema mediante métricas, logs y traces.

---

##  Patrones Implementados

### 1. Bulkhead Pattern (Resilience)

**Ubicación**: `order-service/src/main/java/com/selimhorri/app/resilience/OrderBulkheadService.java`

**Propósito**: Aislar recursos para diferentes tipos de operaciones, previniendo que un tipo de carga consuma todos los recursos disponibles.

**Tipos Implementados**:

#### Bulkhead Semafórico (Síncrono)
```java
@Bulkhead(name = "orderProcessing", 
          fallbackMethod = "orderProcessingFallback", 
          type = Type.SEMAPHORE)
public OrderDto processOrder(OrderDto orderDto) {
    // Limitado a 10 llamadas concurrentes
    OrderDto savedOrder = orderService.save(orderDto);
    return savedOrder;
}
```

#### Bulkhead Thread Pool (Asíncrono)
```java
@Bulkhead(name = "orderAsyncProcessing", 
          fallbackMethod = "orderAsyncProcessingFallback", 
          type = Type.THREADPOOL)
public OrderDto processOrderAsync(OrderDto orderDto) {
    // Thread pool dedicado de 5 threads
    // Queue capacity: 20
    return orderService.save(orderDto);
}
```

**Configuración**:
```yaml
resilience4j:
  bulkhead:
    instances:
      orderProcessing:
        max-concurrent-calls: 10
        max-wait-duration: 0ms
      highPriorityOrders:
        max-concurrent-calls: 5
  
  thread-pool-bulkhead:
    instances:
      orderAsyncProcessing:
        max-thread-pool-size: 5
        core-thread-pool-size: 3
        queue-capacity: 20
```

**Beneficios**:
-  Aislamiento de recursos
-  Prevención de resource starvation
-  Degradación elegante con fallbacks
-  Métricas granulares por operación

**Métricas Expuestas**:
- `resilience4j.bulkhead.available.concurrent.calls`
- `resilience4j.bulkhead.max.allowed.concurrent.calls`
- `resilience4j.bulkhead.concurrent.calls`

---

### 2. Feature Toggle Pattern (Configuration)

**Ubicación**: 
- `order-service/src/main/java/com/selimhorri/app/config/FeatureToggle.java`
- `order-service/src/main/java/com/selimhorri/app/config/FeatureToggleService.java`
- `order-service/src/main/java/com/selimhorri/app/resource/FeatureToggleController.java`

**Propósito**: Habilitar/deshabilitar funcionalidades dinámicamente sin redeployment, facilitando canary releases, A/B testing y kill switches.

**Características**:
-  Activación/desactivación en runtime
-  Rollout porcentual (0-100%)
-  Habilitación por usuario específico
-  Habilitación por ambiente (dev, stage, prod)
-  API REST para gestión

**Uso en Código**:
```java
@Autowired
private FeatureToggleService featureToggleService;

public OrderDto processOrder(OrderDto order) {
    if (featureToggleService.isEnabled("new-checkout-flow")) {
        return newCheckoutFlow(order);
    } else {
        return legacyCheckoutFlow(order);
    }
}
```

**Configuración**:
```yaml
features:
  environment: dev
  toggles:
    new-checkout-flow:
      enabled: true
      rollout-percentage: 50
      description: "New optimized checkout flow"
      enabled-environments:
        - dev
        - stage
    
    advanced-order-analytics:
      enabled: true
      rollout-percentage: 10
      enabled-users:
        - admin@example.com
```

**API REST**:
```bash
# Ver todos los features
GET /actuator/features

# Ver feature específico
GET /actuator/features/new-checkout-flow

# Verificar si está habilitado
GET /actuator/features/new-checkout-flow/enabled

# Activar/Desactivar
POST /actuator/features/new-checkout-flow/enable
POST /actuator/features/new-checkout-flow/disable

# Cambiar rollout percentage
PUT /actuator/features/new-checkout-flow/rollout?percentage=75
```

**Beneficios**:
-  Deployment desacoplado de release
-  Rollback instantáneo sin redeploy
-  Testing en producción con usuarios específicos
-  Canary releases y gradual rollout
-  A/B testing facilitado
-  Kill switches para features problemáticas

---

### 3. Retry Pattern con Exponential Backoff (Resilience)

**Ubicación**: `order-service/src/main/java/com/selimhorri/app/resilience/OrderRetryService.java`

**Propósito**: Reintentar operaciones fallidas con delays exponencialmente crecientes para evitar sobrecarga de servicios temporalmente no disponibles.

**Implementación**:
```java
@Retry(name = "orderProcessing", fallbackMethod = "saveOrderFallback")
public OrderDto saveOrderWithRetry(OrderDto orderDto) {
    log.info("Attempting to save order: {}", orderDto.getOrderId());
    OrderDto savedOrder = orderService.save(orderDto);
    return savedOrder;
}

public OrderDto saveOrderFallback(OrderDto orderDto, Exception exception) {
    log.error("All retry attempts failed: {}", exception.getMessage());
    orderDto.setOrderDesc("FAILED: " + orderDto.getOrderDesc());
    // Store in DLQ for manual processing
    return orderDto;
}
```

**Configuración**:
```yaml
resilience4j:
  retry:
    instances:
      orderProcessing:
        max-attempts: 3
        wait-duration: 1000ms
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - java.net.ConnectException
          - java.net.SocketTimeoutException
          - org.springframework.web.client.HttpServerErrorException
      
      externalApiCall:
        max-attempts: 5
        wait-duration: 500ms
        exponential-backoff-multiplier: 1.5
```

**Casos de Uso**:
- Guardado de órdenes (transient DB issues)
- Llamadas a APIs externas
- Procesamiento de pagos
- Actualización de inventario

**Beneficios**:
-  Manejo automático de fallos transitorios
-  Exponential backoff previene thundering herd
-  Configuración granular por operación
-  Fallback para degradación elegante
-  Métricas de retry para observabilidad

---

### 4. Resilience Event Listeners (Observability)

**Ubicación**: `order-service/src/main/java/com/selimhorri/app/config/ResilienceEventListener.java`

**Propósito**: Proporcionar visibilidad completa de eventos de resiliencia para debugging, monitoring y alerting.

**Eventos Capturados**:
- Circuit Breaker: State transitions, errors, success, calls not permitted
- Bulkhead: Calls rejected, permitted, finished
- Retry: Attempts, success, errors, ignored errors

**Implementación**:
```java
@EventListener
public void onCircuitBreakerStateTransition(CircuitBreakerOnStateTransitionEvent event) {
    log.warn("Circuit Breaker '{}' transitioned from {} to {}",
            event.getCircuitBreakerName(),
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState());
    // Send alert, update dashboard, trigger auto-scaling
}

@EventListener
public void onBulkheadCallRejected(BulkheadOnCallRejectedEvent event) {
    log.warn("Bulkhead '{}' rejected call - max concurrent calls reached",
            event.getBulkheadName());
    // Alert operations team, consider scaling
}
```

**Beneficios**:
-  Logging estructurado de eventos
-  Base para sistema de alertas
-  Métricas custom por evento
-  Debugging facilitado

---

##  Testing y Verificación

### Endpoints de Verificación

```bash
# Health Check
curl http://localhost:8300/order-service/actuator/health | jq

# Feature Toggles
curl http://localhost:8300/order-service/actuator/features | jq

# Métricas Resilience4j
curl http://localhost:8300/order-service/actuator/metrics | jq '.names | map(select(contains("resilience4j")))'

# Métricas específicas
curl http://localhost:8300/order-service/actuator/metrics/resilience4j.bulkhead.concurrent.calls | jq
curl http://localhost:8300/order-service/actuator/metrics/resilience4j.retry.calls | jq
curl http://localhost:8300/order-service/actuator/metrics/resilience4j.circuitbreaker.state | jq
```

### Testing de Feature Toggles

```bash
# Ver feature específico
curl http://localhost:8300/order-service/actuator/features/new-checkout-flow | jq

# Activar/Desactivar
curl -X POST http://localhost:8300/order-service/actuator/features/new-checkout-flow/disable | jq
curl -X POST http://localhost:8300/order-service/actuator/features/new-checkout-flow/enable | jq

# Canary Release (gradual rollout)
curl -X PUT "http://localhost:8300/order-service/actuator/features/new-checkout-flow/rollout?percentage=25" | jq
curl -X PUT "http://localhost:8300/order-service/actuator/features/new-checkout-flow/rollout?percentage=50" | jq
curl -X PUT "http://localhost:8300/order-service/actuator/features/new-checkout-flow/rollout?percentage=100" | jq
```

### Métricas Exportadas

**Total**: 15 métricas de Resilience4j

**Bulkhead** (7 métricas):
- `resilience4j.bulkhead.available.concurrent.calls`
- `resilience4j.bulkhead.max.allowed.concurrent.calls`
- `resilience4j.bulkhead.concurrent.calls`
- `resilience4j.bulkhead.thread.pool.size`
- `resilience4j.bulkhead.queue.depth`
- `resilience4j.bulkhead.core.thread.pool.size`
- `resilience4j.bulkhead.max.thread.pool.size`

**Circuit Breaker** (7 métricas):
- `resilience4j.circuitbreaker.state`
- `resilience4j.circuitbreaker.failure.rate`
- `resilience4j.circuitbreaker.buffered.calls`
- `resilience4j.circuitbreaker.calls`
- `resilience4j.circuitbreaker.not.permitted.calls`
- `resilience4j.circuitbreaker.slow.call.rate`
- `resilience4j.circuitbreaker.slow.calls`

**Retry** (1 métrica):
- `resilience4j.retry.calls`

---

##  Resumen de Cumplimiento

| Requisito | Estado | Detalles |
|-----------|--------|----------|
| Identificar patrones existentes |  100% | 10 patrones documentados |
| Implementar patrón de resiliencia |  100% | Bulkhead + Retry (2 patrones) |
| Implementar patrón de configuración |  100% | Feature Toggle |
| Documentar propósito y beneficios |  100% | Documentación completa |
| Código funcional |  100% | Verificado y testeado |
| Observabilidad |  100% | 15 métricas + eventos |

---

##  Referencias

### Documentación Técnica
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Microservices Patterns - Chris Richardson](https://microservices.io/patterns/)

### Libros
- "Release It!" - Michael Nygard
- "Building Microservices" - Sam Newman
- "Microservices Patterns" - Chris Richardson

### Artículos
- [Martin Fowler - Feature Toggles](https://martinfowler.com/articles/feature-toggles.html)
- [Netflix - Hystrix](https://github.com/Netflix/Hystrix/wiki)

---

**Fecha de última actualización**: Noviembre 30, 2025  
**Versión del documento**: 1.0  
**Proyecto**: E-Commerce Microservices Backend Application
