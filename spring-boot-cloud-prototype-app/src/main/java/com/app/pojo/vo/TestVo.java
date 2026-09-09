package com.app.pojo.vo;

import com.app.pojo.entity.BaseField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * test实体类
 *
 * @author yanlei
 * @since 2025-11-11
 */
@Data
@Accessors(chain = true)
public class TestVo extends BaseField {
    /** 主键id */
    private Long id;

    /** 姓名 */
    private String name;
}