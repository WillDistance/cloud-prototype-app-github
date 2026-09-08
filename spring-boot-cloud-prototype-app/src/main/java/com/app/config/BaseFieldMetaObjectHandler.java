package com.app.config;

import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * mybatis-plus基础字段内容填充处理器
 *
 * @author yanlei
 * @since 2022-11-15
 */
@Component
public class BaseFieldMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = UserContextHolderUtil.get() == null ? null : UserContextHolderUtil.getUserId();
        LocalDateTime now = LocalDateTime.now();
        this.setFieldValByName("deleteFlag", 0, metaObject);
        this.fillStrategy(metaObject, "createBy", userId);
        this.fillStrategy(metaObject, "createTime", now);
        this.fillStrategy(metaObject, "updateBy", userId);
        this.fillStrategy(metaObject, "updateTime", now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userAccount = UserContextHolderUtil.get() == null ? null : UserContextHolderUtil.getUserId();
        this.fillStrategy(metaObject, "updateBy", userAccount);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
