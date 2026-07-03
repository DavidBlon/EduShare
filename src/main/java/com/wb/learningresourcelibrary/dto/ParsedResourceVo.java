package com.wb.learningresourcelibrary.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量导入 - 解析后的资源信息
 */
@Data
public class ParsedResourceVo {

    private String title;

    private String description;

    private String netdiskLink;

    private String netdiskCode;

    /** 自动匹配的分类ID */
    private Long categoryId;

    /** 分类路径，如"小学/语文" */
    private String categoryName;

    /** 识别到的科目 */
    private String subject;

    /** 识别到的年级，如"1-6" */
    private String grade;

    /** 识别到的教材版本 */
    private String textbookVersion;

    /** 自动匹配的标签ID列表 */
    private List<Long> tagIds;

    /** 是否已存在于数据库中（根据网盘链接判断） */
    private Boolean alreadyExists;
}
