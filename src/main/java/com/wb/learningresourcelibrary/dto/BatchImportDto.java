package com.wb.learningresourcelibrary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量导入 - 批量保存请求
 */
@Data
public class BatchImportDto {

    @NotEmpty(message = "导入资源列表不能为空")
    @Valid
    private List<ParsedResourceVo> resources;
}
