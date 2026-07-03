package com.wb.learningresourcelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员新增/编辑 DTO
 */
@Data
public class AdminDto {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private Integer role;

    private Integer status;
}
