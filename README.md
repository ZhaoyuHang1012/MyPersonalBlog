# blog-platform · 多用户博客平台

一个前后端分离的多用户博客平台：邀请码注册、好友关系、大厅动态流、文章/说说/相册、
点赞收藏、按月份归档、独立存储空间，支持本地运行与服务器部署。

## 技术栈

| 层次 | 技术 |
| ---- | ---- |
| 后端 | Java 17+ · Spring Boot 3.3 · Spring Security (JWT) · MyBatis-Plus · commonmark-java · Spring AOP |
| 前端 | Vue 3 · Vite 5 · Vue Router · Pinia · Element Plus · ECharts · md-editor-v3（Markdown 编辑器）· highlight.js |
| 数据库 | MySQL 5.7+（utf8mb4） |
| 部署 | Nginx（静态托管 + 反向代理）· systemd · 可选 Docker |

## 功能总览

**内容**
- 文章：Markdown 编辑器（实时预览、图片上传）、草稿/发布/置顶、分类标签、可见性（开放/仅自己可见）
- 说说：发布动态（文字 + 最多 9 张配图）、权限设置、快捷编辑
- 相册：用户创建相册组，上传照片与视频（自动封面）、权限设置

**互动**
- 好友：申请/同意/拒绝、搜索昵称或 ID 添加、注册自动与管理员结为好友
- 评论：仅登录用户（身份自动识别）、楼中楼、emoji 面板、默认直接通过、后台退回/垃圾/删除、作者评论带粉色 UP 标签
- 点赞：文章与说说点赞（实时联动点赞人列表）、收藏：文章/说说/相册一键收藏（按类型分类查看）

**平台**
- 大厅：按类型 Tab（文章/说说/相册）展示自己+好友的公开内容，多标签组合筛选（再次点击取消）
- 时间线归档：文章/说说/相册按月份自动归档
- 个人中心：我的文章/说说/相册/好友/收藏/媒体/设置（前台风格）
- 邀请码注册、角色体系（管理员/普通用户）、资源隔离、每用户独立存储空间（默认 1GB 配额）

**管理后台（管理员）**
- 仪表盘（ECharts 访问趋势/浏览量 Top10）、文章/分类/标签/评论管理
- 媒体库（按用户筛选、图片放大预览、视频在线播放）
- 说说管理（按用户筛选）、友链审核、站点设置、邀请码、操作日志（密码打码）
- 用户管理：修改任意用户信息、分配角色、重置密码、管理好友关系
- RSS 订阅、黑夜模式、全文搜索、IP 防刷限流

## 目录结构

```
blog-platform/
├── backend/              # Spring Boot 后端（端口 8080）
│   └── src/main/java/com/blog/
│       ├── common/       # 统一响应、异常处理、分页
│       ├── config/       # Security、MyBatis-Plus、访问统计、启动初始化
│       ├── controller/   # 前台接口 + admin/ 后台接口
│       ├── dto/ vo/      # 出入参对象
│       ├── entity/ mapper/
│       ├── security/     # JWT 工具与过滤器
│       ├── service/      # 业务逻辑
│       └── aspect/       # 操作日志切面
├── frontend/             # Vue3 前端（端口 5173）
│   └── src/
│       ├── api/          # axios 封装与接口定义
│       ├── components/   # 博客外壳、说说流、相册网格、点赞/收藏按钮等
│       ├── router/ store/
│       ├── styles/
│       └── views/
│           ├── blog/     # 前台：大厅/文章/归档/收藏/友链/说说/相册/个人主页
│           ├── admin/    # 管理后台
│           ├── me/       # 个人中心
│           └── auth/     # 注册
├── sql/
│   ├── init.sql          # 全新安装的表结构
│   └── upgrade/          # 增量迁移脚本（按编号顺序执行）
├── deploy/               # 服务器部署材料
│   ├── deploy.sh         # Ubuntu 一键部署脚本
│   ├── application-prod.yml  # 生产配置（含占位符）
│   ├── nginx-blog.conf   # Nginx 配置模板
│   └── DEPLOY.md         # 详细部署指南
├── tools/                # 项目自带 Maven 3.9.9 + 阿里云镜像配置
├── start-all.bat         # 一键启动（前后端，Windows 本地）
├── start-backend.bat     # 仅启动后端
├── start-frontend.bat    # 仅启动前端
└── uploads/              # 上传文件（按用户名分目录）
```

## 本地运行

### 环境要求

- **JDK 17+**（本机已装 JDK 18：`C:\Program Files\Java\jdk-18.0.1.1`）
- **Maven**：项目自带便携版 `tools/apache-maven-3.9.9`，无需安装
- **Node.js 16+**（本机已装 Node 24 + npm 11）
- **MySQL 5.7+**（本机已装且服务运行中）

### 一键启动（推荐）

双击项目根目录的 **`start-all.bat`**，自动弹出两个窗口分别运行前后端：

- 后端窗口：首次会自动构建 jar（无 jar 时）
- 前端窗口：首次会自动安装依赖（无 node_modules 时）

### 手动启动

**1. 初始化数据库（仅首次）**

