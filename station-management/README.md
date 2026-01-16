后端说明（station-management）

技术栈
- `Java 21` + `Spring Boot`
- `Spring Web`（REST API）
- `Spring Data JPA` / `Hibernate`（数据访问）
- 数据库：`MySQL 8`

主要特性
- 用户管理接口：普通用户与管理员的创建、查询、更新、删除、状态变更；
- 登录接口（开发版）：支持手机号+密码登录；
- 跨域支持：已开启本地开发跨域（允许 `http://localhost:*`）。

工程结构（简要）
- `src/main/java/com/campus/station/`
  - `CampusStationManagementSystemApplication.java`：应用入口
  - `api/`：REST 控制器（如 `SysUserController`）
  - `service/`：业务服务与实现
  - `repository/`：数据访问接口（JPA）
  - `model/`：实体定义（如 `SysUser`）
  - `config/`：通用配置（如 `WebConfig` 的 CORS）
- `src/main/resources/application.yml`：后端配置文件

本地启动
1) 准备数据库（默认使用 `wuliu` 库）
- 创建数据库示例：`CREATE DATABASE wuliu CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;`
- 在 `application.yml` 中配置你的数据库连接：
  - `spring.datasource.url`（例如：`jdbc:mysql://localhost:3306/wuliu?...`）
  - `spring.datasource.username` / `spring.datasource.password`
  - 开发期可将 `spring.jpa.hibernate.ddl-auto` 设为 `update` 以自动建表

2) 运行后端服务
- 在项目根目录进入 `station-management`：
  - 使用 Maven：`mvn spring-boot:run`
  - 或使用包装脚本：`mvnw.cmd spring-boot:run`
- 默认端口：`http://localhost:8899`

3) 接口约定（示例）
- 创建普通用户：`POST /api/sysUser`（仅允许 `USER` 角色）
- 创建管理员：`POST /api/sysUser/admin`（强制 `ADMIN` 角色）
- 手机号登录：`POST /api/sysUser/loginByPhone`（入参：`{phone, password}`）
- 其他：查询、更新、删除、分页列表、状态变更等。

联调与跨域
- 已开启 CORS：允许 `http://localhost:*`；前端开发可通过 Vite 代理 `/api` 指向后端端口 `8899`。

注意事项
- 当前密码未加密，仅用于开发验证；如需安全加固可接入 `BCrypt`/`JWT` 等。
- 若修改端口/库名，请同步更新 `application.yml` 与前端代理配置。

Swagger / OpenAPI 接口文档
- 依赖：已在 `pom.xml` 中集成 `springdoc-openapi-starter-webmvc-ui`（版本 `3.0.1`），随应用自动生效。
- 启动方式：与正常后端启动一致（参考上文“本地启动”）。
- 文档访问地址：
  - Swagger UI：`http://localhost:8899/swagger-ui/index.html`
  - OpenAPI JSON：`http://localhost:8899/v3/api-docs`
- 使用说明：
  - 打开 Swagger UI 后，可在左侧找到 `SysUser` 分组，展开查看各接口；
  - 点击某个接口中的 “Try it out” 按钮，填写请求参数即可在线调试；
  - 前端联调时，可以 Swagger UI 展示为参考，确认 URL、方法与请求体结构。
