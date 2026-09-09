# 阶段 1 实施说明：基础实体、枚举和数据访问层

## 当前代码基线

本阶段开始前，当前项目实际代码已回到基础测试框架：

- `src/main/java/com/app/pojo` 只有基础测试 DTO、Entity、VO 和 `CommonResult`。
- `src/main/java/com/app/mapper` 只有 `TestMapper`。
- `src/main/resources/mapper` 只有 `TestMapper.xml`。
- 项目已经具备 Spring Boot、MyBatis-Plus、MySQL、Redis、Druid 和多对象存储基础依赖。
- 主启动类已经通过 `@MapperScan("com.app.mapper")` 扫描 Mapper。
- 主启动类 static 初始化块将 JVM 默认时区设置为 UTC。
- 工作区不包含待提交的业务源码修改；`.idea` 目录不纳入本阶段处理。

## 本阶段目标

根据：

- `app-resource/cloud-album-database-schema.sql`
- `app-resource/backend-technology-stack.md`
- `app-resource/requirement-document.md`

建立云相册数据库对应的基础 Java 数据模型和 XML 数据访问层，为后续认证、设备、上传、相册、存储权益和支付功能提供持久化基础。

## 数据库表范围

数据库脚本中包含 12 张业务表：

1. `t_user`
2. `t_email_verification_code`
3. `t_device`
4. `t_device_binding`
5. `t_platform_config`
6. `t_storage_plan`
7. `t_payment_order`
8. `t_payment_callback`
9. `t_user_storage_account`
10. `t_storage_entitlement`
11. `t_photo_file`
12. `t_notification`

## 实施内容

### 1. 基础公共类

- 保留并检查 `BaseField` 审计字段。
- 确认 `createBy`、`createTime`、`updateBy`、`updateTime` 的 Java 类型和数据库字段一致。
- 确认 Entity 使用 Lombok `@Data` 和 `@Accessors(chain = true)`。
- 确认数据库时间使用 `LocalDateTime`，由 JVM UTC 默认时区和应用层规则统一处理。

### 2. 枚举类

根据数据库 CHECK 约束和字段注释创建枚举：

- `UserLanguageEnum`
- `UserStatusEnum`
- `VerificationPurposeEnum`
- `VerificationStatusEnum`
- `DeviceStatusEnum`
- `DeviceBindingStatusEnum`
- `DurationUnitEnum`
- `PlatformConfigStatusEnum`
- `StoragePlanStatusEnum`
- `CurrencyEnum`
- `PaymentChannelEnum`
- `PaymentOrderStatusEnum`
- `CallbackProcessStatusEnum`
- `EntitlementSourceTypeEnum`
- `EntitlementStatusEnum`
- `PhotoFileTypeEnum`
- `PhotoStatusEnum`
- `NotificationTypeEnum`
- `NotificationChannelEnum`
- `NotificationStatusEnum`

每个枚举需要：

- 保存数据库值。
- 提供中文注释。
- 提供前端或业务需要的值读取方法。
- 与数据库 CHECK 约束中的值完全一致。
- 不在 Entity 中使用无法持久化的任意字符串替代枚举。

### 3. Entity 类

为 12 张业务表创建 Entity，统一使用：

```java
@Data
@Accessors(chain = true)
@TableName("t_xxx")
public class XxxEntity extends BaseField {
}
```

字段规范：

- 主键使用 `@TableId(value = "id", type = IdType.ASSIGN_ID)` 或与当前项目统一的主键策略。
- 普通字段使用 `@TableField("column_name")`。
- Entity 字段注释使用单行格式：

```java
/** 用户邮箱 */
@TableField("email")
private String email;
```

- 字段之间保留空行。
- 字段含义以 SQL COMMENT 为准。
- 密码哈希字段不能在 VO 暴露。
- JSON 字段根据实际 MyBatis-Plus 映射策略选择 String、List 或明确的 TypeHandler；本阶段不得凭空引入未配置的转换器。
- 金额字段使用 Long 表示分。
- 容量和文件大小字段使用 Long 表示字节。
- 时间字段使用 LocalDateTime。

### 4. DTO 基础模型

本阶段仅创建后续接口所需的基础 DTO 目录和模型，不实现业务逻辑。

至少预留以下领域 DTO：

- 认证：注册、登录、验证码、密码重置。
- 用户：语言更新。
- 设备：设备绑定、设备上传会话。
- OSS：上传完成回调。
- 相册：照片查询、删除。
- 支付：创建支付订单。

DTO 要求：

- DTO 不继承 Entity。
- 使用 Jakarta Validation 约束请求边界。
- 仅包含请求需要的字段。
- 不包含数据库审计字段。
- 不包含用户 ID 等由 JWT 或设备绑定关系确定的字段。

