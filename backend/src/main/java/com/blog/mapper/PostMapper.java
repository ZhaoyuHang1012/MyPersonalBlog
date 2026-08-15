package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Post;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface PostMapper extends BaseMapper<Post> {

    /** 浏览量 +1 */
    @Update("UPDATE posts SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /** 评论数 +1 */
    @Update("UPDATE posts SET comment_count = comment_count + 1 WHERE id = #{id}")
    int incrCommentCount(@Param("id") Long id);

    /** 评论数 -1（不小于 0） */
    @Update("UPDATE posts SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    int decrCommentCount(@Param("id") Long id);

    /** 点赞数 +1 */
    @Update("UPDATE posts SET like_count = like_count + 1 WHERE id = #{id}")
    int incrLikeCount(@Param("id") Long id);

    /** 点赞数 -1（不小于 0） */
    @Update("UPDATE posts SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    int decrLikeCount(@Param("id") Long id);
}
