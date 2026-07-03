package com.wb.learningresourcelibrary.controller.front;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.entity.Announcement;
import com.wb.learningresourcelibrary.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户端 - 公告控制器（无需登录）
 */
@RestController("front.announcementController")
@RequestMapping("/api/front/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 分页获取已发布公告列表
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        var pageResult = announcementService.getPublishedPage(page, pageSize);
        return Result.success(Map.of(
                "records", pageResult.getRecords(),
                "total", pageResult.getTotal()
        ));
    }

    /**
     * 获取最新公告信息（最新ID + 总数，用于未读判断）
     */
    @GetMapping("/latest")
    public Result<Map<String, Object>> latest() {
        var pageResult = announcementService.getPublishedPage(1, 1);
        List<Announcement> records = pageResult.getRecords();
        Long latestId = records.isEmpty() ? 0L : records.get(0).getId();
        Long total = pageResult.getTotal();
        return Result.success(Map.of(
                "latestId", latestId,
                "total", total
        ));
    }

    /**
     * 获取未读公告数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(@RequestParam(defaultValue = "0") Long lastReadId) {
        Long count = announcementService.getUnreadCount(lastReadId);
        return Result.success(Map.of("count", count));
    }

    /**
     * 获取最近的公告列表（用于弹窗展示）
     */
    @GetMapping("/recent")
    public Result<List<Announcement>> recent() {
        var pageResult = announcementService.getPublishedPage(1, 5);
        return Result.success(pageResult.getRecords());
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        return Result.success(announcementService.getDetail(id));
    }
}
