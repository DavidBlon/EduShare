package com.wb.learningresourcelibrary.controller.front;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.ResourceQueryDto;
import com.wb.learningresourcelibrary.entity.Resource;
import com.wb.learningresourcelibrary.service.DownloadLogService;
import com.wb.learningresourcelibrary.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 资源控制器（无需登录）
 */
@RestController("front.resourceController")
@RequestMapping("/api/front/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final DownloadLogService downloadLogService;

    /**
     * 分页搜索资源
     */
    @GetMapping("/search")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<Resource>> search(ResourceQueryDto query) {
        return Result.success(resourceService.queryResources(query));
    }

    /**
     * 获取资源详情（增加浏览量）
     */
    @GetMapping("/detail/{id}")
    public Result<Resource> detail(@PathVariable Long id) {
        Resource resource = resourceService.getResourceDetail(id);
        // 增加浏览量
        resourceService.incrementViewCount(id);
        return Result.success(resource);
    }

    /**
     * 获取资源的网盘信息（同时记录下载日志）
     */
    @GetMapping("/netdisk/{id}")
    public Result<java.util.Map<String, String>> netdisk(@PathVariable Long id, HttpServletRequest request) {
        Resource resource = resourceService.getResourceDetail(id);
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("title", resource.getTitle());
        result.put("netdiskLink", resource.getNetdiskLink());
        result.put("netdiskCode", resource.getNetdiskCode());

        // 记录下载日志（IP、资源信息、时间）
        String ip = getClientIp(request);
        downloadLogService.recordLog(id, resource.getTitle(), ip);

        // 增加下载量
        resourceService.incrementDownloadCount(id);
        return Result.success(result);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取推荐资源
     */
    @GetMapping("/recommend")
    public Result<List<Resource>> recommend(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(resourceService.getRecommendResources(limit));
    }

    /**
     * 获取热门资源
     */
    @GetMapping("/hot")
    public Result<List<Resource>> hot(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(resourceService.getHotResources(limit));
    }

    /**
     * 获取最新资源
     */
    @GetMapping("/latest")
    public Result<List<Resource>> latest(@RequestParam(defaultValue = "12") int limit) {
        return Result.success(resourceService.getLatestResources(limit));
    }
}
