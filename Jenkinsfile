pipeline {
    
    agent none

    environment {
        DEV_BRANCH = 'dev'
        STAGE_BRANCH = 'stage'
    }
    stages {
        stage('Unit & Integration Tests (PR a dev)') {
            when {
                allOf {
                    changeRequest();
                    expression { env.CHANGE_TARGET == env.DEV_BRANCH }
                }
            }
            steps {
                script {
                    def services = [
                        'api-gateway', 'cloud-config', 'favourite-service', 'order-service',
                        'payment-service', 'product-service', 'proxy-client', 'service-discovery',
                        'shipping-service', 'user-service'
                    ]
                    for (svc in services) {
                        dir(svc) {
                            // IMPORTANTE: Usar 'sh' (Shell de Linux), no 'bat' (Windows)
                            sh 'mvn clean verify'
                        }
                    }
                }
            }
            post {
                always {
                    // Los paths de JUnit también funcionan con la ruta Linux del contenedor
                    junit '**/target/surefire-reports/*.xml'
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }
        // ... (El resto de tus etapas)
    }
}