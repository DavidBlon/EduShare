package com.wb.learningresourcelibrary.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.DailyDownloadStatDTO;
import com.wb.learningresourcelibrary.dto.DailyTopResourceDTO;
import com.wb.learningresourcelibrary.dto.DownloadStatDTO;
import com.wb.learningresourcelibrary.entity.DownloadLog;
import com.wb.learningresourcelibrary.service.DownloadLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 下载日志控制器
 */
@RestController("admin.downloadLogController")
@RequestMapping("/api/admin/download-log")
@RequiredArgsConstructor
public class DownloadLogController {

    private final DownloadLogService downloadLogService;

    /**
     * 分页查询下载日志
     */
    @GetMapping("/list")
    public Result<IPage<DownloadLog>> list(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(downloadLogService.getLogPage(page, pageSize));
    }

    /**
     * 获取下载统计（按资源分组）
     */
    @GetMapping("/stats")
    public Result<List<DownloadStatDTO>> stats(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(downloadLogService.getDownloadStats(limit));
    }

    /**
     * 获取每日下载统计（最近 N 天）
     */
    @GetMapping("/daily-stats")
    public Result<List<DailyDownloadStatDTO>> dailyStats(@RequestParam(defaultValue = "15") Integer days) {
        return Result.success(downloadLogService.getDailyStats(days));
    }

    /**
     * 获取某日下载量 Top N 资源
     */
    @GetMapping("/daily-top")
    public Result<List<DailyTopResourceDTO>> dailyTop(@RequestParam String date,
                                                      @RequestParam(defaultValue = "3") Integer limit) {
        return Result.success(downloadLogService.getDailyTopResources(date, limit));
    }
}
