# APP 后台技术方案

## 1. 基础框架

| 技术 | 当前版本 | 用途 |
| --- | --- | --- |
| Java | 21 | 后端开发语言与运行环境 |
| Maven | - | 项目构建与依赖管理 |
| Spring Boot | 3.4.12 | 后端应用框架 |
| Spring Web | 随 Spring Boot 管理 | 提供 REST API |
| Jakarta Validation | 随 Spring Boot 管理 | 请求参数校验 |
| Lombok | 1.18.42 | 简化实体类、DTO、VO 等样板代码 |
| Log4j | 1.2.17 | 日志能力；Spring Boot 默认日志链路为 SLF4J / Logback |

## 2. 数据存储与访问

| 技术 | 当前版本 | 用途 |
| --- | --- | --- |
| MySQL | Connector/J 9.5.0 | 关系型数据库，持久化用户、设备和业务数据 |
| Druid | 1.1.16 | 数据库连接池与连接监控 |
| MyBatis-Plus | 3.5.17 | ORM/数据访问层，提供基础 CRUD、条件构造等能力 |
| Redis | Spring Data Redis | 缓存、验证码、临时状态及高频读取数据 |

## 3. 文件与对象存储

本地测试使用minio，线上使用，可以以切换华为云obs和阿里云oss

| 技术 | 当前版本 | 用途 |
| --- | --- | --- |
| 环境上运行阿里云 OSS SDK | 3.18.5 | 上传、下载和管理用户头像、设备图片、附件等对象文件 |
| 环境上运行华为云obs | 3.26.6 | 上传、下载和管理用户头像、设备图片、附件等对象文件 |
| 本地运行使用minio | 9.0.3 | 上传、下载和管理用户头像、设备图片、附件等对象文件 |
| Commons IO | 2.21.0 | 文件流与 IO 工具处理 |

## 4. 通用工具与数据处理

| 技术 | 当前版本 | 用途 |
| --- | --- | --- |
| Hutool | 5.8.44 | 日期、字符串、集合、加密等通用工具 |
| Fastjson2 | 2.0.61 | JSON 序列化与反序列化 |
| Commons Lang3 | 3.20.0 | 字符串、对象、日期等通用工具 |
| Commons Collections4 | 4.5.0 | 集合工具 |
| Commons Text | 1.15.0 | 文本处理工具 |

## 5. 测试

| 技术 | 用途 |
| --- | --- |
| Spring Boot Starter Test | 单元测试、MockMvc 接口测试、Mockito 模拟 Service 依赖 |
| JUnit 5 | 测试框架 |

## 6. 后端接口规范

- 对外接口仅使用 **GET** 与 **POST**。
- Controller 仅负责定义 HTTP 接口、接收参数并调用 Service，不处理 DTO、Entity、VO 转换和业务逻辑。
- POST 请求参数使用 `pojo/dto`。
- 数据库表映射实体使用 `pojo/entity`。
- 返回前端的数据对象使用 `pojo/vo`。
- DTO、Entity、VO 转换及业务逻辑在 Service 接口定义，并在 ServiceImpl 中实现。
- Controller 接口统一返回 `CommonResult<T>`，成功结果使用 `CommonResult.success(...)` 封装。
- Controller 的 `@GetMapping` / `@PostMapping` 路径与 Controller 方法名一致。
- 每个 Controller 在项目根目录维护同名 `<Controller类名>.postman_collection.json` 文件，供 Postman 导入测试；文件应覆盖该 Controller 的全部接口及示例请求参数。
