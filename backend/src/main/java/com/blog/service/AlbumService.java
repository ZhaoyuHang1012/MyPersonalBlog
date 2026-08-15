package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.entity.Album;
import com.blog.mapper.AlbumMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 相册服务
 */
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumMapper albumMapper;

    /** 前台：全部相册图片（倒序） */
    public List<Album> list() {
        return albumMapper.selectList(new QueryWrapper<Album>().orderByDesc("id"));
    }

    @Transactional
    public Album create(String url, String description) {
        if (!url.trim().startsWith("/uploads/") && !url.trim().startsWith("http")) {
            throw new BizException("图片地址需为 /uploads/ 开头的本站地址或 http(s) 地址");
        }
        Album album = new Album();
        album.setUrl(url.trim());
        album.setDescription(description == null ? null : description.trim());
        album.setCreatedAt(LocalDateTime.now());
        albumMapper.insert(album);
        return album;
    }

    @Transactional
    public void delete(Long id) {
        if (albumMapper.deleteById(id) == 0) {
            throw new BizException(404, "图片不存在");
        }
    }
}
