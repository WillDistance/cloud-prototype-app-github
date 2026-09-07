# 云相册 App 后台多阶段开发计划

- **计划版本**：v1
- **需求依据**：`requirement-document.md`
- **数据库依据**：`cloud-album-database-schema.sql`
- **技术栈依据**：`backend-technology-stack.md`
- **后台工程目录**：`D:\AllWorkspace\hermesWorkspace\cloud-prototype-app\spring-boot-cloud-prototype-app`
- **资料与数据库设计目录**：`D:\AllWorkspace\hermesWorkspace\cloud-prototype-app\app-resource`
- **执行方式**：严格按阶段顺序开发；每阶段代码、测试、Postman 集合和必要文档完成并验证后，使用中文 Git 提交，再进入下一阶段。

## 1. 开发目标

建设云相册 App 后台，支持：

1. 邮箱注册、验证码、登录、忘记密码和 JWT 登录态。
2. 用户 IANA 时区和中、英、德三种语言偏好。
3. 用户与云端相机永久一对一绑定。
4. 仅允许用户已绑定设备申请 OSS 临时上传链接并上传照片；用户 App、未绑定设备和其他设备禁止上传。
5. 原图、缩略图和预览图完整处理后才发布照片。
6. 相册筛选、分页、详情、原图下载和永久删除。
7. 存储用量、设备赠送权益、购买权益叠加和套餐查询。
8. 乒乓支付订单、异步回调验签和幂等开通权益。
9. 权益到期、容量重算、超额照片自动清理和多语言通知。

## 2. 强制开发约定

### 2.1 接口与分层

- 对外接口只使用 `GET` 和 `POST`。
- 不使用 `@PathVariable`。
- `@GetMapping` / `@PostMapping` 路径必须与 Controller 方法名一致。
- Controller 只负责 HTTP 参数绑定和调用 Service，不进行 DTO、Entity、VO 转换或业务处理。
- POST 请求实体放在 `pojo/dto`。
- 数据库实体放在 `pojo/entity`。
- 前端返回实体放在 `pojo/vo`。
- DTO → Entity、Entity → VO、事务和业务逻辑定义在 Service，并在 ServiceImpl 中实现。
- Controller 返回值统一为 `CommonResult<T>`，成功使用 `CommonResult.success(...)`。
- 每个 Controller 在工程根目录维护同名 `<Controller类名>.postman_collection.json`，覆盖全部接口及示例参数。
- 新建类和方法按现有项目风格添加 JavaDoc：`@author yanlei` 和开发日期。

### 2.2 数据库

- MySQL 数据库：`localhost:3306/app`。
- 当前本地账号由 `application.yml` 配置；开发文档和 Git 提交不得新增或扩散明文密码。
- 数据库表不使用外键，关系一致性由 Service 事务、唯一约束和查询校验保证。
- 所有业务时间保存为 UTC `datetime(3)`。
- 金额以人民币分保存，容量和文件大小以字节保存。
- 按用户自然日查询时，应用将用户时区边界转换为 UTC 左闭右开区间 `[startUtc, endUtc)`。
- 如果开发中确认字段、索引或约束必须调整：
  1. 先修改 `app-resource/cloud-album-database-schema.sql`；
  2. 提供增量迁移 SQL 或明确重建表步骤；
  3. 通知用户更新本地数据库，或在取得执行范围后连接本地 MySQL 执行；
  4. 读取数据库元数据验证目标表结构；
  5. 数据库结构确认后再继续依赖该字段的代码。
- 不直接修改既有生产数据；本地开发数据操作必须可回滚或可重建。

### 2.3 Redis

- Redis 用于验证码短期状态、频控、幂等锁、缓存和任务互斥。
- Redis 不作为账号、订单、权益、照片等最终业务事实的唯一存储。
- Redis 键必须统一前缀、包含业务主体，并设置 TTL。
- 测试不得依赖测试前遗留的 Redis 数据；测试前后清理专用测试键。

### 2.4 测试与提交门禁

每个阶段必须满足：

