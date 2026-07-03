package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.dto.KeywordRuleDto;
import com.wb.learningresourcelibrary.entity.KeywordRule;
import com.wb.learningresourcelibrary.mapper.KeywordRuleMapper;
import com.wb.learningresourcelibrary.service.KeywordRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关键词规则 Service 实现
 */
@Service
@RequiredArgsConstructor
public class KeywordRuleServiceImpl extends ServiceImpl<KeywordRuleMapper, KeywordRule> implements KeywordRuleService {

    private final KeywordRuleMapper keywordRuleMapper;

    @Override
    @Cacheable("keywordRule:active")
    public List<KeywordRule> getActiveRules() {
        return this.lambdaQuery()
                .eq(KeywordRule::getStatus, 1)
                .eq(KeywordRule::getIsDeleted, Constants.NOT_DELETED)
                .orderByAsc(KeywordRule::getSort)
                .list();
    }

    @Override
    @Cacheable("keywordRule:byType")
    public Map<String, List<KeywordRule>> getRulesByType() {
        List<KeywordRule> all = this.lambdaQuery()
                .eq(KeywordRule::getIsDeleted, Constants.NOT_DELETED)
                .orderByAsc(KeywordRule::getSort)
                .list();
        return all.stream().collect(Collectors.groupingBy(
                KeywordRule::getType,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    @Override
    @CacheEvict(cacheNames = {"keywordRule:active", "keywordRule:byType"}, allEntries = true)
    public void addRule(KeywordRuleDto dto) {
        // 检查是否已存在相同关键词+类型的规则
        long count = this.count(new LambdaQueryWrapper<KeywordRule>()
                .eq(KeywordRule::getKeyword, dto.getKeyword())
                .eq(KeywordRule::getType, dto.getType())
                .eq(KeywordRule::getIsDeleted, Constants.NOT_DELETED));
        if (count > 0) {
            throw BusinessException.badRequest("该关键词+类型的规则已存在");
        }

        KeywordRule rule = new KeywordRule();
        rule.setKeyword(dto.getKeyword());
        rule.setType(dto.getType());
        rule.setTargetName(dto.getTargetName());
        rule.setSort(dto.getSort() != null ? dto.getSort() : 0);
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(rule);
    }

    @Override
    @CacheEvict(cacheNames = {"keywordRule:active", "keywordRule:byType"}, allEntries = true)
    public void updateRule(KeywordRuleDto dto) {
        KeywordRule rule = this.getById(dto.getId());
        if (rule == null) {
            throw BusinessException.notFound("规则不存在");
        }

        long count = this.count(new LambdaQueryWrapper<KeywordRule>()
                .eq(KeywordRule::getKeyword, dto.getKeyword())
                .eq(KeywordRule::getType, dto.getType())
                .ne(KeywordRule::getId, dto.getId())
                .eq(KeywordRule::getIsDeleted, Constants.NOT_DELETED));
        if (count > 0) {
            throw BusinessException.badRequest("该关键词+类型的规则已存在");
        }

        rule.setKeyword(dto.getKeyword());
        rule.setType(dto.getType());
        rule.setTargetName(dto.getTargetName());
        rule.setSort(dto.getSort() != null ? dto.getSort() : 0);
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.updateById(rule);
    }

    @Override
    @CacheEvict(cacheNames = {"keywordRule:active", "keywordRule:byType"}, allEntries = true)
    public void deleteRule(Long id) {
        KeywordRule rule = this.getById(id);
        if (rule == null) {
            throw BusinessException.notFound("规则不存在");
        }
        this.removeById(id);
    }
}
