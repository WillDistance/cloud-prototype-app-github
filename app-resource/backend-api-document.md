# APP 后台接口文档

> 本文档用于后续 H5 原型开发。接口清单、请求参数、响应字段和示例均以当前后台代码实际实现为准。

## 一、基础约定

### 1. 服务地址

```text
http://127.0.0.1:8081/app
```

接口完整地址由服务地址、`/api` 和 Controller 路径组成，例如：

```text
POST http://127.0.0.1:8081/app/api/auth/login
```

### 2. 通用响应

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `errorCode` | `string` | 错误码；成功为 `E00000` |
| `errorMag` | `string` | 返回消息；字段名称按后台现有协议保留 |
| `data` | `object/array/boolean/null` | 业务数据 |

### 3. 登录认证

需要用户登录的接口携带：

```http
Authorization: Bearer <accessToken>
```

`accessToken` 仅保存在 H5 本地存储，不得写入日志、URL 或页面文本。用户 ID 由后台从 JWT 获取，前端不传 `userId`。

### 4. 时间、语言和单位

- 数据库时间按 UTC 保存。
- 用户时区使用 IANA 标识，例如 `Asia/Shanghai`、`America/New_York`。
- 注册接口使用 `X-Time-Zone` 传入时区。
- 支持语言：`zh-CN`、`en`、`de`。
- 金额单位为分，容量单位为字节。
- 短签名 URL 仅短期有效，不应长期缓存。

## 二、错误码

| 错误码 | 含义 | 前端建议 |
|---|---|---|
| `E00000` | 成功 | 处理 `data` |
| `E00001` | 文件上传、下载失败 | 提示稍后重试 |
| `E00002` | 文件不存在 | 刷新页面 |
| `A10001` | 邮箱或密码错误 | 提示账号或密码错误 |
| `A10002` | 验证码无效或已过期 | 重新获取验证码 |
| `A10003` | 请先登录 | 清除 Token 并跳转登录 |
| `A10004` | 登录状态已过期 | 清除 Token 并跳转登录 |
| `A10005` | 无权访问该资源 | 提示无权限 |
| `A10006` | 邮箱已注册 | 跳转登录 |
| `A10007` | 验证码发送过于频繁 | 等待后重试 |
| `A10008` | 验证码尝试次数已超限 | 重新获取验证码 |
| `A10009` | 验证码尚未验证或已被使用 | 重新验证 |
| `A10010` | 时区缺失或无效 | 使用有效 IANA 时区 |
| `A10011` | 两次密码不一致 | 重新输入 |
| `U20001` | 不支持的语言偏好 | 仅使用 `zh-CN/en/de` |
| `D20001` | 设备不存在 | 检查设备 ID |
| `D20002` | 设备凭证错误 | 检查设备密码 |
| `D20003` | 设备已绑定 | 更换设备 |
| `D20004` | 设备已禁用 | 联系管理员 |
| `D20005` | 设备未绑定 | 先绑定设备 |
| `D20006` | 设备 ID 格式错误 | 检查设备 ID 格式 |
| `D20007` | 当前用户已绑定设备 | 不能继续绑定 |
| `D20008` | 绑定状态冲突 | 稍后重试 |
| `D20009` | 赠送配置不可用 | 稍后重试 |
| `ST30001` | 存储套餐不存在 | 重新加载套餐 |
| `ST30002` | 存储空间不足 | 购买套餐或删除照片 |
| `ST30003` | 存储权益已过期 | 刷新容量 |
| `ST30004` | 存储账户不存在 | 稍后重试 |
| `PH40001` | 照片不存在 | 刷新相册 |
| `PH40002` | 照片暂不可用 | 稍后重试 |
| `PH40003` | 上传会话无效或已过期 | 重新创建上传会话 |
| `PH40004` | 照片文件不符合要求 | 检查文件类型和大小 |
| `PH40005` | 照片处理失败 | 等待后台重试 |
| `P50001` | 支付套餐无效 | 重新加载套餐 |
| `P50002` | 支付订单不存在 | 不展示该订单 |
| `P50003` | 支付订单状态无效 | 刷新订单状态 |
| `P50004` | 支付签名无效 | 不重复提交，联系后台 |
| `P50005` | 支付金额不一致 | 不继续支付 |
| `S40000` | 请求参数错误 | 检查请求参数 |
| `S50000` | 系统繁忙 | 稍后重试 |
| `S50001` | 依赖服务不可用 | 稍后重试 |
| `S50002` | 请求与当前状态冲突 | 刷新状态后重试 |

