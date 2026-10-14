# Single-pod Redis Cluster

This development deployment enables Redis Cluster on one pod and assigns all
16,384 hash slots to it. It has no replicas, sharding across pods, or failover.
Do not scale this Deployment: additional pods would be independent clusters.

Deploy the ConfigMap, Deployment, and internal Service:

```bash
kubectl --context ecom-multi-node-cluster -n default apply -f ecomRedis/kubernetes.yaml
kubectl --context ecom-multi-node-cluster -n default rollout status deployment/ecom-redis --timeout=180s
kubectl --context ecom-multi-node-cluster -n default exec deployment/ecom-redis -- redis-cli cluster info
```

The startup hook assigns slots only when none are assigned. Readiness requires
`cluster_state:ok` and all slots assigned. The generated topology file is stored
in writable `/data/nodes.conf`, not in the ConfigMap.

RDB and AOF persistence are disabled. Pod replacement loses cluster topology and
rate-limit counters; the new pod initializes a new single-node cluster.
After editing the ConfigMap, apply it and restart the Deployment to load changes:

```bash
kubectl --context ecom-multi-node-cluster -n default rollout restart deployment/ecom-redis
```

For a Spring Boot gateway in the same namespace, use cluster configuration instead
of standalone `spring.data.redis.host` and `spring.data.redis.port` settings:

```properties
spring.data.redis.cluster.nodes=ecom-redis:6379
```

Redis advertises its pod address during topology discovery. Clients must be able
to reach pod IPs; forwarding only the Service port does not provide general
external cluster access. Port 16379 is the cluster bus port, not a client endpoint.
Multi-key operations and Lua scripts require keys in the same hash slot.

There is no authentication or TLS in this development configuration. ClusterIP
does not restrict access from other pods; use authentication and NetworkPolicies
before deploying in a shared or production environment.
