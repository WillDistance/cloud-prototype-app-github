package com.app.service;

import com.app.pojo.dto.TestDto;
import com.app.pojo.entity.TestEntity;
import com.app.pojo.vo.TestVo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 测试服务接口
 *
 * @author yanlei
 * @since 2026-09-05
 */
public interface TestService extends IService<TestEntity> {

    /**
     * 新增测试数据。
     *
     * @param dto 测试数据请求
     * @return 是否新增成功
     */
    boolean save(TestDto.SaveOrUpdate dto);

    /**
     * 删除测试数据。
     *
     * @param dto 测试数据删除请求
     * @return 是否删除成功
     */
    boolean delete(TestDto.Delete dto);

    /**
     * 修改测试数据。
     *
     * @param dto 测试数据请求
     * @return 是否修改成功
     */
    boolean update(TestDto.SaveOrUpdate dto);

    /**
     * 根据ID查询测试数据。
     *
     * @param id 测试数据ID
     * @return 测试数据
     */
    TestVo getTestById(Long id);

    /**
     * 查询全部测试数据。
     *
     * @return 测试数据列表
     */
    List<TestVo> listTest();
}
