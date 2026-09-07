package com.app.config;

import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.utils.UserLoginInfoUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * mybatis-plus基础字段内容填充处理器
 *
 * @author yanlei
 * @since 2022-11-15
 */
@Component
public class BaseFieldMetaObjectHandler implements MetaObjectHandler {

    private final Clock clock;
    private final UserLoginInfoUtil userInfoUtil;

    @Autowired
    public BaseFieldMetaObjectHandler(UserLoginInfoUtil userInfoUtil) {
        this(Clock.systemUTC(), userInfoUtil);
    }

    BaseFieldMetaObjectHandler(Clock clock, UserLoginInfoUtil userInfoUtil) {
        this.clock = clock;
        this.userInfoUtil = userInfoUtil;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        this.setFieldValByName("deleteFlag", 0, metaObject);
        this.fillStrategy(metaObject, "createBy", userId);
        this.fillStrategy(metaObject, "createTime", now);
        this.fillStrategy(metaObject, "updateBy", userId);
        this.fillStrategy(metaObject, "updateTime", now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userAccount = getCurrentUserId();
        this.fillStrategy(metaObject, "updateBy", userAccount);
        this.setFieldValByName("updateTime", LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC), metaObject);
    }

    /**
     * 处理getCurrentUserId相关的业务逻辑。
     *
     * @return 处理结果
     */
    private String getCurrentUserId() {
        if (RequestContextHolder.getRequestAttributes() == null || userInfoUtil == null) {
            return null;
        }
        AuthenticatedUser user = UserContextHolder.get();
        if (user == null) {
            return null;
        }
        return userInfoUtil.getUserId();
    }
}