1. 先编写能体现目标行为的测试，确认测试因功能尚未实现或错误而失败。
2. 完成最小实现，使阶段测试通过。
3. 至少包含：
   - Service 单元测试：业务规则、状态转换、异常分支；
   - Controller MockMvc 测试：HTTP 方法、准确路径、参数绑定、`CommonResult`；
   - Mapper/数据库集成测试：阶段包含持久化或事务时必须覆盖；
   - Redis/OSS/支付边界测试：使用隔离环境、Mock 或测试替身，不调用真实付费流程。
4. 运行阶段定向测试。
5. 运行完整 `mvn test`，确认 Surefire 实际执行测试且测试数不为 0。
6. 检查 `git diff --check` 和 Git 状态，仅保留本阶段预期业务文件。
7. 更新对应 Postman Collection。
8. 测试全部通过后，以中文提交说明提交代码。
9. 提交成功并确认工作区只剩明确忽略项后，才能开始下一阶段。

> 当前环境基线检查发现直接执行 `mvn test` 报错：`Could not find or load main class org.codehaus.plexus.classworlds.launcher.Launcher`（无法找到或加载 Maven Plexus 启动类）。阶段 0 必须先修复或改用可验证的 Maven Wrapper，再开始业务开发。

## 3. 开发阶段总览

| 阶段 | 主题 | 主要交付 | 依赖 |
|---|---|---|---|
| 0 | 工程基线与环境修复 | 可重复构建、测试基线、配置分层 | 无 |
| 1 | 数据访问与公共基础设施 | 14 表 Entity/Mapper、异常、UTC、分页基础 | 阶段 0 |
| 2 | 验证码与用户注册 | 验证码、注册、邮箱唯一、时区校验 | 阶段 1 |
| 3 | 登录、JWT 与用户上下文 | 登录、JWT 鉴权、公开接口白名单 | 阶段 2 |
| 4 | 用户设置与本地化基础 | 语言偏好查询/更新、错误码协议 | 阶段 3 |
| 5 | 设备永久绑定 | 设备查询、永久一对一绑定、赠送权益 | 阶段 3、4 |
| 6 | 平台配置、套餐与存储概览 | 配置读取、套餐列表、容量计算 | 阶段 5 |
| 7 | 绑定设备上传会话与配额预留 | 设备鉴权、上传链接、容量预留 | 阶段 5、6 |
| 8 | OSS 回调、派生图处理与照片发布 | 三文件完成门禁、失败重试 | 阶段 7 |
| 9 | 相册查询、详情与下载 | 筛选、游标分页、签名下载链接 | 阶段 8 |
| 10 | 用户永久删除照片 | 文件组删除、用量回退、失败补偿 | 阶段 9 |
| 11 | 订单创建与支付发起 | 套餐快照、订单幂等、支付参数 | 阶段 6、3 |
| 12 | 支付回调与购买权益 | 验签、金额校验、幂等开通权益 | 阶段 11 |
| 13 | 权益到期与自动清理 | 到期任务、容量重算、最早照片清理 | 阶段 10、12 |
| 14 | 多语言通知 | 验证码、到期、清理、支付通知 | 阶段 4、12、13 |
| 15 | 安全、并发与系统验收 | 全链路回归、并发、性能和发布说明 | 全部阶段 |

## 4. 阶段 0：工程基线、契约冻结与环境修复

### 目标

建立可重复构建、可运行、可测试的开发基线，修复现有 Maven 启动问题，并在业务编码前冻结数据库和接口核心契约。

### 开发任务

- 检查 Java 21 和 Maven 安装来源、`MAVEN_HOME`、`PATH` 及本地仓库配置。
- 优先在工程中加入并验证 Maven Wrapper：`mvnw`、`mvnw.cmd`、`.mvn/wrapper/`。
- 补充 `spring-boot-maven-plugin` 和必要的 Maven 插件版本管理。
- 检查 Spring Boot 3.4.12 与 Druid 1.1.16、Log4j 1.2.17 的兼容性：
  - Druid 1.1.16 较旧，应通过实际启动测试确认 Jakarta/Spring Boot 3 兼容；不兼容时先提交依赖升级方案。
  - 不直接使用 Log4j 1.x 处理应用日志；明确 Spring Boot 默认 SLF4J/Logback 链路，避免冲突和已知安全风险。
