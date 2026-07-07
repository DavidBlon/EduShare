package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wb.learningresourcelibrary.common.exception.BusinessException;
import com.wb.learningresourcelibrary.dto.DisclaimerDto;
import com.wb.learningresourcelibrary.entity.Disclaimer;
import com.wb.learningresourcelibrary.mapper.DisclaimerMapper;
import com.wb.learningresourcelibrary.service.DisclaimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 免责声明 Service 实现
 */
@Service
@RequiredArgsConstructor
public class DisclaimerServiceImpl extends ServiceImpl<DisclaimerMapper, Disclaimer> implements DisclaimerService {

    @Override
    @Cacheable(value = "disclaimer", key = "'config'")
    public Disclaimer getDisclaimer() {
        Disclaimer disclaimer = this.getById(1L);
        if (disclaimer == null) {
            // 自动创建默认行（防止 SQL 未初始化时系统不可用）
            disclaimer = new Disclaimer();
            disclaimer.setId(1L);
            disclaimer.setSection1Title("1. 资源来源与用途");
            disclaimer.setSection1Content("本站所有资源均来源于网络收集、网友分享或用户上传，仅供个人学习交流、参考研究使用，严禁用于任何商业用途。请在下载后24小时内删除。");
            disclaimer.setSection2Title("2. 存储与分发声明");
            disclaimer.setSection2Content("本站仅提供资源的索引展示和网盘跳转链接，不存储、不复制、不制作任何受版权保护的文件内容，所有资源文件均保存在第三方网盘平台（如百度网盘等）。");
            disclaimer.setSection3Title("3. 版权保护");
            disclaimer.setSection3Content("本站尊重并保护知识产权，所有资源版权均归原作者、出版社及版权方所有。如您作为版权方认为本站提供的链接内容侵犯了您的合法权益，请立即通过以下联系方式提供证明文件（如版权证书、侵权链接等）通知我们。");
            disclaimer.setSection4Title("4. 快速响应");
            disclaimer.setSection4Content("我们承诺，在收到符合法律规定的有效通知后，将在24小时内核实并彻底删除相关侵权链接或断开访问，停止转发传播。");
            disclaimer.setSection5Title("5. 用户责任");
            disclaimer.setSection5Content("本站用户下载资源后，请您自行承担使用风险，并自觉在24小时内删除，如需长期学习使用，请购买官方正版图书或参与正规渠道教育。");
            disclaimer.setBriefDisclaimer("本站资源仅供个人学习交流，请于下载后24小时内删除。如有侵权，请联系我们处理。");
            this.save(disclaimer);
        }
        return disclaimer;
    }

    @Override
    @CacheEvict(value = "disclaimer", key = "'config'")
    public void updateDisclaimer(DisclaimerDto dto) {
        Disclaimer disclaimer = this.getById(1L);
        if (disclaimer == null) {
            throw BusinessException.notFound("免责声明配置不存在");
        }

        disclaimer.setSection1Title(dto.getSection1Title());
        disclaimer.setSection1Content(dto.getSection1Content());
        disclaimer.setSection2Title(dto.getSection2Title());
        disclaimer.setSection2Content(dto.getSection2Content());
        disclaimer.setSection3Title(dto.getSection3Title());
        disclaimer.setSection3Content(dto.getSection3Content());
        disclaimer.setSection4Title(dto.getSection4Title());
        disclaimer.setSection4Content(dto.getSection4Content());
        disclaimer.setSection5Title(dto.getSection5Title());
        disclaimer.setSection5Content(dto.getSection5Content());
        disclaimer.setContactEmail(dto.getContactEmail());
        disclaimer.setBriefDisclaimer(dto.getBriefDisclaimer());

        this.updateById(disclaimer);
    }
}
