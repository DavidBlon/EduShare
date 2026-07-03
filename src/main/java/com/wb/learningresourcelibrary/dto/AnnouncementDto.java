package com.wb.learningresourcelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公告新增/编辑 DTO
 */
@Data
public class AnnouncementDto {

    private Long id;

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;
}
