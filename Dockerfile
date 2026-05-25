# ============================================
# 宠物领养救助管理平台 — Docker 构建文件
# 多阶段构建：编译 → 运行
# ============================================

# ---- 阶段1：编译 ----
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /build

# 先只复制 pom.xml，利用 Docker 层缓存加速
COPY pom.xml .
COPY pet-common/pom.xml pet-common/pom.xml
COPY pet-framework/pom.xml pet-framework/pom.xml
COPY pet-gateway/pom.xml pet-gateway/pom.xml
COPY pet-module-system/pom.xml pet-module-system/pom.xml
COPY pet-module-pet/pom.xml pet-module-pet/pom.xml
COPY pet-module-adopt/pom.xml pet-module-adopt/pom.xml
COPY pet-module-mall/pom.xml pet-module-mall/pom.xml
COPY pet-module-volunteer/pom.xml pet-module-volunteer/pom.xml
COPY pet-module-chat/pom.xml pet-module-chat/pom.xml
COPY pet-module-ai/pom.xml pet-module-ai/pom.xml
RUN mvn dependency:go-offline -B

# 复制所有源码并编译（application-secret.yml 已 gitignore，不会进镜像）
COPY . .
RUN mvn clean package -DskipTests -B

# ---- 阶段2：运行 ----
FROM openjdk:17-jre-slim
WORKDIR /app

# 从编译阶段复制 JAR
COPY --from=builder /build/pet-gateway/target/*.jar app.jar

# 上传文件目录
RUN mkdir -p /data/pet-adoption/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
