package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Tag;
import com.blog.vo.TagVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {

    /** 标签列表（含已发布文章数） */
    @Select("SELECT t.id, t.name, t.created_at, COUNT(p.id) AS post_count " +
            "FROM tags t " +
            "LEFT JOIN post_tag pt ON pt.tag_id = t.id " +
            "LEFT JOIN posts p ON p.id = pt.post_id AND p.status = 1 " +
            "GROUP BY t.id, t.name, t.created_at " +
            "ORDER BY t.id ASC")
    List<TagVO> listWithPostCount();
}
