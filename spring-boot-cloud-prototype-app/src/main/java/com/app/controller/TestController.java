package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.TestDto;
import com.app.pojo.vo.TestVo;
import com.app.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试控制器
 *
 * @author yanlei
 * @since 2026-09-05
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestService testService;

    /**
     * 新增测试数据
     *
     * @param dto 测试数据
     * @return 新增结果
     */
    @PostMapping("/save")
    public CommonResult<Boolean> save(@RequestBody TestDto.SaveOrUpdate dto) {
        return CommonResult.success(testService.save(dto));
    }

    /**
     * 删除测试数据
     *
     * @param dto 删除参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody TestDto.Delete dto) {
        return CommonResult.success(testService.delete(dto));
    }

    /**
     * 修改测试数据
     *
     * @param dto 测试数据
     * @return 修改结果
     */
    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody TestDto.SaveOrUpdate dto) {
        return CommonResult.success(testService.update(dto));
    }

    /**
     * 根据ID查询测试数据
     *
     * @param id 测试数据ID
     * @return 测试数据
     */
    @GetMapping("/getById")
    public CommonResult<TestVo> getById(@RequestParam("id") Long id) {
        return CommonResult.success(testService.getTestById(id));
    }

    /**
     * 查询全部测试数据
     *
     * @return 测试数据列表
     */
    @GetMapping("/list")
    public CommonResult<List<TestVo>> list() {
        return CommonResult.success(testService.listTest());
    }
}
