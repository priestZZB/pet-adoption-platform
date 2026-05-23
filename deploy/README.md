# 宠物领养救助管理平台 — 部署文档

## 服务器配置
- CPU: 2核 / 内存: 1.6G / 磁盘: 40G
- 系统: Ubuntu 24.04 LTS
- Docker: 29.5.2 / Docker Compose: v5.1.4

---

## 第一步：本地构建

在**本地开发机**执行：

```bash
# 1. 后端打包
cd pet-adoption-platform
mvn clean package -DskipTests

# 2. 前端打包
cd frontend && npm run build && cd ..

# 3. 复制构建产物到 deploy 目录
cp pet-gateway/target/pet-gateway-1.0.0.jar deploy/
cp -r frontend/dist deploy/frontend-dist
```

---

## 第二步：复制到服务器

方式A — 用 SCP：

```bash
# 本地执行
scp -r deploy/* priest@8.138.110.116:/opt/pet-adoption/
```

方式B — 用宝塔面板：
1. 宝塔 → 文件 → 远程下载 / 上传
2. 把 `deploy/` 整个目录传到 `/opt/pet-adoption/`

---

## 第三步：服务器配置

SSH 到服务器执行：

```bash
# 3.1 创建目录
sudo mkdir -p /opt/pet-adoption
sudo chown -R priest:priest /opt/pet-adoption

# 3.2 复制所有文件到 /opt/pet-adoption/
# （如果 scp 直接传的已存在则跳过）

# 3.3 配置环境变量
cd /opt/pet-adoption
cp .env.example .env
# 编辑 .env 填入真实密码和 Key
vim .env

# 3.4 复制 application-secret.yml
# 这个文件不在 Git 中，需手动从本地复制到 /opt/pet-adoption/
# 或者直接在服务器创建

# 3.5 填写完整的 init.sql
# 把 pet_adoption.sql + migration_v6_add_place_fields.sql 的内容
# 合并到 sql/init.sql 中
```

---

## 第四步：启动

```bash
cd /opt/pet-adoption
docker compose up -d
```

查看启动状态：

```bash
docker compose ps              # 查看所有容器状态
docker compose logs -f         # 查看实时日志
docker compose logs backend    # 只看后端日志
```

---

## 第五步：配置宝塔反向代理

1. 宝塔 → **网站** → **添加站点**
   - 域名：`8.138.110.116`（或你的实际域名）
   - 根目录随便填
2. 站点设置 → **反向代理**
   - 名称：`pet-adoption`
   - 目标URL：`http://127.0.0.1:8081`

这样访问 `http://8.138.110.116` 就直接打开前端了。

---

## 第六步：验证

| 地址 | 说明 |
|:----|:------|
| http://8.138.110.116 | 前端首页 |
| http://8.138.110.116/doc.html | Swagger API 文档 |

---

## 常用命令

```bash
# 启动
docker compose up -d

# 停止
docker compose down

# 重启某个服务（更新代码后）
docker compose up -d --build backend

# 查看日志
docker compose logs -f --tail=100 backend

# 进入容器
docker exec -it pet-backend sh
docker exec -it pet-mysql mysql -uroot -p pet_adoption

# 清理旧数据
docker compose down -v    # 会删除数据库数据！（慎用）
```
