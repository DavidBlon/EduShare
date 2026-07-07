package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.dto.DisclaimerDto;
import com.wb.learningresourcelibrary.entity.Disclaimer;
import com.wb.learningresourcelibrary.service.DisclaimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 免责声明控制器
 */
@RestController("admin.disclaimerController")
@RequestMapping("/api/admin/disclaimer")
@RequiredArgsConstructor
public class DisclaimerController {

    private final DisclaimerService disclaimerService;

    /**
     * 获取免责声明配置
     */
    @GetMapping
    public Result<Disclaimer> getDisclaimer() {
        return Result.success(disclaimerService.getDisclaimer());
    }

    /**
     * 更新免责声明配置
     */
    @PutMapping
    public Result<Void> updateDisclaimer(@RequestBody DisclaimerDto dto) {
        disclaimerService.updateDisclaimer(dto);
        return Result.success("免责声明已更新");
    }
}
