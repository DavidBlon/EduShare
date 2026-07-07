DROP TABLE IF EXISTS `disclaimer`;
CREATE TABLE `disclaimer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `section1_title` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '章节1标题',
    `section1_content` TEXT COMMENT '章节1内容',
    `section2_title` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '章节2标题',
    `section2_content` TEXT COMMENT '章节2内容',
    `section3_title` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '章节3标题',
    `section3_content` TEXT COMMENT '章节3内容',
    `section4_title` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '章节4标题',
    `section4_content` TEXT COMMENT '章节4内容',
    `section5_title` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '章节5标题',
    `section5_content` TEXT COMMENT '章节5内容',
    `contact_email` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '联系邮箱',
    `brief_disclaimer` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '简短免责声明（显示在页脚）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：1-已删除 0-未删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='免责声明配置';

-- 初始化默认免责声明数据（ID=1 的单行配置）
INSERT INTO `disclaimer` (`id`, `section1_title`, `section1_content`, `section2_title`, `section2_content`, `section3_title`, `section3_content`, `section4_title`, `section4_content`, `section5_title`, `section5_content`, `brief_disclaimer`) VALUES
(1,
 '1. 资源来源与用途',
 '本站所有资源均来源于网络收集、网友分享或用户上传，仅供个人学习交流、参考研究使用，严禁用于任何商业用途。请在下载后24小时内删除。',
 '2. 存储与分发声明',
 '本站仅提供资源的索引展示和网盘跳转链接，不存储、不复制、不制作任何受版权保护的文件内容，所有资源文件均保存在第三方网盘平台（如百度网盘等）。',
 '3. 版权保护',
 '本站尊重并保护知识产权，所有资源版权均归原作者、出版社及版权方所有。如您作为版权方认为本站提供的链接内容侵犯了您的合法权益，请立即通过以下联系方式提供证明文件（如版权证书、侵权链接等）通知我们。',
 '4. 快速响应',
 '我们承诺，在收到符合法律规定的有效通知后，将在24小时内核实并彻底删除相关侵权链接或断开访问，停止转发传播。',
 '5. 用户责任',
 '本站用户下载资源后，请您自行承担使用风险，并自觉在24小时内删除，如需长期学习使用，请购买官方正版图书或参与正规渠道教育。',
 '本站资源仅供个人学习交流，请于下载后24小时内删除。如有侵权，请联系我们处理。'
);
