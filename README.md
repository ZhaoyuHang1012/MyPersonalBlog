# blog-platform · 个人博客平台

一个可以本地部署的个人博客平台，前后端分离架构，具备主流博客的核心功能。

## 技术栈

| 层次 | 技术 |
| ---- | ---- |
| 后端 | Java 17+ · Spring Boot 3.3 · Spring Security (JWT) · MyBatis-Plus · commonmark-java |
| 前端 | Vue 3 · Vite 5 · Vue Router · Pinia · Element Plus · md-editor-v3（Markdown 编辑器）· highlight.js |
| 数据库 | MySQL 5.7+（utf8mb4） |

## 目录结构

```
blog-platform/
├── backend/          # Spring Boot 后端（端口 8080）
│   ├── pom.xml
│   └── src/main/java/com/blog/
│       ├── common/       # 统一响应、异常处理、分页
│       ├── config/       # Security、MyBatis-Plus、启动初始化
│       ├── controller/   # 前台接口 + admin/ 后台接口
│       ├── dto/ vo/      # 出入参对象
│       ├── entity/ mapper/
│       ├── security/     # JWT 工具与过滤器
│       └── service/
├── frontend/         # Vue3 前端（端口 5173，dev 代理 /api → 8080）
│   └── src/
│       ├── api/          # axios 封装与接口定义
│       ├── components/   # 公共组件（博客外壳）
│       ├── router/ store/
│       ├── styles/
│       └── views/
│           ├── blog/     # 博客前台：首页 / 文章详情 / 关于
│           └── admin/    # 管理后台：登录 / 仪表盘 / 文章 / 分类 / 标签
├── sql/init.sql      # 数据库初始化脚本
└── tools/settings.xml# 项目专用 Maven 配置（本地仓库 + 阿里云镜像）
```

## 环境要求

- **JDK 17+**（本机已装 JDK 18：`C:\Program Files\Java\jdk-18.0.1.1`）
- **Maven**：项目自带便携版 `tools/apache-maven-3.9.9`，无需安装
- **Node.js 16+**（本机已装 Node 24 + npm 11）
- **MySQL 5.7+**（本机已装且服务运行中）

## 快速启动

### 1. 初始化数据库（仅首次）

```bash
mysql -uroot -p --default-character-set=utf8mb4 -e "source sql/init.sql"
```

脚本会创建 `blog` 库、全部数据表、初始分类（默认分类）与标签（随笔/技术），
并创建专用账号 `blog / blog123456`（在 `backend/src/main/resources/application.yml` 中配置）。

> 注意：项目自带 Maven 专用配置 `tools/settings.xml`（本地仓库放在工作区内 + 阿里云镜像），
> 因为本机 Maven 全局 `settings.xml` 存在 XML 语法错误且镜像地址已失效，且系统 Maven 3.6.1
> 不满足 Spring Boot 3.3 的要求（需 3.6.3+）。请始终使用项目自带的 Maven 3.9.9 构建。
> 构建时带上 `-s tools/settings.xml -gs tools/settings.xml` 参数。

### 2. 启动后端（端口 8080）

```bash
cd backend
set JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1
..\tools\apache-maven-3.9.9\bin\mvn.cmd -s ../tools/settings.xml -gs ../tools/settings.xml spring-boot:run
```

或打包后运行：

```bash
..\tools\apache-maven-3.9.9\bin\mvn.cmd -s ../tools/settings.xml -gs ../tools/settings.xml -DskipTests clean package
java -jar target/blog-backend-0.1.0.jar
```

首次启动会自动创建：
- 管理员账号：**admin / admin123**（登录后请尽快修改）
- 一篇置顶示例文章

### 3. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

### 4. 访问

| 页面 | 地址 |
| ---- | ---- |
| 博客前台 | http://localhost:5173 |
| 管理后台 | http://localhost:5173/admin （admin / admin123） |

前台页面：首页 `/`、文章 `/post/:id`、归档 `/archive`、友链 `/links`、说说 `/murmur`、相册 `/album`、关于 `/about`

## 已实现功能（第一版骨架）

**前台**
- 文章列表（分页 / 置顶 / 分类筛选 / 标签筛选 / 标题搜索）
- 文章详情（Markdown 渲染、GFM 表格、代码高亮、上一篇/下一篇、浏览量统计）
- 分类、标签侧边栏（含文章计数）、关于页

**后台**
- JWT 登录认证（路由守卫 + 请求拦截）
- 仪表盘（文章总数 / 已发布 / 草稿）
- 文章管理：Markdown 编辑器（实时预览）、草稿/发布、置顶、分类、标签（可即时创建）、删除
- 分类管理、标签管理（增删改，含文章数统计）

**第二阶段 · 批次 1（站点设置 + 评论）**
- 站点设置：标题 / 副标题 / 站长昵称 / 公告（首页横幅）/ 关于页（Markdown）/ 页脚 / 备案号 / 评论开关
- 评论系统：访客评论 + 楼中楼回复、emoji 表情面板、审核制（待审核 / 通过 / 垃圾）、评论数统计、IP 防刷限流

**第二阶段 · 批次 2（媒体 + 个人中心）**
- 图片上传：本地磁盘存储（`uploads/` 目录按日期归档）、UUID 重命名、格式白名单、10MB 限制
- 媒体库：图片网格浏览、复制链接、删除
- 编辑器集成：写文章时可直接上传插入图片
- 个人设置：昵称、头像上传、修改密码（改后强制重新登录）

**第二阶段 · 批次 3（归档 / 搜索 / RSS / 统计 / 日志）**
- 归档页：按年月时间线分组展示全部文章
- 全文搜索：标题 + 正文关键词检索
- RSS 2.0 订阅（页脚入口）
- 统计仪表盘：文章/发布/草稿/今日访问卡片、近 7 天访问趋势折线图、浏览量 Top10 柱状图（ECharts）
- 访问记录：前台列表/详情 PV 自动入库
- 操作日志：AOP 记录后台所有增删改操作（用户、参数、IP、耗时、结果），密码等敏感字段自动打码

**第二阶段 · 批次 4（友链 / 说说 / 相册 / 黑夜模式）**
- 友链：前台卡片展示 + 访客申请（URL 校验、防刷限流），后台审核/编辑/排序
- 说说（树洞）：管理员发布碎碎念，前台时间流展示
- 相册：复用媒体库图片，前台网格 + 大图预览，后台精选管理
- 黑夜模式：导航栏一键切换，偏好本地持久化

## 规划中的功能（可选后续）

- [ ] 文章点赞 / 收藏
- [ ] 评论表情包（图片版）
- [ ] 邮件通知（评论/友链审核提醒）
- [ ] 生产部署：前端打包进后端 JAR 或 Nginx 部署
