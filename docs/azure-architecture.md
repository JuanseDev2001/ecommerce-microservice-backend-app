# Arquitectura de Despliegue en Azure

## Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          AZURE CLOUD                                     │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    Resource Group: rg-ecommerce-microservices      │  │
│  │                                                                     │  │
│  │  ┌──────────────────────────────────────────────────────────────┐ │  │
│  │  │           Virtual Network: vnet-ecommerce                     │ │  │
│  │  │                                                                │ │  │
│  │  │  ┌────────────────────────────────────────────────────────┐  │ │  │
│  │  │  │  VM: vm-ecommerce-app                                   │  │ │  │
│  │  │  │  • Ubuntu 22.04 LTS                                     │  │ │  │
│  │  │  │  • 8 vCPUs, 32GB RAM                                    │  │ │  │
│  │  │  │  • 200GB SSD Premium                                    │  │ │  │
│  │  │  │                                                          │  │ │  │
│  │  │  │  ┌────────────────────────────────────────────────┐    │  │ │  │
│  │  │  │  │         DOCKER CONTAINERS                       │    │  │ │  │
│  │  │  │  │                                                  │    │  │ │  │
│  │  │  │  │  ┌─────────────────────────────────────────┐   │    │  │ │  │
│  │  │  │  │  │   INFRASTRUCTURE LAYER                   │   │    │  │ │  │
│  │  │  │  │  ├─────────────────────────────────────────┤   │    │  │ │  │
│  │  │  │  │  │ • Eureka (8761)                          │   │    │  │ │  │
│  │  │  │  │  │ • Config Server (9296)                   │   │    │  │ │  │
│  │  │  │  │  │ • API Gateway (8080)                     │   │    │  │ │  │
│  │  │  │  │  │ • Zipkin (9411)                          │   │    │  │ │  │
│  │  │  │  │  └─────────────────────────────────────────┘   │    │  │ │  │
│  │  │  │  │                      ↓                          │    │  │ │  │
│  │  │  │  │  ┌─────────────────────────────────────────┐   │    │  │ │  │
│  │  │  │  │  │   MICROSERVICES LAYER                    │   │    │  │ │  │
│  │  │  │  │  ├─────────────────────────────────────────┤   │    │  │ │  │
│  │  │  │  │  │ • Proxy Client (8900)                    │   │    │  │ │  │
│  │  │  │  │  │ • User Service (8700)                    │   │    │  │ │  │
│  │  │  │  │  │ • Product Service (8500)                 │   │    │  │ │  │
│  │  │  │  │  │ • Order Service (8300)                   │   │    │  │ │  │
│  │  │  │  │  │ • Payment Service (8400)                 │   │    │  │ │  │
│  │  │  │  │  │ • Shipping Service (8600)                │   │    │  │ │  │
│  │  │  │  │  │ • Favourite Service (8800)               │   │    │  │ │  │
│  │  │  │  │  └─────────────────────────────────────────┘   │    │  │ │  │
│  │  │  │  │                      ↓                          │    │  │ │  │
│  │  │  │  │  ┌─────────────────────────────────────────┐   │    │  │ │  │
│  │  │  │  │  │   OBSERVABILITY LAYER                    │   │    │  │ │  │
│  │  │  │  │  ├─────────────────────────────────────────┤   │    │  │ │  │
│  │  │  │  │  │ Metrics:                                 │   │    │  │ │  │
│  │  │  │  │  │ • Prometheus (9090)                      │   │    │  │ │  │
│  │  │  │  │  │ • Grafana (3000)                         │   │    │  │ │  │
│  │  │  │  │  │ • Alertmanager (9093)                    │   │    │  │ │  │
│  │  │  │  │  │                                           │   │    │  │ │  │
│  │  │  │  │  │ Logging:                                 │   │    │  │ │  │
│  │  │  │  │  │ • Elasticsearch (9200)                   │   │    │  │ │  │
│  │  │  │  │  │ • Logstash (5000)                        │   │    │  │ │  │
│  │  │  │  │  │ • Kibana (5601)                          │   │    │  │ │  │
│  │  │  │  │  │                                           │   │    │  │ │  │
│  │  │  │  │  │ Tracing:                                 │   │    │  │ │  │
│  │  │  │  │  │ • Jaeger (16686)                         │   │    │  │ │  │
│  │  │  │  │  └─────────────────────────────────────────┘   │    │  │ │  │
│  │  │  │  │                                                  │    │  │ │  │
│  │  │  │  │  ┌─────────────────────────────────────────┐   │    │  │ │  │
│  │  │  │  │  │   PERSISTENT STORAGE                     │   │    │  │ │  │
│  │  │  │  │  ├─────────────────────────────────────────┤   │    │  │ │  │
│  │  │  │  │  │ • jenkins_home                           │   │    │  │ │  │
│  │  │  │  │  │ • grafana-storage                        │   │    │  │ │  │
│  │  │  │  │  │ • elasticsearch-data                     │   │    │  │ │  │
│  │  │  │  │  └─────────────────────────────────────────┘   │    │  │ │  │
│  │  │  │  └────────────────────────────────────────────────┘    │  │ │  │
│  │  │  └────────────────────────────────────────────────────────┘  │ │  │
│  │  │                                                                │ │  │
│  │  │  Network Security Group: nsg-ecommerce-vm                     │ │  │
│  │  │  • Inbound Rules: 22, 80, 443, 3000, 5000, 5601, 8080,       │ │  │
│  │  │    8300-8900, 9090, 9093, 9200, 9296, 9411, 8761, 16686      │ │  │
│  │  └──────────────────────────────────────────────────────────────┘ │  │
│  │                                                                     │  │
│  │  Public IP: pip-ecommerce-vm                                       │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↑
                                    │
                              INTERNET ACCESS
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
              ┌─────┴─────┐                  ┌──────┴──────┐
              │  Browser  │                  │  SSH Client │
              │  (HTTP)   │                  │   (Port 22) │
              └───────────┘                  └─────────────┘
