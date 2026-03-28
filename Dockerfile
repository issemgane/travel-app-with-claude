# ── Stage 1: Build Frontend ────────────────────────────
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# ── Stage 2: Build Backend ─────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app

# Copy frontend build output so Maven copies it to static/
COPY --from=frontend-build /app/frontend/dist /app/frontend/dist

COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src

RUN apk add --no-cache maven && \
    cd backend && mvn clean package -DskipTests -q

# ── Stage 3: Runtime ───────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
