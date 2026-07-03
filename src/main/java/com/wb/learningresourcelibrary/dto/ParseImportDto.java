package com.wb.learningresourcelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 批量导入 - 解析文本请求
 */
@Data
public class ParseImportDto {

    @NotBlank(message = "导入文本不能为空")
    private String text;
}
