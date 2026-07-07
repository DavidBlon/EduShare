package com.wb.learningresourcelibrary.dto;

import lombok.Data;

/**
 * 每日下载统计 DTO
 */
@Data
public class DailyDownloadStatDTO {
    private String date;
    private Integer count;
}
