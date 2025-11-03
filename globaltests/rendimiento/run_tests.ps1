# Script para ejecutar todas las pruebas de rendimiento con Locust
# Cada prueba dura 30 segundos y genera reportes en la carpeta reports

param(
    [string]$Environment = "local"
)

Write-Host "Ejecutando pruebas de rendimiento con Locust..." -ForegroundColor Green
Write-Host "Entorno: $Environment" -ForegroundColor Yellow

# Configurar hosts según el entorno
if ($Environment -eq "docker" -or $Environment -eq "k8s") {
    $UserHost = "http://user-service:8700"
    $OrderHost = "http://order-service:8300"
    $ProductHost = "http://product-service:8500"
    $PaymentHost = "http://payment-service:8400"
    $FavouriteHost = "http://favourite-service:8800"
} else {
    $UserHost = "http://localhost:8700"
    $OrderHost = "http://localhost:8300"
    $ProductHost = "http://localhost:8500"
    $PaymentHost = "http://localhost:8400"
    $FavouriteHost = "http://localhost:8800"
}

# User Service
Write-Host "`nProbando User Service..." -ForegroundColor Cyan
python -m locust -f user_locustfile.py --headless -u 50 -r 5 -t 30s --host $UserHost --csv=reports/user_report --html=reports/user_report.html

# Order Service
Write-Host "`nProbando Order Service..." -ForegroundColor Cyan
python -m locust -f order_locustfile.py --headless -u 50 -r 5 -t 30s --host $OrderHost --csv=reports/order_report --html=reports/order_report.html

# Product Service
Write-Host "`nProbando Product Service..." -ForegroundColor Cyan
python -m locust -f product_locustfile.py --headless -u 50 -r 5 -t 30s --host $ProductHost --csv=reports/product_report --html=reports/product_report.html

# Payment Service
Write-Host "`nProbando Payment Service..." -ForegroundColor Cyan
python -m locust -f payment_locustfile.py --headless -u 50 -r 5 -t 30s --host $PaymentHost --csv=reports/payment_report --html=reports/payment_report.html

# Favourite Service
Write-Host "`nProbando Favourite Service..." -ForegroundColor Cyan
python -m locust -f favourite_locustfile.py --headless -u 50 -r 5 -t 30s --host $FavouriteHost --csv=reports/favourite_report --html=reports/favourite_report.html

Write-Host "`nTodas las pruebas completadas. Revisa la carpeta 'reports' para ver los resultados." -ForegroundColor Green

# Validate performance metrics
Write-Host "`nValidando métricas de rendimiento..." -ForegroundColor Cyan
python validate_performance.py

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n Las pruebas de rendimiento NO cumplieron con los umbrales establecidos." -ForegroundColor Red
    exit 1
} else {
    Write-Host "`n Todas las métricas de rendimiento están dentro de los umbrales aceptables." -ForegroundColor Green
    exit 0
}
