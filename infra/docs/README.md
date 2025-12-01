# Infraestructura - Explicación Simple

## ¿Qué tenemos?

```
Jenkins (DigitalOcean)          →          Google Cloud Platform
    $24/mes                                    ~$25-250/mes según ambiente
       |                                              |
       |                                              |
   [CI/CD] ─────────── Push images ──────→  [Artifact Registry]
                                                      |
                                                      ↓
                                              [GKE Dev Cluster]
                                              [GKE Stage Cluster]  
                                              [GKE Prod Cluster]
```

## Componentes

### 1. **Jenkins en DigitalOcean** (infra/modules/digitalocean-jenkins/)

**¿Qué es?**
- Servidor CI/CD que ejecuta tus pipelines
- Droplet con Ubuntu + Docker + Jenkins
- Instalado: gcloud, kubectl, Docker

**¿Qué hace?**
- Escucha commits en GitHub
- Ejecuta tests
- Construye imágenes Docker
- Hace push a Artifact Registry
- Despliega en GKE

**Costo:** $24/mes (gratis primeros 8 meses)

### 2. **GCP - Artifact Registry** (infra/modules/artifact-registry/)

**¿Qué es?**
- Registry privado de Docker images
- Como DockerHub pero privado y en GCP

**¿Qué hace?**
- Almacena imágenes Docker de tus microservicios
- Versiona cada build
- Integrado con GKE para deploys

**Costo:** ~$0.10/GB/mes (muy barato)

### 3. **GCP - GKE Clusters** (infra/modules/gke-cluster/)

**¿Qué es?**
- Kubernetes administrado por Google
- 3 clusters: dev, stage, prod

**¿Qué hace?**
- Corre tus microservicios en contenedores
- Auto-scaling
- Load balancing
- Health checks

**Configuración:**
- **Dev**: 1 nodo e2-medium (~$25/mes) - Para desarrollo
- **Stage**: 3 nodos e2-standard-2 (~$100/mes) - Para pruebas
- **Prod**: 3 nodos e2-standard-4 (~$250/mes) - Para producción

### 4. **GCP - VPC Network** (infra/modules/network/)

**¿Qué es?**
- Red privada virtual
- Conecta todos los recursos GCP

**¿Qué hace?**
- Aisla tu infraestructura
- Reglas de firewall
- Rangos de IP para pods y services

### 5. **GCP - IAM** (infra/modules/iam/)

**¿Qué es?**
- Service Accounts con permisos

**Creamos 2:**
- **GKE SA**: Cluster accede a Artifact Registry
- **Jenkins SA**: Jenkins administra GKE y push a registry

## Flujo de Trabajo

```
1. Developer pushea código
                ↓
2. GitHub webhook notifica a Jenkins
                ↓
3. Jenkins en DigitalOcean:
   - Clona repo
   - Corre tests
   - Build Docker image
   - Push a Artifact Registry (GCP)
                ↓
4. Jenkins despliega a GKE Dev
   - kubectl apply
   - GKE pull imagen de Artifact Registry
   - Pods actualizados
                ↓
5. E2E tests en Dev
                ↓
6. Si pasa → Manual approval para Stage
                ↓
7. Deploy a Stage (mismo proceso)
                ↓
8. Si pasa → Manual approval para Prod
                ↓
9. Deploy a Prod
```

## Estructura Terraform

```
infra/
├── modules/           # Componentes reutilizables
│   ├── network/
│   ├── iam/
│   ├── artifact-registry/
│   ├── gke-cluster/
│   └── digitalocean-jenkins/
│
└── envs/              # Configuración por ambiente
    ├── dev/
    ├── stage/
    └── prod/
```

## ¿Por qué multi-cloud?

| Aspecto | Solo GCP | Multi-cloud (DO + GCP) |
|---------|----------|------------------------|
| Jenkins | Cloud Run ~$80/mes | Droplet $24/mes |
| Créditos gratis | $300 (3 meses) | DO $200 + GCP $300 |
| Meses gratis | ~2 meses | ~10 meses |
| Skills | 1 cloud | 2 clouds |
| Costo después | ~$150/mes | ~$100/mes |

**Ahorro:** ~$50/mes + 8 meses extra gratis

## Variables Importantes

```hcl
# GCP
project_id = "tu-proyecto"
region     = "us-central1"
zone       = "us-central1-a"

# DigitalOcean
do_token   = "dop_v1_xxxxx"
do_region  = "nyc1"

# Jenkins
jenkins_admin_password = "TuPasswordSeguro123!"
ssh_public_key = "ssh-rsa AAAAB3..."
```

## Comandos Clave

```powershell
# Deploy todo
cd infra/envs/dev
terraform init
terraform apply

# Ver outputs
terraform output

# Conectar a GKE
gcloud container clusters get-credentials ecommerce-dev-gke --zone=us-central1-a

# SSH a Jenkins
ssh root@<jenkins-ip>

# Destruir todo
terraform destroy
```

## Seguridad

✅ Service Accounts con permisos mínimos  
✅ Firewall configurado  
✅ VPC privada  
✅ Credenciales en Terraform state (remoto en GCS)  
❌ No exponer contraseñas en Git

## Para el Proyecto Académico

**Cumple:**
- ✅ Infraestructura como Código (Terraform)
- ✅ Multi-ambiente (dev/stage/prod)
- ✅ CI/CD (Jenkins)
- ✅ Containerización (Docker + Kubernetes)
- ✅ Cloud deployment (GCP + DO)
- ✅ Arquitectura de microservicios