```

## Flujo de Datos

```
┌──────────┐
│  Client  │
└────┬─────┘
     │ HTTP Request
     ↓
┌────────────────────┐
│   Azure Public IP  │
│  (pip-ecommerce)   │
└────────┬───────────┘
         │
         ↓
┌────────────────────┐
│       NSG          │
│  (Port Filtering)  │
└────────┬───────────┘
         │
         ↓
┌────────────────────┐
│    API Gateway     │
│    (Port 8080)     │
└────────┬───────────┘
         │
         ↓
┌────────────────────┐
│      Eureka        │
│  (Service Lookup)  │
└────────┬───────────┘
         │
         ↓
┌────────────────────────────────────┐
│         Microservices              │
│  (User, Product, Order, etc.)      │
└────────┬───────────────────────────┘
         │
         ├─────────────────┐
         │                 │
         ↓                 ↓
┌────────────────┐  ┌──────────────┐
│   Prometheus   │  │    Zipkin    │
│   (Metrics)    │  │   (Traces)   │
└────────────────┘  └──────────────┘
         │                 │
         ↓                 ↓
┌────────────────┐  ┌──────────────┐
│    Grafana     │  │    Jaeger    │
│ (Visualization)│  │ (Tracing UI) │
└────────────────┘  └──────────────┘
```

## Distribución de Recursos

### CPU Allocation (Estimado)

```
Service Discovery:     0.5 vCPU
Config Server:         0.5 vCPU
API Gateway:           1.0 vCPU
Microservices (7x):    3.5 vCPU (0.5 cada uno)
Prometheus:            0.5 vCPU
Grafana:               0.5 vCPU
Elasticsearch:         1.5 vCPU
Logstash:              1.0 vCPU
Kibana:                0.5 vCPU
Jaeger:                0.5 vCPU
─────────────────────────────────
Total:                ~10 vCPU
```

### Memory Allocation (Estimado)

```
Service Discovery:     512 MB
Config Server:         512 MB
API Gateway:           1 GB
Microservices (7x):    7 GB (1 GB cada uno)
Prometheus:            2 GB
Grafana:               512 MB
Elasticsearch:         4 GB
Logstash:              2 GB
Kibana:                1 GB
Jaeger:                1 GB
System:                2 GB
─────────────────────────────────
Total:                ~22 GB
```

### Disk Usage (Estimado)

```
Docker Images:         ~15 GB
Elasticsearch Data:    ~20 GB
Logs:                  ~5 GB
Grafana Data:          ~1 GB
Prometheus Data:       ~5 GB
System:                ~10 GB
Free Space:            ~144 GB
─────────────────────────────────
Total Disk (200 GB):   ~200 GB
```

## Network Topology

```
Internet
    │
    ↓
┌───────────────────────────────────────┐
│  Azure Network Security Group (NSG)   │
│  ┌─────────────────────────────────┐  │
│  │  Inbound Rules                   │  │
│  │  • SSH (22)                      │  │
│  │  • HTTP (80)                     │  │
│  │  • HTTPS (443)                   │  │
│  │  • Microservices (8000-9000)    │  │
│  │  • Monitoring (3000, 5601, etc) │  │
│  └─────────────────────────────────┘  │
└───────────────┬───────────────────────┘
                │
                ↓
