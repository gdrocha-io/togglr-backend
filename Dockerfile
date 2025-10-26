FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl and create non-root user with specific UID/GID to match Helm chart
RUN apk add --no-cache curl && \
    addgroup -g 2000 togglr && \
    adduser -u 1000 -G togglr -s /bin/sh -D togglr

# Create tmp directory for Spring Boot
RUN mkdir -p /tmp && chown togglr:togglr /tmp

# Copy the JAR file
COPY target/togglr-*.jar app.jar

# Change ownership
RUN chown togglr:togglr app.jar

# Switch to non-root user
USER 1000:2000

# Expose ports
EXPOSE 8080

# Run the application with optimized JVM settings
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
