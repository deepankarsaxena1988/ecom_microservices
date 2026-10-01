pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    triggers {
        githubPush()
        pollSCM('H/5 * * * *')
    }

    parameters {
        booleanParam(
            name: 'PUBLISH_LATEST',
            defaultValue: false,
            description: 'Also push the latest tag for builds from the selected release branch.'
        )
        booleanParam(
            name: 'DEPLOY_TO_LOCAL_KUBERNETES',
            defaultValue: false,
            description: 'Pause for approval and deploy Eureka and Config Server to Docker Desktop Kubernetes.'
        )
        string(
            name: 'KUBERNETES_CONTEXT',
            defaultValue: 'ecom-multi-node-cluster',
            description: 'kubectl context used for the approved deployment.'
        )
        string(
            name: 'KUBERNETES_NAMESPACE',
            defaultValue: 'default',
            description: 'Kubernetes namespace used for the approved deployment.'
        )
    }

    environment {
        DOCKER_NAMESPACE = 'deepankarsaxena'
        EUREKA_IMAGE = 'ecomeureka'
        CONFIG_SERVER_IMAGE = 'ecomconfigserver'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.COMMIT_SHA = bat(
                        returnStdout: true,
                        script: '@echo off\ngit rev-parse --short=12 HEAD'
                    ).trim()
                    env.COMMIT_TAG = "${env.COMMIT_SHA}-${env.BUILD_NUMBER}"
                }
                bat 'echo Commit: %COMMIT_SHA%'
            }
        }

        stage('Validate tools') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker version'
                bat 'kubectl version --client'
            }
        }

        stage('Build and test') {
            steps {
                bat 'mvn -B -f ecomConfigServer/pom.xml clean verify'
                bat 'mvn -B -f ecomEureka/eureka/pom.xml clean verify'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build Docker images') {
            steps {
                bat 'docker build --pull -t %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG% -t %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:build-%BUILD_NUMBER% ecomConfigServer'
                bat 'docker build --pull -t %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG% -t %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:build-%BUILD_NUMBER% ecomEureka'
                bat 'docker image inspect %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG%'
                bat 'docker image inspect %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG%'
                bat '''(
                    echo commit=%COMMIT_SHA%
                    echo build=%BUILD_NUMBER%
                    echo config-server=%DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG%
                    echo eureka=%DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG%
                ) > image-build.txt'''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'image-build.txt', fingerprint: true
                }
            }
        }

        stage('Push Docker images') {
            steps {
            withCredentials([usernamePassword(
    credentialsId: 'docker-hub-credentials',
    usernameVariable: 'DOCKER_USERNAME',
    passwordVariable: 'DOCKER_TOKEN'
)]) {
    // Clean login using Windows Command Prompt (bat) execution
    bat 'echo %DOCKER_TOKEN%| docker login --username %DOCKER_USERNAME% --password-stdin'
    
    // Your existing push commands
    bat 'docker push %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG%'
    bat 'docker push %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG%'
    bat 'docker push %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:build-%BUILD_NUMBER%'
    bat 'docker push %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:build-%BUILD_NUMBER%'
    
    script {
        if (params.PUBLISH_LATEST) {
            bat 'docker tag %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG% %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:latest'
            bat 'docker tag %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG% %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:latest'
            bat 'docker push %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:latest'
            bat 'docker push %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:latest'
        }
    }
    bat 'docker logout'
}

            }
        }

        stage('Approve deployment') {
            when {
                expression { params.DEPLOY_TO_LOCAL_KUBERNETES }
            }
            steps {
                input message: "Deploy ${env.COMMIT_TAG} to ${params.KUBERNETES_CONTEXT}/${params.KUBERNETES_NAMESPACE}?", ok: 'Deploy'
            }
        }

        stage('Validate Kubernetes manifests') {
            when {
                expression { params.DEPLOY_TO_LOCAL_KUBERNETES }
            }
            steps {
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" --dry-run=client -f ecomConfigServer/kubernetes.yaml'
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" --dry-run=client -f ecomEureka/kubernetes.yaml'
            }
        }

        stage('Deploy to local Kubernetes') {
            when {
                expression { params.DEPLOY_TO_LOCAL_KUBERNETES }
            }
            steps {
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" -f ecomConfigServer/kubernetes.yaml'
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" -f ecomEureka/kubernetes.yaml'
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" set image deployment/ecom-config-server ecom-config-server=%DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG%'
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" set image deployment/ecom-eureka ecom-eureka=%DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG%'
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" rollout status deployment/ecom-config-server --timeout=180s'
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" rollout status deployment/ecom-eureka --timeout=180s'
            }
        }

        stage('Deployment verification') {
            when {
                expression { params.DEPLOY_TO_LOCAL_KUBERNETES }
            }
            steps {
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" get deployments,pods,services -l app=ecom-config-server -o wide'
                bat 'kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" get deployments,pods,services -l app=ecom-eureka -o wide'
            }
        }
    }

    post {
        always {
            bat 'docker logout >NUL 2>&1 || exit /b 0'
        }
        failure {
            echo 'Pipeline failed. Review the stage logs before retrying or rolling back the previous immutable image tag.'
        }
    }
}
