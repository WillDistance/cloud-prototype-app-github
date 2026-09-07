package com.app.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 测试数据传输对象
 *
 * @author yanlei
 * @since 2026-09-05
 */
@Data
@Accessors(chain = true)
public class TestDto {
    /**
     * 测试查询参数
     *
     * @author yanlei
     * @since 2026-09-05
     */
    @Data
    @Accessors(chain = true)
    public static class Query {
        /** 用户姓名 */
        @NotBlank
        @Length(max = 100)
        private String name;
    }

    /**
     * 测试保存或更新参数
     *
     * @author yanlei
     * @since 2026-09-05
     */
    @Data
    @Accessors(chain = true)
    public static class SaveOrUpdate {
        /** id */
        private Long id;

        /** 用户姓名 */
        private String name;
    }

    /**
     * 测试删除参数
     *
     * @author yanlei
     * @since 2026-09-05
     */
    @Data
    @Accessors(chain = true)
    public static class Delete {
        /** id */
        @NotNull
        private Long id;
    }
}
