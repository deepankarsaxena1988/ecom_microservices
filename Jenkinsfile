pipeline {
    agent any

    options {
        timestamps()
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
            steps {
                script {
                    def deploymentDecision = input(
                        message: "Deploy ${env.COMMIT_TAG} to ${params.KUBERNETES_CONTEXT}/${params.KUBERNETES_NAMESPACE}?",
                        ok: 'Submit decision',
                        parameters: [
                            choice(
                                name: 'DEPLOYMENT_DECISION',
                                choices: ['Deny', 'Approve'],
                                description: 'Choose whether this build should deploy.'
                            )
                        ]
                    )
                    env.DEPLOY_TO_LOCAL_KUBERNETES = deploymentDecision == 'Approve' ? 'true' : 'false'
                    echo "Deployment decision: ${deploymentDecision}; Kubernetes deployment enabled: ${env.DEPLOY_TO_LOCAL_KUBERNETES}"
                }
            }
        }

        stage('Ensure Kubernetes cluster is running') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                script {
                    def windowsIdentity = bat(
                        returnStdout: true,
                        script: '@echo off\nwhoami'
                    ).trim()
                    echo "Windows identity executing deployment: ${windowsIdentity}"
                    echo 'Using the default WSL distribution shown by `wsl.exe --list --verbose`.'
                    bat 'echo Windows profile: %USERPROFILE%'
                    bat 'wsl.exe --list --verbose'
                    def wslLaunchStatus = bat(
                        returnStatus: true,
                        script: 'wsl.exe --exec /bin/true'
                    )
                    if (wslLaunchStatus != 0) {
                        error("WSL could not launch the default distribution for Windows identity ${windowsIdentity}. Ensure the Jenkins service user's default WSL distribution is Ubuntu and that this account can launch it interactively.")
                    }
                    env.WSL_WORKSPACE = bat(
                        returnStdout: true,
                        script: '@echo off\nwsl.exe --exec wslpath -a "%WORKSPACE%"'
                    ).trim()
                    def clusterStatus = bat(
                        returnStatus: true,
                        script: 'wsl.exe --exec /home/deepankar/.local/bin/minikube status -p ecom-multi-node-cluster'
                    )
                    if (clusterStatus != 0) {
                        error('Minikube profile ecom-multi-node-cluster exists, but its status check failed. Review the preceding Minikube SSH/API errors and repair the Ubuntu cluster before retrying. Jenkins will not run minikube start or reset the cluster automatically.')
                    }
                    bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" get nodes -o wide'
                }
            }
        }

        stage('Validate Kubernetes manifests') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" --dry-run=client -f "%WSL_WORKSPACE%/ecomConfigServer/kubernetes.yaml"'
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" --dry-run=client -f "%WSL_WORKSPACE%/ecomEureka/kubernetes.yaml"'
            }
        }

        stage('Deploy to local Kubernetes') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" -f "%WSL_WORKSPACE%/ecomConfigServer/kubernetes.yaml"'
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" -f "%WSL_WORKSPACE%/ecomEureka/kubernetes.yaml"'
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" set image deployment/ecom-config-server ecom-config-server=%DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG%'
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" set image deployment/ecom-eureka ecom-eureka=%DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG%'
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" rollout status deployment/ecom-config-server --timeout=180s'
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" rollout status deployment/ecom-eureka --timeout=180s'
            }
        }

        stage('Deployment verification') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" get deployments,pods,services -l app=ecom-config-server -o wide'
                bat 'wsl.exe --exec /usr/local/bin/kubectl --context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" get deployments,pods,services -l app=ecom-eureka -o wide'
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
