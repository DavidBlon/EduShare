package com.wb.learningresourcelibrary.controller.front;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.entity.Admin;
import com.wb.learningresourcelibrary.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台 - 联系方式控制器
 */
@RestController("front.contactController")
@RequestMapping("/api/front/contact")
@RequiredArgsConstructor
public class ContactController {

    private final AdminService adminService;

    /**
     * 获取公开联系方式
     */
    @GetMapping
    public Result<Admin> getContact() {
        return Result.success(adminService.getPublicContactInfo());
    }
}
