项目概览

这是一个校园驿站管理系统（Campus Station Management System）。
- 后端：Spring Boot（Java 21），位于 `station-management` 目录。
- 前端：Vite + Vue 3，位于 `web` 目录。
- 数据库：MySQL 8。

功能目标（规划）
- 站点管理：站点档案、状态维护、容量指标。
- 包裹管理：入库、出库、签收、滞留处理。
- 工单/任务：异常处理、人工干预、值班任务分配。
- 角色与权限：管理员、站点人员、学生用户。
- 报表与统计：业务数据统计与导出。

快速启动
- 后端：进入 `station-management`，执行 `mvn spring-boot:run`，访问 `http://localhost:8899/`。
- 前端：进入 `web`，执行 `npm install`，再运行 `npm run dev`（默认 `http://localhost:5173`）。

环境要求
- JDK `21`（后端）
- Node.js `18+`（前端）
- MySQL `8.x`

后端配置
- 配置文件路径：`station-management/src/main/resources/application.yml`
- 关键配置项：
  - `server.port: 8899`（后端服务端口）
  - `spring.application.name: CampusStationManagementSystem`
  - `spring.datasource.url: jdbc:mysql://localhost:3306/wuliu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8`
  - `spring.datasource.username: <你的用户名>`（示例为 `root`）
  - `spring.datasource.password: <你的密码>`（示例为 `123456`）
  - `spring.jpa.hibernate.ddl-auto: none`（生产建议 `none`；开发可用 `update`）
  - `spring.jpa.properties.hibernate.dialect: org.hibernate.dialect.MySQL8Dialect`

数据库准备
- 创建数据库：
  - `CREATE DATABASE wuliu CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;`
- 如果你使用其他库名，请替换 `spring.datasource.url` 中的库名。
- 建表方式（二选一）：
  - 迁移脚本（推荐）：引入 Flyway，在 `src/main/resources/db/migration` 编写 SQL（如 `V1__init.sql`），启动自动执行。
  - JPA 自动建表（开发期临时）：将 `ddl-auto` 设为 `update` 或 `create`，由实体生成表结构。

跨域与联调
- 已在后端开启 CORS，允许来自 `http://localhost:5173` 的跨域访问。
- 前端通过 REST API 与后端交互，统一响应结构为：
  - `{ "success": true|false, "message": "...", "data": <业务数据>, "timestamp": "..." }`

目录结构（简要）
- `station-management/`：后端代码与构建输出
  - `src/main/java/com/campus/station/`：后端代码（入口类、配置、异常、通用工具等）
  - `src/main/resources/application.yml`：后端配置文件（端口、数据源、JPA）
- `web/`：前端工程与静态资源

常见问题排查
- 端口占用：调整 `server.port` 或释放占用端口。
- 数据库连接失败：检查 `spring.datasource.url/username/password`，确认 MySQL 已启动且库存在。
- 配置冲突：避免同时使用 `application.yml` 与 `application.yaml`，仅保留一个配置文件。

后续规划
- 按业务需求逐步补齐领域模型与接口（站点、包裹、工单、用户/角色等）。
- 引入认证与权限控制（如 Spring Security + JWT）。
- 完善日志、监控与异常告警。