### 5. VO 基础模型

为后续接口准备 VO，至少覆盖：

- 登录结果。
- 注册结果。
- 用户资料。
- 设备信息和设备上传会话。
- 存储概览和存储权益。
- 存储套餐。
- 支付订单。
- 照片列表、照片详情和下载地址。
- OSS 回调处理结果。

VO 要求：

- 不直接返回 Entity。
- 不返回密码、密码哈希、设备初始密码哈希、OSS 长期密钥或内部安全字段。
- 时间字段按接口规范明确 UTC 表示。
- 枚举输出使用稳定业务值，而不是 Java 枚举内部名称的偶然实现。

### 6. Mapper 接口

为 12 张业务表创建 Mapper 接口：

```java
@Mapper
public interface XxxMapper extends BaseMapper<XxxEntity> {
}
```

本阶段 Mapper 只声明数据访问方法，不在 Java 中写 SQL 注解。

后续业务需要的查询方法包括：

- 按邮箱查询用户。
- 按设备业务 ID 查询设备。
- 按用户和设备查询绑定关系。
- 按邮箱、验证码用途和状态查询验证码。
- 查询当前生效平台配置。
- 查询当前生效存储套餐。
- 查询用户存储账户并支持事务锁定。
- 查询用户有效权益并汇总容量。
- 查询订单幂等号和订单号。
- 查询上传会话状态。
- 查询照片和照片文件。
- 查询通知状态和幂等键。

所有需要更新状态的 SQL 必须使用条件更新，避免并发请求覆盖已经变化的状态。

### 7. Mapper XML

为每个 Mapper 创建对应 XML：

```text
src/main/resources/mapper/<MapperSimpleName>.xml
```

统一结构：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.app.mapper.XxxMapper">
</mapper>
```

要求：

- namespace 必须与 Mapper 全限定名一致。
- 方法 ID 必须与 Java Mapper 方法名一致。
- 使用 resultMap 映射复杂实体。
- 抽取 Base_Column_List，避免重复字段列表。
- SQL 中的 `<`、`<=` 使用 XML 转义。
- 锁定查询明确使用 `FOR UPDATE`，并只在事务 Service 中调用。
- 条件状态更新必须在 WHERE 中校验当前状态。
- 不添加数据库外键约束。

## 本阶段不实现的内容

本阶段不实现：

- 注册和登录业务。
- 设备绑定业务。
- OSS 上传 URL 生成。
- OSS 回调处理。
- 图片处理。
- 相册查询和删除。
- 支付下单和回调。
- 定时清理任务。
- 邮件发送。
- 对象存储厂商具体实现改造。

这些内容分别在后续阶段完成。

## 测试计划

### Entity 与模型测试

- 检查 12 张表对应 Entity 是否存在。
- 检查 `@TableName` 与表名一致。
- 检查字段映射覆盖数据库业务字段。
- 检查敏感字段不出现在 VO。
- 检查枚举值与 SQL CHECK 约束一致。

### Mapper 契约测试

- 检查 12 个 Mapper 接口存在。
- 检查 12 个 Mapper XML 存在。
- 检查 XML namespace 与接口全限定名一致。
- 检查每个 XML statement ID 都有对应 Mapper 方法。
- 检查 Java Mapper 中不存在 `@Select`、`@Insert`、`@Update`、`@Delete` SQL 注解。
- 检查 XML 中包含必要的 `resultMap` 和基础字段列表。
- 检查涉及状态更新的 SQL 使用条件状态。

### 构建验证

```bash
cmd.exe /d /c "mvnw.cmd -q -DskipTests compile"
cmd.exe /d /c "mvnw.cmd test"
git diff --check
```

完整测试必须确认 Surefire 实际执行了非零测试数量，不能只根据 `BUILD SUCCESS` 判断通过。

## 阶段验收标准

本阶段完成必须同时满足：

- 12 张数据库表都有 Entity。
- Entity 字段与 DDL 类型、名称和注释含义一致。
- 枚举值与数据库约束一致。
- DTO、Entity、VO 分层明确。
- 12 个 Mapper 接口已创建。
- 12 个 Mapper XML 已创建。
- Java Mapper 不包含 SQL 注解。
- XML namespace、statement ID 和 resultMap 校验通过。
- Mapper 契约测试通过。
- Maven 编译通过。
- 全量测试通过并有实际 Surefire 测试数量。
- 没有修改 `.idea`、`target` 或敏感配置。
- 使用中文 Git 提交信息提交本阶段。

## 阶段提交建议

```text
建立基础实体枚举和数据访问层
```

提交前必须检查：

```bash
git status --short -- . ':!.idea' ':!target'
git diff --check
git diff --cached --name-status
```
