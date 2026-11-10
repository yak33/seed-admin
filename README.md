<div align="center">

# seed-admin

**基于 Spring Boot 4 + Vue Vben Admin 5 的前后端一体管理后台种子工程**

*单体 Multi-Module Maven 后端 + 精简 Vben5 (`web-naive`) 前端，默认打包为单个可执行 JAR*

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.18-2D8CF0)](https://baomidou.com/)
[![Sa-Token](https://img.shields.io/badge/Sa--Token-1.46.0-1E88E5)](https://sa-token.cc/)
[![Redisson](https://img.shields.io/badge/Redisson-4.8.0-DC382D?logo=redis&logoColor=white)](https://redisson.org/)
[![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)

[![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![Vben Admin](https://img.shields.io/badge/Vben%20Admin-5.7.0-009688)](https://github.com/vbenjs/vue-vben-admin)
[![Naive UI](https://img.shields.io/badge/Naive%20UI-2.44.1-18A058)](https://www.naiveui.com/)
[![Vite](https://img.shields.io/badge/Vite-8.3-646CFF?logo=vite&logoColor=white)](https://vite.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Node.js](https://img.shields.io/badge/Node.js-24.16.0-339933?logo=nodedotjs&logoColor=white)](https://nodejs.org/)
[![pnpm](https://img.shields.io/badge/pnpm-11.16.0-F69220?logo=pnpm&logoColor=white)](https://pnpm.io/)
[![Deploy](https://img.shields.io/badge/Deploy-Single%20JAR-555555)](#快速开始)

[核心特性](#核心特性) ·
[技术栈](#技术栈) ·
[项目结构](#项目结构) ·
[快速开始](#快速开始) ·
[开发与构建](#开发与构建) ·
[Docker 部署参考](#docker-部署参考) ·
[架构与接口说明](#架构与接口说明)

</div>

---

## 项目简介

`seed-admin` 是一个面向企业级后台与私有化交付场景的前后端一体种子工程。后端采用模块化单体 Spring Boot 4 工程，前端基于 Vue Vben Admin 5 精简保留 `web-naive` 应用；通过 Maven 构建链路自动将前端产物打入后端 `classpath:/static/`，实现**单 JAR 启动、单进程托管页面与 API**，在保持清晰分层的同时大幅降低 Nginx 与多容器编排运维成本。

## 核心特性

- **单 JAR 一体交付**：[seed-server/pom.xml](./seed-server/pom.xml) 内置 `frontend-maven-plugin`，默认自动拉取隔离环境的 Node.js 与 pnpm、编译 `web-naive` 并打包进可执行 JAR；纯后端迭代时支持 `-Dskip.frontend=true` 跳过前端构建。
- **开箱即用 RBAC**：内置用户管理、角色管理、菜单管理三大基础模块，覆盖分页查询、多角色绑定、菜单树授权、父级目录自动补齐与密码默认/留空不改策略。
- **严谨的权限与会话安全**：
  - 基于 Sa-Token + Redisson 托管登录态与权限缓存（30 天有效期 + 30 分钟不活动自动失效）。
  - 用户更新/停用/删除、角色权限变更、菜单更新/删除后，服务端主动调用 `LoginHelper.logoutByUserId(...)` 使相关在线会话立即失效。
  - 内置 `admin`（`userId=1`）与 `super` 角色多重保护：禁止通过管理接口修改或删除，角色选项接口不下发 `super`，菜单写接口强制校验 `@SaCheckRole("super")`。
- **数据模型与审计规范**：五张 RBAC 核心表统一采用 `VARCHAR(36)` 主键（由后端生成 UUID），全表统一 `create_by / create_time / update_by / update_time` 自动填充，`sys_user` / `sys_role` 支持逻辑删除。
- **前后端契约高度对齐**：
  - 开发期 Vite 代理 `/api` 至 `localhost:8080`（不重写前缀），与生产同源请求路径完全同构。
  - 内置 `SpaForwardConfig` 支持 History 路由刷新回退 `index.html`（静态资源缺失保持 404），兼容 `hash`（默认生产配置）与 `history` 两种前端路由模式。
  - 统一包装响应 `R<T>`（`code=200`）与平级分页响应 `TableDataInfo<T>`（`{ total, rows }`），`401` 自动触发前端重新认证，`403` 提示无操作权限。

## 技术栈

| 分类 | 技术 / 组件 | 版本（来源核对） | 说明 |
| --- | --- | --- | --- |
| **后端核心** | Java / Spring Boot | `25` / `4.1.2` | 单体 Web 应用与依赖管理（[pom.xml](./pom.xml)） |
| **数据访问** | MyBatis-Plus / MySQL | `3.5.18` / `5.7+` | `mybatis-plus-spring-boot4-starter`、分页与审计填充 |
| **认证缓存** | Sa-Token / Redisson | `1.46.0` / `4.8.0` | `sa-token-redisson` 会话持久化、注解鉴权 |
| **密码与工具** | Spring Security Crypto / Hutool | Boot BOM / `5.8.48` | 仅引入 BCrypt 编码器，不引入 Security 过滤器链 |
| **序列化** | Jackson 3 & Jackson 2 | Boot 4 默认 / `2.18.12` | HTTP 层用 Jackson 3，Sa-Token Redis 序列化固定 `2.18.12` |
| **前端核心** | Vue / Vben Admin / Naive UI | `3.5.41` / `5.7.0` / `^2.44.1` | 精简 `@vben/web-naive` monorepo 应用 |
| **前端工程** | Vite / TypeScript / Tailwind CSS | `^8.3.0` / `^6.0.3` / `^4.3.3` | Pinia `^4.0.2`、Vue Router `^5.2.0`、Vitest `^4.1.11` |
| **构建工具链** | Maven / Node.js / pnpm | `3.9+` / `v24.16.0` / `11.16.0` | Maven 托管工具链自动构建并拷贝静态资源 |

## 项目结构

```text
seed-admin
├── pom.xml                                  # Maven 父工程：版本仲裁、Java 25 编译与公共依赖管理
├── .gitignore                               # 忽略 target/node_modules/dist/application-local.yml 等
├── README.md
├── config/                                  # 本地外部配置目录（可放 application-local.yml，不入库）
├── sql/
│   └── seed.sql                             # 5 张 RBAC 表 DDL + 初始角色、菜单与 admin/123456 数据
├── seed-common/                             # 公共基础模块聚合
│   ├── seed-common-core/                    # 常量 Constants、统一返回 R<T>、业务异常、LoginUser
│   ├── seed-common-mybatis/                 # 分页插件、BaseEntity 审计字段、PageQuery/TableDataInfo
│   ├── seed-common-redis/                   # RedissonSaTokenDaoConfig（注册 SaTokenDaoForRedisson）
│   ├── seed-common-satoken/                 # SaInterceptor 拦截、StpInterface、LoginHelper、401/403 异常
│   └── seed-common-web/                     # 全局异常处理 GlobalExceptionHandler、SpaForwardConfig
├── seed-server/                             # 启动模块与系统业务实现
│   ├── pom.xml                              # 默认激活 frontend profile 自动构建并内嵌前端产物
│   └── src/main/
│       ├── java/io/github/yak33/
│       │   ├── SeedApplication.java         # 启动类（@MapperScan）
│       │   ├── config/                      # JacksonConfig（Boot 4 LocalDateTime 格式）、PasswordConfig
│       │   ├── web/                         # AuthController、UserController、SysLoginService
│       │   └── system/                      # SysUser / SysRole / SysMenu 完整分层（Controller/Service/Mapper/Entity/BO/VO）
│       └── resources/
│           ├── application.yml              # 基础配置（通过环境变量占位，不含真实密码）
│           ├── application-example.yml      # 本地配置模板
│           └── META-INF/satoken/
│               └── sa-json-type.list        # Sa-Token Redis 反序列化类型白名单
└── frontend/                                # Vben5 精简前端 monorepo（保留 @vben/web-naive）
    ├── package.json                         # dev:naive / build:naive / check / test:unit 等脚本
    ├── pnpm-workspace.yaml                  # workspace 包与 catalog 依赖版本集中声明
    └── apps/web-naive/
        ├── vite.config.ts                   # /api 开发代理、关闭 vxeTableLazyImport 防构建白屏
        └── src/
            ├── api/                         # core（auth/user/menu）与 system（user/role/menu）接口
            ├── router/access.ts             # backend 动态路由加载
            └── views/
                ├── dashboard/               # 首页 / 工作台
                ├── system/                  # user / role / menu 管理页面
                └── _core/                   # 登录、异常页、关于与个人中心视图
```

## 快速开始

### 1. 环境准备

| 依赖 | 最低要求 | 备注 |
| --- | --- | --- |
| **JDK** | `25` | 工程 `<maven.compiler.release>25</maven.compiler.release>` |
| **Maven** | `3.9+` | 一体构建需能访问 Maven 中央仓库及 npm 源 |
| **MySQL** | `5.7+` / `8.x` | 默认库名 `seed_admin` |
| **Redis** | `6.x+` / `7.x` | 用于 Sa-Token 会话与权限状态存储 |
| **Node.js & pnpm** | `24.16.0` & `11.16.0` | **仅独立开发前端时需要本机预装**；Maven 一体构建会自动下载到 `target` |

### 2. 初始化数据库

执行 [sql/seed.sql](./sql/seed.sql) 创建库表与初始化数据：

> [!WARNING]
> `sql/seed.sql` 包含 `DROP TABLE IF EXISTS`，**仅限全新空库或明确需要重置的环境使用**；已有业务数据的数据库严禁直接全量执行。

初始化后内置角色与管理员账号：

- **默认管理员**：`admin` / `123456`（仅用于首次本地登录验证，生产部署前请立即修改密码）
- **内置角色**：
  - `super`：超级管理员，持有全部菜单与 `*:*:*` 接口权限（不通过管理接口分配）
  - `common`：普通角色，默认仅可访问工作台与个人中心

### 3. 本地配置

仓库不包含真实数据库与 Redis 凭据，默认 [application.yml](./seed-server/src/main/resources/application.yml) 通过环境变量占位符读取连接信息（未配置密码时将无法连接受密码保护的数据库）。

方式一：使用本地配置文件（推荐本地 IDE 开发）

```bash
# macOS / Linux
cp seed-server/src/main/resources/application-example.yml config/application-local.yml
```

```powershell
# Windows PowerShell
Copy-Item seed-server\src\main\resources\application-example.yml config\application-local.yml
```

编辑 `config/application-local.yml` 中的 MySQL 与 Redis 连接信息，并在 IDEA 运行配置中设置 `Active profiles` 为 `local`（或启动时传入 `--spring.profiles.active=local`）。该文件已加入 `.gitignore`，不会提交到 Git。

方式二：使用环境变量（推荐容器 / 服务端部署）

直接注入以下环境变量覆盖默认值：

`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_DB`

### 4. 一体打包与运行

在项目根目录执行 Maven 构建（`frontend` profile 在未传 `-Dskip.frontend=true` 时**默认自动激活**）：

```bash
mvn clean package
```

构建完成后，前端静态资源已内嵌至 `seed-server/target/seed-server-1.0.0-SNAPSHOT.jar` 中，直接启动：

```bash
java -jar seed-server/target/seed-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

浏览器访问：`http://localhost:8080`

## 开发与构建

### 前后端热更新联调（日常开发推荐）

1. **启动后端**：在 IDEA 中启用 `local` profile 并运行 `io.github.yak33.SeedApplication`（默认监听 `8080`）。
2. **启动前端**：

```bash
cd frontend
pnpm install
pnpm dev:naive
```

前端默认监听 `5888` 端口（见 `apps/web-naive/.env.development`），访问 `http://localhost:5888`，所有 `/api/**` 请求会自动代理到 `http://localhost:8080`。

### 常用命令速查

```bash
# ---------- 后端 ----------
# 一体打包（自动安装 Node/pnpm + 构建前端 + 打入 JAR）
mvn clean package

# 跳过前端构建，仅编译打包后端（适合仅调整 Java 代码时使用，此时 JAR 不含页面）
mvn -Dskip.frontend=true clean package

# 后端编译及测试生命周期
mvn -Dskip.frontend=true test

# ---------- 前端（在 frontend/ 目录下执行） ----------
# 类型检查（仅应用）
pnpm -F @vben/web-naive typecheck

# 完整校验（循环依赖、包依赖、TS 类型、拼写检查）
pnpm run check

# 前端单元测试（Vitest）
pnpm run test:unit

# 单独构建 web-naive 生产产物
pnpm run build:naive
```

## Docker 部署参考

> [!NOTE]
> 仓库当前尚未内置根目录 `Dockerfile` 与 `docker-compose.yml`（`frontend/scripts/deploy/` 下为 Vben 上游独立前端 Nginx 镜像脚本）。以下提供适配本项目**单 JAR 一体化形态**的部署参考模板，可直接落盘到根目录或服务端按需调整。

<details>
<summary>点击展开单 JAR Dockerfile 与 docker-compose.yml 参考模板</summary>

1. 宿主机先执行 `mvn clean package` 生成 `seed-server/target/seed-server-1.0.0-SNAPSHOT.jar`（或改用多阶段镜像构建）。
2. 参考 `Dockerfile`：

```dockerfile
FROM eclipse-temurin:25-jre-alpine
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m"
WORKDIR /app
COPY seed-server/target/seed-server-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

3. 参考 `docker-compose.yml`（环境变量与 [application.yml](./seed-server/src/main/resources/application.yml) 占位符一一对应）：

```yaml
services:
  seed-admin:
    build: .
    container_name: seed-admin
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: seed_admin
      DB_USER: seed_admin
      DB_PASSWORD: ${DB_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_DB: 0
    # 若数据库或 Redis 使用了单独的 Spring 配置（如 Redis 密码），可额外挂载外部 config 目录：
    # volumes:
    #   - ./config:/app/config:ro
```

</details>

## 架构与接口说明

### 1. API 与鉴权概览

`SaTokenConfig` 默认拦截 `/api/**`（仅放行 `/api/auth/login`），所有静态资源与前端页面路由不进入接口鉴权链：

| 模块 | 接口前缀 / 路径 | 鉴权要求 | 说明 |
| --- | --- | --- | --- |
| 认证 | `POST /api/auth/login` | 匿名放行 | 账号密码登录，签发 UUID Token |
| 认证 | `POST /api/auth/logout`、`GET /api/auth/codes` | `@SaCheckLogin` | 退出登录、获取当前用户权限码列表 |
| 用户上下文 | `GET /api/user/info` | `@SaCheckLogin` | 实时读库返回基础信息 + 会话角色 + 动态首页路径 `homePath` |
| 动态路由 | `GET /api/menu/all` | `@SaCheckLogin` | 下发当前用户可访问的 `RouterVo` 树 |
| 用户管理 | `/api/system/user/**` | `system:user:list/add/edit/remove` | 分页列表、可分配角色选项、CRUD |
| 角色管理 | `/api/system/role/**` | `system:role:list/add/edit/remove` | 分页列表、详情与菜单树回显、CRUD |
| 菜单管理 | `GET /api/system/menu/list` | `system:menu:list` | 菜单树查询 |
| 菜单写操作 | `POST/PUT /api/system/menu`、`DELETE /api/system/menu/{menuId}` | `@SaCheckRole("super")` + `system:menu:add/edit/remove` | 仅超管可变更菜单结构 |

### 2. RBAC 表关系与菜单节点约定

```text
sys_user ──< sys_user_role >── sys_role ──< sys_role_menu >── sys_menu
```

- `M`（目录）：`path` 必须以 `/` 开头，参与动态路由组装（映射前端 `BasicLayout` 并自动计算首个可见子路由作为 `redirect`）。
- `C`（页面）：`path` 为相对父目录路径，`component` 为相对 `src/views` 的组件路径（如 `/system/user/index`）；建议后端 `route_name` 与前端页面 `defineOptions({ name: '...' })` 保持一致，确保标签页与 `keep-alive` 正常匹配。
- `F`（按钮）：仅维护 `perms` 权限标识（如 `system:user:add`），不参与路由生成；角色保存菜单授权时，后端会自动递归补齐选中节点的上级目录 ID。

### 3. 关键工程实现细节

- **Boot 4 Jackson 3 与 Jackson 2 并行隔离**：Spring Boot 4 HTTP 层默认使用 Jackson 3（`tools.jackson`），由 `JacksonConfig` 统一配置 `LocalDateTime` 的 `yyyy-MM-dd HH:mm:ss` 格式；同时根目录 `pom.xml` 将 `jackson2` 锁定在 `2.18.12`、将 `redisson` 锁定在 `4.8.0`，并在 [sa-json-type.list](./seed-server/src/main/resources/META-INF/satoken/sa-json-type.list) 注册 `LoginUser` 白名单，确保 Sa-Token Redis 会话序列化稳定可靠。
- **构建防白屏配置**：[vite.config.ts](./frontend/apps/web-naive/vite.config.ts) 已显式设置 `vxeTableLazyImport: false`，规避 `vite-plugin-lazy-import` 重写导入导致生产构建路由上下文丢失的白屏问题。
- **路由模式说明**：`apps/web-naive/.env.production` 默认使用 `VITE_ROUTER_HISTORY=hash`，若需切换为 `history` 模式，后端 `SpaForwardConfig` 已做好无扩展名路径自动回退 `static/index.html` 的支持。
- **新增业务模块四步走**：
  1. 在 `seed-server` 下新建业务包并创建 `Controller / Service / Mapper / Entity / BO / VO`（实体继承 `BaseEntity`）；
  2. 在 `SeedApplication` 的 `@MapperScan` 中追加新 Mapper 包路径；
  3. 业务表参考 RBAC 风格预留 `VARCHAR(36)` 主键与审计字段，接口补充 `@SaCheckPermission`；
  4. 在「菜单管理」中新增目录/页面/按钮节点并为对应角色授权，前端 `views/` 下放置对应页面组件即可生效。

### 4. 已知范围说明

- 个人中心页面（`/_core/profile`）目前复用 Vben 上游示例交互，头像更新、个人资料修改与自助改密后端接口未内置，可按业务需要补充。
- 代码内预置测试账号及示例配置仅适用于本地研发验证，生产部署务必替换默认密码并通过环境变量或外部安全挂载注入连接凭据。

## 开源致谢

- `frontend/` 基于 [Vue Vben Admin](https://github.com/vbenjs/vue-vben-admin) (`5.7.0`) 精简改造而来（仅保留 `web-naive` 应用），前端核心框架及其附属包遵循其原有 [MIT License](./frontend/LICENSE)。