┌───────────────────────────────────────┐
│  Virtual Network (10.0.0.0/16)        │
│  ┌─────────────────────────────────┐  │
│  │  Subnet (10.0.0.0/24)            │  │
│  │  ┌───────────────────────────┐  │  │
│  │  │  VM: 10.0.0.4              │  │  │
│  │  │  ┌─────────────────────┐  │  │  │
│  │  │  │  Docker Network     │  │  │  │
│  │  │  │  (172.18.0.0/16)    │  │  │  │
│  │  │  │                      │  │  │  │
│  │  │  │  All containers      │  │  │  │
│  │  │  │  communicate here    │  │  │  │
│  │  │  └─────────────────────┘  │  │  │
│  │  └───────────────────────────┘  │  │
│  └─────────────────────────────────┘  │
└───────────────────────────────────────┘
```

## Security Layers

```
┌─────────────────────────────────────────┐
│  Layer 1: Azure Network Security Group  │
│  • Port-based filtering                 │
│  • Source IP restrictions (optional)    │
└─────────────┬───────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Layer 2: VM Firewall (UFW)             │
│  • Additional port filtering            │
│  • Rate limiting (optional)             │
└─────────────┬───────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Layer 3: Docker Network Isolation      │
│  • Internal network for containers      │
│  • Only exposed ports accessible        │
└─────────────┬───────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Layer 4: Application Security          │
│  • Spring Security                      │
│  • OAuth2/JWT (if configured)           │
│  • Service-to-service authentication    │
└─────────────────────────────────────────┘
```

## Monitoring Stack

```
┌──────────────────────────────────────────────────────────┐
│                    OBSERVABILITY STACK                    │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────┐ │
│  │   METRICS      │  │    LOGGING     │  │  TRACING   │ │
│  ├────────────────┤  ├────────────────┤  ├────────────┤ │
│  │                │  │                │  │            │ │
│  │  Prometheus ←──┼──┤  Logstash   ←──┼──┤  Zipkin    │ │
│  │      ↓         │  │      ↓         │  │     ↓      │ │
│  │   Grafana      │  │  Elasticsearch │  │  Jaeger    │ │
│  │                │  │      ↓         │  │            │ │
│  │  Alertmanager  │  │   Kibana       │  │            │ │
│  │                │  │                │  │            │ │
│  └────────────────┘  └────────────────┘  └────────────┘ │
│           ↑                  ↑                  ↑        │
│           └──────────────────┴──────────────────┘        │
│                              │                           │
│                    All Microservices                     │
│                              │                           │
└──────────────────────────────┴───────────────────────────┘
```

## Deployment Flow

```
┌─────────────┐
│ Developer   │
│ (Local PC)  │
└──────┬──────┘
       │ 1. SSH Connection
       ↓
┌─────────────────────────────┐
│  Azure VM                    │
│  ┌───────────────────────┐  │
│  │ 2. Git Clone/Pull     │  │
│  └───────────┬───────────┘  │
│              ↓               │
│  ┌───────────────────────┐  │
│  │ 3. Docker Compose     │  │
│  │    Pull Images        │  │
│  └───────────┬───────────┘  │
│              ↓               │
│  ┌───────────────────────┐  │
│  │ 4. Start Containers   │  │
│  │    (deploy-gradual.sh)│  │
│  └───────────┬───────────┘  │
│              ↓               │
│  ┌───────────────────────┐  │
│  │ 5. Health Checks      │  │
│  │    (check-services.sh)│  │
│  └───────────┬───────────┘  │
│              ↓               │
│  ┌───────────────────────┐  │
│  │ 6. Services Running   │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
       │
       ↓
┌─────────────┐
│   Users     │
│ (Internet)  │
└─────────────┘
```

## Backup Strategy

```
┌──────────────────────────────────────┐
│         BACKUP SCHEDULE               │
├──────────────────────────────────────┤
│                                       │
│  Daily (2:00 AM):                    │
│  ┌────────────────────────────────┐  │
│  │ • Docker Volumes               │  │
│  │   - jenkins_home               │  │
│  │   - grafana-storage            │  │
│  │   - elasticsearch-data         │  │
│  │                                 │  │
│  │ • Configuration Files          │  │
│  │   - compose.yml                │  │
│  │   - prometheus/                │  │
│  │   - elk/                       │  │
│  │   - .env                       │  │
│  │                                 │  │
│  │ • Custom Scripts               │  │
│  │   - scripts/                   │  │
│  └────────────────────────────────┘  │
│                                       │
│  Retention: 7 days                   │
│  Location: ~/backups/                │
│                                       │
└──────────────────────────────────────┘
```

## Scaling Options

### Vertical Scaling (Current)
```
Single VM with all services
✓ Simple management
✓ Lower cost
✗ Single point of failure
✗ Limited scalability
```

### Horizontal Scaling (Future)
```
Option 1: Multiple VMs + Load Balancer
┌─────────────┐
│ Load Balancer│
└──────┬───────┘
   ┌───┴───┬───────┬───────┐
   │       │       │       │
┌──▼──┐ ┌──▼──┐ ┌──▼──┐ ┌──▼──┐
│ VM1 │ │ VM2 │ │ VM3 │ │ VM4 │
└─────┘ └─────┘ └─────┘ └─────┘

Option 2: Azure Kubernetes Service (AKS)
┌─────────────────────────────┐
│     AKS Cluster             │
│  ┌───────────────────────┐  │
│  │  Multiple Pods         │  │
│  │  Auto-scaling          │  │
│  │  Self-healing          │  │
│  │  Load balancing        │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

---

**Última actualización**: 2025-11-30
