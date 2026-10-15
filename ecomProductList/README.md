# Product List on Minikube

[kubernetes.yaml](./kubernetes.yaml) creates one application replica and a
ClusterIP Service in the `default` namespace. The Kubernetes context is selected
by `kubectl`, not embedded in the manifest.

## Prerequisites

- The `ecom-multi-node-cluster` Minikube profile must be running.
- Config Server (`ecom-config-server:8888`) must serve the `ecomProductList/dev`
  configuration, and Eureka must be available at `ecom-eureka:5000`.
- The Product InnoDB cluster and its router must be ONLINE. The JDBC connection
  uses `ecom-product-db.ecom-product-db-ns.svc.cluster.local:3306`, which routes
  to the writable primary.
- Import [ecomProductList.sql](../ecomProductListDB/ecomProductList.sql) if the
  `ecomProdDB` schema has not been initialized. Provision a database account
  with the privileges required by the application and its configured Hibernate
  schema-management mode. Creating the Kubernetes Secret does not create a
  MySQL account.
- Use JDK 21 to build the application. The Docker runtime also uses Java 21,
  matching [pom.xml](./pom.xml).

## Build and load the image

Run from the repository root:

```bash
cd ecomProductList
sh ./mvnw clean package
cd ..
docker build -t deepankarsaxena/ecomproductlist:latest ecomProductList
minikube -p ecom-multi-node-cluster image load deepankarsaxena/ecomproductlist:latest
```

The manifest uses `IfNotPresent` so Minikube can use the locally loaded image
without publishing it. For subsequent releases, prefer a unique image tag and
update the manifest to match before applying it.

## Create the database Secret

Run these commands in Bash. Enter the credentials of an existing MySQL account;
the password is not echoed or placed in shell history. No credentials belong in
the manifest.

```bash
(
  set -e
  set +x
  umask 077
  secret_dir=$(mktemp -d)
  trap 'rm -f "$secret_dir/username" "$secret_dir/password"; rmdir "$secret_dir"' EXIT
  read -r -p "MySQL username: " db_user
  read -r -s -p "MySQL password: " db_password
  printf '\n'
  printf '%s' "$db_user" > "$secret_dir/username"
  printf '%s' "$db_password" > "$secret_dir/password"
  kubectl --context=ecom-multi-node-cluster -n default create secret generic \
    ecom-product-list-db-secret \
    --from-file=username="$secret_dir/username" \
    --from-file=password="$secret_dir/password"
)
```

The Secret must be in `default`, alongside the application. Kubernetes Secret
references cannot cross namespaces. If the Secret already exists, update it
using your normal credential-management process and restart the application
after rotating credentials.

## Deploy and verify

```bash
kubectl --context=ecom-multi-node-cluster apply -f ecomProductList/kubernetes.yaml
kubectl --context=ecom-multi-node-cluster -n default rollout status \
  deployment/ecom-product-list --timeout=300s
kubectl --context=ecom-multi-node-cluster -n default logs deployment/ecom-product-list
kubectl --context=ecom-multi-node-cluster -n default port-forward \
  service/ecom-product-list 9203:9203
```

In another terminal:

```bash
curl --fail http://127.0.0.1:9203/productList
```

Other applications in `default` can use `http://ecom-product-list:9203`.
Startup and readiness probes check the HTTP TCP port because the application
does not currently include Spring Boot Actuator. These probes do not verify
database queries; the API request above is the functional check.

The deployment overrides development-only host addresses and database
credentials from Config Server. Config Server is required at startup; other
application settings, including Hibernate schema management, still come from
the existing configuration. The JDBC TLS options preserve the existing local
development configuration and are not suitable for a production deployment.
