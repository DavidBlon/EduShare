package com.wb.learningresourcelibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wb.learningresourcelibrary.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 资源 Mapper
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    /**
     * 增加浏览量
     */
    @Update("UPDATE resource SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加下载量
     */
    @Update("UPDATE resource SET download_count = download_count + 1 WHERE id = #{id}")
    int incrementDownloadCount(@Param("id") Long id);

    /**
     * 分页查询资源（含分类名称）
     */
    IPage<Resource> selectResourceWithCategory(Page<Resource> page, @Param("query") com.wb.learningresourcelibrary.dto.ResourceQueryDto query);
}
