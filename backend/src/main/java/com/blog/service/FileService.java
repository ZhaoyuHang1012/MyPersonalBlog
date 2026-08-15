package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
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
 * 文件服务：每个用户独立存储目录（uploads/{username}/...），支持图片与视频，配额控制
 */
@Slf4j
@Service
public class FileService {

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "webm", "mov", "m4v", "avi");

    @Value("${blog.upload.dir:../uploads}")
    private String uploadDir;

    @Value("${blog.upload.max-size:10485760}")
    private long imageMaxSize;

    @Value("${blog.upload.video-max-size:209715200}")
    private long videoMaxSize;

    private final UserMapper userMapper;

    public FileService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    private Path baseDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String usernameOf(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "登录状态已失效");
        }
        return user.getUsername();
    }

    private Path userDir(Long userId) {
        return baseDir().resolve(usernameOf(userId));
    }

    /** 当前用户已用空间（字节） */
    public long usage(Long userId) {
        Path dir = userDir(userId);
        if (!Files.exists(dir)) {
            return 0;
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile).mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    return 0;
                }
            }).sum();
        } catch (IOException e) {
            return 0;
        }
    }

    /** 当前用户配额（字节） */
    public long quotaOf(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null || user.getQuota() == null ? 1073741824L : user.getQuota();
    }

    /**
     * 保存上传文件到当前用户的存储空间（图片/视频分类型限制大小）
     */
    public UploadFileVO store(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }
        String ext = extractExt(file.getOriginalFilename());
        boolean isVideo = VIDEO_EXT.contains(ext);
        if (!IMAGE_EXT.contains(ext) && !isVideo) {
            throw new BizException("仅支持图片（jpg/jpeg/png/gif/webp/bmp）或视频（mp4/webm/mov/m4v/avi）");
        }
        long limit = isVideo ? videoMaxSize : imageMaxSize;
        if (file.getSize() > limit) {
            String label = isVideo ? "视频" : "图片";
            throw new BizException(label + "大小不能超过 " + (limit / 1024 / 1024) + "MB");
        }
        // 配额校验
        long quota = quotaOf(userId);
        if (usage(userId) + file.getSize() > quota) {
            throw new BizException("存储空间不足（配额 " + (quota / 1024 / 1024) + "MB），请清理后重试");
        }
        String username = usernameOf(userId);
        LocalDate now = LocalDate.now();
        String rel = String.format("%s/%d/%02d/%s.%s", username, now.getYear(), now.getMonthValue(),
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
        vo.setMediaType(isVideo ? "video" : "image");
        vo.setLastModified(LocalDateTime.now());
        return vo;
    }

    /**
     * 媒体库列表（管理员 all=true 查看全部，可指定用户名筛选；普通用户仅自己的）
     */
    public PageResult<UploadFileVO> list(Long userId, int page, int size, boolean all, String username) {
        List<UploadFileVO> list = new ArrayList<>();
        Path scanRoot;
        if (all) {
            scanRoot = (username != null && !username.isBlank())
                    ? baseDir().resolve(username).normalize()
                    : baseDir();
        } else {
            scanRoot = userDir(userId);
        }
        if (!scanRoot.startsWith(baseDir()) || !Files.exists(scanRoot)) {
            return new PageResult<>(list, 0, page, size);
        }
        try (Stream<Path> stream = Files.walk(scanRoot)) {
            list = stream.filter(Files::isRegularFile)
                    .map(p -> {
                        UploadFileVO vo = new UploadFileVO();
                        String rel = baseDir().relativize(p).toString().replace('\\', '/');
                        vo.setName(rel);
                        vo.setUrl("/uploads/" + rel);
                        try {
                            vo.setSize(Files.size(p));
                            vo.setLastModified(LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(p).toInstant(), ZoneId.systemDefault()));
                        } catch (IOException e) {
                            vo.setSize(0);
                        }
                        vo.setMediaType(resolveMediaType(rel));
                        return vo;
                    })
                    .sorted(Comparator.comparing(UploadFileVO::getLastModified,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("读取媒体库失败", e);
            throw new BizException(500, "读取媒体库失败");
        }
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        List<UploadFileVO> records = from >= list.size() ? new ArrayList<>() : list.subList(from, to);
        return new PageResult<>(records, list.size(), page, size);
    }

    /**
     * 删除媒体文件（普通用户仅能删除自己的）
     */
    public void delete(Long userId, String name, boolean isAdmin) {
        if (name == null || name.isBlank() || name.contains("..")) {
            throw new BizException("非法的文件路径");
        }
        Path target = baseDir().resolve(name).normalize();
        if (!target.startsWith(baseDir()) || !Files.exists(target)) {
            throw new BizException(404, "文件不存在");
        }
        if (!isAdmin && !name.startsWith(usernameOf(userId) + "/")) {
            throw new BizException(403, "无权删除他人文件");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.error("删除文件失败", e);
            throw new BizException(500, "删除失败，请稍后重试");
        }
    }

    private String resolveMediaType(String relPath) {
        String ext = extractExt(relPath);
        return VIDEO_EXT.contains(ext) ? "video" : "image";
    }

    private String extractExt(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
