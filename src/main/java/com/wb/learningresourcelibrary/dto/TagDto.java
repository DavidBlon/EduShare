package com.wb.learningresourcelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签新增/编辑 DTO
 */
@Data
public class TagDto {

    private Long id;

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private Integer sort;

    private Integer status;
}
