package com.wb.learningresourcelibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wb.learningresourcelibrary.entity.ResourceTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资源-标签关联 Mapper
 */
@Mapper
public interface ResourceTagMapper extends BaseMapper<ResourceTag> {

    /**
     * 根据资源ID查询标签ID列表
     */
    @Select("SELECT tag_id FROM resource_tag WHERE resource_id = #{resourceId}")
    List<Long> selectTagIdsByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 根据资源ID删除所有关联
     */
    int deleteByResourceId(@Param("resourceId") Long resourceId);
}
