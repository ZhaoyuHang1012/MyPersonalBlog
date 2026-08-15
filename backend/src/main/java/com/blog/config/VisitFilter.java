package com.blog.config;

import com.blog.entity.Visit;
import com.blog.mapper.VisitMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 访问记录过滤器：统计前台文章列表/详情的 PV
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitFilter extends OncePerRequestFilter {

    private static final Pattern LIST_PATTERN = Pattern.compile("^/api/posts$");
    private static final Pattern DETAIL_PATTERN = Pattern.compile("^/api/posts/(\\d+)$");

    private final VisitMapper visitMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(request, response);
        try {
            if (!"GET".equalsIgnoreCase(request.getMethod()) || response.getStatus() >= 400) {
                return;
            }
            String uri = request.getRequestURI();
            Matcher detailMatcher = DETAIL_PATTERN.matcher(uri);
            boolean isDetail = detailMatcher.matches();
            boolean isList = LIST_PATTERN.matcher(uri).matches();
            if (!isDetail && !isList) {
                return;
            }
            Visit visit = new Visit();
            visit.setPostId(isDetail ? Long.parseLong(detailMatcher.group(1)) : null);
            visit.setPath(uri);
            visit.setIp(clientIp(request));
            visit.setCreatedAt(LocalDateTime.now());
            visitMapper.insert(visit);
        } catch (Exception e) {
            // 统计失败不影响业务
            log.debug("访问记录失败: {}", e.getMessage());
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
