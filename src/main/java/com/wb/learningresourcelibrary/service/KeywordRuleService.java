package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.KeywordRuleDto;
import com.wb.learningresourcelibrary.entity.KeywordRule;

import java.util.List;
import java.util.Map;

/**
 * 关键词规则 Service
 */
public interface KeywordRuleService extends IService<KeywordRule> {

    /**
     * 获取所有启用的规则
     */
    List<KeywordRule> getActiveRules();

    /**
     * 按类型分组获取规则
     */
    Map<String, List<KeywordRule>> getRulesByType();

    /**
     * 新增规则
     */
    void addRule(KeywordRuleDto dto);

    /**
     * 编辑规则
     */
    void updateRule(KeywordRuleDto dto);

    /**
     * 删除规则
     */
    void deleteRule(Long id);
}
