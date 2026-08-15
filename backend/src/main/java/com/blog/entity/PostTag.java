package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章-标签关联
 */
@Data
@TableName("post_tag")
public class PostTag {

    private Long postId;

    private Long tagId;
}
