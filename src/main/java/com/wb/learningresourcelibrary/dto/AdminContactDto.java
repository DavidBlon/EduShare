package com.wb.learningresourcelibrary.dto;

import lombok.Data;

/**
 * 管理员联系方式 DTO
 */
@Data
public class AdminContactDto {

    private String email;

    private String phone;

    private String address;

    /** 显示邮箱：1-显示 0-隐藏 */
    private Integer showEmail;

    /** 显示电话：1-显示 0-隐藏 */
    private Integer showPhone;

    /** 显示地址：1-显示 0-隐藏 */
    private Integer showAddress;
}
