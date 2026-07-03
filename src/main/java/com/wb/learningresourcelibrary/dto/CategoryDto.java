package com.wb.learningresourcelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类新增/编辑 DTO
 */
@Data
public class CategoryDto {

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId;

    private Integer sort;

    private Integer level;

    private String description;

    private Integer status;
}
