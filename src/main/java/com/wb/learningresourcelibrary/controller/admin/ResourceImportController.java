package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.BatchImportDto;
import com.wb.learningresourcelibrary.dto.ParseImportDto;
import com.wb.learningresourcelibrary.dto.ParsedResourceVo;
import com.wb.learningresourcelibrary.service.ResourceImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 - 批量导入控制器
 */
@RestController("admin.resourceImportController")
@RequestMapping("/api/admin/resource/import")
@RequiredArgsConstructor
public class ResourceImportController {

    private final ResourceImportService resourceImportService;

    /**
     * 解析文本，返回结构化资源列表
     */
    @PostMapping("/parse")
    public Result<List<ParsedResourceVo>> parse(@Valid @RequestBody ParseImportDto dto) {
        List<ParsedResourceVo> list = resourceImportService.parseText(dto.getText());
        return Result.success(list);
    }

    /**
     * 批量导入资源
     */
    @PostMapping("/batch")
    public Result<Void> batchImport(@Valid @RequestBody BatchImportDto dto) {
        resourceImportService.batchImport(dto.getResources());
        return Result.success("批量导入成功");
    }
}
