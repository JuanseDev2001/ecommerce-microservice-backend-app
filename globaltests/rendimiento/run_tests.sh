#!/bin/bash
# Script para ejecutar todas las pruebas de rendimiento con Locust en Linux/Jenkins
# Cada prueba dura 30 segundos y genera reportes en la carpeta reports

echo "Ejecutando pruebas de rendimiento con Locust..."

# Crear directorio de reportes si no existe
mkdir -p reports

# Configurar hosts (usar nombres de servicio de Kubernetes/Docker)
USER_HOST="http://user-service:8700"
ORDER_HOST="http://order-service:8300"
PRODUCT_HOST="http://product-service:8500"
PAYMENT_HOST="http://payment-service:8400"
FAVOURITE_HOST="http://favourite-service:8800"

# User Service
echo -e "\nProbando User Service..."
python3 -m locust -f user_locustfile.py --headless -u 50 -r 5 -t 30s --host $USER_HOST --csv=reports/user_report --html=reports/user_report.html || true

# Order Service
echo -e "\nProbando Order Service..."
python3 -m locust -f order_locustfile.py --headless -u 50 -r 5 -t 30s --host $ORDER_HOST --csv=reports/order_report --html=reports/order_report.html || true

# Product Service
echo -e "\nProbando Product Service..."
python3 -m locust -f product_locustfile.py --headless -u 50 -r 5 -t 30s --host $PRODUCT_HOST --csv=reports/product_report --html=reports/product_report.html || true

# Payment Service
echo -e "\nProbando Payment Service..."
python3 -m locust -f payment_locustfile.py --headless -u 50 -r 5 -t 30s --host $PAYMENT_HOST --csv=reports/payment_report --html=reports/payment_report.html || true

# Favourite Service
echo -e "\nProbando Favourite Service..."
python3 -m locust -f favourite_locustfile.py --headless -u 50 -r 5 -t 30s --host $FAVOURITE_HOST --csv=reports/favourite_report --html=reports/favourite_report.html || true

echo -e "\nTodas las pruebas completadas. Revisa la carpeta 'reports' para ver los resultados."

# Validate performance metrics
echo -e "\nValidando métricas de rendimiento..."
python3 validate_performance.py

if [ $? -ne 0 ]; then
    echo -e "\nLas pruebas de rendimiento NO cumplieron con los umbrales establecidos."
    exit 1
else
    echo -e "\nTodas las métricas de rendimiento están dentro de los umbrales aceptables."
    exit 0
fi
