package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.AdminContactDto;
import com.wb.learningresourcelibrary.dto.AdminDto;
import com.wb.learningresourcelibrary.dto.PasswordDto;
import com.wb.learningresourcelibrary.entity.Admin;

import java.util.Map;

/**
 * 管理员 Service
 */
public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     */
    Map<String, Object> login(String username, String password);

    /**
     * 新增管理员
     */
    void addAdmin(AdminDto dto);

    /**
     * 更新管理员
     */
    void updateAdmin(AdminDto dto);

    /**
     * 修改密码
     */
    void updatePassword(Long adminId, PasswordDto dto);

    /**
     * 获取当前管理员联系方式
     */
    Admin getContactInfo(Long adminId);

    /**
     * 获取公开联系方式（前台展示，仅返回系统管理员配置的）
     */
    Admin getPublicContactInfo();

    /**
     * 更新联系方式（仅系统管理员）
     */
    void updateContact(Long adminId, AdminContactDto dto);
}
