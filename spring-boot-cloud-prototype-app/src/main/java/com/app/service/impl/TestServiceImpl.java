package com.app.service.impl;

import com.app.mapper.TestMapper;
import com.app.pojo.dto.TestDto;
import com.app.pojo.entity.TestEntity;
import com.app.pojo.vo.TestVo;
import com.app.service.TestService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试服务实现类
 *
 * @author yanlei
 * @since 2026-09-05
 */
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, TestEntity> implements TestService {

    @Override
    public boolean save(TestDto.SaveOrUpdate dto) {
        TestEntity testEntity = new TestEntity();
        BeanUtils.copyProperties(dto, testEntity);
        return save(testEntity);
    }

    @Override
    public boolean delete(TestDto.Delete dto) {
        return removeById(dto.getId());
    }

    @Override
    public boolean update(TestDto.SaveOrUpdate dto) {
        TestEntity testEntity = new TestEntity();
        BeanUtils.copyProperties(dto, testEntity);
        return updateById(testEntity);
    }

    @Override
    public TestVo getTestById(Long id) {
        return convertToVo(super.getById(id));
    }

    @Override
    public List<TestVo> listTest() {
        return super.list().stream().map(this::convertToVo).toList();
    }

    /**
     * 将测试数据实体转换为前端测试数据对象。
     *
     * @param testEntity 测试数据实体
     * @return 方法处理后的结果
     */
    private TestVo convertToVo(TestEntity testEntity) {
        if (testEntity == null) {
            return null;
        }
        TestVo testVo = new TestVo();
        BeanUtils.copyProperties(testEntity, testVo);
        return testVo;
    }
}
