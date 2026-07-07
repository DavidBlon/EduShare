package com.wb.learningresourcelibrary.dto;

import lombok.Data;

/**
 * 某日下载量 Top N 资源 DTO
 */
@Data
public class DailyTopResourceDTO {
    private String resourceTitle;
    private Integer downloadCount;
}
