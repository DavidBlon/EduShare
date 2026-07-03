package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.ResourceDto;
import com.wb.learningresourcelibrary.dto.ResourceQueryDto;
import com.wb.learningresourcelibrary.entity.Resource;

import java.util.List;

/**
 * 资源 Service
 */
public interface ResourceService extends IService<Resource> {

    /**
     * 分页查询资源（前端用户）
     */
    IPage<Resource> queryResources(ResourceQueryDto query);

    /**
     * 分页查询资源（管理端）
     */
    IPage<Resource> queryAdminResources(ResourceQueryDto query);

    /**
     * 获取资源详情（含标签）
     */
    Resource getResourceDetail(Long id);

    /**
     * 新增资源
     */
    void addResource(ResourceDto dto);

    /**
     * 编辑资源
     */
    void updateResource(ResourceDto dto);

    /**
     * 删除资源（软删除）
     */
    void deleteResource(Long id);

    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);

    /**
     * 增加下载量
     */
    void incrementDownloadCount(Long id);

    /**
     * 获取推荐资源
     */
    List<Resource> getRecommendResources(int limit);

    /**
     * 获取热门资源
     */
    List<Resource> getHotResources(int limit);

    /**
     * 获取最新资源
     */
    List<Resource> getLatestResources(int limit);

    /**
     * 切换推荐状态
     */
    void toggleRecommend(Long id);
}