- 建立 `application.yml`、`application-local.yml`、`application-test.yml` 分层。
- 敏感配置改为环境变量占位；不在后续新增文件中写入密码、JWT 密钥、OSS 密钥或支付密钥。
- 将 Jackson 和 JVM/数据库连接统一为 UTC，输出具体时间点时使用 ISO 8601 UTC 格式。
- 检查 MyBatis-Plus 审计字段自动填充，修正 `LocalDateTime.now()` 依赖服务器默认时区的问题。
- 建立统一测试包结构和测试命名规则。
- 核对并冻结当前 14 张表的字段、索引、唯一约束、CHECK 约束和枚举中文含义。
- 冻结以下核心状态机及合法迁移，后续变更必须先更新需求和 DDL：
  - 验证码：`PENDING → VERIFIED/EXPIRED/INVALIDATED`；
  - 设备：`UNBOUND → BOUND`，禁用时进入 `DISABLED`，不提供解绑后重绑；
  - 上传会话：`URL_ISSUED → ORIGINAL_UPLOADED → PROCESSING → COMPLETED/FAILED/EXPIRED`；
  - 照片与文件：`AVAILABLE → DELETE_PENDING → DELETE_FAILED` 或物理删除完成；
  - 订单：`PENDING → PAID/CLOSED/FAILED`，退款能力确认后才允许 `REFUNDED`；
  - 权益：`ACTIVE → EXPIRED/REVOKED`。
- 冻结 JWT 主体字段、设备鉴权格式、OSS 对象 Key 规则、接口错误码和 `CommonResult` 输出契约。
- 为计划中的 Controller 创建 Postman Collection 骨架，后续阶段逐项补全并实测。

### 测试

- 应用上下文启动测试。
- UTC 序列化和审计字段 UTC 填充测试。
- MySQL 数据源连通性测试。
- Redis `PING`、写入、TTL、读取和删除测试。
- 现有测试完整执行且测试数不为 0。

### 验收标准

- `mvnw.cmd test` 或可靠的 `mvn test` 在本机成功。
- Spring Boot 能使用测试配置启动。
- MySQL 和 Redis 测试连接通过。
- 14 张表、核心状态机、接口清单及鉴权边界形成明确基线。
- `t_photo_file.delete_reason` 冲突已有明确选定方案和数据库更新安排。
- 无业务代码变更之外的 `.idea`、`target` 文件进入提交。

### 建议提交说明

`完善后台工程构建与测试基线`

## 5. 阶段 1：数据访问与公共基础设施

### 目标

按数据库设计建立持久化模型和通用后台能力。

### 开发任务

- 对当前 14 张表建立 Entity 和 Mapper：
  - `t_user`
  - `t_email_verification_code`
  - `t_device`
  - `t_device_binding`
  - `t_platform_config`
  - `t_storage_plan`
  - `t_payment_order`
  - `t_payment_callback`
  - `t_user_storage_account`
  - `t_storage_entitlement`
  - `t_upload_session`
  - `t_photo`
  - `t_photo_file`
  - `t_notification`
- 所有 Entity 继承或复用 `BaseField`，匹配 `create_by/create_time/update_by/update_time`。
- 对 JSON 字段配置 MyBatis-Plus TypeHandler。
- 建立数据库枚举对应的 Java 枚举，禁止业务代码散落魔法字符串。
- 建立统一业务异常、参数异常、鉴权异常和全局异常处理。
- 扩展 `ErrorCodeEnum`，为认证、设备、存储、照片、支付和系统错误预留稳定错误码。
- 建立分页游标模型和业务编号生成器。
- 建立时区工具：IANA 校验、用户自然日到 UTC 区间、自然月/自然年到期时间计算。
- 建立数据库无外键前提下的关联存在性校验规范。

### 测试

- 14 个 Entity 表名与字段映射测试。
- Mapper 基础插入、查询、更新测试。
- JSON 字段读写测试。
- 审计字段自动填充测试。
- UTC、夏令时、月末加月、闰年加年测试。
- 全局异常到 `CommonResult` 的映射测试。

### 验收标准

- DDL 与 Entity 字段一一对应。
- 无 SQL 外键依赖。
- 错误码稳定，不直接把数据库异常或固定中文消息当作 App 最终文案。

### 建议提交说明

`建立业务数据模型与公共基础设施`

## 6. 阶段 2：邮箱验证码与用户注册

### 目标

