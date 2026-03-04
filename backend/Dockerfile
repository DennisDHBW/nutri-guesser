# ============================================
# Stage 1: Build with Maven Wrapper
# ============================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven Wrapper and project config first (layer caching)
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Make mvnw executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the Quarkus application (skip tests for Docker build)
RUN ./mvnw package -DskipTests -B

# ============================================
# Stage 2: Runtime with JRE
# ============================================
FROM eclipse-temurin:21-jre

ARG APP_DIR=/opt/app
WORKDIR ${APP_DIR}

# Create data directory for H2 database and install curl for healthcheck
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /data/h2 && \
    groupadd -r appuser && \
    useradd -r -g appuser -d ${APP_DIR} appuser && \
    chown -R appuser:appuser ${APP_DIR} /data/h2

# Copy the Quarkus fast-jar output
COPY --from=build --chown=appuser:appuser /app/target/quarkus-app/lib/ ./lib/
COPY --from=build --chown=appuser:appuser /app/target/quarkus-app/*.jar ./
COPY --from=build --chown=appuser:appuser /app/target/quarkus-app/app/ ./app/
COPY --from=build --chown=appuser:appuser /app/target/quarkus-app/quarkus/ ./quarkus/

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/q/health/ready || exit 1

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]



