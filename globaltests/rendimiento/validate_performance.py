#!/usr/bin/env python3
"""
Script para validar las métricas de rendimiento de Locust
contra umbrales definidos y determinar si el PR debe pasar.
"""

import csv
import sys
import os
from pathlib import Path

# Definir umbrales de rendimiento aceptables
THRESHOLDS = {
    'max_avg_response_time': 1000,  # ms - Tiempo de respuesta promedio máximo
    'max_error_rate': 5.0,           # % - Tasa de errores máxima permitida
    'min_throughput': 10.0,          # req/s - Throughput mínimo aceptable
    'max_95_percentile': 2000        # ms - Percentil 95 máximo
}

def read_stats_csv(filepath):
    """Lee el archivo _stats.csv de Locust y retorna las métricas."""
    if not os.path.exists(filepath):
        print(f"Error: No se encontró el archivo {filepath}")
        return None
    
    with open(filepath, 'r') as f:
        reader = csv.DictReader(f)
        rows = list(reader)
        
        # La última fila suele ser el agregado total
        if rows:
            # Buscar la fila "Aggregated" o usar la última
            for row in rows:
                if row.get('Name') == 'Aggregated':
                    return row
            return rows[-1]
    return None

def validate_metrics(service_name, stats_file):
    """Valida las métricas de un servicio contra los umbrales."""
    print(f"\n{'='*60}")
    print(f"Validando métricas de rendimiento para: {service_name}")
    print(f"{'='*60}")
    
    stats = read_stats_csv(stats_file)
    if not stats:
        print(f"No se pudieron leer las estadísticas para {service_name}")
        return False
    
    passed = True
    
    # Validar tiempo de respuesta promedio
    avg_response = float(stats.get('Average Response Time', 0))
    print(f"\n Tiempo de Respuesta Promedio: {avg_response:.2f} ms")
    if avg_response > THRESHOLDS['max_avg_response_time']:
        print(f"FALLO: Excede el límite de {THRESHOLDS['max_avg_response_time']} ms")
        passed = False
    else:
        print(f"PASA: Dentro del límite de {THRESHOLDS['max_avg_response_time']} ms")
    
    # Validar tasa de errores
    total_requests = int(stats.get('Request Count', 1))
    failures = int(stats.get('Failure Count', 0))
    error_rate = (failures / total_requests * 100) if total_requests > 0 else 0
    print(f"\n Tasa de Errores: {error_rate:.2f}% ({failures}/{total_requests})")
    if error_rate > THRESHOLDS['max_error_rate']:
        print(f"FALLO: Excede el límite de {THRESHOLDS['max_error_rate']}%")
        passed = False
    else:
        print(f"PASA: Dentro del límite de {THRESHOLDS['max_error_rate']}%")
    
    # Validar throughput (requests per second)
    rps = float(stats.get('Requests/s', 0))
    print(f"\n Throughput: {rps:.2f} req/s")
    if rps < THRESHOLDS['min_throughput']:
        print(f"FALLO: Por debajo del mínimo de {THRESHOLDS['min_throughput']} req/s")
        passed = False
    else:
        print(f"PASA: Cumple el mínimo de {THRESHOLDS['min_throughput']} req/s")
    
    # Validar percentil 95
    p95 = float(stats.get('95%', 0))
    print(f"\n Percentil 95: {p95:.2f} ms")
    if p95 > THRESHOLDS['max_95_percentile']:
        print(f"FALLO: Excede el límite de {THRESHOLDS['max_95_percentile']} ms")
        passed = False
    else:
        print(f"PASA: Dentro del límite de {THRESHOLDS['max_95_percentile']} ms")
    
    return passed

def main():
    """Función principal que valida todos los servicios."""
    reports_dir = Path(__file__).parent / 'reports'
    
    services = [
        'user_report',
        'order_report',
        'product_report',
        'payment_report',
        'favourite_report'
    ]
    
    all_passed = True
    results = {}
    
    print("\n" + "="*60)
    print("INICIANDO VALIDACIÓN DE PRUEBAS DE RENDIMIENTO")
    print("="*60)
    
    for service in services:
        stats_file = reports_dir / f"{service}_stats.csv"
        service_name = service.replace('_report', '').title() + ' Service'
        
        passed = validate_metrics(service_name, stats_file)
        results[service_name] = passed
        
        if not passed:
            all_passed = False
    
    # Resumen final
    print("\n" + "="*60)
    print("RESUMEN DE VALIDACIÓN")
    print("="*60)
    
    for service_name, passed in results.items():
        status = "APROBADO" if passed else "RECHAZADO"
        print(f"{service_name}: {status}")
    
    print("\n" + "="*60)
    if all_passed:
        print("RESULTADO FINAL: TODAS LAS PRUEBAS PASARON")
        print("El PR puede ser aprobado")
        print("="*60)
        sys.exit(0)
    else:
        print("RESULTADO FINAL: ALGUNAS PRUEBAS FALLARON")
        print("El PR debe ser rechazado")
        print("="*60)
        sys.exit(1)

if __name__ == '__main__':
    main()
