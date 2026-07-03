package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.KeywordRuleDto;
import com.wb.learningresourcelibrary.entity.KeywordRule;
import com.wb.learningresourcelibrary.service.KeywordRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端 - 关键词规则控制器
 */
@RestController("admin.keywordRuleController")
@RequestMapping("/api/admin/keyword-rule")
@RequiredArgsConstructor
public class KeywordRuleController {

    private final KeywordRuleService keywordRuleService;

    /**
     * 获取所有规则（按类型分组）
     */
    @GetMapping("/list")
    public Result<Map<String, List<KeywordRule>>> list() {
        return Result.success(keywordRuleService.getRulesByType());
    }

    /**
     * 新增规则
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody KeywordRuleDto dto) {
        keywordRuleService.addRule(dto);
        return Result.success("新增规则成功");
    }

    /**
     * 编辑规则
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody KeywordRuleDto dto) {
        keywordRuleService.updateRule(dto);
        return Result.success("更新规则成功");
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        keywordRuleService.deleteRule(id);
        return Result.success("删除规则成功");
    }
}
