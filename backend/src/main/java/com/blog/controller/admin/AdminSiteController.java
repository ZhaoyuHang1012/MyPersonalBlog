package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.SiteUpdateRequest;
import com.blog.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台站点设置接口
 */
@RestController
@RequestMapping("/api/admin/site")
@RequiredArgsConstructor
public class AdminSiteController {

    private final SiteService siteService;

    @PutMapping
    public Result<Void> update(@RequestBody SiteUpdateRequest request) {
        siteService.update(request);
        return Result.ok();
    }
}