## 三、认证接口

公共路径：`/api/auth`。

### 1. 发送注册验证码

```http
POST /api/auth/sendRegisterCode
Content-Type: application/json
```

请求参数：

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---:|---|---|
| `email` | `string` | 是 | 合法邮箱，最多 320 字符 | 接收验证码的邮箱，后台转为小写 |

请求示例：

```json
{
  "email": "user@example.com"
}
```

响应示例：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": true
}
```

验证码有效期、发送频率和尝试次数由后台控制，前端不保存验证码到日志。

### 2. 验证注册验证码

```http
POST /api/auth/verifyRegisterCode
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 约束 |
|---|---|---:|---|
| `email` | `string` | 是 | 合法邮箱 |
| `code` | `string` | 是 | 6 位数字 |

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

成功响应：`data: true`。

### 3. 注册

```http
POST /api/auth/register
Content-Type: application/json
X-Time-Zone: Asia/Shanghai
Accept-Language: zh-CN
```

请求体：

| 字段 | 类型 | 必填 | 约束 |
|---|---|---:|---|
| `email` | `string` | 是 | 合法邮箱，最多 320 字符 |
| `password` | `string` | 是 | 6—72 字符 |
| `confirmPassword` | `string` | 是 | 与 `password` 一致 |

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

响应：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "userId": 10001,
    "email": "user@example.com",
    "timeZone": "Asia/Shanghai",
    "preferredLanguage": "zh-CN"
  }
}
```

注册成功后前端进入登录。用户未绑定设备时：主页展示空状态，“我的”页面展示绑定设备提示。

### 4. 登录

```http
POST /api/auth/login
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 约束 |
|---|---|---:|---|
| `email` | `string` | 是 | 合法邮箱 |
| `password` | `string` | 是 | 6—72 字符 |

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  }
}
```

### 5. 发送重置密码验证码

```http
POST /api/auth/sendResetPasswordCode
Content-Type: application/json
```

```json
{
  "email": "user@example.com"
}
```

成功时 `data` 为 `true`。

### 6. 验证重置密码验证码

```http
POST /api/auth/verifyResetPasswordCode
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

成功时 `data` 为 `true`。

### 7. 重置密码

```http
POST /api/auth/resetPassword
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

成功时 `data` 为 `true`。前端应清除旧 Token 并要求用户重新登录。

### 8. 退出登录

```http
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

无请求体，成功时 `data` 为 `true`。当前后台采用无状态 JWT，前端收到成功响应后必须删除本地 Token。

## 四、用户设置接口

公共路径：`/api/user`。需要 JWT。

### 1. 获取用户资料

```http
GET /api/user/getProfile
Authorization: Bearer <accessToken>
```

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "email": "user@example.com",
    "timeZone": "Asia/Shanghai",
    "preferredLanguage": "zh-CN"
  }
}
```

### 2. 更新通知语言

```http
POST /api/user/updateLanguage
Authorization: Bearer <accessToken>
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 可选值 |
|---|---|---:|---|
| `language` | `string` | 是 | `zh-CN`、`en`、`de` |

```json
{
  "language": "en"
}
```

成功响应中的 `data` 返回更新后的用户资料，结构同 `getProfile`。

## 五、设备接口

公共路径：`/api/device`。需要 JWT。

### 1. 查询当前用户设备

```http
GET /api/device/getMyDevice
Authorization: Bearer <accessToken>
```

