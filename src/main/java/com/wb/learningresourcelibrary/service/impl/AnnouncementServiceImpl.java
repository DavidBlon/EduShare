package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.dto.AnnouncementDto;
import com.wb.learningresourcelibrary.entity.Admin;
import com.wb.learningresourcelibrary.entity.Announcement;
import com.wb.learningresourcelibrary.mapper.AnnouncementMapper;
import com.wb.learningresourcelibrary.service.AdminService;
import com.wb.learningresourcelibrary.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告 Service 实现
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final AdminService adminService;

    @Override
    public IPage<Announcement> getAdminPage(Integer page, Integer pageSize) {
        Page<Announcement> pageParam = new Page<>(page, pageSize);
        IPage<Announcement> result = this.page(pageParam,
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getIsDeleted, Constants.NOT_DELETED)
                        .orderByDesc(Announcement::getCreatedAt)
        );

        // 填充发布管理员昵称
        for (Announcement a : result.getRecords()) {
            Admin admin = adminService.getById(a.getAdminId());
            if (admin != null) {
                a.setAdminName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
            }
        }

        return result;
    }

    @Override
    public IPage<Announcement> getPublishedPage(Integer page, Integer pageSize) {
        Page<Announcement> pageParam = new Page<>(page, pageSize);
        IPage<Announcement> result = this.page(pageParam,
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getIsDeleted, Constants.NOT_DELETED)
                        .eq(Announcement::getIsPublished, 1)
                        .orderByDesc(Announcement::getCreatedAt)
        );

        // 填充发布管理员昵称
        for (Announcement a : result.getRecords()) {
            Admin admin = adminService.getById(a.getAdminId());
            if (admin != null) {
                a.setAdminName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
            }
        }

        return result;
    }

    @Override
    @CacheEvict(cacheNames = {"announcement:detail", "announcement:unread"}, allEntries = true)
    public void addAnnouncement(AnnouncementDto dto, Long adminId) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setAdminId(adminId);
        announcement.setIsPublished(0); // 默认为草稿

        this.save(announcement);
    }

    @Override
    @CacheEvict(cacheNames = {"announcement:detail", "announcement:unread"}, allEntries = true)
    public void updateAnnouncement(AnnouncementDto dto) {
        Announcement announcement = this.getById(dto.getId());
        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());

        this.updateById(announcement);
    }

    @Override
    @CacheEvict(cacheNames = {"announcement:detail", "announcement:unread"}, allEntries = true)
    public void togglePublish(Long id, Integer isPublished) {
        Announcement announcement = this.getById(id);
        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        announcement.setIsPublished(isPublished);
        this.updateById(announcement);
    }

    @Override
    @CacheEvict(cacheNames = {"announcement:detail", "announcement:unread"}, allEntries = true)
    public void deleteAnnouncement(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        this.removeById(id);
    }

    @Override
    @Cacheable(value = "announcement:detail", key = "#id")
    public Announcement getDetail(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        // 填充发布管理员昵称
        Admin admin = adminService.getById(announcement.getAdminId());
        if (admin != null) {
            announcement.setAdminName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
        }

        return announcement;
    }

    @Override
    public Long getUnreadCount(Long lastReadId) {
        if (lastReadId == null || lastReadId <= 0) {
            // 没有读过任何公告，返回总数
            return this.count(new LambdaQueryWrapper<Announcement>()
                    .eq(Announcement::getIsDeleted, Constants.NOT_DELETED)
                    .eq(Announcement::getIsPublished, 1));
        }
        return this.count(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getIsDeleted, Constants.NOT_DELETED)
                .eq(Announcement::getIsPublished, 1)
                .gt(Announcement::getId, lastReadId));
    }
}
