# Togglr Backend Helm Chart

This Helm chart deploys the Togglr Feature Toggle Management System Backend on Kubernetes.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- PostgreSQL database (self-hosted)
- Redis (optional, for distributed caching)

## Installing the Chart

```bash
# Add the chart repository (if published)
helm repo add togglr https://charts.togglr.com
helm repo update

# Install with default values
helm install togglr-backend togglr/togglr-backend

# Install with custom values
helm install togglr-backend togglr/togglr-backend -f values.yaml
```

## Configuration

The following table lists the configurable parameters and their default values.

### Application Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `config.server.port` | Server port | `8080` |
| `config.database.url` | Database URL | `jdbc:postgresql://postgres:5432/togglr` |
| `config.database.username` | Database username | `togglr` |
| `config.database.password` | Database password | `password` |
| `config.database.existingSecret` | Use existing secret for DB credentials (optional) | `""` |
| `config.database.usernameKey` | Username key in existing secret | `username` |
| `config.database.passwordKey` | Password key in existing secret | `password` |
| `config.cache.type` | Cache type (caffeine/redis) | `caffeine` |
| `config.cache.redis.host` | Redis host | `redis-master` |
| `config.cache.redis.port` | Redis port | `6379` |
| `config.cache.redis.password` | Redis password | `""` |
| `config.cache.redis.existingSecret` | Use existing secret for Redis (optional) | `""` |
| `config.cache.redis.passwordKey` | Password key in existing secret | `password` |
| `config.jwt.secret` | JWT secret key | `your-jwt-secret-key-change-in-production` |
| `config.jwt.expiration` | JWT expiration time (ms) | `86400000` |
| `config.jwt.existingSecret` | Use existing secret for JWT (optional) | `""` |
| `config.jwt.secretKey` | JWT secret key in existing secret | `jwt-secret` |
| `config.logging.level` | Application log level | `INFO` |

### Deployment Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Image repository | `togglr/backend` |
| `image.tag` | Image tag | `1.0.0` |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |
| `resources.limits.cpu` | CPU limit | `1000m` |
| `resources.limits.memory` | Memory limit | `1Gi` |
| `resources.requests.cpu` | CPU request | `500m` |
| `resources.requests.memory` | Memory request | `512Mi` |

### Service Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `service.type` | Service type | `ClusterIP` |
| `service.port` | Service port | `8080` |

### Ingress Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `ingress.enabled` | Enable ingress | `false` |
| `ingress.className` | Ingress class name | `""` |
| `ingress.hosts[0].host` | Hostname | `togglr-backend.local` |

### Autoscaling Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `autoscaling.enabled` | Enable HPA | `false` |
| `autoscaling.minReplicas` | Minimum replicas | `1` |
| `autoscaling.maxReplicas` | Maximum replicas | `10` |
| `autoscaling.targetCPUUtilizationPercentage` | CPU target | `80` |

## Examples

### Basic Installation with External Database

```yaml
# values-production.yaml
config:
  database:
    url: "jdbc:postgresql://my-postgres.example.com:5432/togglr"
    username: "togglr_user"
    existingSecret: "togglr-db-secret"
    usernameKey: "username"
    passwordKey: "password"
  
  jwt:
    existingSecret: "togglr-jwt-secret"
    secretKey: "jwt-secret"

ingress:
  enabled: true
  className: "nginx"
  hosts:
    - host: togglr-api.example.com
      paths:
        - path: /
          pathType: Prefix

resources:
  limits:
    cpu: 2000m
    memory: 2Gi
  requests:
    cpu: 1000m
    memory: 1Gi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
```

### Mixed Secrets Configuration

```yaml
# values-mixed-secrets.yaml
config:
  database:
    url: "jdbc:postgresql://postgres.example.com:5432/togglr"
    username: "togglr"
    password: "my-db-password"  # Will create secret
  
  jwt:
    existingSecret: "external-jwt-secret"  # Use existing secret
    secretKey: "jwt-key"
  
  cache:
    type: "redis"
    redis:
      host: "redis.example.com"
      port: 6379
      password: "redis-password"  # Will create secret
```

### Installation with Redis Cache

```yaml
# values-redis.yaml
config:
  cache:
    type: "redis"
    redis:
      host: "redis-cluster.example.com"
      port: 6379
      existingSecret: "redis-secret"
      passwordKey: "password"
```

## Secret Management

The chart provides flexible secret management:

### Automatic Secret Creation
If no `existingSecret` is specified, the chart creates secrets automatically:
```yaml
config:
  database:
    password: "my-password"  # Creates secret with db-password key
  jwt:
    secret: "jwt-key"       # Creates secret with jwt-secret key
```

### Using Existing Secrets
Reference existing Kubernetes secrets:
```yaml
config:
  database:
    existingSecret: "my-db-secret"
    passwordKey: "password"
  jwt:
    existingSecret: "my-jwt-secret"
    secretKey: "jwt-key"
```

### Mixed Configuration
Combine both approaches as needed:
```yaml
config:
  database:
    existingSecret: "external-db-secret"  # Use existing
  jwt:
    secret: "generated-jwt-key"          # Create new
```

## Security Considerations

1. **Change default passwords**: Always change default passwords in production
2. **Use existing secrets**: Reference existing Kubernetes secrets for sensitive data
3. **Enable TLS**: Configure ingress with TLS certificates
4. **Resource limits**: Set appropriate resource limits
5. **Security context**: The chart runs with non-root user by default

## Monitoring

The application exposes health check endpoints:

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Metrics: `/actuator/metrics`

## Uninstalling

```bash
helm uninstall togglr-backend
```

## Contributing

1. Make changes to the chart
2. Update version in `Chart.yaml`
3. Test with `helm lint ./helm`
4. Package with `helm package ./helm`