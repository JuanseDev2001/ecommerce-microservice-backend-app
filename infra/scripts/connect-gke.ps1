# Connect to GKE cluster and configure kubectl
# This script retrieves credentials for the GKE cluster

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet('dev', 'stage', 'prod')]
    [string]$Environment,
    
    [Parameter(Mandatory=$true)]
    [string]$ProjectId,
    
    [Parameter(Mandatory=$false)]
    [string]$Region = "us-central1",
    
    [Parameter(Mandatory=$false)]
    [string]$Zone = "us-central1-a",
    
    [Parameter(Mandatory=$false)]
    [string]$ClusterName = ""
)

$ErrorActionPreference = "Stop"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Connecting to GKE Cluster" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Default cluster name if not provided
if ([string]::IsNullOrEmpty($ClusterName)) {
    $ClusterName = "ecommerce-$Environment-gke"
}

Write-Host "Environment: $Environment" -ForegroundColor Green
Write-Host "Project: $ProjectId" -ForegroundColor Green
Write-Host "Cluster: $ClusterName" -ForegroundColor Green
Write-Host ""

# Check if cluster is regional or zonal
if ($Environment -eq "prod") {
    Write-Host "Connecting to regional cluster..." -ForegroundColor Yellow
    gcloud container clusters get-credentials $ClusterName --region $Region --project $ProjectId
} else {
    Write-Host "Connecting to zonal cluster..." -ForegroundColor Yellow
    gcloud container clusters get-credentials $ClusterName --zone $Zone --project $ProjectId
}

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Connected Successfully!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Show cluster info
Write-Host "Cluster Information:" -ForegroundColor Yellow
kubectl cluster-info
Write-Host ""

Write-Host "Available Nodes:" -ForegroundColor Yellow
kubectl get nodes
Write-Host ""

Write-Host "Namespaces:" -ForegroundColor Yellow
kubectl get namespaces
Write-Host ""

# Check for Jenkins
Write-Host "Jenkins Status:" -ForegroundColor Yellow
kubectl get pods -n jenkins 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Jenkins namespace not found or no pods deployed yet" -ForegroundColor Yellow
}
