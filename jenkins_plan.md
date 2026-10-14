## Plan: Jenkins Microservices Build And Deployment

Build a Jenkins pipeline for the repository that triggers from Git commits, exposes clear CI/CD phases, builds the services, rebuilds Docker images from each service Dockerfile, pushes versioned images to Docker Hub, and pauses for manual approval before deploying Kubernetes manifests to the Docker Desktop local cluster.

**Steps**
1. **Jenkins and webhook foundation**: Run Jenkins on the same Windows host as Docker Desktop, or use an agent with access to the Docker Desktop engine and Kubernetes API. Install/configure Pipeline, Git, Docker Pipeline, Credentials Binding, and Kubernetes CLI support. Add the repository webhook to trigger the Jenkins job on push events. Configure a multibranch Pipeline or a Pipeline job pointing to the root `Jenkinsfile`.
2. **Credentials and agent validation**: Add Docker Hub username plus access token as a Jenkins username/password credential; bind it only during image push. Provide Git credentials only if private repositories require them. Make `docker`, `kubectl`, Java/Maven, and Node/npm available on the agent. Verify the agent can run `docker info`, `kubectl config current-context`, `kubectl cluster-info`, and `kubectl auth can-i apply` against the `ecom-multi-node-cluster` context. Do not commit kubeconfig files, tokens, or application secrets.
3. **Pipeline source and parameters**: Create a root `Jenkinsfile`. Define parameters for the deployment environment, selected service scope, and whether to publish `latest`. Use a Git commit SHA plus Jenkins build number as the immutable image tag; retain `latest` only as an optional convenience tag. Keep the Docker Hub namespace in a Jenkins/job parameter or approved constant rather than embedding credentials.
4. **Preparation phase**: Checkout the commit that triggered the job, print the commit and branch, validate required tools, identify changed services, and fail early when a changed service has no supported build definition. Archive the generated test reports and retain the commit SHA as the build identity.
5. **Build and test phases**: Build Maven services using the repository's Maven wrapper or the configured Maven tool, with JDK 17/21 compatibility handled explicitly per module. Build the Node-based checkout service with its package scripts. Run unit tests and publish JUnit/Surefire results. Start with a controlled service allowlist, because the repository contains many modules and not every directory currently has a Kubernetes manifest.
6. **Docker image phase**: For each selected buildable service with a Dockerfile, run Docker build from that service directory after its artifact exists. Use stable image names matching the existing convention, such as `deepankarsaxena/ecomeureka` and `deepankarsaxena/ecomconfigserver`. Tag each image with the commit SHA and build number; optionally add `latest` only on the chosen branch. Generate a build manifest recording service, image, tag, commit, and Dockerfile path.
7. **Image verification and push phase**: Run local image inspection and an image vulnerability check if Trivy is available. Authenticate with Docker Hub using Jenkins credentials, push immutable tags first, then optional `latest` tags, and log only image names and digests. Verify the pushed digest or registry tag before allowing deployment.
8. **Manual approval gate**: Add an `input` step after successful pushes and before any Kubernetes command. Display the commit, image tags/digests, target context, namespace, and manifests that will be applied. Approval continues; rejection or timeout marks the build appropriately and performs no deployment.
9. **Manifest validation and deployment phase**: Validate Kubernetes YAML with `kubectl --dry-run=client`, verify that image references resolve to the just-pushed tag, select the `ecom-multi-node-cluster` context explicitly, and apply manifests in dependency order: Config Server, Eureka, then application services. Initially deploy only the existing manifests `ecomEureka/kubernetes.yaml` and `ecomConfigServer/kubernetes.yaml`; add the remaining services only after their manifests, secrets, persistence, probes, and service dependencies are defined.
10. **Post-deployment verification and rollback**: Wait for deployment rollouts, check pod readiness and events, verify service endpoints, and archive `kubectl get`/describe output. On failure, stop the pipeline and provide a rollback command or run `kubectl rollout undo` for deployments changed by the build. Keep previous immutable image tags available for rollback; do not rely exclusively on `latest`.
11. **Documentation and hardening**: Document Jenkins setup, webhook URL, credentials IDs, Docker Desktop prerequisites, kubeconfig access, required local cluster context, approval behavior, image naming, and rollback. Add resource limits, readiness/liveness probes, Kubernetes Secrets, and imagePullSecrets where needed before expanding deployment beyond the two existing manifests.

