package com.wb.learningresourcelibrary.controller.front;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.entity.Category;
import com.wb.learningresourcelibrary.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 分类控制器（无需登录）
 */
@RestController("front.categoryController")
@RequestMapping("/api/front/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类树（树形结构）
     */
    @GetMapping("/tree")
    public Result<List<Category>> tree() {
        return Result.success(categoryService.getCategoryTree());
    }

    /**
     * 按层级获取分类（1-小学 2-初中 3-高中 4-中考 5-高考）
     */
    @GetMapping("/level/{level}")
    public Result<List<Category>> byLevel(@PathVariable Integer level) {
        return Result.success(categoryService.getCategoriesByLevel(level));
    }
}
