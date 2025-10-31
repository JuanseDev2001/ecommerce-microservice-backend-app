# PowerShell script to build and push all microservice images to Docker Hub

$ErrorActionPreference = 'Continue'

# Remove all local Docker images (force, only if any exist)
$localImages = docker images -q
if ($localImages) {
    Write-Host "Removing all local Docker images..." -ForegroundColor Yellow
    docker rmi -f $localImages
} else {
    Write-Host "No local Docker images to remove." -ForegroundColor Green
}

# Array of service names
$services = @(
    "cloud-config",
    "service-discovery",
    "api-gateway",
    "proxy-client",
    "order-service",
    "payment-service",
    "product-service",
    "shipping-service",
    "user-service",
    "favourite-service"
)

# Docker Hub username
$dockerUser = "juanse201"


foreach ($service in $services) {
    $imageName = "$dockerUser/$service-ecommerce-boot:0.1.0"
    $dockerfilePath = "./$service/Dockerfile"
    Write-Host "Building $imageName ..."
    docker build -t $imageName -f $dockerfilePath .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed for $service" -ForegroundColor Red
        exit 1
    }
    Write-Host "Pushing $imageName ..."
    docker push $imageName
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Push failed for $service" -ForegroundColor Red
        exit 1
    }
}

Write-Host "All images built and pushed successfully!" -ForegroundColor Green