成功示例：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "deviceId": "CC-2026-AB12-8A2F",
    "model": "Cloud Camera Pro",
    "status": "BOUND",
    "bindTime": "2026-09-06T03:00:00.000Z",
    "lastOnlineTime": "2026-09-06T04:00:00.000Z"
  }
}
```

未绑定时 `data` 可能为 `null`。

### 2. 永久绑定设备

```http
POST /api/device/bindDevice
Authorization: Bearer <accessToken>
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 约束 |
|---|---|---:|---|
| `deviceId` | `string` | 是 | `CC-YYYY-XXXX-XXXX` |
| `password` | `string` | 是 | 最多 128 字符 |

```json
{
  "deviceId": "CC-2026-AB12-8A2F",
  "password": "device-password"
}
```

成功示例：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "deviceId": "CC-2026-AB12-8A2F",
    "model": "Cloud Camera Pro",
    "status": "BOUND",
    "giftCapacityBytes": 107374182400,
    "giftDurationUnit": "MONTH",
    "giftExpireTime": "2026-10-06T16:00:00.000Z"
  }
}
```

绑定关系永久有效，不提供解绑和换绑接口。

## 六、设备上传接口

公共路径：`/api/deviceUpload`。

> 这些接口使用设备凭证，不使用用户 JWT。H5 原型一般不直接调用；如需模拟设备上传，按以下流程处理。

### 1. 创建上传会话

```http
POST /api/deviceUpload/createUploadSession
X-Device-Id: CC-2026-AB12-8A2F
X-Device-Password: device-password
Content-Type: application/json
```

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| `originalFileName` | `string` | 是 | 原始文件名 |
| `contentType` | `string` | 是 | MIME 类型，如 `image/jpeg` |
| `fileSizeBytes` | `number` | 是 | 文件大小，必须大于 0 |

```json
{
  "originalFileName": "IMG_0001.jpg",
  "contentType": "image/jpeg",
  "fileSizeBytes": 5242880
}
```

成功响应：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "uploadNo": "UPL202609060001",
    "objectKey": "users/10001/devices/20001/original/2026/09/random.jpg",
    "uploadUrl": "https://oss.example.com/...",
    "uploadUrlExpireTime": "2026-09-06T04:30:00.000Z",
    "declaredFileSizeBytes": 5242880,
    "declaredContentType": "image/jpeg",
    "status": "URL_ISSUED"
  }
}
```

### 2. 查询上传会话状态

```http
GET /api/deviceUpload/getUploadSessionStatus?uploadNo=UPL202609060001
X-Device-Id: CC-2026-AB12-8A2F
X-Device-Password: device-password
```

成功响应：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "uploadNo": "UPL202609060001",
    "status": "COMPLETED",
    "photoNo": "PHT202609060001",
    "failureCode": null,
    "failureReason": null
  }
}
```

## 七、套餐和存储接口

### 1. 查询有效套餐

```http
GET /api/storagePlan/listActivePlans
```

不需要 JWT。成功响应：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": [
    {
      "planCode": "BASIC",
      "planVersion": 1,
      "planName": "基础套餐",
      "capacityBytes": 107374182400,
      "durationValue": 1,
      "durationUnit": "MONTH",
      "priceCent": 990,
      "currency": "CNY",
      "recommended": false
    }
  ]
}
```

只返回已生效且状态为 `ACTIVE` 的套餐。

### 2. 查询存储概览

```http
GET /api/storage/getOverview
Authorization: Bearer <accessToken>
```

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "totalCapacityBytes": 107374182400,
    "usedBytes": 5242880,
    "reservedBytes": 0,
    "remainingBytes": 107321753600,
    "nextExpireTime": "2026-10-06T16:00:00.000Z"
  }
}
```

剩余容量：`max(totalCapacityBytes - usedBytes - reservedBytes, 0)`。

### 3. 查询有效权益

```http
GET /api/storage/listEntitlements
Authorization: Bearer <accessToken>
```

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": [
    {
      "entitlementNo": "ENT202609060001",
      "sourceType": "DEVICE_GIFT",
      "nameSnapshot": "设备赠送权益",
      "capacityBytes": 107374182400,
      "effectiveTime": "2026-09-06T16:00:00.000Z",
      "expireTime": "2026-10-06T16:00:00.000Z"
    }
  ]
}
```

有效时间边界为：`effectiveTime <= now < expireTime`。

