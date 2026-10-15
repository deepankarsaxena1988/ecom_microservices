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

def deploymentServices() {
    return [
        [directory: 'ecomConfigServer', image: 'ecomconfigserver', deployment: 'ecom-config-server'],
        [directory: 'ecomEureka', image: 'ecomeureka', deployment: 'ecom-eureka'],
        [directory: 'ecomProductList', image: 'ecomproductlist', deployment: 'ecom-product-list'],
        [directory: 'ecomAPIGateWay', image: 'ecomapigateway', deployment: 'ecom-api-gateway']
    ]
}

def runKubectl(String unixArguments, String windowsArguments) {
    return withCredentials([file(
        credentialsId: 'kubernetes-kubeconfig',
        variable: 'KUBECONFIG_FILE'
    )]) {
        if (isUnix()) {
            def serverArgument = env.KUBERNETES_API_SERVER ? '--server "$KUBERNETES_API_SERVER" ' : ''
            return runOnAgent(
                'kubectl --kubeconfig "$KUBECONFIG_FILE" ' + serverArgument + unixArguments,
                ''
            )
        }
        def wslKubeconfig = bat(
            returnStdout: true,
            script: '@echo off\nwsl.exe --exec wslpath -a "%KUBECONFIG_FILE%"'
        ).trim()
        return withEnv(["WSL_KUBECONFIG_FILE=${wslKubeconfig}"]) {
            def serverArgument = env.KUBERNETES_API_SERVER ? '--server "%KUBERNETES_API_SERVER%" ' : ''
            bat(script: 'wsl.exe --exec /usr/local/bin/kubectl --kubeconfig "%WSL_KUBECONFIG_FILE%" ' + serverArgument + windowsArguments)
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
                runMaven('-B -f ../../ecomProductList/pom.xml clean verify')
                runMaven('-B -f ../../ecomAPIGateWay/pom.xml clean verify')
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build Docker images') {
            steps {
                script {
                    def imageRecord = "commit=${env.COMMIT_SHA}\nbuild=${env.BUILD_NUMBER}\n"
                    deploymentServices().each { service ->
                        withEnv(["SERVICE_IMAGE=${service.image}", "SERVICE_DIRECTORY=${service.directory}"]) {
                            runOnAgent(
                                'docker build --pull -t "$DOCKER_NAMESPACE/$SERVICE_IMAGE:$COMMIT_TAG" -t "$DOCKER_NAMESPACE/$SERVICE_IMAGE:build-$BUILD_NUMBER" "$SERVICE_DIRECTORY"',
                                'docker build --pull -t %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:%COMMIT_TAG% -t %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:build-%BUILD_NUMBER% "%SERVICE_DIRECTORY%"'
                            )
                            runOnAgent(
                                'docker image inspect "$DOCKER_NAMESPACE/$SERVICE_IMAGE:$COMMIT_TAG"',
                                'docker image inspect %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:%COMMIT_TAG%'
                            )
                        }
                        imageRecord += "${service.deployment}=${env.DOCKER_NAMESPACE}/${service.image}:${env.COMMIT_TAG}\n"
                    }
                    writeFile file: 'image-build.txt', text: imageRecord
                }
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
                    script {
                        deploymentServices().each { service ->
                            withEnv(["SERVICE_IMAGE=${service.image}"]) {
                                runOnAgent(
                                    'docker push "$DOCKER_NAMESPACE/$SERVICE_IMAGE:$COMMIT_TAG"',
                                    'docker push %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:%COMMIT_TAG%'
                                )
                                runOnAgent(
                                    'docker push "$DOCKER_NAMESPACE/$SERVICE_IMAGE:build-$BUILD_NUMBER"',
                                    'docker push %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:build-%BUILD_NUMBER%'
                                )
                                if (params.PUBLISH_LATEST) {
                                    runOnAgent(
                                        'docker tag "$DOCKER_NAMESPACE/$SERVICE_IMAGE:$COMMIT_TAG" "$DOCKER_NAMESPACE/$SERVICE_IMAGE:latest"',
                                        'docker tag %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:%COMMIT_TAG% %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:latest'
                                    )
                                    runOnAgent(
                                        'docker push "$DOCKER_NAMESPACE/$SERVICE_IMAGE:latest"',
                                        'docker push %DOCKER_NAMESPACE%/%SERVICE_IMAGE%:latest'
                                    )
                                }
                            }
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
                    if (params.KUBERNETES_NAMESPACE != 'default') {
                        error('These application manifests and dependency DNS names require the default namespace.')
                    }
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
                    env.KUBERNETES_API_SERVER = ''
                    if (params.KUBERNETES_CONTEXT == 'ecom-multi-node-cluster') {
                        def apiBinding = runOnAgent(
                            'docker port ecom-multi-node-cluster 8443/tcp',
                            '@echo off\ndocker port ecom-multi-node-cluster 8443/tcp',
                            [returnStdout: true]
                        ).trim()
                        if (!(apiBinding ==~ /127\.0\.0\.1:[0-9]+/)) {
                            error("Expected one loopback API port for ecom-multi-node-cluster, got: ${apiBinding}. Check the Minikube profile and Docker context on this agent.")
                        }
                        env.KUBERNETES_API_SERVER = "https://${apiBinding}"
                        echo "Using current Minikube API endpoint ${env.KUBERNETES_API_SERVER}; authentication and TLS trust still come from the Jenkins-managed kubeconfig."
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
                    deploymentServices().each { service ->
                        def manifest = readFile("${service.directory}/kubernetes.yaml")
                        def sourceImage = "image: ${env.DOCKER_NAMESPACE}/${service.image}:latest"
                        if (!manifest.contains(sourceImage)) {
                            error("Expected image reference ${sourceImage} in ${service.directory}/kubernetes.yaml")
                        }
                        writeFile file: "${service.directory}/kubernetes-deploy.yaml",
                            text: manifest.replace(sourceImage, "image: ${env.DOCKER_NAMESPACE}/${service.image}:${env.COMMIT_TAG}")
                        withEnv(["SERVICE_DIRECTORY=${service.directory}"]) {
                            runKubectl(
                                '--context "$KUBERNETES_CONTEXT" apply --namespace "$KUBERNETES_NAMESPACE" --dry-run=client -f "$KUBERNETES_WORKSPACE/$SERVICE_DIRECTORY/kubernetes-deploy.yaml"',
                                '--context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" --dry-run=client -f "%KUBERNETES_WORKSPACE%/%SERVICE_DIRECTORY%/kubernetes-deploy.yaml"'
                            )
                        }
                    }
                }
            }
        }

        stage('Validate application dependencies') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n default describe secret ecom-product-list-db-secret',
                    '--context "%KUBERNETES_CONTEXT%" -n default describe secret ecom-product-list-db-secret'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n ecom-product-db-ns wait --for=jsonpath=\'{.status.cluster.status}\'=ONLINE innodbcluster/ecom-product-db --timeout=120s',
                    '--context "%KUBERNETES_CONTEXT%" -n ecom-product-db-ns wait --for=jsonpath={.status.cluster.status}=ONLINE innodbcluster/ecom-product-db --timeout=120s'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n ecom-product-db-ns rollout status deployment/ecom-product-db-router --timeout=120s',
                    '--context "%KUBERNETES_CONTEXT%" -n ecom-product-db-ns rollout status deployment/ecom-product-db-router --timeout=120s'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n default rollout status deployment/ecom-redis --timeout=120s',
                    '--context "%KUBERNETES_CONTEXT%" -n default rollout status deployment/ecom-redis --timeout=120s'
                )
                runKubectl(
                    '--context "$KUBERNETES_CONTEXT" -n default get service ecom-redis',
                    '--context "%KUBERNETES_CONTEXT%" -n default get service ecom-redis'
                )
            }
        }

        stage('Deploy to local Kubernetes') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                script {
                    deploymentServices().each { service ->
                        withEnv(["SERVICE_DIRECTORY=${service.directory}", "SERVICE_DEPLOYMENT=${service.deployment}"]) {
                            runKubectl(
                                '--context "$KUBERNETES_CONTEXT" apply --namespace "$KUBERNETES_NAMESPACE" -f "$KUBERNETES_WORKSPACE/$SERVICE_DIRECTORY/kubernetes-deploy.yaml"',
                                '--context "%KUBERNETES_CONTEXT%" apply --namespace "%KUBERNETES_NAMESPACE%" -f "%KUBERNETES_WORKSPACE%/%SERVICE_DIRECTORY%/kubernetes-deploy.yaml"'
                            )
                            runKubectl(
                                '--context "$KUBERNETES_CONTEXT" -n "$KUBERNETES_NAMESPACE" rollout status deployment/"$SERVICE_DEPLOYMENT" --timeout=600s',
                                '--context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" rollout status deployment/%SERVICE_DEPLOYMENT% --timeout=600s'
                            )
                        }
                    }
                }
            }
        }

        stage('Deployment verification') {
            when {
                expression { env.DEPLOY_TO_LOCAL_KUBERNETES == 'true' }
            }
            steps {
                script {
                    deploymentServices().each { service ->
                        withEnv(["SERVICE_DEPLOYMENT=${service.deployment}"]) {
                            runKubectl(
                                '--context "$KUBERNETES_CONTEXT" -n "$KUBERNETES_NAMESPACE" get deployments,pods,services -l app="$SERVICE_DEPLOYMENT" -o wide',
                                '--context "%KUBERNETES_CONTEXT%" -n "%KUBERNETES_NAMESPACE%" get deployments,pods,services -l app=%SERVICE_DEPLOYMENT% -o wide'
                            )
                        }
                    }
                }
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
