# Togglr Backend

<div align="center">

![Togglr Logo](logo.png)

**Modern Feature Toggle Management System**

[![Build Status](https://github.com/gdrocha-io/togglr-backend/workflows/CI/badge.svg)](https://github.com/gdrocha-io/togglr-backend/actions)
[![Docker Pulls](https://img.shields.io/docker/pulls/gdrocha/togglr-backend)](https://hub.docker.com/r/gdrocha/togglr-backend)
[![Helm Chart](https://img.shields.io/badge/helm-chart-blue)](https://artifacthub.io/packages/helm/togglr/togglr-backend)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

</div>

## Overview

Togglr Backend is a powerful, enterprise-grade Feature Toggle Management System built with Java 17 and Spring Boot. It provides granular control over application features through namespaces and environments, enabling safe feature rollouts, A/B testing, and instant feature toggling without code deployments.

## ✨ Features

- **🚀 Feature Management**: Complete CRUD operations with instant enable/disable capabilities
- **🏢 Multi-tenancy**: Organize features by namespaces and environments
- **🔒 Security**: JWT authentication with role-based access control (USER, MANAGER, ADMIN)
- **📊 Audit Trail**: Complete tracking of all feature changes and access patterns
- **⚡ High Performance**: In-memory caching with Caffeine and optional Redis support
- **🐳 Cloud Native**: Docker-ready with Kubernetes Helm charts
- **📈 Monitoring**: Built-in health checks and metrics with Spring Boot Actuator
- **🔧 Flexible Metadata**: Support for complex JSON configurations

## 🚀 Quick Start

### Using Docker

```bash
# Run with Docker Compose
curl -O https://raw.githubusercontent.com/gdrocha-io/togglr-backend/main/docker-compose.yml
docker-compose up -d

# Or run directly
docker run -d \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://localhost:5432/togglr \
  -e DB_USERNAME=togglr \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=your-secret-key \
  gdrocha/togglr-backend:latest
```

### Using Kubernetes

```bash
# Add Helm repository
helm repo add togglr https://gdrocha-io.github.io/togglr-charts
helm repo update

# Install
helm install my-togglr togglr/togglr-backend
```

### Local Development

```bash
# Prerequisites: Java 17, PostgreSQL
git clone https://github.com/gdrocha-io/togglr-backend.git
cd togglr-backend

# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/togglr
export DB_USERNAME=togglr
export DB_PASSWORD=password
export JWT_SECRET=your-secret-key

# Run
./mvnw spring-boot:run
```

## 📋 Requirements

- **Java**: 17 or higher
- **Database**: PostgreSQL 12+
- **Memory**: 512MB minimum, 1GB recommended
- **Cache** (optional): Redis 6+

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection URL | Required |
| `DB_USERNAME` | Database username | Required |
| `DB_PASSWORD` | Database password | Required |
| `JWT_SECRET` | JWT signing secret | Required |
| `JWT_EXPIRATION` | JWT expiration time (ms) | `86400000` |
| `CACHE_TYPE` | Cache provider (`caffeine`/`redis`) | `caffeine` |
| `REDIS_HOST` | Redis host (if using Redis) | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `LOG_LEVEL` | Application log level | `INFO` |

### Database Setup

```sql
-- Create database and user
CREATE DATABASE togglr;
CREATE USER togglr WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE togglr TO togglr;
```

## 📚 API Documentation

Once running, access the interactive API documentation:

- **Scalar UI**: `http://localhost:8080/api/v1/docs/scalar`

## 🔐 Security

### Authentication

```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# Use token in subsequent requests
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/features
```

### Roles

- **USER**: Read-only access to features
- **MANAGER**: Create and edit features, namespaces, environments
- **ADMIN**: Full system access including user management

## 📊 Monitoring

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Metrics**: `/actuator/metrics`

### Logging

Structured JSON logging with configurable levels:

```yaml
logging:
  level:
    com.togglr: INFO
    org.springframework.security: WARN
```

## 🤝 Contributing

We welcome contributions!

### Development Setup

```bash
# Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/togglr-backend.git
cd togglr-backend

# Create feature branch
git checkout -b feature/amazing-feature

# Make changes and test
./mvnw test

# Submit pull request
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [PostgreSQL](https://www.postgresql.org/) - Database
- [Caffeine](https://github.com/ben-manes/caffeine) - Caching library
- [JJWT](https://github.com/jwtk/jjwt) - JWT library

## 📞 Support

- 📖 [Documentation](https://github.com/gdrocha-io/togglr/wiki)
- 🐛 [Issue Tracker](https://github.com/gdrocha-io/togglr-backend/issues)
- 💬 [Discussions](https://github.com/gdrocha-io/togglr-backend/discussions)
- 📧 [Email](mailto:gabriel@gdrocha.io)

---

<div align="center">

**[⭐ Star this project](https://github.com/gdrocha-io/togglr-backend) if you find it useful!**

Made with ❤️ by [Gabriel da Rocha](https://github.com/gdrocha)

</div>