package com.app.service.impl;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.LanguageEnum;
import com.app.exception.BusinessException;
import com.app.exception.RequestParameterException;
import com.app.mapper.UserMapper;
import com.app.pojo.dto.UpdateLanguageRequest;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.UserVo;
import com.app.service.UserService;
import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户资料业务实现。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserVo getProfile() {
        return toVo(currentUser());
    }

    @Override
    @Transactional
    public UserVo updateLanguage(UpdateLanguageRequest request) {
        String language = supportedLanguage(request.getLanguage());
        UserEntity user = currentUser();
        user.setPreferredLanguage(language);
        userMapper.updateById(user);
        return toVo(user);
    }

    /**
     * 从请求级认证上下文读取当前用户并查询最新资料。
     *
     * @return 当前用户实体
     */
    private UserEntity currentUser() {
        Long userId = UserContextHolderUtil.getUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_REQUIRED);
        }
        return user;
    }

    /**
     * 校验语言代码并返回系统统一的持久化值。
     *
     * @param language 待校验的语言代码
     * @return 支持的标准语言代码
     */
    private String supportedLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new RequestParameterException(ErrorCodeEnum.USER_LANGUAGE_UNSUPPORTED);
        }
        for (LanguageEnum item : LanguageEnum.values()) {
            if (item.getValue().equalsIgnoreCase(language)) {
                return item.getValue();
            }
        }
        throw new RequestParameterException(ErrorCodeEnum.USER_LANGUAGE_UNSUPPORTED);
    }

    /**
     * 将用户实体转换为不包含密码和审计字段的用户资料响应。
     *
     * @param user 用户实体
     * @return 用户资料响应
     */
    private UserVo toVo(UserEntity user) {
        return new UserVo(user.getId(), user.getEmail(), user.getTimeZone(), user.getPreferredLanguage());
    }
}
