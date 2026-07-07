package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.DailyDownloadStatDTO;
import com.wb.learningresourcelibrary.dto.DailyTopResourceDTO;
import com.wb.learningresourcelibrary.dto.DownloadStatDTO;
import com.wb.learningresourcelibrary.entity.DownloadLog;
import java.util.List;

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

    /**
     * 获取按资源分组的下载统计（下载量 Top N）
     */
    List<DownloadStatDTO> getDownloadStats(Integer limit);

    /**
     * 获取每日下载统计（最近 N 天）
     */
    List<DailyDownloadStatDTO> getDailyStats(Integer days);

    /**
     * 获取某日下载量 Top N 资源
     */
    List<DailyTopResourceDTO> getDailyTopResources(String date, Integer limit);
}
