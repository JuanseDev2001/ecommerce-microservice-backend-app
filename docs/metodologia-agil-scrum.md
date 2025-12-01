# Implementación de Metodología Ágil - Scrum

## Información del Proyecto

**Proyecto:** E-Commerce Microservices Backend Application  
**Duración del Sprint:** 1 semana (7 días)  
**Fecha:** 25 de noviembre - 1 de diciembre de 2025  
**Herramienta:** Trello  
**Metodología:** Scrum Framework  

---

## Equipo Scrum

**Product Owner:** Jhonatan
- Definición y priorización del Product Backlog
- Gestión de requisitos del producto

**Scrum Master:** Juan Sebastian
- Facilitador del proceso Scrum
- Eliminación de impedimentos

**Development Team:** Jhonatan y Juan Sebastian
- Desarrollo de microservicios
- Configuración de infraestructura Azure
- Implementación de observabilidad

---


## Sprint Planning

### Objetivo del Sprint
Desplegar una aplicación de microservicios de e-commerce completamente funcional en Azure VM, con infraestructura de soporte y stack de observabilidad.

### User Stories Completadas

**Infraestructura (35 puntos)**
- US-001: Configurar VM en Azure (Ubuntu 22.04, 8 vCPUs, 32GB RAM) - 8 pts
- US-002: Instalar Docker, Java 17, Maven - 5 pts
- US-003: Desplegar Eureka Server (Service Discovery) - 5 pts
- US-004: Desplegar Config Server - 3 pts
- US-005: Desplegar API Gateway - 5 pts
- US-006: Configurar NSG y puertos en Azure - 5 pts
- US-007: Resolver problema Java 11/cgroup v2 (migración a Java 17) - 4 pts

**Microservicios (13 puntos)**
- US-008: Desplegar 7 microservicios de negocio (User, Product, Order, Payment, Shipping, Favourite, Proxy) - 13 pts

**Observabilidad (12 puntos)**
- US-009: Stack ELK (Elasticsearch, Logstash, Kibana) - 5 pts
- US-010: Prometheus + Grafana + Alertmanager - 5 pts
- US-011: Zipkin + Jaeger (Distributed Tracing) - 2 pts

**Total:** 60 Story Points

---

## Organización en Trello

### Listas del Tablero
1. **Product Backlog** - Historias priorizadas
2. **Sprint Backlog** - Tareas del sprint actual
3. **In Progress** - Trabajo activo (límite WIP: 3)
4. **Testing** - Verificación de criterios
5. **Done** - Tareas completadas

### Etiquetas
- **Backend** - Microservicios
- **Infrastructure** - Azure/Docker
- **Observability** - Monitoreo
- **Bug** - Correcciones

---

## Daily Scrums

| Día | Progreso | Impedimentos |
|-----|----------|--------------|
| D1  | Configuración VM Azure | Ninguno |
| D2  | Eureka + Config + Gateway | Ninguno |
| D3  | Microservicios desplegados | Java 11/cgroup v2 |
| D4  | Stack ELK funcionando | Resuelto con Java 17 |
| D5 | Prometheus + Grafana | Conflicto puertos |
| D6  | Resuelto acceso externo | NSG bloqueado |
| D7  | Review + Retrospectiva | Ninguno |

---

## Sprint Review

**Fecha:** 1 de diciembre de 2025

### Incremento Entregado
- 9 microservicios funcionando y registrados en Eureka
- API Gateway como punto único de entrada
- Stack completo de observabilidad (ELK, Prometheus/Grafana, Jaeger)
- Todos los servicios accesibles externamente
- Documentación técnica completa

**Resultado:** 60/60 Story Points completados (100%)

---

## Sprint Retrospective

### Qué funcionó bien
- Despliegue gradual con scripts automatizados
- Documentación continua durante el desarrollo
- Resolución rápida de impedimentos (< 24h)

### Qué mejorar
- Verificar compatibilidad de versiones antes del sprint
- Usar Infrastructure as Code (Terraform)
- Agregar más tests automatizados

### Impedimentos Resueltos
1. **Java 11/cgroup v2 (Día 3)** → Migración a Eclipse Temurin Java 17
2. **Conflicto puertos Jaeger/Zipkin (Día 5)** → Jaeger en puerto 9412
3. **NSG Azure bloqueado (Día 6)** → Configuración de reglas de entrada

---

## Métricas del Sprint

### Velocity
- **Planificados:** 60 puntos
- **Completados:** 60 puntos
- **Velocity:** 60 puntos/semana

