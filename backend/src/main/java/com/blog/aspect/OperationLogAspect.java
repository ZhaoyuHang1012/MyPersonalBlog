package com.blog.aspect;

import com.blog.entity.OperationLog;
import com.blog.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面：记录后台写操作（POST/PUT/DELETE）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_PARAMS_LENGTH = 500;

    private final OperationLogMapper logMapper;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.blog.controller.admin..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        boolean isWrite = method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
        if (!isWrite && method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            isWrite = Arrays.asList(mapping.method()).stream()
                    .anyMatch(m -> m.name().equals("POST") || m.name().equals("PUT") || m.name().equals("DELETE"));
        }
        if (!isWrite) {
            return pjp.proceed();
        }
        long start = System.currentTimeMillis();
        boolean success = true;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            success = false;
            throw t;
        } finally {
            try {
                record(pjp, start, success);
            } catch (Exception e) {
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    private void record(ProceedingJoinPoint pjp, long start, boolean success) {
        HttpServletRequest request = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            request = attrs.getRequest();
        }
        OperationLog entry = new OperationLog();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long uid) {
            entry.setUserId(uid);
            entry.setUsername(auth.getDetails() == null ? String.valueOf(uid) : auth.getDetails().toString());
        }
        entry.setMethod(request == null ? "" : request.getMethod());
        entry.setUri(request == null ? pjp.getSignature().toShortString() : request.getRequestURI());
        entry.setParams(serializeParams(pjp.getArgs()));
        entry.setIp(request == null ? null : clientIp(request));
        entry.setDurationMs((int) (System.currentTimeMillis() - start));
        entry.setSuccess(success ? 1 : 0);
        entry.setCreatedAt(LocalDateTime.now());
        logMapper.insert(entry);
    }

    private String serializeParams(Object[] args) {
        Object[] safe = Arrays.stream(args)
                .filter(a -> !(a instanceof MultipartFile)
                        && !(a instanceof HttpServletRequest)
                        && !(a instanceof HttpServletResponse))
                .toArray();
        if (safe.length == 0) {
            return "[]";
        }
        try {
            String json = objectMapper.writeValueAsString(safe);
            // 敏感字段打码
            json = json.replaceAll("(?i)(\"(?:oldPassword|newPassword|password)\"\\s*:\\s*\")[^\"]*(\")", "$1***$2");
            return json.length() > MAX_PARAMS_LENGTH ? json.substring(0, MAX_PARAMS_LENGTH) + "..." : json;
        } catch (Exception e) {
            return "[参数序列化失败]";
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
