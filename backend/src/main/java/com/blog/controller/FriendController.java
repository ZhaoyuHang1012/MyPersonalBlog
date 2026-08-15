package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.FriendRequestSendRequest;
import com.blog.service.FriendService;
import com.blog.util.SecurityUtil;
import com.blog.vo.FriendRequestVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 好友接口（需登录）
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 好友列表 */
    @GetMapping
    public Result<List<Map<String, Object>>> friends() {
        return Result.ok(friendService.listFriends(SecurityUtil.currentUserId()));
    }

    /** 收到的待处理申请 */
    @GetMapping("/requests")
    public Result<List<FriendRequestVO>> received() {
        return Result.ok(friendService.listReceived(SecurityUtil.currentUserId()));
    }

    /** 发出的申请 */
    @GetMapping("/requests/sent")
    public Result<List<FriendRequestVO>> sent() {
        return Result.ok(friendService.listSent(SecurityUtil.currentUserId()));
    }

    /** 发送申请 */
    @PostMapping("/requests")
    public Result<Void> send(@Valid @RequestBody FriendRequestSendRequest request) {
        friendService.sendRequest(SecurityUtil.currentUserId(), request);
        return Result.ok();
    }

    @PutMapping("/requests/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        friendService.approve(SecurityUtil.currentUserId(), id);
        return Result.ok();
    }

    @PutMapping("/requests/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        friendService.reject(SecurityUtil.currentUserId(), id);
        return Result.ok();
    }

    /** 删除好友 */
    @DeleteMapping("/{friendId}")
    public Result<Void> remove(@PathVariable Long friendId) {
        friendService.removeFriend(SecurityUtil.currentUserId(), friendId);
        return Result.ok();
    }

    /** 与目标用户的关系（用户主页按钮状态） */
    @GetMapping("/relation/{targetUserId}")
    public Result<Map<String, Object>> relation(@PathVariable Long targetUserId) {
        return Result.ok(friendService.relation(SecurityUtil.currentUserId(), targetUserId));
    }
}
