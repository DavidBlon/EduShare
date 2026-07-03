package com.wb.learningresourcelibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关键词规则（用于批量导入时的智能解析）
 */
@Data
@TableName("keyword_rule")
public class KeywordRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 匹配关键词 */
    private String keyword;

    /** 规则类型：TAG-标签匹配 VERSION-教材版本 */
    private String type;

    /** 映射目标（标签名/版本名） */
    private String targetName;

    /** 排序号 */
    private Integer sort;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
