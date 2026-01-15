项目概览

后端：Spring Boot（Java 21），位于 `station-management` 目录。
前端：Vite + Vue，位于 `web` 目录。

快速启动
- 后端：进入 `station-management`，执行 `mvn spring-boot:run`，访问 `http://localhost:8899/`。
- 前端：进入 `web`，执行 `npm install`，再运行 `npm run dev`。

环境要求
- JDK `21`（后端）
- Node.js `18+`（前端）

说明
- 当前后端未配置数据库，已禁用数据源自动配置，便于无数据库启动。
- 后续如需接入数据库，请在 `station-management/src/main/resources/application.yaml` 中添加配置。

目录结构（简要）
- `station-management/`：后端代码与构建输出
- `web/`：前端工程与静态资源