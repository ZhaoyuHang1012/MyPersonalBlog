package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Murmur;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface MurmurMapper extends BaseMapper<Murmur> {

    @Update("UPDATE murmurs SET like_count = like_count + 1 WHERE id = #{id}")
    int incrLikeCount(@Param("id") Long id);

    @Update("UPDATE murmurs SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    int decrLikeCount(@Param("id") Long id);
}
