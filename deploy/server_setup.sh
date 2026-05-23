#!/bin/bash
# ============================================================
# 宠物领养救助管理平台 — 服务器一键环境配置
# 在服务器上 SSH 后，直接复制粘贴执行
# ============================================================
# ⚠️ 注意：此脚本不含任何真实密钥！
#    所有 Key/密码 请从本地 application-secret.yml 复制
# ============================================================

set -e

echo "=========================================="
echo "🚀 开始配置服务器环境"
echo "=========================================="

# ---- 1. 创建项目目录 ----
echo ""
echo "【1/6】创建项目目录..."
sudo mkdir -p /opt/pet-adoption
sudo chown -R $USER:$USER /opt/pet-adoption
mkdir -p /opt/pet-adoption/{nginx,sql,uploads}
echo "  ✅ 目录已创建"

# ---- 2. 写入 Nginx 配置 ----
echo ""
echo "【2/6】写入 Nginx 配置..."
cat > /opt/pet-adoption/nginx/default.conf << 'NGINX'
server {
    listen       80;
    server_name  localhost;
    client_max_body_size 50M;

    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    location /uploads/ {
        proxy_pass http://backend:8080/uploads/;
        proxy_set_header Host $host;
    }
}
NGINX
echo "  ✅ Nginx 配置已写入"

# ---- 3. 写入 .env 模板 ----
echo ""
echo "【3/6】创建 .env 模板（请手动填入真实值）..."
cat > /opt/pet-adoption/.env << 'ENV'
# ============================================================
# Docker Compose 环境变量
# 请从本地 application-secret.yml 复制真实值填入
# ============================================================

# 数据库密码
DB_PASSWORD=***

# 高德 Web服务 API Key
AMAP_WEB_KEY=***

# DeepSeek AI API Key
DEEPSEEK_KEY=***

# 第三方服务 AppCode
APPCODE=***
ENV
echo "  ⚠️  请编辑 /opt/pet-adoption/.env 填入真实密钥"

# ---- 4. 创建 application-secret.yml 模板 ----
echo ""
echo "【4/6】创建 application-secret.yml 模板..."
cat > /opt/pet-adoption/application-secret.yml << 'SECRET'
# ============================================================
# 敏感配置 — 请从本地的 application-secret.yml 复制
# ============================================================
pet:
  ai:
    api-key: "***"               # DeepSeek API Key
  jwt:
    secret: "***"                # JWT 密钥
  third-party:
    appcode: "***"               # AppCode
    captcha:
      app-id: "***"              # 滑块验证码 AppID
      app-secret: "***"          # 滑块验证码 Secret
    sms:
      template-id: "***"         # 短信模板 ID
      sign-id: "***"             # 短信签名 ID
  map:
    amap-key: "***"              # 高德 Web 服务 API Key

spring:
  datasource:
    password: "***"              # 数据库密码
SECRET
echo "  ⚠️  请编辑 /opt/pet-adoption/application-secret.yml 填入真实密钥"

# ---- 5. 创建 MySQL 初始化 SQL 模板 ----
echo ""
echo "【5/6】创建数据库初始化脚本模板..."
cat > /opt/pet-adoption/sql/init.sql << 'SQL'
-- ============================================================
-- 请将 pet_adoption.sql 的全部内容粘贴到此处
-- 文件位置：本机 C:\Users\Priest\Desktop\宠物网站开发\pet_adoption.sql
-- 然后在末尾追加 migration_v6_add_place_fields.sql 的内容
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pet_adoption` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pet_adoption`;
SELECT '请替换此文件为完整的 pet_adoption.sql' AS warning;
SQL
echo "  ⚠️  请将完整 SQL 复制到 /opt/pet-adoption/sql/init.sql"

# ---- 6. 克隆项目代码 ----
echo ""
echo "【6/6】克隆项目代码..."
if [ -d "/opt/pet-adoption/repo" ]; then
    echo "  仓库已存在，拉取更新..."
    cd /opt/pet-adoption/repo && git pull
else
    cd /opt/pet-adoption
    git clone https://github.com/priestZZB/pet-adoption-platform.git repo
fi
echo "  ✅ 代码已克隆"

# ---- 完成 ----
echo ""
echo "=========================================="
echo "✅ 服务器环境配置完成！"
echo "=========================================="
echo ""
echo "📌 后续步骤："
echo ""
echo " 1. 填入真实密钥："
echo "    vim /opt/pet-adoption/.env"
echo "    vim /opt/pet-adoption/application-secret.yml"
echo ""
echo " 2. 替换数据库初始化脚本："
echo "    将本地的 pet_adoption.sql + migration.sql 合并到"
echo "    /opt/pet-adoption/sql/init.sql"
echo ""
echo " 3. 本地构建并上传 jar + 前端 dist："
echo "    cd pet-adoption-platform"
echo "    mvn clean package -DskipTests"
echo "    cd frontend && npm run build"
echo "    然后将 jar 和 dist 上传到 /opt/pet-adoption/"
echo ""
echo " 4. 启动 Docker："
echo "    cd /opt/pet-adoption && docker compose up -d"
echo ""
echo " 5. 宝塔面板设置反向代理："
echo "    网站 → 添加站点 → 反向代理 → http://127.0.0.1:8081"
echo "=========================================="
