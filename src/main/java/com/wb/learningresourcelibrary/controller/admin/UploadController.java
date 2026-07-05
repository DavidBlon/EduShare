package com.wb.learningresourcelibrary.controller.admin;

import cn.hutool.core.date.DateUtil;
import com.wb.learningresourcelibrary.common.constant.Constants;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端 - 文件上传控制器
 */
@RestController("admin.uploadController")
@RequestMapping("/api/admin/upload")
public class UploadController {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Value("${file.max-size:10485760}")
    private long maxSize;

    /**
     * 封面上传
     */
    @PostMapping("/cover")
    public Result<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw BusinessException.badRequest("文件大小不能超过10MB");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw BusinessException.badRequest("文件名不能为空");
        }
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw BusinessException.badRequest("文件格式不正确，仅支持 jpg/jpeg/png/gif/webp/svg/bmp 格式的图片");
        }
        String suffix = originalFilename.substring(dotIndex).toLowerCase();
        if (!suffix.matches("\\.(jpg|jpeg|png|gif|webp|svg|bmp)$")) {
            throw BusinessException.badRequest("仅支持 jpg/jpeg/png/gif/webp/svg/bmp 格式的图片");
        }

        // 生成文件名：日期 + UUID + 后缀
        String dateDir = DateUtil.format(java.util.Date.from(java.time.Instant.now()), "yyyyMMdd");
        String fileName = System.currentTimeMillis() + "_" + cn.hutool.core.util.IdUtil.fastSimpleUUID() + suffix;
        String filePath = Constants.COVER_DIR + File.separator + dateDir + File.separator + fileName;

        // 使用绝对路径，避免 transferTo 将相对路径解析到 Tomcat 临时目录
        File dest = new File(uploadPath + File.separator + filePath).getAbsoluteFile();
        File parentDir = dest.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        // 返回访问路径
        String url = "/uploads/" + filePath.replace("\\", "/");
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("fileName", originalFilename);
        return Result.success("上传成功", result);
    }

    /**
     * 微信群二维码上传（覆盖固定路径）
     */
    @PostMapping("/qrcode")
    public Result<Map<String, String>> uploadQrCode(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw BusinessException.badRequest("文件大小不能超过10MB");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw BusinessException.badRequest("文件名不能为空");
        }
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw BusinessException.badRequest("文件格式不正确，仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }
        String suffix = originalFilename.substring(dotIndex).toLowerCase();
        if (!suffix.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
            throw BusinessException.badRequest("仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }

        // 保存到固定路径，直接覆盖旧文件
        // 使用绝对路径，避免 transferTo 将相对路径解析到 Tomcat 临时目录
        String savePath = uploadPath + "/group-qrcode.png";
        File dest = new File(savePath).getAbsoluteFile();
        File parentDir = dest.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("二维码上传失败", e);
        }

        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/group-qrcode.png");
        result.put("fileName", "group-qrcode.png");
        return Result.success("二维码更新成功", result);
    }
}
