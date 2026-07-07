package com.wb.learningresourcelibrary.dto;

import lombok.Data;
import java.util.List;

/**
 * 资源查询参数 DTO
 */
@Data
public class ResourceQueryDto {

    /** 页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 分类ID */
    private Long categoryId;

    /** 关键词（搜索标题和描述） */
    private String keyword;

    /** 标签ID列表（支持多选） */
    private List<Long> tagIds;

    /** 是否推荐 */
    private Integer isRecommend;

    /** 排序方式：new-最新 hot-热门 recommend-推荐 */
    private String sortBy;
}
