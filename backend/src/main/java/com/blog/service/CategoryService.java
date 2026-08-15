package com.blog.service;

import com.blog.common.BizException;
import com.blog.entity.Category;
import com.blog.mapper.CategoryMapper;
import com.blog.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<CategoryVO> list() {
        return categoryMapper.listWithPostCount();
    }

    @Transactional
    public Category create(String name, Integer sort) {
        checkName(name);
        Category category = new Category();
        category.setName(name.trim());
        category.setSort(sort == null ? 0 : sort);
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public void update(Long id, String name, Integer sort) {
        checkName(name);
        Category category = new Category();
        category.setId(id);
        category.setName(name.trim());
        category.setSort(sort);
        categoryMapper.updateById(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryMapper.clearPostsCategory(id);
        categoryMapper.deleteById(id);
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException("分类名不能为空");
        }
        if (name.trim().length() > 50) {
            throw new BizException("分类名不能超过 50 个字符");
        }
    }
}
