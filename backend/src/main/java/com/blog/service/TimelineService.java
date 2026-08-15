package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.AlbumGroup;
import com.blog.entity.Murmur;
import com.blog.entity.Post;
import com.blog.mapper.AlbumGroupMapper;
import com.blog.mapper.MurmurMapper;
import com.blog.mapper.PostMapper;
import com.blog.vo.TimelineGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 时间线归档服务：文章/说说/相册按月份归档
 * 登录用户展示自己+好友的公开内容；未登录展示全部公开内容
 */
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final PostMapper postMapper;
    private final MurmurMapper murmurMapper;
    private final AlbumGroupMapper albumGroupMapper;

    public List<TimelineGroupVO> timeline(Long viewerId) {
        // 文章
        QueryWrapper<Post> postQw = new QueryWrapper<Post>().eq("status", 1).eq("visibility", 1);
        applyFriendScope(postQw, viewerId);
        List<Post> posts = postMapper.selectList(postQw);

        // 说说
        QueryWrapper<Murmur> murmurQw = new QueryWrapper<Murmur>().eq("visibility", 1);
        applyFriendScope(murmurQw, viewerId);
        List<Murmur> murmurs = murmurMapper.selectList(murmurQw);

        // 相册
        QueryWrapper<AlbumGroup> albumQw = new QueryWrapper<AlbumGroup>().eq("visibility", 1);
        applyFriendScope(albumQw, viewerId);
        List<AlbumGroup> albums = albumGroupMapper.selectList(albumQw);

        // 汇总为统一条目并倒序
        List<TimelineGroupVO.ItemVO> all = new ArrayList<>();
        for (Post p : posts) {
            all.add(new TimelineGroupVO.ItemVO("post", p.getId(), p.getTitle(), p.getPublishedAt()));
        }
        for (Murmur m : murmurs) {
            String content = m.getContent();
            String title = content.length() > 60 ? content.substring(0, 60) + "…" : content;
            all.add(new TimelineGroupVO.ItemVO("murmur", m.getId(), title, m.getCreatedAt()));
        }
        for (AlbumGroup a : albums) {
            all.add(new TimelineGroupVO.ItemVO("album", a.getId(), a.getName(), a.getCreatedAt()));
        }
        all.sort(Comparator.comparing(TimelineGroupVO.ItemVO::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // 按年-月分组（保持时间倒序）
        Map<String, TimelineGroupVO> groups = new LinkedHashMap<>();
        for (TimelineGroupVO.ItemVO item : all) {
            LocalDateTime time = item.getCreatedAt();
            if (time == null) {
                continue;
            }
            String key = time.getYear() + "-" + time.getMonthValue();
            TimelineGroupVO group = groups.computeIfAbsent(key, k -> {
                TimelineGroupVO g = new TimelineGroupVO();
                g.setYear(time.getYear());
                g.setMonth(time.getMonthValue());
                return g;
            });
            group.getItems().add(item);
        }
        return new ArrayList<>(groups.values());
    }

    private <T> void applyFriendScope(QueryWrapper<T> qw, Long viewerId) {
        if (viewerId != null) {
            qw.and(w -> w.eq("user_id", viewerId)
                    .or().inSql("user_id",
                            "SELECT friend_id FROM friends WHERE user_id = " + viewerId));
        }
    }
}
