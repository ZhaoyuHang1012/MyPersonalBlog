package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Category;
import com.blog.vo.CategoryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {

    /** 分类列表（含已发布文章数） */
    @Select("SELECT c.id, c.name, c.sort, c.created_at, COUNT(p.id) AS post_count " +
            "FROM categories c " +
            "LEFT JOIN posts p ON p.category_id = c.id AND p.status = 1 " +
            "GROUP BY c.id, c.name, c.sort, c.created_at " +
            "ORDER BY c.sort ASC, c.id ASC")
    List<CategoryVO> listWithPostCount();

    /** 删除分类前，将其下文章的分类置空 */
    @Update("UPDATE posts SET category_id = NULL WHERE category_id = #{categoryId}")
    int clearPostsCategory(@Param("categoryId") Long categoryId);
}
