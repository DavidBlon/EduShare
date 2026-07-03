package com.wb.learningresourcelibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体
 */
@Data
@TableName("admin")
public class Admin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    /** 邮箱 */
    private String email;

    /** 电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 显示邮箱 */
    private Integer showEmail;

    /** 显示电话 */
    private Integer showPhone;

    /** 显示地址 */
    private Integer showAddress;

    /**
     * 角色：0-系统管理员 1-普通管理员
     */
    private Integer role;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
