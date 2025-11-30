# Enable required GCP APIs for the project
# This script enables all necessary Google Cloud APIs for the infrastructure

param(
    [Parameter(Mandatory=$true)]
    [string]$ProjectId
)

$ErrorActionPreference = "Stop"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Enabling GCP APIs" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# List of required APIs
$apis = @(
    "compute.googleapis.com",
    "container.googleapis.com",
    "artifactregistry.googleapis.com",
    "iam.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "storage.googleapis.com",
    "secretmanager.googleapis.com",
    "logging.googleapis.com",
    "monitoring.googleapis.com",
    "cloudtrace.googleapis.com",
    "servicenetworking.googleapis.com"
)

# Set project
Write-Host "Setting GCP project: $ProjectId" -ForegroundColor Green
gcloud config set project $ProjectId
Write-Host ""

# Enable each API
foreach ($api in $apis) {
    Write-Host "Enabling API: $api" -ForegroundColor Yellow
    gcloud services enable $api --project=$ProjectId
    Write-Host "  ✓ Enabled" -ForegroundColor Green
}

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "All APIs Enabled!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Enabled APIs:" -ForegroundColor Yellow
foreach ($api in $apis) {
    Write-Host "  • $api" -ForegroundColor White
}
