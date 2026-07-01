# ratatouille-tools — Spring Boot 4 / Java 21 MCP-сервер.
# Собирается центральным CD (ratatouille-deploy) как kind=docker, build-on-server.
# Профиль стенда задаётся через SPRING_PROFILES_ACTIVE (ift → :9196; default/prod → :9096).
# Секреты (IFT_CROSSOVER_API_KEY / PROM_CROSSOVER_API_KEY / PROM_SMARTRING_UUID) — из env_file .env.

# --- build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Слой зависимостей кэшируется, пока не менялся pom.xml (go-offline не фатален — package дотянет остальное).
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -B -q -DskipTests clean package

# --- runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
# 9096 — prod-профиль, 9196 — ift-профиль (реальный порт берётся из application[-<profile>].yml)
EXPOSE 9096 9196
ENTRYPOINT ["java","-jar","app.jar"]