**Relevant files**
- `Jenkinsfile` - create the declarative pipeline and stage boundaries.
- `pom.xml` - root Maven module/build definition to reuse or narrow safely.
- `run.bat` - existing Windows build conventions; do not make Jenkins depend on a developer-specific Java path.
- `ecomEureka/kubernetes.yaml` - existing Eureka deployment and image reference to parameterize.
- `ecomConfigServer/kubernetes.yaml` - existing Config Server deployment and image reference to parameterize.
- `ecomEureka/Dockerfile` and `ecomEureka/eureka/Dockerfile` - determine which Dockerfile is the supported build context.
- `ecomConfigServer/Dockerfile` - Config Server image build input.
- `ecomConfigServer/src/main/resources/application.properties` - external config repository and runtime dependency assumptions.
- Service-specific `pom.xml`, `package.json`, `Dockerfile`, and `kubernetes.yaml` files - add to the service catalog as each service becomes deployable.
- `.gitignore` - ensure generated logs, build output, kubeconfig, and local Jenkins artifacts remain untracked.

**Verification**
1. Push a harmless commit to a test branch and confirm the webhook creates exactly one Jenkins build with the expected commit SHA.
2. Confirm preparation fails clearly when Docker, Maven/Java, or kubectl is unavailable.
3. Run a two-service pipeline for Eureka and Config Server; verify Maven tests, Docker tags, Docker Hub pushes, and recorded image digests.
4. Confirm the pipeline pauses after pushing images and before any `kubectl apply`; reject once and verify no deployment occurs.
5. Approve a run and verify the explicit `ecom-multi-node-cluster` context, dry-run validation, rollout completion, pod readiness, and service connectivity.
6. Force a deployment failure and verify diagnostics plus rollback behavior using the prior immutable image tag.
7. Test a second commit and confirm changed-image rebuild behavior, reproducible tags, and no secret values in Jenkins console output.

**Decisions**
- Start with Eureka and Config Server because they are the only services with existing Kubernetes manifests verified in the repository.
- Use immutable commit/build tags for deployment; treat `latest` as optional and never as the only rollback reference.
- Manual approval is required after Docker Hub push and before all Kubernetes access.
- Jenkins must have explicit access to Docker Desktop's Docker engine and kubeconfig; a containerized Jenkins without those mounts cannot deploy to the local cluster.
- Database and observability containers are outside the first deployable pipeline slice until manifests, persistence, secrets, and dependencies are defined.

**Local Ubuntu agent setup**
- The pipeline selects the `docker-desktop` agent label rather than running on the Jenkins controller. On Ubuntu, use an inbound WebSocket agent running as the user who owns Docker Desktop, with one executor and this label.
- The local agent is named `ecom-desktop-agent`. Its user service is `jenkins-ecom-agent.service`; inspect it with `systemctl --user status jenkins-ecom-agent.service` and its logs with `journalctl --user -u jenkins-ecom-agent.service`.
- The service uses `DOCKER_HOST=unix:///home/deepankar/.docker/desktop/docker.sock` and includes `/usr/local/bin`, `/usr/bin`, `/bin`, and `/snap/bin` in `PATH`. Adapt the socket and agent paths for another Ubuntu user.
- The agent JAR and owner-only agent secret are stored outside the repository in `~/.local/share/jenkins-agent`. Never commit the connection secret.
- The user service starts with the user's systemd session; with lingering disabled, do not assume it runs before login or after logout. Docker Desktop must also be running.
- Maven validation and both service builds use the existing wrapper in `ecomEureka/eureka`, pinned to Maven 3.8.7. Config Server is built with `-f ../../ecomConfigServer/pom.xml` from that directory; no system Maven installation is required.
- Keep the Jenkins credentials `docker-hub-credentials` (username/token) and `kubernetes-kubeconfig` (file) configured. Deployment still requires an explicit approval.
- After publishing a `Jenkinsfile` change, use **Build Now** or **Build with Parameters** to load the updated pipeline from SCM. **Replay** reuses the selected build's pipeline script, even if its checkout stage fetches a newer commit, so it can repeat an already-fixed failure.
- After approval, the pipeline renders `kubernetes-deploy.yaml` beside each source manifest, replacing the expected `latest` image with the immutable commit/build tag. It validates and applies these rendered Deployment/Service manifests directly, avoiding a temporary `latest` rollout followed by a second image update. Source manifests are unchanged; an unexpected source image reference fails explicitly.
- Rollout verification waits up to 600 seconds per deployment to accommodate local cluster image pulls and rolling replacement. A timeout still fails the build; inspect pod events and container logs rather than assuming the application is healthy.

**Further Considerations**
1. Choose whether Jenkins runs directly on Windows or in Docker. Direct Windows Jenkins is simpler for Docker Desktop Kubernetes access; containerized Jenkins requires Docker socket/CLI and kubeconfig/API certificate access.
2. Decide whether every commit publishes images or only commits to an integration branch. Recommended: build/test every commit, push and offer deployment approval only for the integration branch.
3. Decide whether to create per-service manifests or introduce Kustomize overlays. Recommended: Kustomize once more than the two existing manifests need the same image-tag substitution and environment configuration.
