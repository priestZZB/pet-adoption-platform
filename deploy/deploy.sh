#!/bin/bash
# ============================================================
# 宠物领养救助管理平台 — 服务器部署脚本
# 在服务器上 /opt/pet-adoption/ 目录下执行
# ============================================================

set -e

echo "🚀 开始部署..."

# 1. 检查 .env
if [ ! -f .env ]; then
    echo "❌ 请先创建 .env 文件（cp .env.example .env 并填入真实值）"
    exit 1
fi

# 2. 检查必需文件
required_files=(
    "pet-gateway-1.0.0.jar"
    "frontend-dist/index.html"
    "nginx/default.conf"
    "sql/init.sql"
    "application-secret.yml"
)
for f in "${required_files[@]}"; do
    if [ ! -f "$f" ]; then
        echo "❌ 缺少文件: $f"
        exit 1
    fi
done

# 3. 拉取镜像并启动
echo "📦 拉取镜像..."
docker compose pull

echo "🏗️  构建并启动容器..."
docker compose up -d --build

# 4. 等待就绪
echo "⏳ 等待服务就绪..."
sleep 10

# 5. 检查状态
echo ""
echo "📊 容器状态:"
docker compose ps

echo ""
echo "✅ 部署完成！"
echo "   前端: http://localhost:8081"
echo "   后端: http://localhost:8080"
echo "   API文档: http://localhost:8080/doc.html"
echo ""
echo "📝 查看日志: docker compose logs -f"
