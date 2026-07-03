package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.AnnouncementDto;
import com.wb.learningresourcelibrary.entity.Announcement;

/**
 * 公告 Service
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页获取公告列表（管理端，含草稿和已发布）
     */
    IPage<Announcement> getAdminPage(Integer page, Integer pageSize);

    /**
     * 分页获取已发布公告列表（前台）
     */
    IPage<Announcement> getPublishedPage(Integer page, Integer pageSize);

    /**
     * 新增公告
     */
    void addAnnouncement(AnnouncementDto dto, Long adminId);

    /**
     * 编辑公告
     */
    void updateAnnouncement(AnnouncementDto dto);

    /**
     * 发布/撤回公告
     */
    void togglePublish(Long id, Integer isPublished);

    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);

    /**
     * 获取公告详情
     */
    Announcement getDetail(Long id);

    /**
     * 获取未读公告数量
     */
    Long getUnreadCount(Long lastReadId);
}
