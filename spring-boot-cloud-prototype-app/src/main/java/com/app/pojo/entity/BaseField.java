package com.app.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 表基础字段
 *
 * @author yanlei
 * @since 2026-09-05
 */
@Data
@Accessors(chain = true)
public class BaseField {
    /** 创建人 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;


    /** 创建时间（UTC，带毫秒） */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;


    /** 更新时间（UTC，带毫秒） */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public BaseField setCreateUpdateUserId(Long userId) {
        this.createBy = userId;
        this.updateBy = userId;
        return this;
    }

    public BaseField setCreateUpdateDate(LocalDateTime date) {
        this.createTime = date;
        this.updateTime = date;
        return this;
    }
}
