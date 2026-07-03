package com.wb.learningresourcelibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 下载日志实体
 */
@Data
@TableName("download_log")
public class DownloadLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 资源标题
     */
    private String resourceTitle;

    /**
     * 用户IP地址
     */
    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
