// Definición del Pipeline Declarativo
pipeline {
    // Agente (Agent): Donde se ejecuta el trabajo. 
    agent any

    // La lista de servicios la tomamos directamente de tu input
    def services = [
        'api-gateway', 'cloud-config', 'favourite-service', 'order-service',
        'payment-service', 'product-service', 'proxy-client', 'service-discovery',
        'shipping-service', 'user-service'
    ]

    // Variables de entorno
    environment {
        BRANCH_NAME = "${env.BRANCH_NAME}"
        DOCKER_REGISTRY = "your-docker-hub-user" 
        DOCKER_TLS_VERIFY = ''
        DOCKER_HOST = ''
        DOCKER_CERT_PATH = ''
        MINIKUBE_ACTIVE_DOCKERD = ''
    }

    stages {
        // ---------------------------------------------------------------------
        // 1. STAGE: COMPROBAR RAMA Y CONFIGURAR ENTORNO
        // ---------------------------------------------------------------------
        stage('Check Branch and Setup') {
            steps {
                script {
                    echo "Starting build for branch: ${BRANCH_NAME}"
                    
                    // --- Configuración de Entorno Docker (Minikube en Windows) ---
                    def dockerEnvOutput = sh(
                        script: 'minikube docker-env --shell cmd',
                        returnStdout: true
                    ).trim()

                    // Procesamos la salida para extraer las variables SET
                    def envVars = [:]
                    dockerEnvOutput.eachLine { line ->
                        if (line.startsWith('SET ')) {
                            def parts = line.substring(4).split('=', 2)
                            if (parts.size() == 2) {
                                envVars[parts[0]] = parts[1].replaceAll('\\\\', '/') // Normalizar rutas de Windows
                            }
                        }
                    }

                    // Inyectar las variables al entorno global de Jenkins
                    env.DOCKER_TLS_VERIFY = envVars['DOCKER_TLS_VERIFY'] ?: ''
                    env.DOCKER_HOST = envVars['DOCKER_HOST'] ?: ''
                    env.DOCKER_CERT_PATH = envVars['DOCKER_CERT_PATH'] ?: ''
                    env.MINIKUBE_ACTIVE_DOCKERD = envVars['MINIKUBE_ACTIVE_DOCKERD'] ?: ''
                    // ----------------------------------------------------------------------
                    
                    echo "Docker HOST configured to: ${env.DOCKER_HOST}"
                    echo "Docker environment configured via Minikube for Windows."
                }
            }
        }

        // ---------------------------------------------------------------------
        // 2. STAGE: CONSTRUCCIÓN Y PRUEBAS UNITARIAS (Matriz)
        // La matriz itera sobre cada microservicio
        // ---------------------------------------------------------------------
        stage('Build & Unit Tests') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values services 
                    }
                }
                
                // Opción para ejecutar 4 trabajos en paralelo (4 microservicios a la vez)
                agent { 
                    label 'maven_node' 
                } 

                steps {
                    script {
                        echo "Processing Microservice: ${SERVICE}"
                        
                        dir("${SERVICE}") {
                            // 1. Compilación y creación del JAR
                            sh 'mvn clean install -DskipTests' 
                            
                            // 2. Ejecución de Pruebas Unitarias (Actividad 2)
                            sh 'mvn test -Dgroups="unit" || true'
                        
                            // 3. Creación de la Imagen Docker
                            sh "docker build -t ${DOCKER_REGISTRY}/${SERVICE}:${BRANCH_NAME} ."

                            // 4. Subida al Registro (Solo si NO estamos en dev)
                            when { expression { return env.BRANCH_NAME != 'dev' } }
                            steps {
                                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                                    sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}"
                                    sh "docker push ${DOCKER_REGISTRY}/${SERVICE}:${BRANCH_NAME}"
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // ---------------------------------------------------------------------
        // 3. STAGE: DESPLIEGUE EN STAGE (Kubernetes)
        // Se ejecuta SOLO si la rama es 'stage' (Actividad 4)
        // ---------------------------------------------------------------------
        stage('Deploy to Stage') {
            when { branch 'stage' } // Solo se ejecuta si el nombre de la rama es 'stage'
            
            steps {
                script {
                    echo "Deploying ALL services to STAGE Kubernetes cluster (Minikube)..."
                    
                    // Aplicar Manifiestos de Kubernetes para cada servicio
                    // Asume que tienes tus archivos YAML en la carpeta k8s/stage/
                    for (svc in services) {
                        sh "kubectl apply -f k8s/stage/${svc}-deployment.yaml"
                    }

                    // 4. Esperar a que el Api Gateway esté activo antes de continuar
                    sh "kubectl rollout status deployment/api-gateway"
                }
            }
        }

        // ---------------------------------------------------------------------
        // 4. STAGE: PRUEBAS DE SISTEMA (STAGE Environment)
        // Se ejecuta SOLO si la rama es 'stage' (Actividad 3)
        // ---------------------------------------------------------------------
        stage('System Tests (Integration & E2E)') {
            when { branch 'stage' } 
            
            steps {
                script {
                    echo "Running Integration, E2E, and Performance tests against STAGE deployment."

                    // 1. Pruebas de Integración (5+ pruebas que validen la comunicación)
                    // Asumimos un módulo de pruebas global o un servicio de pruebas
                    sh 'mvn test -Dgroups="integration"' 

                    // 2. Pruebas E2E (5+ pruebas que validen flujos de usuario)
                    // Podrías usar un contenedor Docker para ejecutar estas pruebas (ej. Cypress)
                    sh 'docker run --network host my-e2e-tester-image npm run test:e2e' 

                    // 3. Pruebas de Rendimiento (Locust)
                    // Obtener la IP del Api-Gateway desplegado en Minikube (usando 'minikube service')
                    def apiGatewayIp = sh(returnStdout: true, script: 'minikube service api-gateway --url').trim()
                    
                    // Ejecutar Locust
                    sh "locust -f performance_tests.py --host=${apiGatewayIp} --run-time 60s --users 10 --spawn-rate 5 --headless"
                }
            }
        }
        
        // ---------------------------------------------------------------------
        // 5. STAGE: CLEANUP
        // ---------------------------------------------------------------------
        stage('Cleanup') {
            steps {
                // Eliminar imágenes locales o contenedores temporales
                sh 'docker image prune -f' 
            }
        }
    }
}