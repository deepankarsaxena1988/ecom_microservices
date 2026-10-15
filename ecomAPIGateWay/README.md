# API Gateway routing in Kubernetes

This document explains the routing choices discussed for the
`ecom-multi-node-cluster` Minikube cluster. The recommended approach is
**Kubernetes Service DNS routing** when all application services run in
Kubernetes.

Product List now uses Service DNS routing and a Redis-backed rate limiter.
Other explicit service routes are unchanged. The alternative Eureka examples
below explain the other routing option; they are not the Product List setup.
Creating these files does not deploy the gateway. See
[application.properties](./src/main/resources/application.properties) for the
current gateway configuration.

## Kubernetes names, labels, and selectors

The Product List [manifest](../ecomProductList/kubernetes.yaml) defines a
Deployment and a ClusterIP Service.

```yaml
kind: Service
metadata:
  name: ecom-product-list
  namespace: default
  labels:
    app: ecom-product-list
spec:
  selector:
    app: ecom-product-list
```

- `metadata.name` names the Service and determines its Kubernetes DNS name.
- `metadata.namespace` scopes the Service. This Service lives in `default`.
- `metadata.labels` tags the Service for searches, monitoring, and automation.
  `app` is a conventional label key, not a special routing keyword.
- `spec.selector` selects destination Pods in the Service's namespace by their
  labels. It does not select the Deployment by name.
- The Deployment's `spec.template.metadata.labels` labels its Pods. Its
  `spec.selector.matchLabels` must match those Pod template labels.
- Top-level Deployment labels are not automatically inherited by Pods.

Changing the Service's `metadata.labels.app` to `product-api-service` does not
change its DNS name or which Pods receive traffic. It can affect tools that
select Services by the old label. The gateway destination remains
`http://ecom-product-list:9203`.

Changing the Service's `spec.selector`, by contrast, changes which Pods are
selected. A selector that matches no Pods leaves the Service without usable
Pod endpoints.

## Option 1: Gateway calls a Kubernetes Service (recommended)

```text
External client
  -> Ingress or exposed Gateway Service
  -> API Gateway Pod
  -> Product List ClusterIP Service
  -> ready Product List Pod
```

The client reaches the gateway, not the Product List Pod directly. The gateway
uses Kubernetes DNS to resolve the Product List Service:

- Same namespace: `http://ecom-product-list:9203`
- Different namespace: `http://ecom-product-list.default.svc.cluster.local:9203`

The qualified name assumes the standard `cluster.local` cluster domain.
ClusterIP is internal access; exposing the gateway to external clients requires
a separate mechanism such as Ingress, LoadBalancer, NodePort, or a local
development port-forward.

For the existing `/ecom/**` route, use:

```properties
spring.cloud.gateway.routes[0].id=product-list
spring.cloud.gateway.routes[0].uri=http://ecom-product-list:9203
spring.cloud.gateway.routes[0].predicates[0]=Path=/ecom/**
```

`id` identifies the route and is arbitrary. The `uri` is the destination.
Without a path-rewriting filter, `/ecom/productList` is forwarded unchanged,
matching the Product List controller.

If all routes use explicit Kubernetes Service destinations, disable automatic
discovery routes:

```properties
spring.cloud.gateway.discovery.locator.enabled=false
```

This does not by itself disable Eureka registration or registry fetching.
Eureka can remain during a gradual migration. If the gateway no longer needs
Eureka at all, its Eureka client can be disabled separately:

```properties
eureka.client.enabled=false
```

### Why prefer this approach here?

- Kubernetes already provides Service discovery and backend selection.
- Service endpoints track Pod changes and normally exclude unready Pods from
  new traffic.
- No separate Eureka registry and client registry caches are needed for routing.
- Gateway and internal service calls can use the same DNS-based approach.

Service load balancing is generally connection-based, not guaranteed
round-robin per HTTP request. HTTP keep-alive and gateway connection pooling
can produce uneven traffic across replicas. Readiness changes also do not
automatically terminate existing connections.

## Option 2: Gateway uses Eureka discovery

```text
Product List Pods -- registration and heartbeats --> Eureka
API Gateway Pod  -- periodic registry fetch ------> Eureka

External client
  -> Ingress or exposed Gateway Service
  -> API Gateway Pod
  -> registered Product List Pod selected by Spring Cloud LoadBalancer
```

Eureka is a registry, not a proxy for application requests. The gateway
periodically fetches and caches registrations; it does not normally query
Eureka for every request.

The Product List deployment uses:

```yaml
- name: SPRING_APPLICATION_NAME
  value: ecomProductList
- name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
  value: http://ecom-eureka:5000/eureka
- name: EUREKA_INSTANCE_PREFER_IP_ADDRESS
  value: "true"
```

With registration enabled, each replica advertises its own reachable address
and application port. The application normally appears in Eureka as
`ECOMPRODUCTLIST`; for example, two replicas might advertise
`10.244.0.25:9203` and `10.244.0.26:9203`. These are illustrative Pod IPs.

