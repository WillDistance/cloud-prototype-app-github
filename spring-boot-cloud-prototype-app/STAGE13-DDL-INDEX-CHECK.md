# 阶段13 DDL/索引检查（不执行）

现有 `t_photo.idx_photo_album (user_id, status, uploaded_time, id)` 已覆盖按用户、状态、`uploaded_time ASC, id ASC` 的清理选择。

权益到期扫描建议先检查、后按需执行（本次未执行）：

```sql
SHOW INDEX FROM t_storage_entitlement;

ALTER TABLE t_storage_entitlement
  ADD KEY idx_entitlement_expiration_scan (status, expire_time, expired_processed_time);
```

仅当 `SHOW INDEX` 不含 `idx_entitlement_expiration_scan` 时执行 `ALTER TABLE`；生产库应在变更窗口评估执行计划与锁影响。
