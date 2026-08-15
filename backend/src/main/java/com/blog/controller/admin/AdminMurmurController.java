package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.MurmurRequest;
import com.blog.entity.Murmur;
import com.blog.service.MurmurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台说说管理接口
 */
@RestController
@RequestMapping("/api/admin/murmurs")
@RequiredArgsConstructor
public class AdminMurmurController {

    private final MurmurService murmurService;

    @GetMapping
    public Result<List<Murmur>> list() {
        return Result.ok(murmurService.list());
    }

    @PostMapping
    public Result<Murmur> create(@Valid @RequestBody MurmurRequest request) {
        return Result.ok(murmurService.create(request.getContent()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        murmurService.delete(id);
        return Result.ok();
    }
}