The gateway would use:

```properties
eureka.client.serviceUrl.defaultZone=http://ecom-eureka:5000/eureka
eureka.client.fetch-registry=true

spring.cloud.gateway.routes[0].id=product-list
spring.cloud.gateway.routes[0].uri=lb://ECOMPRODUCTLIST
spring.cloud.gateway.routes[0].predicates[0]=Path=/ecom/**
```

This requires the Eureka discovery client and Spring Cloud LoadBalancer
integration to be available at runtime. `ECOMPRODUCTLIST` is the registered
application ID, not a Kubernetes name or label.

With per-Pod registrations, the gateway normally connects directly to the
selected Product List Pod, bypassing the Product List ClusterIP Service.
Kubernetes networking and network policies must allow that connection.
The gateway still uses the `ecom-eureka` Kubernetes Service to reach Eureka.

Kubernetes readiness and Eureka instance status are separate mechanisms.
Marking a Pod unready does not automatically remove its Eureka registration;
registry updates and client cache refreshes can delay removal of stale
addresses.

### When Eureka may be preferable

- Services run across both Kubernetes and virtual machines.
- Existing client-side routing depends on Eureka instance metadata.
- A migration needs to retain a shared discovery system temporarily.

## Routing is not determined by whether the caller is external

Both the gateway and other microservices can use either approach:

| Caller destination | Discovery and backend selection |
|---|---|
| `http://ecom-product-list:9203` | Kubernetes DNS and Service networking |
| `lb://ECOMPRODUCTLIST` in a Gateway route | Eureka discovery and Spring Cloud LoadBalancer |

The external client only calls the exposed gateway address. It does not need
to know Product List Pod IPs, Kubernetes labels, or Eureka service IDs.

## Product List rate limiting and throttling

The `ECOMPRODUCTLIST` route matches `/ecom/**`, including
`/ecom/productList` and `/ecom/productDetail/{productId}`. It forwards to
`http://ecom-product-list.default.svc.cluster.local:9203` without rewriting
the path.

Its `RequestRateLimiter` filter uses Spring Cloud Gateway's atomic Redis
token-bucket script with this policy:

| Setting | Value |
|---|---|
| Key | `product-list:<direct-client-ip>`, without the source port |
| Refill rate | 10 tokens per second |
| Bucket capacity / initial burst | 20 tokens |
| Cost per request | 1 token |
| Exhausted bucket | HTTP 429; request is not forwarded |
| Redis unavailable / script failure | HTTP 503; request is not forwarded |
| Missing client IP | HTTP 400 |

This is rejection-based throttling, not a queue or concurrency limit. A full
bucket allows an immediate burst of 20; tokens then refill at 10 per second,
up to the capacity of 20. Unused tokens do not accumulate beyond that.

All Product List paths share a client's bucket. Different client IPs have
separate buckets, and gateway replicas share those buckets through Redis.
Responses from the limiter expose `X-RateLimit-Remaining`,
`X-RateLimit-Replenish-Rate`, `X-RateLimit-Burst-Capacity`, and
`X-RateLimit-Requested-Tokens`.

[FailClosedRedisRateLimiter](./src/main/java/ecom/apiGateway/FailClosedRedisRateLimiter.java)
reuses the built-in script but rejects its failure sentinel instead of using
the built-in fail-open behavior. Failures are logged explicitly. This depends
on the pinned Spring Cloud Gateway version's negative-token failure sentinel;
keep the Redis failure test when upgrading.

Checkout, webhook, and account routes do not have this filter. Automatic
discovery routes remain enabled for other applications, but the include
expression excludes `ecomProductList` case-insensitively to prevent an
unlimited, discovery-generated Product List route.

### Reusing the limiter for another service

The service-neutral beans are `scopedClientIpKeyResolver` and
`redisRateLimiter`, defined in
[RateLimitConfiguration](./src/main/java/ecom/apiGateway/RateLimitConfiguration.java).
The resolver prefixes each client IP with the route's `rate-limit-scope`
metadata. Without that metadata, the route ID is the scope.
Explicit scopes must contain only letters, digits, dots, underscores, or
hyphens, and cannot be empty.

For example, to enable Checkout later, add the following settings (they are
not enabled by this change):

```properties
spring.cloud.gateway.routes[1].metadata.rate-limit-scope=checkout
spring.cloud.gateway.routes[1].filters[0].name=RequestRateLimiter
spring.cloud.gateway.routes[1].filters[0].args.key-resolver=#{@scopedClientIpKeyResolver}
spring.cloud.gateway.routes[1].filters[0].args.rate-limiter=#{@redisRateLimiter}
spring.cloud.gateway.routes[1].filters[0].args.redis-rate-limiter.replenishRate=5
spring.cloud.gateway.routes[1].filters[0].args.redis-rate-limiter.burstCapacity=10
spring.cloud.gateway.routes[1].filters[0].args.redis-rate-limiter.requestedTokens=1
```

