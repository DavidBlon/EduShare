package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.TagDto;
import com.wb.learningresourcelibrary.entity.Tag;
import com.wb.learningresourcelibrary.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 标签控制器
 */
@RestController("admin.tagController")
@RequestMapping("/api/admin/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取所有标签（含资源数量）
     */
    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return Result.success(tagService.getAllTagsWithCount());
    }

    /**
     * 新增标签
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody TagDto tagDto) {
        tagService.addTag(tagDto);
        return Result.success("新增标签成功");
    }

    /**
     * 编辑标签
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody TagDto tagDto) {
        tagService.updateTag(tagDto);
        return Result.success("更新标签成功");
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success("删除标签成功");
    }
}
