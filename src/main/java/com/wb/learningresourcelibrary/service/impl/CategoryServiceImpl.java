package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.dto.CategoryDto;
import com.wb.learningresourcelibrary.entity.Category;
import com.wb.learningresourcelibrary.mapper.CategoryMapper;
import com.wb.learningresourcelibrary.service.CategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类 Service 实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    @Cacheable("category:tree")
    public List<Category> getCategoryTree() {
        List<Category> all = this.lambdaQuery()
                .eq(Category::getIsDeleted, Constants.NOT_DELETED)
                .eq(Category::getStatus, Constants.STATUS_ENABLE)
                .orderByAsc(Category::getSort)
                .list();
        return buildTree(all, 0L);
    }

    @Override
    @Cacheable("category:all")
    public List<Category> getAllCategories() {
        return this.lambdaQuery()
                .eq(Category::getIsDeleted, Constants.NOT_DELETED)
                .orderByAsc(Category::getSort)
                .list();
    }

    @Override
    @CacheEvict(cacheNames = {"category:tree", "category:all", "category:level"}, allEntries = true)
    public void addCategory(CategoryDto dto) {
        // 检查同级下是否有重名
        long count = this.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, dto.getParentId() != null ? dto.getParentId() : 0)
                .eq(Category::getName, dto.getName())
                .eq(Category::getIsDeleted, Constants.NOT_DELETED));
        if (count > 0) {
            throw BusinessException.badRequest("同级分类下已存在同名分类");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : 0);
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        this.save(category);
    }

    @Override
    @CacheEvict(cacheNames = {"category:tree", "category:all", "category:level"}, allEntries = true)
    public void updateCategory(CategoryDto dto) {
        Category category = this.getById(dto.getId());
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }

        // 检查同级下是否有重名
        long count = this.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, dto.getParentId() != null ? dto.getParentId() : 0)
                .eq(Category::getName, dto.getName())
                .ne(Category::getId, dto.getId())
                .eq(Category::getIsDeleted, Constants.NOT_DELETED));
        if (count > 0) {
            throw BusinessException.badRequest("同级分类下已存在同名分类");
        }

        category.setName(dto.getName());
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : 0);
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        this.updateById(category);
    }

    @Override
    @CacheEvict(cacheNames = {"category:tree", "category:all", "category:level"}, allEntries = true)
    public void deleteCategory(Long id) {
        Category category = this.getById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }

        // 检查是否有子分类
        long childCount = this.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id)
                .eq(Category::getIsDeleted, Constants.NOT_DELETED));
        if (childCount > 0) {
            throw BusinessException.badRequest("该分类下有子分类，无法直接删除");
        }

        this.removeById(id);
    }

    @Override
    @Cacheable(value = "category:level", key = "#level")
    public List<Category> getCategoriesByLevel(Integer level) {
        return this.lambdaQuery()
                .eq(Category::getIsDeleted, Constants.NOT_DELETED)
                .eq(Category::getStatus, Constants.STATUS_ENABLE)
                .eq(Category::getLevel, level)
                .orderByAsc(Category::getSort)
                .list();
    }

    /**
     * 递归构建分类树
     */
    private List<Category> buildTree(List<Category> all, Long parentId) {
        List<Category> children = all.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .collect(Collectors.toList());

        for (Category child : children) {
            List<Category> grandChildren = buildTree(all, child.getId());
            if (!grandChildren.isEmpty()) {
                child.setChildren(grandChildren);
            }
        }

        return children;
    }
}
