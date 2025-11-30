# Google Cloud Setup

## 1. Prerequisitos

```powershell
# Login
gcloud auth login
gcloud auth application-default login

# Configurar proyecto
$env:PROJECT_ID = "tu-proyecto-id"
gcloud config set project $env:PROJECT_ID

# Habilitar APIs
gcloud services enable compute.googleapis.com container.googleapis.com artifactregistry.googleapis.com iam.googleapis.com

# Crear bucket para Terraform state
gsutil mb -p $env:PROJECT_ID gs://$env:PROJECT_ID-terraform-state
gsutil versioning set on gs://$env:PROJECT_ID-terraform-state
```

## 2. Configurar Variables

```powershell
cd infra/envs/dev
cp terraform.tfvars.example terraform.tfvars
notepad terraform.tfvars
```

Edita `terraform.tfvars`:
```hcl
project_id = "tu-proyecto-gcp"
region     = "us-central1"
zone       = "us-central1-a"
```

Edita `backend.tf`:
```hcl
terraform {
  backend "gcs" {
    bucket = "tu-proyecto-gcp-terraform-state"
    prefix = "terraform/dev"
  }
}
```

## 3. Deploy

```powershell
terraform init
terraform plan
terraform apply
```

Tarda ~10 minutos. Crea:
- VPC network
- GKE cluster (dev: 1 nodo, stage: 3 nodos, prod: 3 nodos)
- Artifact Registry
- Service Accounts

## 4. Conectarse al Cluster

```powershell
# Ver outputs
terraform output

# Conectar kubectl
$CLUSTER = terraform output -raw gke_cluster_name
gcloud container clusters get-credentials $CLUSTER --zone=us-central1-a --project=$env:PROJECT_ID

# Verificar
kubectl get nodes
```

## 5. Configurar Docker

```powershell
# Autenticar Docker con Artifact Registry
gcloud auth configure-docker us-central1-docker.pkg.dev

# Push imagen de ejemplo
docker tag mi-imagen:latest us-central1-docker.pkg.dev/$env:PROJECT_ID/ecommerce-dev/mi-imagen:v1
docker push us-central1-docker.pkg.dev/$env:PROJECT_ID/ecommerce-dev/mi-imagen:v1
```

## 6. Destruir (cuando termines)

```powershell
terraform destroy
```

## Costos Estimados

- **Dev**: ~$25/mes (1 nodo e2-medium preemptible)
- **Stage**: ~$100/mes (3 nodos e2-standard-2 preemptible)
- **Prod**: ~$250/mes (3 nodos e2-standard-4 no preemptible)
