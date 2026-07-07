package com.wb.learningresourcelibrary.controller.front;

import com.wb.learningresourcelibrary.common.result.Result;
import com.wb.learningresourcelibrary.entity.Disclaimer;
import com.wb.learningresourcelibrary.service.DisclaimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台 - 免责声明控制器
 */
@RestController("front.disclaimerController")
@RequestMapping("/api/front/disclaimer")
@RequiredArgsConstructor
public class DisclaimerController {

    private final DisclaimerService disclaimerService;

    /**
     * 获取公开免责声明
     */
    @GetMapping
    public Result<Disclaimer> getDisclaimer() {
        return Result.success(disclaimerService.getDisclaimer());
    }
}
