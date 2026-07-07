package com.wb.learningresourcelibrary.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wb.learningresourcelibrary.dto.DisclaimerDto;
import com.wb.learningresourcelibrary.entity.Disclaimer;

/**
 * 免责声明 Service
 */
public interface DisclaimerService extends IService<Disclaimer> {

    /**
     * 获取免责声明配置（自动创建默认行）
     */
    Disclaimer getDisclaimer();

    /**
     * 更新免责声明配置
     */
    void updateDisclaimer(DisclaimerDto dto);
}