实现注册验证码申请、验证和账号创建。

### 建议 Controller

`AuthController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/auth/sendRegisterCode` | 发送注册验证码 |
| POST | `/api/auth/verifyRegisterCode` | 验证注册验证码 |
| POST | `/api/auth/register` | 使用已验证验证码注册账号 |

### 开发任务

- 规范化邮箱并保证唯一。
- 校验 `X-Time-Zone` 为有效 IANA 时区，缺失或非法时拒绝注册。
- 校验并规范化 `Accept-Language`，不支持时使用英语。
- 验证码仅保存哈希；Redis 保存短期验证状态、5 分钟 TTL、发送频控和尝试次数。
- MySQL 保存验证码业务记录和最终状态。
- 注册时原子消费验证码并创建用户，防止并发重复注册和验证码复用。
- 使用可靠密码哈希，不保存明文密码。

### 测试

- 正常发送、验证、注册流程。
- 邮箱格式错误、邮箱已存在。
- 时区缺失、非法时区和夏令时地区合法时区。
- 验证码错误、过期、重复消费、尝试次数超限。
- 并发使用同一验证码只能成功一次。
- MockMvc 校验接口路径、请求头、DTO 校验和 `CommonResult`。

### Postman

更新 `AuthController.postman_collection.json`，加入注册全流程和错误示例。

### 建议提交说明

`实现邮箱验证码与用户注册功能`

## 7. 阶段 3：登录、JWT、忘记密码与用户上下文

### 目标

实现完整认证闭环和统一用户端鉴权。

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/auth/login` | 邮箱密码登录并签发 JWT |
| POST | `/api/auth/sendResetPasswordCode` | 发送重置密码验证码 |
| POST | `/api/auth/verifyResetPasswordCode` | 验证重置密码验证码 |
| POST | `/api/auth/resetPassword` | 重置密码 |
| POST | `/api/auth/logout` | 客户端退出登录；服务端按最终令牌策略处理 |

### 开发任务

- JWT 载荷至少包含用户 ID、用户时区、签发时间、过期时间和令牌标识。
- 建立 JWT 拦截器和公开接口白名单。
- 从验证后的 JWT 建立请求级用户上下文，替换当前固定返回 `12345` 的 `UserLoginInfoUtil`。
- 禁止 Controller DTO 和查询参数覆盖当前用户 ID。
- 统一处理 JWT 缺失、签名错误和过期。
- 忘记密码时原子消费验证码并更新密码哈希及 `password_update_time`。
- 明确是否需要 Redis 令牌黑名单：若要求退出立即失效，则按 JWT 剩余 TTL 存储黑名单；否则记录为无状态 JWT 限制。

### 测试

- 登录成功、密码错误、账号锁定/禁用。
- JWT 合法、缺失、伪造、过期。
- JWT 中时区来源正确。
- 用户上下文不能被请求参数伪造。
- 重置密码验证码错误、过期、复用；重置后旧密码失败、新密码成功。

### Postman

补充登录、重置密码和带 JWT 的请求示例。

### 建议提交说明

`实现JWT登录鉴权与密码重置`

## 8. 阶段 4：用户设置与本地化基础

### 建议 Controller

`UserController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| GET | `/api/user/getProfile` | 获取当前用户资料 |
| POST | `/api/user/updateLanguage` | 更新服务端通知语言偏好 |

### 开发任务

- 当前用户资料返回邮箱、IANA 时区和语言偏好。
- 语言只允许 `zh-CN/en/de`，不支持值回退英语或按接口规则拒绝。
- 后端返回稳定业务错误码；App 负责本地文案映射。
- 建立通知模板资源组织规范，为后续通知阶段预留接口。

### 测试

- 用户只能读取和修改自身资料。
- 语言切换正确持久化。
- 非法语言处理和未登录访问。

### Postman

创建 `UserController.postman_collection.json`。

### 建议提交说明

`实现用户资料与语言偏好设置`

## 9. 阶段 5：设备永久绑定与赠送权益

### 建议 Controller

