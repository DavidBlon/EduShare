package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.CategoryDto;
import com.wb.learningresourcelibrary.entity.Category;

import java.util.List;

/**
 * 分类 Service
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树（前端使用）
     */
    List<Category> getCategoryTree();

    /**
     * 获取所有分类列表（扁平，管理端使用）
     */
    List<Category> getAllCategories();

    /**
     * 新增分类
     */
    void addCategory(CategoryDto dto);

    /**
     * 编辑分类
     */
    void updateCategory(CategoryDto dto);

    /**
     * 删除分类（含子分类检查）
     */
    void deleteCategory(Long id);

    /**
     * 根据层级获取分类
     */
    List<Category> getCategoriesByLevel(Integer level);
}
