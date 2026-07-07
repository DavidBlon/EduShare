package com.wb.learningresourcelibrary.dto;

import lombok.Data;

/**
 * 下载统计 DTO
 */
@Data
public class DownloadStatDTO {
    private Long resourceId;
    private String resourceTitle;
    private Integer downloadCount;
}