This gives `checkout:<ip>` and `product-list:<ip>` independent buckets even
when the same client calls both services. Multiple routes may deliberately
use the same scope to share a service-wide bucket; all routes sharing a scope
must use the same token policy. Route-specific policies override constructor
defaults, not each other's configuration.

Switching from the previous IP-only keys starts fresh scoped buckets. Old
unscoped keys expire automatically.

### Client IP behind an ingress or proxy

The resolver deliberately uses the TCP peer IP and does not trust client-supplied
`X-Forwarded-For` or `Forwarded` headers. If a proxy or Kubernetes NAT replaces
the source address, requests can share a proxy/node IP bucket. Clients behind
the same NAT can also share a limit.

For real end-user IP limits behind an ingress, configure a trusted-proxy
boundary and a corresponding trusted IP resolver before exposing it. Do not
blindly trust arbitrary forwarding headers or enable forwarded-header
transformation from untrusted clients. Port-forwarding is suitable for local
testing, but does not establish distinct real-client IPs.

### Redis connection

The defaults are `ecom-redis.default.svc.cluster.local:6379`, overridable with
`REDIS_HOST` and `REDIS_PORT`. Connect and command timeouts are two seconds.

The existing Redis is cluster-enabled but has one node owning all 16,384
slots. The gateway deliberately uses a standalone Redis connection, avoiding
topology discovery of advertised Pod addresses. The shipped Lua script uses
hash-tagged keys so each bucket's keys belong to the same Redis slot.
Do not configure `spring.data.redis.cluster.nodes` for this one-node setup.
This setup must be revisited if Redis is scaled into a multi-node cluster.

The existing Redis has no authentication or TLS, so keep it internal to the
development cluster. Its data is ephemeral; a Redis restart resets buckets,
and a Redis outage temporarily blocks Product List traffic with HTTP 503.

## Build, deploy, and test on Minikube

The gateway now uses a Java 21 runtime, matching its Maven Java target.
From the repository root, with JDK 21 installed:

```bash
cd ecomAPIGateWay
sh ./mvnw clean verify
cd ..
docker build -t deepankarsaxena/ecomapigateway:latest ecomAPIGateWay
minikube -p ecom-multi-node-cluster image load deepankarsaxena/ecomapigateway:latest
kubectl --context=ecom-multi-node-cluster apply -f ecomAPIGateWay/kubernetes.yaml
kubectl --context=ecom-multi-node-cluster -n default rollout status \
  deployment/ecom-api-gateway --timeout=300s
kubectl --context=ecom-multi-node-cluster -n default port-forward \
  service/ecom-api-gateway 4000:4000
```

The [gateway manifest](./kubernetes.yaml) creates an internal Service and one
gateway replica in `default`, and sets the Kubernetes Eureka address.
Startup/readiness probes only check the gateway's listening port; they do
not check downstream availability. Redis must be healthy with all slots
assigned, and the Product List Deployment, Service, and its dependencies
must already be ready. For image updates, prefer unique tags in both the
build command and manifest to avoid reusing cached `latest` images.

From another terminal:

```bash
curl -i http://127.0.0.1:4000/ecom/productList

# Parallel requests should produce HTTP 429 once the shared IP bucket is exhausted.
seq 1 60 | xargs -P 30 -I '{}' \
  curl -s -o /dev/null -w '%{http_code}\n' \
  http://127.0.0.1:4000/ecom/productList
```

The exact number of successes depends on execution duration and token refill;
this command is a smoke test, not a deterministic rate assertion.

The normal test suite checks HTTP forwarding, headers, HTTP 429, HTTP 503,
unchanged Checkout filtering, spoofed-IP headers, and discovery exclusion.
Optional real-Redis tests verify the exact 20-token burst, 10-token refill,
capacity cap, independent clients, and sharing across limiter instances.
Run them against a dedicated local single-node Redis cluster with all slots
assigned, not production Redis:

```bash
cd ecomAPIGateWay
REDIS_INTEGRATION_PORT=16380 sh ./mvnw test
```

Without that environment variable, real-Redis tests are skipped. Their keys
use random test IDs and are removed after each test.

## Remaining configuration caveats

The checked-in gateway configuration still contains the local-development
Eureka address and localhost destinations for services other than Product List:

```properties
eureka.client.serviceUrl.defaultZone=http://localhost:5000/eureka
spring.cloud.gateway.routes[1].uri=http://localhost:8085
```

Inside a gateway Pod, `localhost` refers to that Pod, not to Eureka or a
separate backend Pod. The gateway manifest overrides the Eureka address.
The other explicit localhost routes still need appropriate destinations before
their services can be reached in Kubernetes; they were intentionally not
migrated as part of the Product List change.

The current setting:

```properties
spring.cloud.gateway.discovery.locator.enabled=true
```

creates additional discovery-based routes. It does not convert explicit
`http://localhost:...` routes into Eureka load-balanced routes.

For this all-Kubernetes deployment, prefer explicit Service DNS destinations.
Keep Eureka only where it provides a concrete benefit; removing it from all
applications is not a prerequisite for changing gateway routing.
