package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.vo.UploadFileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件服务：本地磁盘存储 + 媒体库管理
 */
@Slf4j
@Service
public class FileService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    @Value("${blog.upload.dir:../uploads}")
    private String uploadDir;

    @Value("${blog.upload.max-size:10485760}")
    private long maxSize;

    private Path baseDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 保存上传文件，返回访问信息
     */
    public UploadFileVO store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }
        if (file.getSize() > maxSize) {
            throw new BizException("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }
        String ext = extractExt(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持图片格式：jpg / jpeg / png / gif / webp / bmp");
        }
        LocalDate now = LocalDate.now();
        String rel = String.format("%d/%02d/%s.%s", now.getYear(), now.getMonthValue(),
                UUID.randomUUID().toString().replace("-", ""), ext);
        Path target = baseDir().resolve(rel).normalize();
        if (!target.startsWith(baseDir())) {
            throw new BizException("非法的文件路径");
        }
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BizException(500, "文件保存失败，请稍后重试");
        }
        UploadFileVO vo = new UploadFileVO();
        vo.setName(rel);
        vo.setUrl("/uploads/" + rel);
        vo.setSize(file.getSize());
        vo.setLastModified(LocalDateTime.now());
        return vo;
    }

    /**
     * 媒体库列表（按修改时间倒序，内存分页）
     */
    public PageResult<UploadFileVO> list(int page, int size) {
        List<UploadFileVO> all = new ArrayList<>();
        Path base = baseDir();
        if (!Files.exists(base)) {
            return new PageResult<>(all, 0, page, size);
        }
        try (Stream<Path> stream = Files.walk(base)) {
            all = stream.filter(Files::isRegularFile)
                    .map(p -> {
                        UploadFileVO vo = new UploadFileVO();
                        String rel = base.relativize(p).toString().replace('\\', '/');
                        vo.setName(rel);
                        vo.setUrl("/uploads/" + rel);
                        try {
                            vo.setSize(Files.size(p));
                            vo.setLastModified(LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(p).toInstant(), ZoneId.systemDefault()));
                        } catch (IOException e) {
                            vo.setSize(0);
                        }
                        return vo;
                    })
                    .sorted(Comparator.comparing(UploadFileVO::getLastModified,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("读取媒体库失败", e);
            throw new BizException(500, "读取媒体库失败");
        }
        int from = Math.min((page - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        List<UploadFileVO> records = from >= all.size() ? new ArrayList<>() : all.subList(from, to);
        return new PageResult<>(records, all.size(), page, size);
    }

    /**
     * 删除媒体文件
     */
    public void delete(String name) {
        if (name == null || name.isBlank() || name.contains("..")) {
            throw new BizException("非法的文件路径");
        }
        Path target = baseDir().resolve(name).normalize();
        if (!target.startsWith(baseDir()) || !Files.exists(target)) {
            throw new BizException(404, "文件不存在");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.error("删除文件失败", e);
            throw new BizException(500, "删除失败，请稍后重试");
        }
    }

    private String extractExt(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }
}
