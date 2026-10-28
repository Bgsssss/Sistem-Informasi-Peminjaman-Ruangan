# syntax=docker/dockerfile:1.7
# Image aplikasi Spring Boot (Maven). Frontend html/css/js di src/main/resources/static ikut masuk ke jar.
# Letakkan file ini di root proyek, sejajar dengan pom.xml.
#
# JAVA_VERSION harus sama dengan <java.version> di pom.xml.
ARG JAVA_VERSION=25

# ---------- tahap 1: build ----------
FROM maven:3.9-eclipse-temurin-${JAVA_VERSION} AS build
WORKDIR /workspace

# Salin pom.xml dulu agar dependensi di-cache selama pom.xml tidak berubah.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -q dependency:resolve || true

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests clean package \
 && cp "$(ls target/*.jar | grep -v -E 'original|plain' | head -n 1)" /workspace/app.jar

# ---------- tahap 2: runtime ----------
FROM eclipse-temurin:${JAVA_VERSION}-jre AS runtime

ENV TZ=Asia/Jakarta \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Jakarta"

# Jalankan sebagai pengguna biasa, bukan root.
RUN groupadd --system app && useradd --system --gid app --home-dir /app --shell /usr/sbin/nologin app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar
USER app

EXPOSE 8080

# Cek port terbuka (image JRE tidak membawa curl). Spring Boot butuh sekitar 30-60 detik untuk siap.
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=6 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]