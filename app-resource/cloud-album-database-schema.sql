-- 云相册 App 数据库表设计
-- 适用数据库：MySQL 8.0+
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- 时间约定：所有 datetime(3) 字段均保存 UTC 时间，由应用层负责用户时区转换
-- 金额约定：金额统一使用人民币分保存
-- 容量约定：容量和文件大小统一使用字节保存

SET NAMES utf8mb4;

-- =========================================================
-- 1. 用户与认证
-- =========================================================

CREATE TABLE IF NOT EXISTS `t_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(320) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录邮箱，统一规范化为小写',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码安全哈希，禁止保存明文，使用ARGON2ID算法',
  `time_zone` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'IANA时区标识，例如Asia/Shanghai',
  `preferred_language` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'en' COMMENT '服务端通知语言偏好：zh-CN=简体中文，en=英语，de=德语',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE=正常可用，LOCKED=已锁定，DISABLED=已禁用',
  `last_login_time` datetime(3) DEFAULT NULL COMMENT '最近登录时间(UTC，带毫秒)',
  `password_update_time` datetime(3) DEFAULT NULL COMMENT '密码最近更新时间(UTC，带毫秒)',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`),
  KEY `idx_user_status` (`status`),
  CONSTRAINT `chk_user_language` CHECK (`preferred_language` IN ('zh-CN', 'en', 'de')),
  CONSTRAINT `chk_user_status` CHECK (`status` IN ('ACTIVE', 'LOCKED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

CREATE TABLE IF NOT EXISTS `t_email_verification_code` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(320) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收验证码的邮箱',
  `user_id` bigint unsigned DEFAULT NULL COMMENT '关联用户ID，注册场景可为空',
  `purpose` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用途：REGISTER=注册，RESET_PASSWORD=重置密码',
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码明文保存',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：SEND_FAIL=发送失败，PENDING=待验证，VERIFIED=已验证，EXPIRED=已过期，INVALIDATED=已作废',
  `send_count` int unsigned NOT NULL DEFAULT 1 COMMENT '发送次数',
  `verify_fail_count` int unsigned NOT NULL DEFAULT 0 COMMENT '验证失败次数',
  `expire_time` datetime(3) NOT NULL COMMENT '验证码过期时间(UTC，带毫秒)',
  `verified_time` datetime(3) DEFAULT NULL COMMENT '验证成功时间(UTC，带毫秒)',
  `request_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申请验证码的客户端IP',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  KEY `idx_verify_email_purpose_status` (`email`, `purpose`, `status`, `expire_time`),
  KEY `idx_verify_user_id` (`user_id`),
  CONSTRAINT `chk_verify_purpose` CHECK (`purpose` IN ('REGISTER', 'RESET_PASSWORD')),
  CONSTRAINT `chk_verify_status` CHECK (`status` IN ('SEND_FAIL', 'PENDING', 'VERIFIED', 'EXPIRED', 'INVALIDATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- =========================================================
-- 2. 设备与永久绑定
-- =========================================================

CREATE TABLE IF NOT EXISTS `t_device` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备业务ID，例如CC-2026-AB12-8A2F',
  `initial_password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备初始绑定密码安全哈希，使用ARGON2ID算法',
  `model` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备型号',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNBOUND' COMMENT '设备状态：UNBOUND=未绑定，BOUND=已绑定，DISABLED=已禁用',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_device_id` (`device_id`),
  KEY `idx_device_status` (`status`),
  CONSTRAINT `chk_device_status` CHECK (`status` IN ('UNBOUND', 'BOUND', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='云端相机设备表';

CREATE TABLE IF NOT EXISTS `t_device_binding` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '绑定用户ID',
  `device_id` bigint unsigned NOT NULL COMMENT '设备表主键ID',
  `bind_time` datetime(3) NOT NULL COMMENT '永久绑定时间(UTC，带毫秒)',
  `bind_user_time_zone` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绑定时用户IANA时区快照',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BOUND' COMMENT '绑定状态：BOUND=已永久绑定（当前仅允许该值）',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_binding_user` (`user_id`),
  UNIQUE KEY `uk_binding_device` (`device_id`),
  CONSTRAINT `chk_binding_status` CHECK (`status` = 'BOUND')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备永久绑定关系表';

-- =========================================================
-- 3. 平台配置与存储套餐
-- =========================================================

CREATE TABLE IF NOT EXISTS `t_platform_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_version` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置版本号',
  `max_file_size_bytes` bigint unsigned NOT NULL COMMENT '单文件大小上限(字节)',
  `allowed_extensions` json NOT NULL COMMENT '允许的文件扩展名JSON数组，例如["jpg","jpeg","png","heic"]',
  `device_gift_capacity_bytes` bigint unsigned NOT NULL COMMENT '设备绑定赠送容量(字节)',
  `device_gift_duration_value` int unsigned NOT NULL COMMENT '设备赠送有效期数值',
  `device_gift_duration_unit` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备赠送有效期单位：DAY=天，MONTH=自然月，YEAR=自然年',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态，仅能存在一条生效中的数据，归档的数据类似于软删除：ACTIVE=生效，ARCHIVED=已归档',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_config_version` (`config_version`),
  KEY `idx_platform_config_status_effective` (`status`, `effective_time`),
  CONSTRAINT `chk_platform_duration_unit` CHECK (`device_gift_duration_unit` IN ('DAY', 'MONTH', 'YEAR')),
  CONSTRAINT `chk_platform_config_status` CHECK (`status` IN ('ACTIVE', 'ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台全局配置表';

CREATE TABLE IF NOT EXISTS `t_storage_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐编码',
  `plan_version` int unsigned NOT NULL COMMENT '套餐版本',
  `plan_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐名称',
  `capacity_bytes` bigint unsigned NOT NULL COMMENT '增加容量(字节)',
  `duration_value` int unsigned NOT NULL COMMENT '有效期数值',
  `duration_unit` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '有效期单位：DAY=天，MONTH=自然月，YEAR=自然年',
  `price_cent` bigint unsigned NOT NULL COMMENT '销售金额(美元、人民币)',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USD' COMMENT '币种：USD=美元，CNY=人民币',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '展示排序，数值越小越靠前',
  `recommended` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否推荐：0=否，1=是',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态，归档的数据类似于软删除：ACTIVE=生效销售中，OFF_SHELF=已下架，ARCHIVED=已归档',
  `effective_time` datetime(3) DEFAULT NULL COMMENT '生效时间(UTC，带毫秒)',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_code_version` (`plan_code`, `plan_version`),
  KEY `idx_plan_status_sort` (`status`, `sort_order`),
  CONSTRAINT `chk_plan_duration_unit` CHECK (`duration_unit` IN ('DAY', 'MONTH', 'YEAR')),
  CONSTRAINT `chk_plan_currency` CHECK (`currency` IN ('CNY','USD')),
  CONSTRAINT `chk_plan_recommended` CHECK (`recommended` IN (0, 1)),
  CONSTRAINT `chk_plan_status` CHECK (`status` IN ('ACTIVE', 'OFF_SHELF', 'ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='云存储套餐表';

-- =========================================================
-- 4. 订单、支付与幂等回调
-- =========================================================

CREATE TABLE IF NOT EXISTS `t_payment_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户订单号',
  `user_id` bigint unsigned NOT NULL COMMENT '下单用户ID',
  `storage_plan_id` bigint unsigned NOT NULL COMMENT '下单时选择的套餐ID',
  `plan_code_snapshot` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐编码快照',
  `plan_version_snapshot` int unsigned NOT NULL COMMENT '套餐版本快照',
  `plan_name_snapshot` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐名称快照',
  `capacity_bytes_snapshot` bigint unsigned NOT NULL COMMENT '套餐容量快照(字节)',
  `duration_value_snapshot` int unsigned NOT NULL COMMENT '套餐有效期数值快照',
  `duration_unit_snapshot` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐有效期单位快照：DAY=天，MONTH=自然月，YEAR=自然年',
  `amount_cent` bigint unsigned NOT NULL COMMENT '应付金额(美元、人民币)',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种：USD=美元，CNY=人民币',
  `payment_channel` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PINGPONG' COMMENT '支付渠道：PINGPONG=乒乓支付',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING=待支付，PAID=已支付，CLOSED=已关闭，FAILED=支付失败，REFUNDED=已退款',
  `expire_time` datetime(3) NOT NULL COMMENT '订单支付过期时间(UTC，带毫秒)',
  `paid_time` datetime(3) DEFAULT NULL COMMENT '支付成功时间(UTC，带毫秒)',
  `closed_time` datetime(3) DEFAULT NULL COMMENT '订单关闭时间(UTC，带毫秒)',
  `client_request_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端创建订单幂等号',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_order_no` (`order_no`),
  UNIQUE KEY `uk_order_user_request` (`user_id`, `client_request_id`),
  KEY `idx_order_user_status_time` (`user_id`, `status`, `create_time`),
  KEY `idx_order_plan_id` (`storage_plan_id`),
  CONSTRAINT `chk_order_duration_unit` CHECK (`duration_unit_snapshot` IN ('DAY', 'MONTH', 'YEAR')),
  CONSTRAINT `chk_order_currency` CHECK (`currency` IN ('CNY','USD')),
  CONSTRAINT `chk_order_channel` CHECK (`payment_channel` = 'PINGPONG'),
  CONSTRAINT `chk_order_status` CHECK (`status` IN ('PENDING', 'PAID', 'CLOSED', 'FAILED', 'REFUNDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储套餐支付订单表';

CREATE TABLE IF NOT EXISTS `t_payment_callback` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_order_id` bigint unsigned DEFAULT NULL COMMENT '关联订单ID，无法匹配订单时可为空',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回调中的商户订单号',
  `payment_channel` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PINGPONG' COMMENT '支付渠道：PINGPONG=乒乓支付',
  `channel_transaction_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付平台交易号',
  `callback_event_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付平台回调事件ID',
  `raw_payload` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付回调原文',
  `signature_value` text COLLATE utf8mb4_unicode_ci COMMENT '回调签名原文',
  `signature_verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT '验签结果：0=失败，1=成功',
  `callback_amount_cent` bigint unsigned DEFAULT NULL COMMENT '回调支付金额(分)',
  `callback_currency` char(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回调币种',
  `callback_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付平台回调状态原值',
  `process_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECEIVED' COMMENT '处理状态：RECEIVED=已接收，SUCCESS=处理成功，REJECTED=已拒绝，FAILED=处理失败，DUPLICATE=重复回调',
  `failure_reason` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验签或处理失败原因',
  `received_time` datetime(3) NOT NULL COMMENT '回调接收时间(UTC，带毫秒)',
  `processed_time` datetime(3) DEFAULT NULL COMMENT '回调处理完成时间(UTC，带毫秒)',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_callback_content_hash` (`callback_content_hash`),
  UNIQUE KEY `uk_callback_channel_event` (`payment_channel`, `callback_event_id`),
  KEY `idx_callback_order_no` (`order_no`),
  KEY `idx_callback_transaction_no` (`channel_transaction_no`),
  KEY `idx_callback_process_status` (`process_status`, `received_time`),
  CONSTRAINT `chk_callback_signature` CHECK (`signature_verified` IN (0, 1)),
  CONSTRAINT `chk_callback_process_status` CHECK (`process_status` IN ('RECEIVED', 'SUCCESS', 'REJECTED', 'FAILED', 'DUPLICATE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='乒乓支付异步回调与审计表';

-- =========================================================
-- 5. 存储账户与独立权益
-- =========================================================

CREATE TABLE IF NOT EXISTS `t_user_storage_account` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `used_bytes` bigint unsigned NOT NULL DEFAULT 0 COMMENT '已确认照片占用容量(字节，仅计算原图)',
  `reserved_bytes` bigint unsigned NOT NULL DEFAULT 0 COMMENT '上传处理中预留容量(字节)',
  `lock_version` bigint unsigned NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_storage_account_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户存储用量账户表';

CREATE TABLE IF NOT EXISTS `t_storage_entitlement` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `entitlement_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权益业务编号',
  `user_id` bigint unsigned NOT NULL COMMENT '权益所属用户ID',
  `source_type` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权益来源：DEVICE_GIFT=设备绑定赠送，PURCHASE=用户购买',
  `payment_order_id` bigint unsigned DEFAULT NULL COMMENT '购买权益关联支付订单ID',
  `name_snapshot` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户购买权益名称快照',
  `capacity_bytes` bigint unsigned NOT NULL COMMENT '权益提供容量(字节)',
  `duration_value` int unsigned NOT NULL COMMENT '权益有效期数值快照',
  `duration_unit` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权益有效期单位快照：DAY=天，MONTH=自然月，YEAR=自然年',
  `user_time_zone_snapshot` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计算自然月或自然年到期时间时使用的IANA时区快照',
  `effective_time` datetime(3) NOT NULL COMMENT '权益生效时间(UTC，带毫秒)',
  `expire_time` datetime(3) NOT NULL COMMENT '权益到期时间(UTC，带毫秒)',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '权益状态：ACTIVE=有效，EXPIRED=已到期，REVOKED=已撤销',
  `expired_processed_time` datetime(3) DEFAULT NULL COMMENT '到期容量重算及清理处理完成时间(UTC，带毫秒)',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_entitlement_no` (`entitlement_no`),
  UNIQUE KEY `uk_entitlement_order` (`payment_order_id`),
  KEY `idx_entitlement_user_status_expire` (`user_id`, `status`, `expire_time`),
  CONSTRAINT `chk_entitlement_source` CHECK (`source_type` IN ('DEVICE_GIFT', 'PURCHASE')),
  CONSTRAINT `chk_entitlement_duration_unit` CHECK (`duration_unit` IN ('DAY', 'MONTH', 'YEAR')),
  CONSTRAINT `chk_entitlement_status` CHECK (`status` IN ('ACTIVE', 'EXPIRED', 'REVOKED')),
  CONSTRAINT `chk_entitlement_time` CHECK (`expire_time` > `effective_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户独立存储权益表';

-- =========================================================
-- 6. 设备上传照片与派生文件
-- =========================================================

CREATE TABLE IF NOT EXISTS `t_photo_file` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '照片所属用户ID，从上传会话的设备绑定的账号关系获取',
  `device_id` bigint unsigned NOT NULL COMMENT '绑定设备ID',
  `file_name` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `extension` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件扩展名',
  `mime_type` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原图MIME类型',
  `size_bytes` bigint unsigned NOT NULL COMMENT '文件实际大小(字节)，用于容量计费',
  `user_time_zone_snapshot` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上传时用户IANA时区快照',
  `upload_url_expire_time` datetime(3) NOT NULL COMMENT 'OSS临时上传链接过期时间(UTC，带毫秒)',
  `file_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件版本：ORIGINAL=原图，THUMBNAIL=缩略图，PREVIEW=预览图',
  `object_key` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OSS对象路径，不直接作为公网下载地址',
  `status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'URL_ISSUED' COMMENT '照片状态：URL_ISSUED=已签发上传链接，ORIGINAL_UPLOADED=原图上传回调成功，PROCESSING=派生图处理中，COMPLETED=处理完成，AVAILABLE=可访问，FAILED=处理失败，DELETE_PENDING=待永久删除，DELETE_FAILED=删除失败',
  `delete_reason` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '删除原因：未删除时为空；USER_MANUAL=用户主动删除，ENTITLEMENT_EXPIRED=权益到期自动清理',
  `uploaded_time` datetime(3) NOT NULL COMMENT '原图上传回调成功时间(UTC，带毫秒)，相册排序及清理依据',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_photo_file_type` (`photo_id`, `file_type`),
  UNIQUE KEY `uk_photo_file_object_key` (`object_key`),
  UNIQUE KEY `uk_photo_file_oss_callback` (`oss_callback_event_id`),
  KEY `idx_photo_file_status` (`status`),
  CONSTRAINT `chk_photo_file_type` CHECK (`file_type` IN ('ORIGINAL', 'THUMBNAIL', 'PREVIEW')),
  CONSTRAINT `chk_photo_file_status` CHECK (`status` IN ('URL_ISSUED','ORIGINAL_UPLOADED','PROCESSING','COMPLETED','AVAILABLE', 'DELETE_PENDING', 'DELETE_FAILED')),
  CONSTRAINT `chk_photo_file_delete_reason` CHECK (`delete_reason` IS NULL OR `delete_reason` IN ('USER_MANUAL', 'ENTITLEMENT_EXPIRED'))
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传照片记录，原图及派生文件表';

-- =========================================================
-- =========================================================
-- 7. 多语言通知
-- =========================================================

CREATE TABLE IF NOT EXISTS `t_notification` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notification_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知业务编号',
  `user_id` bigint unsigned NOT NULL COMMENT '通知用户ID',
  `notification_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：VERIFY_CODE=邮箱验证码，EXPIRY_REMINDER=权益到期提醒，CLEANUP_STARTED=自动清理开始，CLEANUP_COMPLETED=自动清理完成，PAYMENT_RESULT=支付结果',
  `language_code` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'en' COMMENT '通知语言：zh-CN=简体中文，en=英语，de=德语',
  `channel` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EMAIL' COMMENT '发送渠道：EMAIL=电子邮件，IN_APP=应用内通知',
  `recipient` varchar(320) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收地址或接收人标识',
  `subject` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知标题',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知正文',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '发送状态：PENDING=待发送，SENDING=发送中，SENT=发送成功，FAILED=发送失败',
  `retry_count` int unsigned NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `next_retry_time` datetime(3) DEFAULT NULL COMMENT '下次重试时间(UTC，带毫秒)',
  `sent_time` datetime(3) DEFAULT NULL COMMENT '发送成功时间(UTC，带毫秒)',
  `failure_reason` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送失败原因',
  `create_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间(UTC，带毫秒)',
  `update_by` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间(UTC，带毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_no` (`notification_no`),
  KEY `idx_notification_user_time` (`user_id`, `create_time`),
  CONSTRAINT `chk_notification_language` CHECK (`language_code` IN ('zh-CN', 'en', 'de')),
  CONSTRAINT `chk_notification_channel` CHECK (`channel` IN ('EMAIL', 'IN_APP')),
  CONSTRAINT `chk_notification_status` CHECK (`status` IN ('PENDING', 'SENDING', 'SENT', 'FAILED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户多语言通知发送记录表';