```bash
mysql -uroot -p --default-character-set=utf8mb4 -e "source sql/init.sql"
for f in sql/upgrade/*.sql; do mysql -uroot -p --default-character-set=utf8mb4 blog < "$f"; done
```

> 项目专用 Maven 配置见 `tools/settings.xml`（本地仓库 + 阿里云镜像）。
> 本机 Maven 全局 settings.xml 有损坏且版本过旧，构建时请始终使用项目自带 Maven 并带
> `-s tools/settings.xml -gs tools/settings.xml` 参数。

**2. 启动后端（端口 8080）**

```bash
cd backend
set JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1
..\tools\apache-maven-3.9.9\bin\mvn.cmd -s ../tools/settings.xml -gs ../tools/settings.xml spring-boot:run
```

**3. 启动前端（端口 5173）**

```bash
cd frontend
npm install
npm run dev
```

### 访问地址与账号

| 页面 | 地址 |
| ---- | ---- |
| 大厅（前台） | http://localhost:5173 |
| 管理后台 | http://localhost:5173/admin （admin / admin123） |
| 注册页 | http://localhost:5173/register |

前台页面：大厅 `/`、文章 `/post/:id`、归档（时间线）`/archive`、收藏 `/favorites`、友链 `/links`、
说说 `/murmur`、相册 `/album`、关于 `/about`、个人博客页 `/u/{用户名}`

账号体系：
- **管理员**：admin / admin123（首次启动自动创建，请尽快修改密码）
- **普通用户**：通过邀请码注册（管理员后台「邀请码」页生成，注册后自动与管理员成为好友）

## 服务器部署

详细指南见 **[deploy/DEPLOY.md](deploy/DEPLOY.md)**，核心三步：

```bash
# ① 本地打包上传（排除依赖目录）
tar -czf blog-platform.tar.gz --exclude=blog-platform/frontend/node_modules \
    --exclude=blog-platform/backend/target --exclude=blog-platform/.git \
    --exclude=blog-platform/uploads blog-platform
scp blog-platform.tar.gz root@服务器IP:/root/

# ② SSH 登录服务器，一键部署（自动装 JDK/Nginx/MySQL、建库、构建、部署、启动）
ssh root@服务器IP
cd /root && tar -xzf blog-platform.tar.gz
cd /root/blog-platform && chmod +x deploy/deploy.sh && sudo bash deploy/deploy.sh

# ③ 访问
http://服务器IP          # 前台大厅
http://服务器IP/admin    # 管理后台
```

服务器建议配置：**Ubuntu 22.04 · 2核2G 起步 · 带宽 3M+**，安全组放行 22/80/443 端口。

上线后必做：
1. 登录后台 → 个人设置 → **修改 admin 密码**
2. 后台 → 站点设置 → 修改站点标题/公告
3. 备份数据（见下节）

## 服务器日常维护

### SSH 登录

```bash
ssh root@服务器IP
```

> 登录后提示符形如 `root@xxx:~#`；退出执行 `exit`。
> 服务器密码遗忘时，在云控制台「实例 → 重置密码」后重启实例。

### 服务管理

| 操作 | 命令 |
|------|------|
| 查看后端日志 | `journalctl -u blog -f`（实时）/ `journalctl -u blog -n 100`（最近100行） |
| 重启后端 | `systemctl restart blog` |
| 停止/启动后端 | `systemctl stop blog` / `systemctl start blog` |
| 开机自启状态 | `systemctl status blog` |
| 重启 Nginx | `systemctl restart nginx` |
| 重载 Nginx 配置 | `nginx -t && systemctl reload nginx` |

### 数据备份（建议定期执行）

```bash
# 备份数据库
mysqldump -uroot blog > /root/backup/blog-$(date +%Y%m%d).sql

# 备份上传文件（用户图片/视频）
tar -czf /root/backup/uploads-$(date +%Y%m%d).tar.gz -C /opt/blog uploads

# 下载到本地电脑（在本地 cmd 执行）
scp root@服务器IP:/root/backup/blog-20260815.sql .
```

### 更新部署（本地有新版本代码后）

```bash
# ① 本地重新打包上传（同部署步骤①）
# ② 服务器上重新构建并替换
cd /root/blog-platform
git pull   # 或重新上传解压覆盖
sudo bash deploy/deploy.sh   # 脚本会重新构建并重启服务
```

### 常见问题排查

| 现象 | 排查 |
|------|------|
| 网站打不开 | 云控制台安全组是否放行 80 端口；`systemctl status blog nginx` 看服务状态 |
| 上传大文件失败 | Nginx 的 `client_max_body_size` 已配 2g；检查后端 `application-prod.yml` 的 multipart 限制 |
| 图片/视频 404 | 检查 `/opt/blog/uploads` 目录与 Nginx 中 `alias` 路径是否一致 |
| 数据库连接失败 | `mysql -ublog -p` 测试；查看后端日志中的连接报错 |
| 忘记数据库密码 | 云服务器 root 执行 `mysql -uroot` 重置（数据库 root 无密码策略下可直接进） |

## 规划中的功能（可选后续）

- [ ] 评论表情包（图片版）
- [ ] 邮件通知（评论/友链审核提醒）
- [ ] HTTPS / 域名绑定
- [ ] Docker 部署方式
