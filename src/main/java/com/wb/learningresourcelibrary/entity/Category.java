package com.wb.learningresourcelibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类实体（树形结构）
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 父分类ID，顶级分类为0
     */
    private Long parentId;

    private Integer sort;

    /**
     * 层级：1-小学 2-初中 3-高中 4-中考 5-高考
     */
    private Integer level;

    private String description;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 子分类列表（非数据库字段） */
    @TableField(exist = false)
    private List<Category> children;
}
