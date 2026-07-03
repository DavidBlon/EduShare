package com.wb.learningresourcelibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源实体
 */
@Data
@TableName("resource")
public class Resource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /**
     * 封面图片路径
     */
    private String cover;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 百度网盘链接
     */
    private String netdiskLink;

    /**
     * 百度网盘提取码
     */
    private String netdiskCode;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 下载量（点击复制链接次数）
     */
    private Integer downloadCount;

    /**
     * 是否推荐：1-推荐 0-普通
     */
    private Integer isRecommend;

    /**
     * 状态：1-发布 0-草稿
     */
    private Integer status;

    private Integer sort;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 关联的标签列表（非数据库字段） */
    @TableField(exist = false)
    private List<Tag> tags;

    /** 分类名称（非数据库字段） */
    @TableField(exist = false)
    private String categoryName;
}