`DeviceController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| GET | `/api/device/getMyDevice` | 查询当前用户绑定设备 |
| POST | `/api/device/bindDevice` | 永久绑定设备 |

### 开发任务

- 校验设备 ID 格式、设备存在性、密码哈希。
- 一次事务内完成：
  - 锁定用户和设备相关记录；
  - 校验用户未绑定其他设备；
  - 校验设备从未绑定其他用户；
  - 创建永久绑定关系；
  - 更新设备状态；
  - 读取当前平台配置；
  - 按用户 IANA 时区计算赠送权益到期时间并创建唯一权益；
  - 创建用户存储账户。
- 不实现解绑、换绑接口。
- 接口不返回设备密码或密码哈希。

### 测试

- 绑定成功及赠送权益正确创建。
- 设备格式错误、不存在、密码错误、已绑定。
- 用户已有设备。
- 并发绑定同一设备或同一用户只能有一个成功。
- 绑定失败时所有数据回滚。

### Postman

创建 `DeviceController.postman_collection.json`。

### 建议提交说明

`实现设备永久绑定与赠送存储权益`

## 10. 阶段 6：平台配置、套餐与存储概览

### 建议 Controller

- `StoragePlanController`
- `StorageController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| GET | `/api/storagePlan/listActivePlans` | 查询生效套餐 |
| GET | `/api/storage/getOverview` | 查询已用、预留、有效总容量和剩余容量 |
| GET | `/api/storage/listEntitlements` | 查询权益明细和最近到期权益 |

### 开发任务

- 只读取当前生效平台配置和套餐。
- 有效容量实时按 `effective_time <= nowUtc < expire_time` 汇总，不能只信状态列。
- 计算 `remaining = total - used - reserved`，最小为 0。
- 返回权益来源、名称快照、容量、生效和到期时间。
- 配置缓存可使用 Redis，但数据库为最终来源。

### 测试

- 无权益、单权益、多权益叠加和到期边界。
- 赠送和购买权益同时存在。
- 套餐下架/归档不展示。
- Redis 缓存命中、失效和数据库回源。

### Postman

分别创建对应 Controller 的集合。

### 建议提交说明

`实现存储套餐查询与容量概览`

## 11. 阶段 7：绑定设备上传会话与配额预留

### 建议 Controller

`DeviceUploadController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/deviceUpload/createUploadSession` | 绑定设备申请 OSS 临时上传链接 |
| GET | `/api/deviceUpload/getUploadSessionStatus` | 绑定设备查询自身上传会话处理状态 |

### 开发任务

- 该接口使用设备 ID 与设备密码鉴权，不接受用户 JWT 作为上传凭据。
- 不接收用户 ID；用户 ID、时区和绑定 ID只能由设备永久绑定关系取得。
- 校验设备状态、唯一绑定、文件扩展名、MIME、声明大小和平台上限。
- 通过用户存储账户行锁或乐观锁执行：
  `used_bytes + reserved_bytes + declared_size <= active_capacity`。
- 校验成功后增加 `reserved_bytes`，创建 `t_upload_session`。
- 服务端生成不可预测对象 Key 和短时、最小权限 OSS 上传链接。
- 防止用户 App、未绑定设备和其他设备申请或复用上传链接。
- 为上传会话过期释放预留容量提供定时扫描入口。
- 上传状态查询仅允许原申请设备查询自身会话，不返回其他用户、其他设备或 OSS 内部敏感信息。

### 测试

- 绑定设备成功申请。
- App JWT 调用被拒绝、未绑定设备、密码错误、禁用设备被拒绝。
- 文件类型、大小、容量不足错误。
- 并发申请不能超卖容量。
- OSS SDK 使用 Mock，验证对象路径、权限和过期时间。
- 会话创建失败时预留容量回滚。
- 上传状态查询的设备所有权隔离、会话不存在和跨设备访问拒绝。

### Postman

创建 `DeviceUploadController.postman_collection.json`，明确设备鉴权请求头。

### 建议提交说明

`实现绑定设备上传会话与容量预留`

## 12. 阶段 8：OSS 回调、派生图与照片发布

### 建议 Controller

`OssCallbackController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/ossCallback/uploadCompleted` | 接收并校验 OSS 上传完成回调 |

### 开发任务

