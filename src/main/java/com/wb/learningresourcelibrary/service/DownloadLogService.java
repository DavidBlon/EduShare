package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.entity.DownloadLog;

/**
 * 下载日志 Service
 */
public interface DownloadLogService extends IService<DownloadLog> {

    /**
     * 记录下载日志
     */
    void recordLog(Long resourceId, String resourceTitle, String ipAddress);

    /**
     * 分页查询下载日志
     */
    IPage<DownloadLog> getLogPage(Integer page, Integer pageSize);
}
