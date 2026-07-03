package com.wb.learningresourcelibrary.controller.front;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.entity.Tag;
import com.wb.learningresourcelibrary.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 - 标签控制器（无需登录）
 */
@RestController("front.tagController")
@RequestMapping("/api/front/tag")
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
}
