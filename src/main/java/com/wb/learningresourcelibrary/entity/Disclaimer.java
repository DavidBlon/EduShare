package com.wb.learningresourcelibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 免责声明配置实体
 */
@Data
@TableName("disclaimer")
public class Disclaimer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String section1Title;

    private String section1Content;

    private String section2Title;

    private String section2Content;

    private String section3Title;

    private String section3Content;

    private String section4Title;

    private String section4Content;

    private String section5Title;

    private String section5Content;

    private String contactEmail;

    private String briefDisclaimer;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
