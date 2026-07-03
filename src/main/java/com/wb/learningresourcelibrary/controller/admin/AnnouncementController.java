package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.AnnouncementDto;
import com.wb.learningresourcelibrary.entity.Announcement;
import com.wb.learningresourcelibrary.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 - 公告控制器
 */
@RestController("admin.announcementController")
@RequestMapping("/api/admin/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 分页获取公告列表（管理端）
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        var pageResult = announcementService.getAdminPage(page, pageSize);
        return Result.success(Map.of(
                "records", pageResult.getRecords(),
                "total", pageResult.getTotal()
        ));
    }

    /**
     * 新增公告
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody AnnouncementDto dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ADMIN_ID_ATTR);
        announcementService.addAnnouncement(dto, adminId);
        return Result.success("新增公告成功");
    }

    /**
     * 编辑公告
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AnnouncementDto dto) {
        announcementService.updateAnnouncement(dto);
        return Result.success("更新公告成功");
    }

    /**
     * 发布/撤回公告
     */
    @PutMapping("/publish")
    public Result<Void> togglePublish(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer isPublished = Integer.valueOf(body.get("isPublished").toString());
        announcementService.togglePublish(id, isPublished);
        String msg = isPublished == 1 ? "发布公告成功" : "撤回公告成功";
        return Result.success(msg);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success("删除公告成功");
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        return Result.success(announcementService.getDetail(id));
    }
}
