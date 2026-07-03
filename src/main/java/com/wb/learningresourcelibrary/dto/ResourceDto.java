package com.wb.learningresourcelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 资源新增/编辑 DTO
 */
@Data
public class ResourceDto {

    private Long id;

    @NotBlank(message = "资源标题不能为空")
    private String title;

    private String description;

    private String cover;

    @NotNull(message = "请选择分类")
    private Long categoryId;

    private String netdiskLink;

    private String netdiskCode;

    private Integer isRecommend;

    private Integer status;

    private Integer sort;

    /** 关联的标签ID列表 */
    private List<Long> tagIds;
}
