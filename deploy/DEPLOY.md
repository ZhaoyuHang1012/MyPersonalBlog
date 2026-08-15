# 博客平台 · 服务器部署指南（Nginx 分离部署，IP 访问）

## 一、服务器选购建议

| 项目 | 建议 |
|------|------|
| 云厂商 | 阿里云 / 腾讯云（国内访问快，新手有优惠） |
| 系统 | **Ubuntu 22.04 LTS**（脚本按此编写，其他 Linux 也可手动部署） |
| 配置 | **2 核 2G 起步**（个人博客足够）；带宽 3M 以上体验更好 |
| 地域 | 离你的用户近即可 |
| 安全组 | 放行 **80 端口**（HTTP）、**22 端口**（SSH）；后续配 HTTPS 再放 443 |

## 二、部署步骤（买好服务器后）

### 0. 上传项目到服务器

在**本地电脑**执行（把项目上传到服务器，替换 IP 与路径）：

```bash
# Windows 用 scp 命令（在项目上级目录执行）：
scp -r blog-platform root@服务器IP:/root/
```

### 1. 一键部署

SSH 登录服务器后执行：

```bash
cd /root/blog-platform
chmod +x deploy/deploy.sh
sudo bash deploy/deploy.sh
```

脚本会自动完成：装 JDK/Nginx/MySQL → 建库导数据 → 构建前后端 → 部署到 `/opt/blog` → 配置 systemd 与 Nginx → 启动。

### 2. 访问

- 前台大厅：`http://服务器IP`
- 管理后台：`http://服务器IP/admin`（admin / admin123，**登录后立刻改密码**）

## 三、手动部署（不用脚本时）

1. **安装依赖**：`apt install -y openjdk-17-jdk-headless nginx mysql-server`
2. **数据库**：执行 `sql/init.sql` 和 `sql/upgrade/` 下全部脚本
3. **后端**：`backend` 目录 Maven 打包 → `java -jar blog.jar --spring.profiles.active=prod`
   - 提前编辑 `deploy/application-prod.yml` 的 3 个占位符（数据库密码/JWT 密钥/服务器 IP）
4. **前端**：`frontend` 目录 `npm install && npm run build`，把 `dist/` 交给 Nginx
5. **Nginx**：使用 `deploy/nginx-blog.conf`
6. **systemd**：参考脚本中的 `blog.service` 模板

## 四、上线后必做清单

- [ ] 登录后台 → 个人设置 → **修改 admin 密码**
- [ ] 后台 → 站点设置 → 修改站点标题/公告
- [ ] 备份：`/opt/blog/uploads`（用户上传文件）+ 数据库（`mysqldump -uroot blog > blog.sql`）
- [ ] 有条件时配置域名 + HTTPS（Let's Encrypt 免费证书）

## 五、日常运维

| 操作 | 命令 |
|------|------|
| 查看后端日志 | `journalctl -u blog -f` |
| 重启后端 | `systemctl restart blog` |
| 重启 Nginx | `systemctl restart nginx` |
| 数据库备份 | `mysqldump -uroot blog > backup.sql` |
| 上传文件目录 | `/opt/blog/uploads`（按用户名分目录） |

## 六、与本地开发版差异

- 生产配置在 `deploy/application-prod.yml`（独立数据库密码/JWT 密钥/站点地址）
- 上传目录改为 `/opt/blog/uploads`（相对 jar 运行目录）
- 前端由 Nginx 直接托管，`/api` 与 `/uploads` 反向代理到后端
