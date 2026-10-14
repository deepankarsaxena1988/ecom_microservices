def runOnAgent(String unixCommand, String windowsCommand, Map options = [:]) {
    if (isUnix()) {
        return withEnv(['PATH+LOCAL=/usr/local/bin']) {
            sh(options + [script: unixCommand])
        }
    }
    return bat(options + [script: windowsCommand])
}

def runMaven(String arguments) {
    dir('ecomEureka/eureka') {
        runOnAgent("sh ./mvnw ${arguments}", "mvnw.cmd ${arguments}")
    }
}

def runKubectl(String unixArguments, String windowsArguments) {
    return withCredentials([file(
        credentialsId: 'kubernetes-kubeconfig',
        variable: 'KUBECONFIG_FILE'
    )]) {
        if (isUnix()) {
            return runOnAgent(
                'kubectl --kubeconfig "$KUBECONFIG_FILE" ' + unixArguments,
                ''
            )
        }
        def wslKubeconfig = bat(
            returnStdout: true,
            script: '@echo off\nwsl.exe --exec wslpath -a "%KUBECONFIG_FILE%"'
        ).trim()
        return withEnv(["WSL_KUBECONFIG_FILE=${wslKubeconfig}"]) {
            bat(script: 'wsl.exe --exec /usr/local/bin/kubectl --kubeconfig "%WSL_KUBECONFIG_FILE%" ' + windowsArguments)
        }
    }
}