- 校验 OSS 回调来源、签名、对象 Key、上传会话和实际文件元数据。
- 实际大小超过声明值或平台上限时拒绝并清理对象。
- 状态机：`URL_ISSUED → ORIGINAL_UPLOADED → PROCESSING → COMPLETED/FAILED`。
- 后台下载原图并生成 480px 缩略图和 1920px 预览图，上传至 OSS。
- 在 `t_photo_file` 写入 ORIGINAL、THUMBNAIL、PREVIEW 三条记录。
- 三种文件全部可用后，在同一发布事务中：
  - 创建 `t_photo`；
  - 上传会话转为完成；
  - `reserved_bytes` 转为 `used_bytes`；
  - App 才能查询到照片。
- 失败时释放或保持可重试的预留策略，清理孤儿对象。

### 必须先确认的数据库问题

当前 `t_photo_file.delete_reason` 定义为 `NOT NULL`，且只允许：

- `USER_MANUAL`
- `ENTITLEMENT_EXPIRED`

但新创建的可用文件尚未发生删除，没有合法值可写。开发本阶段前必须二选一并更新 DDL：

1. **已确认方案**：改为 `DEFAULT NULL`，并允许未删除时为空；真正进入删除流程时再写原因。
2. 增加 `NOT_DELETED=未删除` 枚举值，但字段语义不如可空方案自然。

在用户确认并更新数据库前，不开始依赖该字段插入照片文件。

### 测试

- 合法回调成功发布照片。
- 回调重复到达不重复生成照片或增加用量。
- 对象 Key 不匹配、签名失败、实际大小异常。
- 任一派生图失败时照片不可见。
- 三文件成功后照片一次性可见。
- `reserved_bytes` 与 `used_bytes` 转换正确。
- 重试不会产生重复文件记录。

### Postman

创建 `OssCallbackController.postman_collection.json`，回调签名字段使用测试值。

### 建议提交说明

`实现照片派生处理与原子发布`

## 13. 阶段 9：相册查询、详情与原图下载

### 建议 Controller

`PhotoController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| GET | `/api/photo/listPhotos` | 按时间筛选并游标分页查询相册 |
| GET | `/api/photo/getPhotoDetail` | 查询照片详情 |
| GET | `/api/photo/getOriginalDownloadUrl` | 获取原图短时下载链接 |

### 开发任务

- 当前用户 ID 和时区只能取自 JWT。
- 支持今天、最近 7 天、最近 1 个月和全部。
- 应用层计算用户自然时间边界并转换为 UTC。
- 使用 `(uploaded_time, id)` 稳定游标分页，不使用大 OFFSET。
- 列表只返回缩略图签名地址，详情返回预览图，下载严格返回原图。
- 用户只能访问自己的照片；不可用或待删除照片不返回。
- OSS 地址均为短时签名地址，不暴露永久公共地址。

### 测试

- 四个筛选和用户时区跨日边界。
- 夏令时开始、结束日期。
- 相同 `uploaded_time` 下游标无重复、无遗漏。
- 越权访问其他用户照片被拒绝。
- 原图缺失时不回退预览图。

### Postman

创建 `PhotoController.postman_collection.json`。

### 建议提交说明

`实现相册查询详情与原图下载`

## 14. 阶段 10：用户永久删除照片

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/photo/deletePhotos` | 批量永久删除当前用户照片 |

### 开发任务

- 单次最多删除 9 张，与 App 多选限制一致。
- 先校验全部照片属于当前用户，再进入删除流程。
- 照片状态切换到 `DELETE_PENDING`，相册立即排除。
- 删除 ORIGINAL、THUMBNAIL、PREVIEW 完整文件组。
- `t_photo_file.delete_reason` 写入 `USER_MANUAL`。
- 删除成功后减少 `used_bytes`，清除相关 CDN/缓存。
- 不保留回收站和独立删除审计表。
- 删除失败记录 `DELETE_FAILED` 和失败原因，并支持幂等重试。

### 测试

- 单张和最多 9 张删除成功。
- 超过 9 张、重复 ID、其他用户照片。
- OSS 部分失败时状态和用量处理一致。
- 重复删除请求幂等。
- 删除后相册、详情和容量立即更新。

### 建议提交说明

`实现照片批量永久删除`

## 15. 阶段 11：订单创建与支付发起

### 建议 Controller

`PaymentOrderController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/paymentOrder/createOrder` | 根据套餐创建支付订单 |
| GET | `/api/paymentOrder/getOrder` | 查询当前用户订单 |

