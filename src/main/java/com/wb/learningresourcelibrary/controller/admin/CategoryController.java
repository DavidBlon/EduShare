package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.CategoryDto;
import com.wb.learningresourcelibrary.entity.Category;
import com.wb.learningresourcelibrary.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 分类控制器
 */
@RestController("admin.categoryController")
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类（扁平列表，含禁用）
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.getAllCategories());
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CategoryDto categoryDto) {
        categoryService.addCategory(categoryDto);
        return Result.success("新增分类成功");
    }

    /**
     * 编辑分类
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CategoryDto categoryDto) {
        categoryService.updateCategory(categoryDto);
        return Result.success("更新分类成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除分类成功");
    }
}
