# Jenkins en DigitalOcean

## 1. Obtener API Token

1. Crear cuenta: https://try.digitalocean.com/freetrialoffer/ ($200 gratis por 60 días)
2. Obtener token: https://cloud.digitalocean.com/account/api/tokens
   - Generate New Token → `ecommerce-terraform` → Read + Write
   - Copiar el token (solo se muestra una vez)

## 2. Generar SSH Key

```powershell
ssh-keygen -t rsa -b 4096 -C "jenkins@ecommerce" -f $env:USERPROFILE\.ssh\jenkins_rsa
Get-Content $env:USERPROFILE\.ssh\jenkins_rsa.pub
```

## 3. Configurar terraform.tfvars

```powershell
cd infra/envs/dev
notepad terraform.tfvars
```

Agregar:
```hcl
# DigitalOcean
do_token   = "dop_v1_xxxxxxxxxxxxx"
do_region  = "nyc1"

# Jenkins
jenkins_droplet_size   = "s-2vcpu-4gb"
jenkins_admin_user     = "admin"
jenkins_admin_password = "TuPasswordSeguro123!"

# SSH
ssh_public_key = "ssh-rsa AAAAB3... jenkins@ecommerce"
```

## 4. Deploy

```powershell
# Desde infra/envs/dev (después de deploy GCP)
terraform apply
```

Tarda ~5 minutos. Crea:
- Droplet Ubuntu 20.04 con Docker + Jenkins
- Firewall (SSH + Jenkins UI)
- Instala: Jenkins, gcloud, kubectl

## 5. Configurar Jenkins con GCP

```powershell
# Obtener IP
$IP = terraform output -raw jenkins_droplet_ip

# Copiar service account key
terraform output -raw jenkins_sa_key > jenkins-key.json
scp jenkins-key.json root@${IP}:/var/lib/jenkins/gcp-key.json

# Configurar GCP en Jenkins
ssh root@$IP
sudo -u jenkins /var/lib/jenkins/setup-gcp.sh

# Verificar
sudo -u jenkins gcloud projects list
sudo -u jenkins kubectl get nodes
```

## 6. Acceder a Jenkins

```
URL: http://<IP>:8080
User: admin
Pass: (el que pusiste en terraform.tfvars)
```

## 7. Configurar Manualmente en Jenkins UI

1. **Instalar plugins** (Manage Jenkins → Plugins):
   - Git
   - Docker Pipeline
   - Kubernetes
   - Google Kubernetes Engine

2. **Agregar credenciales** (Manage Jenkins → Credentials):
   - GitHub token (si usas repos privados)
   - GCP Service Account key (ya está en `/var/lib/jenkins/gcp-key.json`)

3. **Crear pipeline job**:
   - New Item → Pipeline
   - Pipeline script from SCM → Git
   - Repository URL: tu repo
   - Script path: `Jenkinsfile`

## Jenkinsfile de Ejemplo

```groovy
pipeline {
    agent any
    
    environment {
        PROJECT_ID = 'tu-proyecto-gcp'
        REGISTRY = 'us-central1-docker.pkg.dev/tu-proyecto-gcp/ecommerce-dev'
        IMAGE = 'user-service'
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'docker build -t ${IMAGE}:${BUILD_NUMBER} .'
            }
        }
        
        stage('Push') {
            steps {
                sh '''
                    gcloud auth configure-docker us-central1-docker.pkg.dev
                    docker tag ${IMAGE}:${BUILD_NUMBER} ${REGISTRY}/${IMAGE}:${BUILD_NUMBER}
                    docker push ${REGISTRY}/${IMAGE}:${BUILD_NUMBER}
                '''
            }
        }
        
        stage('Deploy') {
            steps {
                sh '''
                    gcloud container clusters get-credentials ecommerce-dev-gke --zone=us-central1-a
                    kubectl set image deployment/${IMAGE} ${IMAGE}=${REGISTRY}/${IMAGE}:${BUILD_NUMBER}
                '''
            }
        }
    }
}
```

## Troubleshooting

```bash
# Ver logs de Jenkins
ssh root@<IP>
journalctl -u jenkins -f

# Restart Jenkins
systemctl restart jenkins

# Test GCP connection
sudo -u jenkins gcloud projects list
sudo -u jenkins kubectl get nodes
```

## Costo

**$24/mes** (gratis primeros 8 meses con $200 de créditos)
