package com.wb.learningresourcelibrary.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.dto.AdminContactDto;
import com.wb.learningresourcelibrary.dto.AdminDto;
import com.wb.learningresourcelibrary.dto.PasswordDto;
import com.wb.learningresourcelibrary.entity.Admin;
import com.wb.learningresourcelibrary.mapper.AdminMapper;
import com.wb.learningresourcelibrary.security.JwtUtil;
import com.wb.learningresourcelibrary.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 Service 实现
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(String username, String password) {
        Admin admin = this.getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username));

        if (admin == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        if (admin.getStatus() == 0) {
            throw BusinessException.forbidden("该账号已被禁用");
        }

        // SHA256 加密验证
        String encryptedPassword = DigestUtil.sha256Hex(password);
        if (!encryptedPassword.equals(admin.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 生成 JWT
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("nickname", admin.getNickname());
        result.put("avatar", admin.getAvatar());
        result.put("role", admin.getRole());

        return result;
    }

    @Override
    public void addAdmin(AdminDto dto) {
        // 检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, dto.getUsername()));
        if (count > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(dto.getUsername());
        admin.setPassword(DigestUtil.sha256Hex(dto.getPassword()));
        admin.setNickname(dto.getNickname());
        admin.setAvatar(dto.getAvatar());
        admin.setRole(Constants.ROLE_NORMAL); // 新增的管理员默认为普通管理员
        admin.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        this.save(admin);
    }

    @Override
    public void updateAdmin(AdminDto dto) {
        Admin admin = this.getById(dto.getId());
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }

        // 检查用户名是否与其他管理员冲突
        Admin existAdmin = this.getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, dto.getUsername())
                .ne(Admin::getId, dto.getId()));
        if (existAdmin != null) {
            throw BusinessException.badRequest("用户名已存在");
        }

        admin.setUsername(dto.getUsername());
        admin.setNickname(dto.getNickname());
        admin.setAvatar(dto.getAvatar());
        admin.setStatus(dto.getStatus());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            admin.setPassword(DigestUtil.sha256Hex(dto.getPassword()));
        }

        this.updateById(admin);
    }

    @Override
    public void updatePassword(Long adminId, PasswordDto dto) {
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }

        String oldEncrypted = DigestUtil.sha256Hex(dto.getOldPassword());
        if (!oldEncrypted.equals(admin.getPassword())) {
            throw BusinessException.badRequest("原密码错误");
        }

        admin.setPassword(DigestUtil.sha256Hex(dto.getNewPassword()));
        this.updateById(admin);
    }

    @Override
    @Cacheable(value = "admin:contact", key = "#adminId")
    public Admin getContactInfo(Long adminId) {
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }
        return buildContactResult(admin);
    }

    @Override
    @Cacheable("admin:publicContact")
    public Admin getPublicContactInfo() {
        // 查找系统管理员（role=0）
        Admin admin = this.getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getRole, Constants.ROLE_SUPER)
                .last("LIMIT 1"));
        if (admin == null) {
            return buildContactResult(new Admin());
        }
        return buildContactResult(admin);
    }

    /**
     * 构建仅包含联系方式的 Admin 对象
     */
    private Admin buildContactResult(Admin admin) {
        Admin result = new Admin();
        result.setEmail(admin.getEmail());
        result.setPhone(admin.getPhone());
        result.setAddress(admin.getAddress());
        result.setShowEmail(admin.getShowEmail());
        result.setShowPhone(admin.getShowPhone());
        result.setShowAddress(admin.getShowAddress());
        return result;
    }

    @Override
    @CacheEvict(cacheNames = {"admin:contact", "admin:publicContact"}, allEntries = true)
    public void updateContact(Long adminId, AdminContactDto dto) {
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }

        admin.setEmail(dto.getEmail());
        admin.setPhone(dto.getPhone());
        admin.setAddress(dto.getAddress());
        admin.setShowEmail(dto.getShowEmail() != null ? dto.getShowEmail() : 1);
        admin.setShowPhone(dto.getShowPhone() != null ? dto.getShowPhone() : 1);
        admin.setShowAddress(dto.getShowAddress() != null ? dto.getShowAddress() : 1);
        this.updateById(admin);
    }
}
