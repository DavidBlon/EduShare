-- H2 兼容的建表语句（测试环境）

CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `avatar` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`username`)
);

CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `sort` INT DEFAULT 0,
    `level` INT DEFAULT 1,
    `description` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT DEFAULT 1,
    `is_deleted` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `sort` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `is_deleted` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`name`)
);

CREATE TABLE IF NOT EXISTS `resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `cover` VARCHAR(255) DEFAULT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `netdisk_link` VARCHAR(500) DEFAULT NULL,
    `netdisk_code` VARCHAR(50) DEFAULT NULL,
    `view_count` INT DEFAULT 0,
    `download_count` INT DEFAULT 0,
    `is_recommend` TINYINT DEFAULT 0,
    `status` TINYINT DEFAULT 0,
    `sort` INT DEFAULT 0,
    `is_deleted` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `resource_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `resource_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE (`resource_id`, `tag_id`)
);

CREATE TABLE IF NOT EXISTS `download_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `resource_id` BIGINT NOT NULL,
    `resource_title` VARCHAR(200) DEFAULT NULL,
    `ip_address` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `admin_id` BIGINT NOT NULL,
    `is_published` TINYINT DEFAULT 0,
    `is_deleted` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

-- 初始数据
INSERT INTO `admin` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', 1);

INSERT INTO `category` (`id`, `name`, `parent_id`, `sort`, `level`, `description`) VALUES
(1,  '小学',     0,  1,  1, '小学阶段教育资源'),
(11, '语文',     1,  1,  1, '小学语文'),
(12, '数学',     1,  2,  1, '小学数学'),
(2,  '初中',     0,  2,  2, '初中阶段教育资源'),
(21, '语文',     2,  1,  2, '初中语文');

INSERT INTO `tag` (`id`, `name`, `sort`) VALUES
(1, '期中考试',  1),
(2, '期末考试',  2),
(3, '单元测试',  3);
