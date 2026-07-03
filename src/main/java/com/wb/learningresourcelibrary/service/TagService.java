package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.TagDto;
import com.wb.learningresourcelibrary.entity.Tag;

import java.util.List;

/**
 * 标签 Service
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取所有标签（含资源数量）
     */
    List<Tag> getAllTagsWithCount();

    /**
     * 新增标签
     */
    void addTag(TagDto dto);

    /**
     * 编辑标签
     */
    void updateTag(TagDto dto);

    /**
     * 删除标签
     */
    void deleteTag(Long id);

    /**
     * 根据资源ID获取标签列表
     */
    List<Tag> getTagsByResourceId(Long resourceId);
}