pipeline {
    agent { label 'docker-desktop' }

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
                    env.COMMIT_SHA = runOnAgent(
                        'git rev-parse --short=12 HEAD',
                        '@echo off\ngit rev-parse --short=12 HEAD',
                        [returnStdout: true]
                    ).trim()
                    env.COMMIT_TAG = "${env.COMMIT_SHA}-${env.BUILD_NUMBER}"
                }
                echo "Commit: ${env.COMMIT_SHA}"
            }
        }

        stage('Validate tools') {
            steps {
                runOnAgent('java -version', 'java -version')
                runMaven('-version')
                runOnAgent('docker version', 'docker version')
                runOnAgent('kubectl version --client', 'kubectl version --client')
            }
        }

        stage('Build and test') {
            steps {
                runMaven('-B -f ../../ecomConfigServer/pom.xml clean verify')
                runMaven('-B clean verify')
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build Docker images') {
            steps {
                runOnAgent(
                    'docker build --pull -t "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:$COMMIT_TAG" -t "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:build-$BUILD_NUMBER" ecomConfigServer',
                    'docker build --pull -t %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG% -t %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:build-%BUILD_NUMBER% ecomConfigServer'
                )
                runOnAgent(
                    'docker build --pull -t "$DOCKER_NAMESPACE/$EUREKA_IMAGE:$COMMIT_TAG" -t "$DOCKER_NAMESPACE/$EUREKA_IMAGE:build-$BUILD_NUMBER" ecomEureka',
                    'docker build --pull -t %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG% -t %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:build-%BUILD_NUMBER% ecomEureka'
                )
                runOnAgent(
                    'docker image inspect "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:$COMMIT_TAG"',
                    'docker image inspect %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG%'
                )
                runOnAgent(
                    'docker image inspect "$DOCKER_NAMESPACE/$EUREKA_IMAGE:$COMMIT_TAG"',
                    'docker image inspect %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG%'
                )
                writeFile file: 'image-build.txt', text: """commit=${env.COMMIT_SHA}
build=${env.BUILD_NUMBER}
config-server=${env.DOCKER_NAMESPACE}/${env.CONFIG_SERVER_IMAGE}:${env.COMMIT_TAG}
eureka=${env.DOCKER_NAMESPACE}/${env.EUREKA_IMAGE}:${env.COMMIT_TAG}
"""
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
                    script {
                        env.DOCKER_LOGIN_ATTEMPTED = 'true'
                    }
                    runOnAgent(
                        'set +x\nprintf \'%s\' "$DOCKER_TOKEN" | docker login --username "$DOCKER_USERNAME" --password-stdin',
                        '@echo off\necho %DOCKER_TOKEN%| docker login --username %DOCKER_USERNAME% --password-stdin'
                    )
                    runOnAgent(
                        'docker push "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:$COMMIT_TAG"',
                        'docker push %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG%'
                    )
                    runOnAgent(
                        'docker push "$DOCKER_NAMESPACE/$EUREKA_IMAGE:$COMMIT_TAG"',
                        'docker push %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG%'
                    )
                    runOnAgent(
                        'docker push "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:build-$BUILD_NUMBER"',
                        'docker push %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:build-%BUILD_NUMBER%'
                    )
                    runOnAgent(
                        'docker push "$DOCKER_NAMESPACE/$EUREKA_IMAGE:build-$BUILD_NUMBER"',
                        'docker push %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:build-%BUILD_NUMBER%'
                    )
                    script {
                        if (params.PUBLISH_LATEST) {
                            runOnAgent(
                                'docker tag "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:$COMMIT_TAG" "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:latest"',
                                'docker tag %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:%COMMIT_TAG% %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:latest'
                            )
                            runOnAgent(
                                'docker tag "$DOCKER_NAMESPACE/$EUREKA_IMAGE:$COMMIT_TAG" "$DOCKER_NAMESPACE/$EUREKA_IMAGE:latest"',
                                'docker tag %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:%COMMIT_TAG% %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:latest'
                            )
                            runOnAgent(
                                'docker push "$DOCKER_NAMESPACE/$CONFIG_SERVER_IMAGE:latest"',
                                'docker push %DOCKER_NAMESPACE%/%CONFIG_SERVER_IMAGE%:latest'
                            )
                            runOnAgent(
                                'docker push "$DOCKER_NAMESPACE/$EUREKA_IMAGE:latest"',
                                'docker push %DOCKER_NAMESPACE%/%EUREKA_IMAGE%:latest'
                            )
                        }
                    }
                    runOnAgent('docker logout', 'docker logout')
                    script {
                        env.DOCKER_LOGIN_ATTEMPTED = 'false'
                    }
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

        stage('Validate Kubernetes access') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                script {
                    def agentIdentity = runOnAgent('whoami', '@echo off\nwhoami', [returnStdout: true]).trim()
                    echo "Agent identity executing deployment: ${agentIdentity}"
                    if (isUnix()) {
                        env.KUBERNETES_WORKSPACE = env.WORKSPACE
                        echo 'Using native kubectl with the Jenkins-managed kubeconfig; no Jenkins-owned Minikube profile is required.'
                    } else {
                        echo 'Using the default WSL distribution shown by `wsl.exe --list --verbose`.'
                        bat 'echo Windows profile: %USERPROFILE%'
                        bat 'wsl.exe --list --verbose'
                        def wslLaunchStatus = bat(
                            returnStatus: true,
                            script: 'wsl.exe --exec /bin/true'
                        )
                        if (wslLaunchStatus != 0) {
                            error("WSL could not launch the default distribution for Windows identity ${agentIdentity}. Ensure the Jenkins service user's default WSL distribution is Ubuntu and that this account can launch it interactively.")
                        }
                        env.KUBERNETES_WORKSPACE = bat(
                            returnStdout: true,
                            script: '@echo off\nwsl.exe --exec wslpath -a "%WORKSPACE%"'
                        ).trim()
                    }
                    runKubectl(
                        '--context "$KUBERNETES_CONTEXT" --request-timeout=30s get nodes -o wide',
                        '--context "%KUBERNETES_CONTEXT%" --request-timeout=30s get nodes -o wide'
                    )
                }
            }
        }

        stage('Validate Kubernetes manifests') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                script {
                    [
                        [directory: 'ecomConfigServer', image: env.CONFIG_SERVER_IMAGE],
                        [directory: 'ecomEureka', image: env.EUREKA_IMAGE]
                    ].each { service ->
                        def manifest = readFile("${service.directory}/kubernetes.yaml")
                        def sourceImage = "image: ${env.DOCKER_NAMESPACE}/${service.image}:latest"
                        if (!manifest.contains(sourceImage)) {
                            error("Expected image reference ${sourceImage} in ${service.directory}/kubernetes.yaml")
                        }
                        writeFile file: "${service.directory}/kubernetes-deploy.yaml",
                            text: manifest.replace(sourceImage, "image: ${env.DOCKER_NAMESPACE}/${service.image}:${env.COMMIT_TAG}")
                    }
                }
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" apply --namespace "$KUBERNETES_NAMESPACE" --dry-run=client -f "$KUBERNETES_WORKSPACE/ecomConfigServer/kubernetes-deploy.yaml"',
                    '--context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" --dry-run=client -f "%KUBERNETES_WORKSPACE%/ecomConfigServer/kubernetes-deploy.yaml"'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" apply --namespace "$KUBERNETES_NAMESPACE" --dry-run=client -f "$KUBERNETES_WORKSPACE/ecomEureka/kubernetes-deploy.yaml"',
                    '--context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" --dry-run=client -f "%KUBERNETES_WORKSPACE%/ecomEureka/kubernetes-deploy.yaml"'
                )
            }
        }

        stage('Deploy to local Kubernetes') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" apply --namespace "$KUBERNETES_NAMESPACE" -f "$KUBERNETES_WORKSPACE/ecomConfigServer/kubernetes-deploy.yaml"',
                    '--context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" -f "%KUBERNETES_WORKSPACE%/ecomConfigServer/kubernetes-deploy.yaml"'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" apply --namespace "$KUBERNETES_NAMESPACE" -f "$KUBERNETES_WORKSPACE/ecomEureka/kubernetes-deploy.yaml"',
                    '--context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" -f "%KUBERNETES_WORKSPACE%/ecomEureka/kubernetes-deploy.yaml"'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n "$KUBERNETES_NAMESPACE" rollout status deployment/ecom-config-server --timeout=600s',
                    '--context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" rollout status deployment/ecom-config-server --timeout=600s'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n "$KUBERNETES_NAMESPACE" rollout status deployment/ecom-eureka --timeout=600s',
                    '--context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" rollout status deployment/ecom-eureka --timeout=600s'
                )
            }
        }

        stage('Deployment verification') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n "$KUBERNETES_NAMESPACE" get deployments,pods,services -l app=ecom-config-server -o wide',
                    '--context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" get deployments,pods,services -l app=ecom-config-server -o wide'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n "$KUBERNETES_NAMESPACE" get deployments,pods,services -l app=ecom-eureka -o wide',
                    '--context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" get deployments,pods,services -l app=ecom-eureka -o wide'
                )
            }
        }
    }

    post {
        always {
            script {
                if (env.DOCKER_LOGIN_ATTEMPTED == 'true') {
                    def logoutStatus = runOnAgent('docker logout', 'docker logout', [returnStatus: true])
                    if (logoutStatus != 0) {
                        echo "WARNING: Docker logout failed with exit code ${logoutStatus}; remove the registry credentials from this agent manually."
                    }
                }
            }
        }
        failure {
            echo 'Pipeline failed. Review the stage logs before retrying or rolling back the previous immutable image tag.'
        }
    }
}
