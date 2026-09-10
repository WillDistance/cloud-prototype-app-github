# H5 前端项目说明

## 项目定位

这是云相册 APP 的独立 H5 前端项目，使用 Vue 3 + Vite + TypeScript + Vant 构建，调用同级目录下 `spring-boot-cloud-prototype-app` 提供的后台 API

## 项目目录

```text
cloud-prototype-app/
├─ app-resource/                         # 需求、原型截图、接口文档
├─ spring-boot-cloud-prototype-app/      # Spring Boot 后台
└─ cloud-album-h5/                       # 本前端项目
```

## 启动

```bash
cd cloud-album-h5
npm install
npm run dev
```

默认访问：

```text
http://localhost:5173
```

## 构建

```bash
npm run build
```

## API 代理

开发环境通过 Vite 将 `/api` 代理到：

```text
http://127.0.0.1:8081/app
```

生产环境可通过 `VITE_API_BASE_URL` 指定后台地址。

## 页面和流程

- 登录
- 注册三步流程
- 忘记密码三步流程
- 相册首页
- 相册筛选和游标加载
- 照片详情
- 原图下载
- 照片多选和永久删除确认
- 我的页面
- 设备绑定
- 存储概览
- 存储权益
- 套餐购买和订单确认
- 语言切换：简体中文、英语、德语
- 未绑定设备空状态
- 全局错误提示、加载状态和登录过期处理

## 原型数据策略

- 默认使用接口文档中约定的 API 请求和响应结构。
- 在后台 API 不可用时，开发环境提供明确的演示模式，便于查看原型页面。
- 演示模式不会伪造真实支付、真实 OSS 上传或真实邮件发送。
- 所有敏感信息只保存在内存或浏览器本地 Token 存储中，不写入日志。

## 后台接口依据

前端接口适配以以下文档为准：

```text
../app-resource/backend-api-document.md
```

需求和视觉实现依据：

```text
../app-resource/requirement-document.md
../app-resource/screenshots/
```

## 注意事项

- `accessToken` 通过 `Authorization: Bearer <token>` 发送。
- 不向后台传递 `userId`。
- 设备上传接口使用设备身份，不使用用户 JWT。
- 语言偏好切换后同步调用后台 `updateLanguage`。
- 日期显示由前端根据用户时区和当前语言格式化。
- 删除操作明确提示不可恢复。
- 阶段 12 支付异步回调仍等待乒乓支付正式协议，前端暂不根据支付页面回跳自行开通权益。
