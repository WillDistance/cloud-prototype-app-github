-- PayPal Checkout一次性迁移脚本。
-- 生产执行前请先备份数据库，并确认当前表尚未存在同名字段/索引。
ALTER TABLE t_payment_order
    DROP CHECK chk_order_channel,
    DROP CHECK chk_order_currency,
    ADD COLUMN provider_order_id varchar(128) NULL COMMENT '支付平台订单号' AFTER payment_channel,
    ADD COLUMN provider_capture_id varchar(128) NULL COMMENT '支付平台扣款交易号' AFTER provider_order_id,
    ADD COLUMN payment_method varchar(32) NULL COMMENT '支付方式：PAYPAL_WALLET或CARD' AFTER provider_capture_id,
    ADD COLUMN checkout_url varchar(1024) NULL COMMENT '支付平台托管收银台地址' AFTER payment_method,
    ADD UNIQUE KEY uk_order_provider_order (provider_order_id),
    ADD UNIQUE KEY uk_order_provider_capture (provider_capture_id),
    ADD CONSTRAINT chk_order_channel CHECK (payment_channel IN ('PAYPAL','PINGPONG')),
    ADD CONSTRAINT chk_order_currency CHECK (currency IN ('CNY','USD','EUR','GBP'));

ALTER TABLE t_payment_callback
    MODIFY payment_channel varchar(32) NOT NULL DEFAULT 'PAYPAL'
        COMMENT '支付渠道：PAYPAL=PayPal Checkout';

ALTER TABLE t_storage_plan
    DROP CHECK chk_plan_currency,
    ADD CONSTRAINT chk_plan_currency CHECK (currency IN ('CNY','USD','EUR','GBP'));
