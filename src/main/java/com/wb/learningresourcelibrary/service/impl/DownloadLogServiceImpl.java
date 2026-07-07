package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.dto.DailyDownloadStatDTO;
import com.wb.learningresourcelibrary.dto.DailyTopResourceDTO;
import com.wb.learningresourcelibrary.dto.DownloadStatDTO;
import com.wb.learningresourcelibrary.entity.DownloadLog;
import com.wb.learningresourcelibrary.mapper.DownloadLogMapper;
import com.wb.learningresourcelibrary.service.DownloadLogService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 下载日志 Service 实现
 *
 * 自动清理超过保留天数的日志记录，防止数据库无限增长。
 * 保留天数通过 application.properties 中的 log.retention.days 配置，默认 90 天。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadLogServiceImpl extends ServiceImpl<DownloadLogMapper, DownloadLog> implements DownloadLogService {

    private final DownloadLogMapper downloadLogMapper;

    /** 日志保留天数 */
    @Value("${log.retention.days:90}")
    private int retentionDays;

    @Override
    public void recordLog(Long resourceId, String resourceTitle, String ipAddress) {
        DownloadLog log = new DownloadLog();
        log.setResourceId(resourceId);
        log.setResourceTitle(resourceTitle);
        log.setIpAddress(ipAddress);
        downloadLogMapper.insert(log);
    }

    @Override
    public IPage<DownloadLog> getLogPage(Integer page, Integer pageSize) {
        Page<DownloadLog> pageParam = new Page<>(page, pageSize);
        return this.page(pageParam, new LambdaQueryWrapper<DownloadLog>()
                .orderByDesc(DownloadLog::getCreatedAt));
    }

    @Override
    public List<DownloadStatDTO> getDownloadStats(Integer limit) {
        QueryWrapper<DownloadLog> wrapper = new QueryWrapper<DownloadLog>()
                .select("resource_id, ANY_VALUE(resource_title) as resource_title, COUNT(*) as download_count")
                .groupBy("resource_id")
                .orderByDesc("download_count")
                .last("LIMIT " + limit);
        List<Map<String, Object>> maps = downloadLogMapper.selectMaps(wrapper);
        return maps.stream().map(m -> {
            DownloadStatDTO dto = new DownloadStatDTO();
            dto.setResourceId(((Number) m.get("resource_id")).longValue());
            dto.setResourceTitle((String) m.get("resource_title"));
            dto.setDownloadCount(((Number) m.get("download_count")).intValue());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DailyDownloadStatDTO> getDailyStats(Integer days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        QueryWrapper<DownloadLog> wrapper = new QueryWrapper<DownloadLog>()
                .select("DATE(created_at) as date, COUNT(*) as count")
                .ge("created_at", since)
                .groupBy("DATE(created_at)")
                .orderByAsc("DATE(created_at)");
        List<Map<String, Object>> maps = downloadLogMapper.selectMaps(wrapper);
        return maps.stream().map(m -> {
            DailyDownloadStatDTO dto = new DailyDownloadStatDTO();
            dto.setDate(String.valueOf(m.get("date")));
            dto.setCount(((Number) m.get("count")).intValue());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DailyTopResourceDTO> getDailyTopResources(String date, Integer limit) {
        QueryWrapper<DownloadLog> wrapper = new QueryWrapper<DownloadLog>()
                .select("resource_id, ANY_VALUE(resource_title) as resource_title, COUNT(*) as download_count")
                .ge("created_at", date + " 00:00:00")
                .lt("created_at", date + " 23:59:59")
                .groupBy("resource_id")
                .orderByDesc("download_count")
                .last("LIMIT " + limit);
        List<Map<String, Object>> maps = downloadLogMapper.selectMaps(wrapper);
        return maps.stream().map(m -> {
            DailyTopResourceDTO dto = new DailyTopResourceDTO();
            dto.setResourceTitle((String) m.get("resource_title"));
            dto.setDownloadCount(((Number) m.get("download_count")).intValue());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 定时清理超过保留天数的下载日志
     * 默认每天凌晨 3:00 执行一次
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredLogs() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(retentionDays);
        try {
            int deleted = downloadLogMapper.delete(
                    new LambdaQueryWrapper<DownloadLog>()
                            .lt(DownloadLog::getCreatedAt, deadline)
            );
            if (deleted > 0) {
                log.info("已清理 {} 条超过 {} 天的下载日志记录", deleted, retentionDays);
            }
        } catch (Exception e) {
            log.error("清理过期下载日志失败", e);
        }
    }
}
