-- =============================================================
-- EduShare 教育资源共享平台 - 数据库初始化脚本
-- =============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS edushare DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE edushare;

-- =============================================================
-- 管理员表
-- =============================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（SHA256加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `show_email` TINYINT DEFAULT 1 COMMENT '显示邮箱：1-显示 0-隐藏',
    `show_phone` TINYINT DEFAULT 1 COMMENT '显示电话：1-显示 0-隐藏',
    `show_address` TINYINT DEFAULT 1 COMMENT '显示地址：1-显示 0-隐藏',
    `role` TINYINT DEFAULT 1 COMMENT '角色：0-系统管理员 1-普通管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- =============================================================
-- 分类表（树形结构）
-- =============================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，顶级分类为0',
    `sort` INT DEFAULT 0 COMMENT '排序号（升序）',
    `level` INT DEFAULT 1 COMMENT '层级：1-小学 2-初中 3-高中',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：1-已删除 0-未删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- =============================================================
-- 标签表
-- =============================================================
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `sort` INT DEFAULT 0 COMMENT '排序号（升序）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：1-已删除 0-未删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- =============================================================
-- 资源表
-- =============================================================
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '资源标题',
    `description` TEXT COMMENT '资源描述/简介',
    `cover` VARCHAR(255) DEFAULT NULL COMMENT '封面图片路径',
    `category_id` BIGINT DEFAULT NULL COMMENT '所属分类ID',
    `netdisk_link` VARCHAR(500) DEFAULT NULL COMMENT '百度网盘链接',
    `netdisk_code` VARCHAR(50) DEFAULT NULL COMMENT '百度网盘提取码',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `download_count` INT DEFAULT 0 COMMENT '下载量（点击复制网盘链接次数）',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐：1-推荐 0-普通',
    `status` TINYINT DEFAULT 0 COMMENT '状态：1-发布 0-草稿',
    `sort` INT DEFAULT 0 COMMENT '排序号（升序）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：1-已删除 0-未删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_is_recommend` (`is_recommend`),
    KEY `idx_view_count` (`view_count`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- =============================================================
-- 资源-标签关联表
-- =============================================================
DROP TABLE IF EXISTS `resource_tag`;
CREATE TABLE `resource_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_tag_id` (`tag_id`),
    UNIQUE KEY `uk_resource_tag` (`resource_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源-标签关联表';

-- =============================================================
-- 下载日志表
-- =============================================================
DROP TABLE IF EXISTS `download_log`;
CREATE TABLE `download_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `resource_title` VARCHAR(200) DEFAULT NULL COMMENT '资源标题',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '用户IP地址',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
    PRIMARY KEY (`id`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载日志表';

-- =============================================================
-- 公告表
-- =============================================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(255) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `admin_id` BIGINT NOT NULL COMMENT '发布管理员ID',
    `is_published` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已发布',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：1-已删除 0-未删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_is_published` (`is_published`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =============================================================
-- 初始数据
-- =============================================================

-- 初始管理员（密码：admin123，SHA256加密）
INSERT INTO `admin` (`username`, `password`, `nickname`, `email`, `phone`, `address`, `role`, `status`) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', 'contact@hanmizhitu.com', '400-888-8888', '中国 · 北京', 0, 1);

-- 初始分类数据
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort`, `level`, `description`) VALUES
-- 小学
(1,  '小学',     0,  1,  1, '小学阶段教育资源'),
(11, '语文',     1,  1,  1, '小学语文'),
(12, '数学',     1,  2,  1, '小学数学'),
(13, '英语',     1,  3,  1, '小学英语'),
(14, '科学',     1,  4,  1, '小学科学'),
(15, '一年级试卷', 1, 10, 1, '小学一年级试卷'),
(16, '二年级试卷', 1, 11, 1, '小学二年级试卷'),
(17, '三年级试卷', 1, 12, 1, '小学三年级试卷'),
(18, '四年级试卷', 1, 13, 1, '小学四年级试卷'),
(19, '五年级试卷', 1, 14, 1, '小学五年级试卷'),
(110, '六年级试卷', 1, 15, 1, '小学六年级试卷'),
-- 初中
(2,  '初中',     0,  2,  2, '初中阶段教育资源'),
(21, '语文',     2,  1,  2, '初中语文'),
(22, '数学',     2,  2,  2, '初中数学'),
(23, '英语',     2,  3,  2, '初中英语'),
(24, '物理',     2,  4,  2, '初中物理'),
(25, '化学',     2,  5,  2, '初中化学'),
(26, '历史',     2,  6,  2, '初中历史'),
(27, '地理',     2,  7,  2, '初中地理'),
(28, '道德与法治', 2,  8,  2, '初中道德与法治'),
(29, '生物',     2,  9,  2, '初中生物'),
(210, '初一试卷', 2, 10, 2, '初一年级试卷'),
(211, '初二试卷', 2, 11, 2, '初二年级试卷'),
(212, '初三试卷', 2, 12, 2, '初三年级试卷'),
-- 高中
(3,  '高中',     0,  3,  3, '高中阶段教育资源'),
(31, '语文',     3,  1,  3, '高中语文'),
(32, '数学',     3,  2,  3, '高中数学'),
(33, '英语',     3,  3,  3, '高中英语'),
(34, '物理',     3,  4,  3, '高中物理'),
(35, '化学',     3,  5,  3, '高中化学'),
(36, '生物',     3,  6,  3, '高中生物'),
(37, '历史',     3,  7,  3, '高中历史'),
(38, '地理',     3,  8,  3, '高中地理'),
(39, '政治',     3,  9,  3, '高中政治'),
(310, '高一试卷', 3, 10, 3, '高一年级试卷'),
(311, '高二试卷', 3, 11, 3, '高二年级试卷'),
(312, '高三试卷', 3, 12, 3, '高三年级试卷'),
-- 中考
(4,  '中考',     0,  4,  4, '中考备考资源'),
(41, '语文',     4,  1,  4, '中考语文'),
(42, '数学',     4,  2,  4, '中考数学'),
(43, '英语',     4,  3,  4, '中考英语'),
(44, '物理',     4,  4,  4, '中考物理'),
(45, '化学',     4,  5,  4, '中考化学'),
(46, '历史',     4,  6,  4, '中考历史'),
(47, '道德与法治', 4,  7,  4, '中考道德与法治'),
-- 高考
(5,  '高考',     0,  5,  5, '高考备考资源'),
(51, '语文',     5,  1,  5, '高考语文'),
(52, '数学',     5,  2,  5, '高考数学'),
(53, '英语',     5,  3,  5, '高考英语'),
(54, '物理',     5,  4,  5, '高考物理'),
(55, '化学',     5,  5,  5, '高考化学'),
(56, '生物',     5,  6,  5, '高考生物'),
(57, '历史',     5,  7,  5, '高考历史'),
(58, '地理',     5,  8,  5, '高考地理'),
(59, '政治',     5,  9,  5, '高考政治');

-- 初始标签
INSERT INTO `tag` (`id`, `name`, `sort`) VALUES
(1, '期中考试',  1),
(2, '期末考试',  2),
(3, '单元测试',  3),
(4, '知识点总结', 4),
(5, '真题试卷',  5),
(6, '同步练习',  6),
(7, '复习资料',  7),
(8, '课件教案',  8),
(9, '月考',      9),
(10, '联考',     10),
(11, '质量检测',  11),
(12, '模拟卷',    12);

-- =============================================================
-- 关键词规则表（用于批量导入时的智能解析）
-- =============================================================
DROP TABLE IF EXISTS `keyword_rule`;
CREATE TABLE `keyword_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keyword` VARCHAR(50) NOT NULL COMMENT '匹配关键词',
    `type` VARCHAR(20) NOT NULL COMMENT '规则类型：TAG-标签匹配 VERSION-教材版本',
    `target_name` VARCHAR(100) NOT NULL COMMENT '映射目标（标签名/版本名）',
    `sort` INT DEFAULT 0 COMMENT '排序号（升序）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：1-已删除 0-未删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词规则表';

-- 初始关键词规则
INSERT INTO `keyword_rule` (`keyword`, `type`, `target_name`, `sort`) VALUES
-- 标签匹配（标题中含关键词 → 自动打上对应标签）
('暑假', 'TAG', '复习资料', 1),
('寒假', 'TAG', '复习资料', 2),
('期中', 'TAG', '期中考试', 3),
('期末', 'TAG', '期末考试', 4),
('单元', 'TAG', '单元测试', 5),
('真题', 'TAG', '真题试卷', 6),
('同步', 'TAG', '同步练习', 7),
('复习', 'TAG', '复习资料', 8),
('知识点', 'TAG', '知识点总结', 9),
('课件', 'TAG', '课件教案', 10),
('教案', 'TAG', '课件教案', 11),
('试卷', 'TAG', '真题试卷', 12),
('练习', 'TAG', '同步练习', 13),
('大通关', 'TAG', '同步练习', 14),
('衔接', 'TAG', '复习资料', 15),
('书写', 'TAG', '同步练习', 16),
('单词', 'TAG', '同步练习', 17),
('月考', 'TAG', '月考', 18),
('联考', 'TAG', '联考', 19),
('质量检测', 'TAG', '质量检测', 20),
('模拟', 'TAG', '模拟卷', 21),
-- 教材版本
('人教版', 'VERSION', '人教版', 1),
('人教', 'VERSION', '人教版', 2),
('北师版', 'VERSION', '北师版', 3),
('北师大版', 'VERSION', '北师大版', 4),
('北师大', 'VERSION', '北师大版', 5),
('苏教版', 'VERSION', '苏教版', 6),
('沪教版', 'VERSION', '沪教版', 7),
('外研版', 'VERSION', '外研版', 8),
('外研', 'VERSION', '外研版', 9),
('鲁教版', 'VERSION', '鲁教版', 10),
('译林版', 'VERSION', '译林版', 11),
('译林', 'VERSION', '译林版', 12),
('冀教版', 'VERSION', '冀教版', 13),
('湘教版', 'VERSION', '湘教版', 14),
('教科版', 'VERSION', '教科版', 15),
('粤教版', 'VERSION', '粤教版', 16),
('苏教', 'VERSION', '苏教版', 17),
('沪教', 'VERSION', '沪教版', 18),
('冀教', 'VERSION', '冀教版', 19),
('湘教', 'VERSION', '湘教版', 20),
('教科', 'VERSION', '教科版', 21),
('粤教', 'VERSION', '粤教版', 22),
('鲁教', 'VERSION', '鲁教版', 23);