### 开发任务

- 只允许选择当前生效套餐。
- 创建订单时固化套餐编码、版本、名称、容量、期限、金额和币种快照。
- `client_request_id` 实现用户范围内幂等。
- 订单结算只使用快照字段。
- 生成订单号、支付过期时间和乒乓支付参数或链接。
- 商户密钥只在服务端安全配置中使用。

### 测试

- 正常创建订单和快照完整性。
- 套餐不存在、下架、归档。
- 同一幂等号重复请求返回同一订单。
- 后续套餐改价不改变已创建订单。
- 订单只能由所属用户查询。

### Postman

创建 `PaymentOrderController.postman_collection.json`。

### 建议提交说明

`实现存储套餐订单创建`

## 16. 阶段 12：支付回调与购买权益

### 建议 Controller

`PaymentCallbackController`

### 建议接口

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/api/paymentCallback/handlePingPongCallback` | 处理乒乓支付异步通知 |

### 开发任务

- 以正式乒乓支付文档确认回调字段、签名算法和通知唯一 ID。
- 先保存回调原文和哈希，再进行验签和业务处理。
- 校验订单号、金额、币种、平台状态和订单当前状态。
- 唯一回调事件、原文哈希、订单条件更新和权益订单唯一索引形成多层幂等。
- 同一事务内完成：订单 `PENDING → PAID`、创建购买权益、记录处理结果。
- App 前端回跳不能开通权益。

### 测试

- 合法回调开通一笔权益。
- 重复、并发回调只产生一笔权益。
- 签名失败、金额不一致、币种错误、订单不存在、状态不匹配。
- 后续套餐变更不影响按订单快照创建权益。
- 回调处理失败可重试且不会重复开通。

### Postman

创建 `PaymentCallbackController.postman_collection.json`。

### 建议提交说明

`实现支付回调验签与权益开通`

## 17. 阶段 13：权益到期与自动清理

### 目标

按用户时区处理权益到期，并在容量不足时永久清理最早照片。

### 开发任务

- 定时扫描 `expire_time <= nowUtc` 且尚未完成到期处理的权益。
- 任务加 Redis 分布式互斥，并以数据库条件更新保证幂等。
- 重新计算有效总容量。
- 当 `used_bytes > active_capacity` 时，按 `(uploaded_time ASC, id ASC)` 选择最早照片。
- 每次删除完整文件组，直到用量不超过容量。
- `t_photo_file.delete_reason` 写入 `ENTITLEMENT_EXPIRED`。
- 不建立独立清理任务表和删除审计表。
- 更新权益状态、`expired_processed_time`、照片状态和存储用量。
- 清理前、开始、完成事件交给通知模块。

### 测试

- 到期后仍未超额，不删除照片。
- 容量降至 0，删除全部照片。
- 多项权益同时到期和其他权益仍有效。
- 严格按上传时间及 ID 清理。
- 任务重复执行和多实例并发不重复扣减用量。
- OSS 部分删除失败时保留可重试状态。
- 按用户时区计算到期通知日期和任务边界。

### 建议提交说明

`实现权益到期与超额照片自动清理`

## 18. 阶段 14：多语言通知

### 开发任务

- 使用 `t_notification` 建立通知创建、发送、重试和幂等流程。
- 支持：
  - 邮箱验证码；
  - 权益到期提醒；
  - 自动清理开始；
  - 自动清理完成；
  - 支付结果。
- 中文、英语、德语模板完整覆盖。
- 优先使用用户服务端语言偏好；缺失或非法时使用英语。
- 通知中的日期按用户 IANA 时区和语言格式化，数据库仍保存 UTC。
- 发送失败按 `retry_count/next_retry_time` 重试。

### 测试

- 三种语言模板内容完整。
- 缺失语言回退英语。
- 同一业务事件不重复通知。
- 失败重试和最终失败状态。
- 日期格式与用户时区一致。

### 建议提交说明

`实现多语言业务通知`

## 19. 阶段 15：安全、并发和全链路验收

### 开发任务

- 清理或隔离现有 Test 示例代码，保留有价值的规范测试。
- 核查所有接口只有 GET/POST，路径与方法名一致，无 `@PathVariable`。
- 核查 Controller 无 DTO/Entity/VO 转换和业务逻辑。
- 核查所有用户接口从 JWT 取当前用户，设备上传从设备身份和绑定关系取用户。
- 核查日志不输出密码、验证码、JWT、OSS 签名 URL、支付密钥和回调敏感字段。
- 增加认证、验证码、上传链接和回调接口限流。
- 增加数据库无外键场景的数据一致性检查脚本或管理测试。
- 完成关键并发测试：注册、设备绑定、容量预留、支付回调、到期任务。
- 完成测试数据初始化与清理脚本。
- 汇总全部 Postman Collection 和本地运行说明。

### 全链路验收场景

1. 注册验证码 → 注册 → 登录 → JWT 鉴权。
2. 绑定设备 → 创建赠送权益 → 查询存储概览。
3. 绑定设备申请上传 → OSS 回调 → 派生图 → 照片发布。
4. App 查询相册 → 详情 → 下载 → 批量删除。
5. 查询套餐 → 创建订单 → 合法支付回调 → 权益叠加。
6. 权益到期 → 容量下降 → 最早照片自动清理 → 通知。
7. App、未绑定设备和其他设备尝试上传均被拒绝。

### 非功能验收

- 全量测试通过且测试数不为 0。
- 核心查询有对应索引，执行计划无明显全表扫描风险。
- 关键事务无容量超卖、重复绑定、重复权益和重复照片发布。
- 所有时间逻辑不依赖服务器本地时区。
- 所有 Controller 均有可导入 Postman 的集合。
- 无 `.idea`、`target`、密钥或本地敏感配置进入业务提交。

### 建议提交说明

`完成后台全链路测试与交付验收`

## 20. 每阶段执行模板

后续按本计划开发每个阶段时，统一执行以下步骤：

```text
1. 确认上一阶段已提交，工作区无未提交业务文件
2. 读取该阶段需求、DDL 和现有相邻代码
3. 如需改表，先修改 DDL 并确认数据库迁移
4. 编写失败测试（RED）
5. 实现最小业务代码（GREEN）
6. 重构并补齐异常、并发和边界测试
7. 更新对应 Controller 的 Postman Collection
8. 运行阶段测试
9. 运行完整 Maven 测试套件
10. 检查 Git diff、格式和敏感信息
11. 使用中文提交
12. 报告提交哈希、测试数、数据库变化和剩余限制
```

## 21. 开发前待确认或修正事项

### P0：必须在依赖阶段开始前解决

1. **Maven 环境不可用**：当前 `mvn test` 无法加载 Plexus Launcher。阶段 0 修复。
2. **`t_photo_file.delete_reason NOT NULL` 与创建流程冲突**：未删除的可用文件没有合法删除原因。阶段 8 前调整 DDL 并更新数据库。
3. **图片处理库未明确**：现有技术栈只有 Commons IO 和 OSS SDK，没有可靠的缩略图/HEIC 处理实现。阶段 8 前确定：
   - Java 图片处理库；
   - HEIC 解码方案；
   - WebP/JPEG 输出方案；
   - EXIF 旋转和安全处理方案。
4. **乒乓支付协议未提供**：阶段 11、12 前需要正式 API 文档、沙箱参数、验签规则和回调唯一标识。

### P1：建议尽早确定

1. JWT 密钥来源、有效期、是否支持退出立即失效。
2. 验证码邮件服务商、发送频率和单日上限。
3. OSS Endpoint、Bucket、对象路径规范、回调签名和 CDN 配置。
4. 上传链接有效期、上传会话过期时间和派生任务最大重试次数。
5. 到期提醒提前天数和任务调度频率。
6. 是否允许支付退款；如果允许，需要明确已开通权益的撤销规则。
7. 当前 Druid 版本和 Log4j 1.x 是否升级或移除。

## 22. 计划完成定义

后台项目完成必须同时满足：

- 所有需求接口已实现并符合项目接口规范。
- 数据库结构与最终 DDL 一致。
- 认证、设备、上传、相册、容量、支付、清理和通知完整联通。
- 每阶段测试和最终回归测试全部通过。
- 每个 Controller 有完整 Postman Collection。
- 每阶段有独立中文 Git 提交和可追溯测试结果。
- 未解决限制、外部服务依赖和数据库迁移均已明确记录。
