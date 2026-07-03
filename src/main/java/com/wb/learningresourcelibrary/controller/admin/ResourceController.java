package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.ResourceDto;
import com.wb.learningresourcelibrary.dto.ResourceQueryDto;
import com.wb.learningresourcelibrary.entity.Resource;
import com.wb.learningresourcelibrary.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 资源控制器
 */
@RestController("admin.resourceController")
@RequestMapping("/api/admin/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * 分页查询资源列表
     */
    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<Resource>> page(ResourceQueryDto query) {
        return Result.success(resourceService.queryAdminResources(query));
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/{id}")
    public Result<Resource> detail(@PathVariable Long id) {
        return Result.success(resourceService.getResourceDetail(id));
    }

    /**
     * 新增资源
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ResourceDto resourceDto) {
        resourceService.addResource(resourceDto);
        return Result.success("新增资源成功");
    }

    /**
     * 编辑资源
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ResourceDto resourceDto) {
        if (resourceDto.getId() == null) {
            throw BusinessException.badRequest("资源ID不能为空");
        }
        resourceService.updateResource(resourceDto);
        return Result.success("更新资源成功");
    }

    /**
     * 删除资源（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success("删除资源成功");
    }

    /**
     * 切换推荐状态
     */
    @PatchMapping("/{id}/recommend")
    public Result<Void> toggleRecommend(@PathVariable Long id) {
        resourceService.toggleRecommend(id);
        return Result.success("推荐状态已更新");
    }
}
