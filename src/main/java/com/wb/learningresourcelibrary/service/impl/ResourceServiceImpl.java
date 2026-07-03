package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.dto.ResourceDto;
import com.wb.learningresourcelibrary.dto.ResourceQueryDto;
import com.wb.learningresourcelibrary.entity.Resource;
import com.wb.learningresourcelibrary.entity.ResourceTag;
import com.wb.learningresourcelibrary.entity.Tag;
import com.wb.learningresourcelibrary.mapper.ResourceMapper;
import com.wb.learningresourcelibrary.mapper.ResourceTagMapper;
import com.wb.learningresourcelibrary.mapper.TagMapper;
import com.wb.learningresourcelibrary.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final ResourceTagMapper resourceTagMapper;
    private final TagMapper tagMapper;

    @Override
    public IPage<Resource> queryResources(ResourceQueryDto query) {
        Page<Resource> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<Resource> result = resourceMapper.selectResourceWithCategory(page, query);
        fillTagsForResources(result.getRecords());
        return result;
    }

    @Override
    public IPage<Resource> queryAdminResources(ResourceQueryDto query) {
        Page<Resource> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<Resource> result = resourceMapper.selectResourceWithCategory(page, query);
        fillTagsForResources(result.getRecords());
        return result;
    }

    @Override
    @Cacheable(value = "resource:detail", key = "#id")
    public Resource getResourceDetail(Long id) {
        Resource resource = this.getById(id);
        if (resource == null || resource.getIsDeleted() == 1) {
            throw BusinessException.notFound("资源不存在");
        }

        // 填充标签
        List<Long> tagIds = resourceTagMapper.selectTagIdsByResourceId(id);
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            resource.setTags(tags);
        }

        return resource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"resource:recommend", "resource:hot", "resource:latest", "resource:detail"}, allEntries = true)
    public void addResource(ResourceDto dto) {
        // 如果设为推荐，检查上限
        if (Constants.RECOMMEND_YES.equals(dto.getIsRecommend())) {
            checkRecommendLimit();
        }
        Resource resource = new Resource();
        setResourceFields(resource, dto);
        resource.setViewCount(0);
        resource.setDownloadCount(0);
        this.save(resource);

        // 保存标签关联
        saveTagRelations(resource.getId(), dto.getTagIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"resource:recommend", "resource:hot", "resource:latest", "resource:detail"}, allEntries = true)
    public void updateResource(ResourceDto dto) {
        Resource resource = this.getById(dto.getId());
        if (resource == null) {
            throw BusinessException.notFound("资源不存在");
        }
        // 如果设为推荐且之前不是推荐，检查上限
        if (Constants.RECOMMEND_YES.equals(dto.getIsRecommend())
                && Constants.RECOMMEND_NO.equals(resource.getIsRecommend())) {
            checkRecommendLimit();
        }

        setResourceFields(resource, dto);
        this.updateById(resource);

        // 更新标签关联：先删后插
        resourceTagMapper.deleteByResourceId(resource.getId());
        saveTagRelations(resource.getId(), dto.getTagIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"resource:recommend", "resource:hot", "resource:latest", "resource:detail"}, allEntries = true)
    public void deleteResource(Long id) {
        Resource resource = this.getById(id);
        if (resource == null) {
            throw BusinessException.notFound("资源不存在");
        }
        // 软删除
        this.removeById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        resourceMapper.incrementViewCount(id);
    }

    @Override
    public void incrementDownloadCount(Long id) {
        resourceMapper.incrementDownloadCount(id);
    }

    @Override
    @Cacheable("resource:recommend")
    public List<Resource> getRecommendResources(int limit) {
        return this.lambdaQuery()
                .eq(Resource::getIsDeleted, Constants.NOT_DELETED)
                .eq(Resource::getStatus, Constants.STATUS_ENABLE)
                .eq(Resource::getIsRecommend, Constants.RECOMMEND_YES)
                .orderByAsc(Resource::getSort)
                .orderByDesc(Resource::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    @Cacheable("resource:hot")
    public List<Resource> getHotResources(int limit) {
        return this.lambdaQuery()
                .eq(Resource::getIsDeleted, Constants.NOT_DELETED)
                .eq(Resource::getStatus, Constants.STATUS_ENABLE)
                .orderByDesc(Resource::getDownloadCount)
                .orderByAsc(Resource::getSort)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    @Cacheable("resource:latest")
    public List<Resource> getLatestResources(int limit) {
        return this.lambdaQuery()
                .eq(Resource::getIsDeleted, Constants.NOT_DELETED)
                .eq(Resource::getStatus, Constants.STATUS_ENABLE)
                .orderByDesc(Resource::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    @CacheEvict(cacheNames = {"resource:recommend", "resource:hot", "resource:latest", "resource:detail"}, allEntries = true)
    public void toggleRecommend(Long id) {
        Resource resource = this.getById(id);
        if (resource == null) {
            throw BusinessException.notFound("资源不存在");
        }
        // 如果是取消推荐，不限制；如果是设为推荐，检查是否已达上限
        if (Constants.RECOMMEND_NO.equals(resource.getIsRecommend())) {
            checkRecommendLimit();
        }
        resource.setIsRecommend(Constants.RECOMMEND_YES.equals(resource.getIsRecommend())
                ? Constants.RECOMMEND_NO : Constants.RECOMMEND_YES);
        this.updateById(resource);
    }

    /**
     * 检查推荐资源是否已达上限（4个）
     */
    private void checkRecommendLimit() {
        long count = this.lambdaQuery()
                .eq(Resource::getIsDeleted, Constants.NOT_DELETED)
                .eq(Resource::getIsRecommend, Constants.RECOMMEND_YES)
                .count();
        if (count >= 4) {
            throw BusinessException.badRequest("推荐资源已达上限（4个），请先取消其他推荐");
        }
    }

    /**
     * 设置资源字段
     */
    private void setResourceFields(Resource resource, ResourceDto dto) {
        resource.setTitle(dto.getTitle());
        resource.setDescription(dto.getDescription());
        if (dto.getCover() != null) {
            resource.setCover(dto.getCover());
        }
        resource.setCategoryId(dto.getCategoryId());
        resource.setNetdiskLink(dto.getNetdiskLink());
        resource.setNetdiskCode(dto.getNetdiskCode());
        resource.setIsRecommend(dto.getIsRecommend() != null ? dto.getIsRecommend() : 0);
        resource.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        resource.setSort(dto.getSort() != null ? dto.getSort() : 0);
    }

    /**
     * 保存资源-标签关联
     */
    private void saveTagRelations(Long resourceId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            List<ResourceTag> relations = tagIds.stream()
                    .filter(tagId -> tagId != null)
                    .map(tagId -> {
                        ResourceTag rt = new ResourceTag();
                        rt.setResourceId(resourceId);
                        rt.setTagId(tagId);
                        return rt;
                    })
                    .collect(Collectors.toList());
            relations.forEach(resourceTagMapper::insert);
        }
    }

    /**
     * 填充资源的标签信息
     */
    private void fillTagsForResources(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) return;

        for (Resource resource : resources) {
            List<Long> tagIds = resourceTagMapper.selectTagIdsByResourceId(resource.getId());
            if (!tagIds.isEmpty()) {
                List<Tag> tags = tagMapper.selectBatchIds(tagIds);
                resource.setTags(tags);
            }
        }
    }
}
