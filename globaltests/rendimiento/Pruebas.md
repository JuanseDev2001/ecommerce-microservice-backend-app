## Pasos para ejecutar pruebas de rendimiento con Locust

1. **Asegúrate de que los microservicios estén corriendo**
	- Despliega los servicios (por ejemplo, usando Minikube o Docker Compose).
	- Verifica que los endpoints estén accesibles desde donde ejecutarás Locust.

2. **Instala las dependencias de Python**
	- Abre una terminal en la carpeta `globaltests/rendimiento`.
	- Ejecuta:
	  ```
	  pip install -r requirements.txt
	  ```

3. **Ejecuta Locust para cada microservicio**
	
	**Opción 1: Usar scripts automatizados**
	
	Para entorno local (localhost):
	```
	.\run_tests.ps1
	```
	
	Para entorno Docker/Kubernetes:
	```
	.\run_tests.ps1 -Environment docker
	```
	
	En Linux/Jenkins (usa nombres de servicio):
	```
	./run_tests.sh
	```
	
	**Opción 2: Ejecutar manualmente**
	
	- Por ejemplo, para el servicio de favoritos:
	  ```
	  locust -f favourite_locustfile.py --headless -u 50 -r 5 -t 2m --host http://localhost:PORT --csv=favourite_report
	  ```
	- Cambia el archivo locustfile y el host según el microservicio que quieras probar.
	- Parámetros útiles:
	  - `-u`: usuarios concurrentes
	  - `-r`: usuarios nuevos por segundo
	  - `-t`: duración de la prueba
	  - `--csv`: nombre base para los archivos de reporte

4. **Repite el paso anterior para cada microservicio:**
	- `user_locustfile.py`, `order_locustfile.py`, `product_locustfile.py`, `payment_locustfile.py`, etc.

5. **Obtén y analiza las métricas**
	- Los archivos CSV generados contendrán:
	  - **Tiempo de respuesta:** columnas como `Average response time`, `Median response time`.
	  - **Throughput:** columna `Requests/s` o `Total requests`.
	  - **Tasa de errores:** columnas `# Fails` o `Failure %`.
	- También puedes ver estas métricas en la consola o en la interfaz web de Locust (si no usas `--headless`).

6. **(Opcional) Ajusta los parámetros de carga**
   - Modifica `-u`, `-r` y `-t` para simular diferentes escenarios de carga.

## Validación Automática de Métricas

El script `validate_performance.py` valida automáticamente las métricas contra umbrales definidos:

**Umbrales configurados:**
- Tiempo de respuesta promedio: ≤ 1000 ms
- Tasa de errores: ≤ 1.0%
- Throughput mínimo: ≥ 10 req/s
- Percentil 95: ≤ 2000 ms

**Ejecución manual:**
```bash
python validate_performance.py
```

El script retorna:
- Exit code 0: Todas las pruebas pasaron ✅
- Exit code 1: Alguna prueba falló ❌

**En el pipeline:**
Si alguna métrica excede los umbrales, el pipeline fallará automáticamente y el PR será rechazado.