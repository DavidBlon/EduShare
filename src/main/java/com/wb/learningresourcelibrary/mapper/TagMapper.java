package com.wb.learningresourcelibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wb.learningresourcelibrary.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签 Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 统计标签关联的资源数量
     */
    @Select("SELECT COUNT(*) FROM resource_tag WHERE tag_id = #{tagId}")
    Integer countResourcesByTagId(@Param("tagId") Long tagId);

    /**
     * 根据资源ID查询标签列表
     */
    @Select("SELECT t.* FROM tag t INNER JOIN resource_tag rt ON t.id = rt.tag_id WHERE rt.resource_id = #{resourceId} AND t.is_deleted = 0")
    List<Tag> selectListByResourceId(@Param("resourceId") Long resourceId);
}
