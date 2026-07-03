package com.wb.learningresourcelibrary.service;

import com.wb.learningresourcelibrary.dto.ParsedResourceVo;

import java.util.List;

/**
 * 批量导入 Service
 */
public interface ResourceImportService {

    /**
     * 解析文本，返回结构化资源列表
     */
    List<ParsedResourceVo> parseText(String text);

    /**
     * 批量导入资源
     */
    void batchImport(List<ParsedResourceVo> resources);
}