### Burndown
```
60 |●
50 |  ●
40 |    ●
30 |      ●
20 |        ●●
10 |          ●
 0 |___________●
   D1 D2 D3 D4 D5 D6 D7
```

---

## Definition of Done

- Código desplegado en Azure VM
- Servicio corriendo sin errores
- Registrado en Eureka (si aplica)
- Logs visibles en Kibana
- Métricas en Prometheus
- Documentación actualizada
- Accesible externamente

---

## Artefactos Generados

### Código
- 10 microservicios desplegados
- 4 archivos Docker Compose
- 5 scripts de automatización

### Documentación
- Arquitectura de Azure
- Quick Start Guide
- Guía Scripts
- Metodología Scrum

### Infraestructura
- VM Azure (8 vCPUs, 32GB RAM)
- 19 contenedores Docker
- 3 volúmenes persistentes

---


## Conclusiones

Sprint completado exitosamente con 100% de las user stories cumplidas. Scrum permitió identificar y resolver impedimentos rápidamente. El uso de Trello facilitó la transparencia y el seguimiento del trabajo.

### Lecciones Aprendidas
1. Validar compatibilidad de tecnologías antes del sprint
2. Automatizar despliegues desde el inicio
3. Documentar mientras se desarrolla

---

## Estado del Proyecto Final

### Requisitos Completados

| Requisito | Peso | Completado | % Logrado | Estado |
|-----------|------|------------|-----------|--------|
| **1. Metodología Ágil** | 10% | ✅ | 10% | Implementado Scrum con Trello, sprints documentados |
| **2. Terraform (IaC)** | 20% | ❌ | 0% | Pendiente - Actualmente usando Docker Compose |
| **3. Patrones de Diseño** | 10% | ⚠️ | 8% | Circuit Breaker, Retry, Feature Toggle implementados |
| **4. CI/CD Avanzado** | 15% | ⚠️ | 2% | Jenkins básico, falta SonarQube, Trivy, versionado |
| **5. Pruebas Completas** | 15% | ⚠️ | 3% | Pruebas básicas, faltan E2E, performance, seguridad |
| **6. Change Management** | 5% | ✅ | 5% | Scripts de versionado y release notes |
| **7. Observabilidad** | 10% | ✅ | 10% | ELK Stack, Prometheus, Grafana, Jaeger, Zipkin |
| **8. Seguridad** | 5% | ⚠️ | 1% | Configuración básica, falta escaneo, secrets mgmt |
| **9. Documentación** | 10% | ⚠️ | 8% | Arquitectura, guías, metodología documentadas |

**Total Proyecto Base:** 47% completado

### Bonificaciones

| Bonificación | Peso | Estado | Notas |
|--------------|------|--------|-------|
| Multi-Cloud | 5% | ❌ | No implementado |
| GitOps (ArgoCD/Flux) | 5% | ❌ | No implementado |
| Service Mesh | 5% | ❌ | No implementado |
| Chaos Engineering | 5% | ❌ | No implementado |
| FinOps | 5% | ❌ | No implementado |
| KEDA Autoscaling | 5% | ❌ | No implementado |

**Total Bonificaciones:** 0% completado

---

## Próximos Pasos

### Prioridad Alta (Crítico para aprobación)
1. **Implementar Terraform (20%)**
   - Migrar infraestructura a código
   - Configurar múltiples ambientes (dev/stage/prod)
   - Backend remoto para estado

2. **Completar CI/CD (13% restante)**
   - Integrar SonarQube
   - Implementar Trivy para seguridad de contenedores
   - Versionado semántico automático
   - Notificaciones y aprobaciones

3. **Implementar Pruebas (12% restante)**
   - Pruebas E2E completas
   - Pruebas de rendimiento con Locust
   - Pruebas de seguridad con OWASP ZAP
   - Automatización en pipelines

4. **Reforzar Seguridad (4% restante)**
   - Escaneo continuo de vulnerabilidades
   - Gestión segura de secretos (Vault)
   - RBAC configurado
   - TLS para servicios públicos

### Prioridad Media
5. **Completar Patrones (2% restante)** - Documentar mejor los existentes
6. **Mejorar Documentación (2% restante)** - Manual de operaciones completo

### Consideraciones para Bonificaciones
- **GitOps (+5%)** - Más fácil de implementar, alto impacto
- **FinOps (+5%)** - Dashboards de costos, optimización Azure
- **Multi-Cloud (+5%)** - Si hay presupuesto disponible

---

**Documento generado:** 1 de diciembre de 2025  
**Equipo:** Jhonatan y Juan Sebastian  
**Completado:** 47% del proyecto base

