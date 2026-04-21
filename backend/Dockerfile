FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy only pom first to leverage layer caching for dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy sources and build the application
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create non-root user/group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Install curl for healthchecks
RUN apk add --no-cache curl

# Copy jar from builder
COPY --from=builder /build/target/*.jar /app/app.jar

RUN chown -R appuser:appgroup /app
USER appuser
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080

# Healthcheck against Spring Boot actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
