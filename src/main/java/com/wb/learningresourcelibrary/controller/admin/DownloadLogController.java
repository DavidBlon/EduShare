package com.wb.learningresourcelibrary.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.entity.DownloadLog;
import com.wb.learningresourcelibrary.service.DownloadLogService;
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
}
