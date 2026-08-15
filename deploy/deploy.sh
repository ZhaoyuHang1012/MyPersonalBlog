#!/bin/bash
# =============================================
# 博客平台 · Ubuntu/Debian 一键部署脚本
# 用法: 将整个项目目录上传到服务器后，以 root 执行:
#   chmod +x deploy/deploy.sh && sudo bash deploy/deploy.sh
# =============================================
set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DEPLOY_ROOT="/opt/blog"
DB_PASSWORD="${DB_PASSWORD:-Blog@2026#Strong}"
JWT_SECRET="${JWT_SECRET:-$(head -c 48 /dev/urandom | base64 | tr -d '\n')}"

echo "=============================================="
echo "  博客平台部署脚本"
echo "  项目目录: $PROJECT_DIR"
echo "  部署目录: $DEPLOY_ROOT"
echo "=============================================="

# ---------- 1. 安装依赖 ----------
echo "[1/7] 安装 JDK 17 / Nginx / MySQL ..."
apt-get update -y
apt-get install -y openjdk-17-jdk-headless nginx mysql-server

# ---------- 2. 初始化数据库 ----------
echo "[2/7] 初始化数据库 ..."
mysql -uroot <<SQL
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS 'blog'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON blog.* TO 'blog'@'localhost';
FLUSH PRIVILEGES;
SQL

echo "[3/7] 导入表结构与初始数据 ..."
mysql -uroot --default-character-set=utf8mb4 blog < "$PROJECT_DIR/sql/init.sql"
for f in "$PROJECT_DIR"/sql/upgrade/*.sql; do
  echo "  导入: $(basename "$f")"
  mysql -uroot --default-character-set=utf8mb4 blog < "$f"
done

# ---------- 3. 构建后端 ----------
echo "[4/7] 构建后端 jar ..."
if [ ! -x "$PROJECT_DIR/tools/apache-maven-3.9.9/bin/mvn" ]; then
  echo "  未找到项目自带 Maven，尝试系统 mvn ..."
  MVN="mvn"
else
  MVN="$PROJECT_DIR/tools/apache-maven-3.9.9/bin/mvn"
fi
(cd "$PROJECT_DIR/backend" && "$MVN" -B -DskipTests package -s "$PROJECT_DIR/tools/settings.xml" -gs "$PROJECT_DIR/tools/settings.xml")

# ---------- 4. 构建前端 ----------
echo "[5/7] 构建前端静态文件 ..."
if [ ! -d "$PROJECT_DIR/frontend/node_modules" ]; then
  (cd "$PROJECT_DIR/frontend" && npm install --no-audit --no-fund)
fi
(cd "$PROJECT_DIR/frontend" && npm run build)

# ---------- 5. 部署文件 ----------
echo "[6/7] 部署文件到 $DEPLOY_ROOT ..."
mkdir -p "$DEPLOY_ROOT"/{logs,uploads}
cp "$PROJECT_DIR/backend/target/blog-backend-0.1.0.jar" "$DEPLOY_ROOT/blog.jar"
cp "$PROJECT_DIR/deploy/application-prod.yml" "$DEPLOY_ROOT/application-prod.yml"
cp -r "$PROJECT_DIR/frontend/dist" "$DEPLOY_ROOT/dist"

# 写入生产配置中的敏感项
sed -i "s|CHANGE_ME_DB_PASSWORD|${DB_PASSWORD}|g" "$DEPLOY_ROOT/application-prod.yml"
sed -i "s|CHANGE_ME_JWT_SECRET|${JWT_SECRET}|g" "$DEPLOY_ROOT/application-prod.yml"
SERVER_IP=$(hostname -I | awk '{print $1}')
sed -i "s|CHANGE_ME_SERVER_IP|${SERVER_IP}|g" "$DEPLOY_ROOT/application-prod.yml"

# ---------- 6. systemd 服务 ----------
echo "[7/7] 配置 systemd 服务与 Nginx ..."
cat > /etc/systemd/system/blog.service <<EOF
[Unit]
Description=Blog Platform Backend
After=network.target mysql.service

[Service]
WorkingDirectory=$DEPLOY_ROOT
ExecStart=/usr/bin/java -jar $DEPLOY_ROOT/blog.jar --spring.profiles.active=prod
Restart=always
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF

cp "$PROJECT_DIR/deploy/nginx-blog.conf" /etc/nginx/conf.d/blog.conf
rm -f /etc/nginx/sites-enabled/default

systemctl daemon-reload
systemctl enable blog
systemctl restart blog
systemctl restart nginx

echo ""
echo "=============================================="
echo "  部署完成！"
echo "  访问地址: http://$SERVER_IP"
echo "  管理后台: http://$SERVER_IP/admin  (admin / admin123)"
echo "  后端日志: journalctl -u blog -f"
echo "  注意: 请在云服务器安全组放行 80 端口"
echo "=============================================="
