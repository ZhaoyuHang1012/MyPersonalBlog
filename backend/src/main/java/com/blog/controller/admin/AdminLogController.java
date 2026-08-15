package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.OperationLog;
import com.blog.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台操作日志接口
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final OperationLogMapper logMapper;

    @GetMapping
    public Result<PageResult<OperationLog>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "15") int size) {
        Page<OperationLog> result = logMapper.selectPage(new Page<>(page, size),
                new QueryWrapper<OperationLog>().orderByDesc("id"));
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(),
                result.getCurrent(), result.getSize()));
    }
}
