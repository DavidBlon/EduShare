package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.AdminContactDto;
import com.wb.learningresourcelibrary.dto.AdminDto;
import com.wb.learningresourcelibrary.dto.LoginDto;
import com.wb.learningresourcelibrary.dto.PasswordDto;
import com.wb.learningresourcelibrary.entity.Admin;
import com.wb.learningresourcelibrary.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端 - 管理员控制器
 */
@RestController("admin.adminController")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDto loginDto) {
        Map<String, Object> result = adminService.login(loginDto.getUsername(), loginDto.getPassword());
        return Result.success("登录成功", result);
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/profile")
    public Result<Admin> profile(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ADMIN_ID_ATTR);
        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }
        admin.setPassword(null);
        return Result.success(admin);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest request,
                                       @Valid @RequestBody PasswordDto passwordDto) {
        Long adminId = (Long) request.getAttribute(Constants.ADMIN_ID_ATTR);
        adminService.updatePassword(adminId, passwordDto);
        return Result.success("密码修改成功");
    }

    /**
     * 获取管理员列表（仅系统管理员）
     */
    @GetMapping("/admins")
    public Result<List<Admin>> list(HttpServletRequest request) {
        checkSuperAdmin(request);
        List<Admin> admins = adminService.list();
        admins.forEach(admin -> admin.setPassword(null));
        return Result.success(admins);
    }

    /**
     * 新增管理员（仅系统管理员）
     */
    @PostMapping("/admin")
    public Result<Void> add(HttpServletRequest request, @Valid @RequestBody AdminDto adminDto) {
        checkSuperAdmin(request);
        adminService.addAdmin(adminDto);
        return Result.success("新增管理员成功");
    }

    /**
     * 编辑管理员（仅系统管理员）
     */
    @PutMapping("/admin")
    public Result<Void> update(HttpServletRequest request, @Valid @RequestBody AdminDto adminDto) {
        checkSuperAdmin(request);
        adminService.updateAdmin(adminDto);
        return Result.success("更新管理员成功");
    }

    /**
     * 删除管理员（仅系统管理员）
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        checkSuperAdmin(request);
        Admin admin = adminService.getById(id);
        if (admin == null) {
            throw BusinessException.notFound("管理员不存在");
        }
        if (Constants.ROLE_SUPER.equals(admin.getRole())) {
            throw BusinessException.badRequest("不能删除系统管理员");
        }
        adminService.removeById(id);
        return Result.success("删除管理员成功");
    }

    /**
     * 获取联系方式（仅系统管理员）
     */
    @GetMapping("/contact")
    public Result<Admin> getContact(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ADMIN_ID_ATTR);
        return Result.success(adminService.getContactInfo(adminId));
    }

    /**
     * 更新联系方式（仅系统管理员）
     */
    @PutMapping("/contact")
    public Result<Void> updateContact(HttpServletRequest request,
                                       @RequestBody AdminContactDto dto) {
        Long adminId = (Long) request.getAttribute(Constants.ADMIN_ID_ATTR);
        adminService.updateContact(adminId, dto);
        return Result.success("联系方式已更新");
    }

    /**
     * 校验当前登录管理员是否为系统管理员
     */
    private void checkSuperAdmin(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute(Constants.ADMIN_ROLE_ATTR);
        // 兼容旧 token（无 role 字段时按普通管理员处理）
        if (role == null || !Constants.ROLE_SUPER.equals(role)) {
            throw BusinessException.forbidden("无权执行此操作，仅系统管理员可管理管理员");
        }
    }
}
