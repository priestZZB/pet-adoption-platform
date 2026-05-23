# ============================================================
# 宠物领养救助管理平台 — 后端 Dockerfile
# 使用预编译的 fat-jar，需要先本地 mvn clean package
# ============================================================

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制后端 jar
COPY pet-gateway-1.0.0.jar app.jar

# 复制外部配置文件（包含 API Key、密码等敏感信息）
COPY application-secret.yml /app/config/

# 上传文件目录
VOLUME /data/pet-adoption/uploads

EXPOSE 8080

# 启动时加载外部配置覆盖内置配置
ENTRYPOINT ["java", "-jar", "app.jar", \
    "--spring.config.additional-location=/app/config/application-secret.yml"]
