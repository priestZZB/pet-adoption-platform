# 服务器部署检查清单

## 🔴 必做：配置 Nginx `/uploads/` 代理

### 问题
用户上传的图片存储在 `/data/pet-adoption/uploads/`，由 Spring Boot 通过 `/uploads/**` 映射提供访问。但如果 Nginx 在前面，`/uploads/` 请求不会自动到达 Spring Boot，导致全站上传图片不显示。

### 修复步骤

```bash
# 1. SSH 到服务器
ssh root@<your-server>

# 2. 编辑 Nginx 配置（通常位于 /etc/nginx/sites-enabled/ 或 /etc/nginx/conf.d/）
sudo vim /etc/nginx/sites-enabled/pet-adoption.conf
```

在 `server` 块中添加以下配置（在 `/api/` location 之前或之后均可）：

```nginx
location /uploads/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

```bash
# 3. 检查配置语法
sudo nginx -t

# 4. 重载 Nginx
sudo nginx -s reload
```

### 验证

```bash
# 上传一张测试图片后，确认能直接访问
curl -I https://adopt.priestlab.top/uploads/banner/2026/06/02/test.jpg
# 应返回 200 OK（如果文件不存在则 404，但不应返回 Nginx 默认 404 页面）
```

---

## 🟡 推荐：确保 systemd 服务使用 prod 环境

检查 Spring Boot 服务是否正确设置了 prod profile：

```bash
# 查看服务配置
sudo systemctl cat spring_pet-adoption.service

# 确认 ExecStart 包含 prod profile：
# ExecStart=/usr/bin/java -jar ... --spring.profiles.active=prod

# 如果不包含，编辑服务文件：
sudo systemctl edit spring_pet-adoption.service
# 添加：
# [Service]
# Environment="SPRING_PROFILES_ACTIVE=prod"

# 重启服务：
sudo systemctl daemon-reload
sudo systemctl restart spring_pet-adoption.service
```

---

## 🟢 可选：设置上传目录权限

```bash
# 确保 Spring Boot 进程用户对上传目录有读写权限
sudo chown -R <app-user>:<app-user> /data/pet-adoption/uploads
sudo chmod -R 755 /data/pet-adoption/uploads
```