## 八、相册接口

公共路径：`/api/photo`。除删除外均为 GET，所有接口需要 JWT。

### 1. 查询相册

```http
GET /api/photo/listPhotos?filter=TODAY&size=20
Authorization: Bearer <accessToken>
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---:|---:|---|
| `filter` | `string` | 否 | `ALL` | `TODAY`、`SEVEN_DAYS`、`ONE_MONTH`、`ALL` |
| `cursorTime` | `string` | 否 | 无 | 与 `cursorId` 同时提供 |
| `cursorId` | `number` | 否 | 无 | 上一页最后一条照片 ID |
| `size` | `number` | 否 | 20 | 每页数量 |

下一页示例：

```http
GET /api/photo/listPhotos?filter=TODAY&cursorTime=2026-09-06T01:02:03Z&cursorId=100&size=20
Authorization: Bearer <accessToken>
```

响应示例：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "items": [
      {
        "photoNo": "PHT202609060001",
        "fileName": "IMG_0001.jpg",
        "widthPixels": 4032,
        "heightPixels": 3024,
        "takenTime": "2026-09-06T02:00:00.000Z",
        "uploadedTime": "2026-09-06T02:01:00.000Z",
        "thumbnailUrl": "https://oss.example.com/signed-thumbnail-url"
      }
    ],
    "hasMore": false,
    "nextCursorTime": null,
    "nextCursorId": null
  }
}
```

列表只返回缩略图地址，按 `(uploadedTime DESC, id DESC)` 排序，只查询当前用户照片。

### 2. 查询照片详情

```http
GET /api/photo/getPhotoDetail?photoNo=PHT202609060001
Authorization: Bearer <accessToken>
```

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "photoNo": "PHT202609060001",
    "fileName": "IMG_0001.jpg",
    "originalMimeType": "image/jpeg",
    "originalSizeBytes": 5242880,
    "widthPixels": 4032,
    "heightPixels": 3024,
    "takenTime": "2026-09-06T02:00:00.000Z",
    "uploadedTime": "2026-09-06T02:01:00.000Z",
    "previewUrl": "https://oss.example.com/signed-preview-url"
  }
}
```

### 3. 获取原图下载地址

```http
GET /api/photo/getOriginalDownloadUrl?photoNo=PHT202609060001
Authorization: Bearer <accessToken>
```

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "downloadUrl": "https://oss.example.com/signed-original-url"
  }
}
```

### 4. 批量永久删除照片

```http
POST /api/photo/deletePhotos
Authorization: Bearer <accessToken>
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 约束 |
|---|---|---:|---|
| `photoNos` | `array<string>` | 是 | 1—9 个，不能重复 |

```json
{
  "photoNos": [
    "PHT202609060001",
    "PHT202609060002"
  ]
}
```

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": null
}
```

删除是永久删除，没有回收站。删除成功后相册立即不再展示。OSS 删除失败由后台记录并重试。

## 九、支付订单接口

公共路径：`/api/paymentOrder`。需要 JWT。

### 1. 创建支付订单

```http
POST /api/paymentOrder/createOrder
Authorization: Bearer <accessToken>
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| `planCode` | `string` | 是 | 当前生效套餐编码 |
| `planVersion` | `number` | 是 | 套餐版本 |
| `clientRequestId` | `string` | 是 | 用户范围内幂等请求号 |

```json
{
  "planCode": "BASIC",
  "planVersion": 1,
  "clientRequestId": "h5-order-request-20260906-0001"
}
```

响应示例：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "orderNo": "PO202609060001",
    "planCodeSnapshot": "BASIC",
    "planVersionSnapshot": 1,
    "planNameSnapshot": "基础套餐",
    "capacityBytesSnapshot": 107374182400,
    "durationValueSnapshot": 1,
    "durationUnitSnapshot": "MONTH",
    "amountCent": 990,
    "currency": "CNY",
    "paymentChannel": "PINGPONG",
    "status": "PENDING",
    "expireTime": "2026-09-06T04:30:00.000Z",
    "paidTime": null,
    "closedTime": null,
    "clientRequestId": "h5-order-request-20260906-0001",
    "paymentRedirectUrl": "https://pay.invalid/checkout?orderNo=PO202609060001",
    "paymentParameters": {
      "merchantId": "configured-merchant",
      "orderNo": "PO202609060001",
      "amountCent": "990",
      "currency": "CNY",
      "expireTime": "2026-09-06T04:30:00"
    }
  }
}
```

