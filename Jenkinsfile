pipeline {

    agent none

    environment {
        DEV_BRANCH = 'dev'
        STAGE_BRANCH = 'stage'
    }
    stages {
        stage('Unit & Integration Tests (PR a dev)') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-17'
                }
            }
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
                            sh 'mvn clean verify'
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        junit '**/target/surefire-reports/*.xml'
                        junit '**/target/failsafe-reports/*.xml'
                    }
                }
            }
        }
        stage('Global Tests (PR dev a stage)') {
            when {
                allOf {
                    changeRequest();
                    expression { env.CHANGE_SOURCE == env.DEV_BRANCH && env.CHANGE_TARGET == env.STAGE_BRANCH }
                }
            }
            steps {
                dir('globaltests') {
                    sh 'mvn clean verify'
                }
            }
            post {
                always {
                    script {
                        junit 'globaltests/target/surefire-reports/*.xml'
                    }
                }
            }
        }
    }
}
