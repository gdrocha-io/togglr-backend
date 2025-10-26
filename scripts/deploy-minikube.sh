#!/bin/bash

set -e

echo "🚀 Deploying Togglr Backend to Minikube"

# Check if minikube is running
if ! minikube status > /dev/null 2>&1; then
    echo "❌ Minikube is not running. Please start it first:"
    echo "   minikube start --cpus=4 --memory=8192"
    exit 1
fi

# Configure Docker to use Minikube's registry
echo "🔧 Configuring Docker environment..."
eval $(minikube docker-env)

# Build the application
echo "📦 Building application..."
mvn clean package -DskipTests

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t togglr/backend:1.0.0 .

# Create namespace
echo "📁 Creating namespace..."
kubectl create namespace togglr --dry-run=client -o yaml | kubectl apply -f -

# Deploy PostgreSQL
echo "🐘 Deploying PostgreSQL..."
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: togglr
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15
        env:
        - name: POSTGRES_DB
          value: "togglr"
        - name: POSTGRES_USER
          value: "togglr"
        - name: POSTGRES_PASSWORD
          value: "password"
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: togglr
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
EOF

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/postgres -n togglr

# Deploy Togglr Backend with Helm
echo "🎯 Deploying Togglr Backend..."
helm upgrade --install togglr-backend ./helm \
  --namespace togglr \
  --values helm/values-minikube.yaml \
  --wait

# Wait for deployment to be ready
echo "⏳ Waiting for Togglr Backend to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/togglr-backend -n togglr

# Get service URL
echo "🌐 Getting service information..."
kubectl get svc -n togglr

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Next steps:"
echo "   1. Port forward to access the API:"
echo "      kubectl port-forward svc/togglr-backend 8080:8080 -n togglr"
echo ""
echo "   2. Test the API:"
echo "      curl http://localhost:8080/actuator/health"
echo ""
echo "   3. View logs:"
echo "      kubectl logs -f deployment/togglr-backend -n togglr"
echo ""
echo "   4. Access Minikube dashboard:"
echo "      minikube dashboard"