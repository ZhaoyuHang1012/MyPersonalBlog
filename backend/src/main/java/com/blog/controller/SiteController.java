package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.SiteService;
import com.blog.vo.SiteInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点信息接口
 */
@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @GetMapping
    public Result<SiteInfoVO> info() {
        return Result.ok(siteService.info());
    }
}
