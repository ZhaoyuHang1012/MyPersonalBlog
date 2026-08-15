package com.blog.util;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 简单的内存 IP 限流器：同一 IP 在窗口期内最多提交 N 次
 * （个人博客场景足够；单机部署，重启即重置）
 */
@Component
public class IpRateLimiter {

    private static final int WINDOW_MS = 60_000;
    private static final int MAX_REQUESTS = 5;
    private static final int MAX_IPS = 10_000;

    private final Map<String, Deque<Long>> store = new ConcurrentHashMap<>();

    public boolean tryAcquire(String ip) {
        if (store.size() > MAX_IPS) {
            store.clear();
        }
        long now = System.currentTimeMillis();
        Deque<Long> queue = store.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && now - queue.peekFirst() > WINDOW_MS) {
                queue.pollFirst();
            }
            if (queue.size() >= MAX_REQUESTS) {
                return false;
            }
            queue.addLast(now);
            return true;
        }
    }
}
