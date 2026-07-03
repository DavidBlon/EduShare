package com.wb.learningresourcelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 关键词规则 DTO
 */
@Data
public class KeywordRuleDto {

    private Long id;

    @NotBlank(message = "关键词不能为空")
    private String keyword;

    @NotBlank(message = "规则类型不能为空")
    private String type;

    @NotBlank(message = "映射目标不能为空")
    private String targetName;

    private Integer sort;

    private Integer status;
}
