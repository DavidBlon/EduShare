package com.wb.learningresourcelibrary.common.constant;

/**
 * 系统常量
 */
public interface Constants {

    /** 默认密码 */
    String DEFAULT_PASSWORD = "123456";

    /** 令牌前缀 */
    String TOKEN_PREFIX = "Bearer ";

    /** 令牌请求头 */
    String AUTHORIZATION_HEADER = "Authorization";

    /** 管理员ID 请求属性名 */
    String ADMIN_ID_ATTR = "adminId";

    /** 管理员用户名 请求属性名 */
    String ADMIN_USERNAME_ATTR = "adminUsername";

    /** 管理员角色 请求属性名 */
    String ADMIN_ROLE_ATTR = "adminRole";

    /** 角色：系统管理员 */
    Integer ROLE_SUPER = 0;

    /** 角色：普通管理员 */
    Integer ROLE_NORMAL = 1;

    /** 是否删除：是 */
    Integer DELETED = 1;

    /** 是否删除：否 */
    Integer NOT_DELETED = 0;

    /** 状态：启用 */
    Integer STATUS_ENABLE = 1;

    /** 状态：禁用 */
    Integer STATUS_DISABLE = 0;

    /** 是否推荐：是 */
    Integer RECOMMEND_YES = 1;

    /** 是否推荐：否 */
    Integer RECOMMEND_NO = 0;

    /** 默认每页大小 */
    Integer DEFAULT_PAGE_SIZE = 10;

    /** 文件上传目录 */
    String UPLOAD_DIR = "uploads";

    /** 封面图片子目录 */
    String COVER_DIR = "covers";
}
