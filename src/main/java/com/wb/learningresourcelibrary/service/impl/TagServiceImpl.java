package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.dto.TagDto;
import com.wb.learningresourcelibrary.entity.Tag;
import com.wb.learningresourcelibrary.mapper.TagMapper;
import com.wb.learningresourcelibrary.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签 Service 实现
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final TagMapper tagMapper;

    @Override
    @Cacheable("tag:all")
    public List<Tag> getAllTagsWithCount() {
        List<Tag> tags = this.lambdaQuery()
                .eq(Tag::getIsDeleted, Constants.NOT_DELETED)
                .orderByAsc(Tag::getSort)
                .list();

        // 填充每个标签的资源数量
        for (Tag tag : tags) {
            Integer count = tagMapper.countResourcesByTagId(tag.getId());
            tag.setResourceCount(count);
        }

        return tags;
    }

    @Override
    @CacheEvict(cacheNames = {"tag:all", "tag:byResource"}, allEntries = true)
    public void addTag(TagDto dto) {
        long count = this.count(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, dto.getName())
                .eq(Tag::getIsDeleted, Constants.NOT_DELETED));
        if (count > 0) {
            throw BusinessException.badRequest("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setSort(dto.getSort() != null ? dto.getSort() : 0);
        tag.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        this.save(tag);
    }

    @Override
    @CacheEvict(cacheNames = {"tag:all", "tag:byResource"}, allEntries = true)
    public void updateTag(TagDto dto) {
        Tag tag = this.getById(dto.getId());
        if (tag == null) {
            throw BusinessException.notFound("标签不存在");
        }

        long count = this.count(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, dto.getName())
                .ne(Tag::getId, dto.getId())
                .eq(Tag::getIsDeleted, Constants.NOT_DELETED));
        if (count > 0) {
            throw BusinessException.badRequest("标签名称已存在");
        }

        tag.setName(dto.getName());
        tag.setSort(dto.getSort() != null ? dto.getSort() : 0);
        tag.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        this.updateById(tag);
    }

    @Override
    @CacheEvict(cacheNames = {"tag:all", "tag:byResource"}, allEntries = true)
    public void deleteTag(Long id) {
        Tag tag = this.getById(id);
        if (tag == null) {
            throw BusinessException.notFound("标签不存在");
        }
        this.removeById(id);
    }

    @Override
    @Cacheable(value = "tag:byResource", key = "#resourceId")
    public List<Tag> getTagsByResourceId(Long resourceId) {
        return baseMapper.selectListByResourceId(resourceId);
    }
}