订单保存套餐快照，后续套餐改价不影响该订单。同一用户使用相同 `clientRequestId` 时返回同一订单。

### 2. 查询订单

```http
GET /api/paymentOrder/getOrder?orderNo=PO202609060001
Authorization: Bearer <accessToken>
```

响应结构同创建订单，只能查询当前用户自己的订单。

## 十、OSS 上传完成回调

> 此接口由 OSS 服务调用，不是 H5 页面接口。阶段 12 支付回调因缺少正式乒乓支付协议，当前未列入。

### 上传完成回调

```http
POST /api/ossCallback/uploadCompleted
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| `eventId` | `string` | 是 | 回调事件唯一 ID |
| `uploadNo` | `string` | 是 | 上传会话编号 |
| `objectKey` | `string` | 是 | OSS 对象 Key |
| `sizeBytes` | `number` | 是 | 对象大小 |
| `contentType` | `string` | 是 | MIME 类型 |
| `signature` | `string` | 是 | 回调签名 |
| `callbackTime` | `string` | 是 | ISO-8601 时间 |

```json
{
  "eventId": "evt-test-001",
  "uploadNo": "UPL202609060001",
  "objectKey": "users/10001/devices/20001/original/2026/09/random.jpg",
  "sizeBytes": 5242880,
  "contentType": "image/jpeg",
  "signature": "callback-signature",
  "callbackTime": "2026-09-06T04:00:00Z"
}
```

成功响应：

```json
{
  "errorCode": "E00000",
  "errorMag": "success",
  "data": {
    "uploadNo": "UPL202609060001",
    "status": "COMPLETED",
    "photoNo": "PHT202609060001",
    "idempotent": false
  }
}
```

重复回调不会重复创建照片或增加容量用量。

## 十一、H5 推荐调用流程

### 1. 登录后首页

1. `POST /api/auth/login`
2. 保存 `data.accessToken`
3. `GET /api/user/getProfile`
4. `GET /api/device/getMyDevice`
5. 未绑定设备：展示主页空状态和绑定引导
6. 已绑定设备：调用 `getOverview`、`listEntitlements`、`listPhotos`

### 2. 绑定设备

1. 输入设备 ID 和密码。
2. 调用 `POST /api/device/bindDevice`。
3. 成功后刷新设备、存储概览和权益。

### 3. 浏览相册

1. 首次调用 `listPhotos`，不传游标。
2. 用 `nextCursorTime` 和 `nextCursorId` 请求下一页。
3. 列表展示 `thumbnailUrl`。
4. 详情展示 `previewUrl`。
5. 下载原图前调用 `getOriginalDownloadUrl`。

### 4. 购买套餐

1. `GET /api/storagePlan/listActivePlans`
2. 生成唯一 `clientRequestId`
3. `POST /api/paymentOrder/createOrder`
4. 打开返回的支付跳转地址
5. 权益以服务端支付回调为准，前端回跳不能直接开通权益

### 5. 删除照片

1. 前端最多选择 9 张照片。
2. 去重 `photoNo`。
3. 调用 `POST /api/photo/deletePhotos`。
4. 成功后移除列表项并刷新存储概览。

## 十二、接口实现边界

- 当前接口只使用 GET/POST，不使用 `@PathVariable`。
- 用户身份只从 JWT 获取，不从请求参数获取。
- 设备上传只接受设备 ID 和设备密码，不使用用户 JWT 代替设备身份。
- Controller 只负责请求绑定和调用 Service。
- 所有数据库 SQL 已迁移到 `resources/mapper/*.xml`。
- 当前已完成订单创建和支付发起抽象；支付回调需要乒乓支付正式字段、签名算法、密钥来源和通知唯一 ID 后再补充。
