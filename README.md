# EduShare - 教育资源共享平台

> 基于 Spring Boot 4 + Vue 3 的网盘资源分享平台，支持资源批量导入、智能解析分类、标签匹配、Redis 缓存加速。

## 📋 项目概述

EduShare 是一个面向教育领域的资源分享平台，帮助教育工作者和学生快速查找和分享学习资源（电子教材、试卷、课件等）。支持管理员后台批量导入网盘资源，自动解析标题提取年级、科目、版本信息并匹配分类。

## 🏗️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 4.0.7 |
| **ORM** | MyBatis Plus | 3.5.9 |
| **数据库** | MySQL 8 + H2（测试） | - |
| **缓存** | Redis 8 | 8.6.3 |
| **认证** | JWT (jjwt) | 0.12.6 |
| **工具库** | Hutool | 5.8.32 |
| **前端框架** | Vue 3 (Composition API) | - |
| **UI 组件** | Element Plus | - |
| **构建工具** | Vite | - |
| **项目管理** | Maven | - |

## 📁 项目结构

```
EduShare/
├── frontend/                        # Vue 3 前端
│   ├── src/
│   │   ├── api/                     # API 请求层
│   │   ├── components/front/        # 前台公共组件
│   │   ├── layout/                  # 布局组件
│   │   ├── router/                  # 路由配置
│   │   ├── utils/                   # 工具（axios 封装等）
│   │   └── views/
│   │       ├── admin/               # 管理后台页面
│   │       └── front/               # 前台页面
│   ├── public/                      # 静态资源
│   └── vite.config.js
│
├── src/main/java/com/wb/learningresourcelibrary/
│   ├── common/
│   │   ├── config/                  # 配置类（MyBatisPlus、Redis、WebMvc）
│   │   ├── constant/                # 常量定义
│   │   ├── exception/               # 业务异常 + 全局异常处理
│   │   └── result/                  # 统一 API 响应封装
│   ├── controller/
│   │   ├── admin/                   # 管理端控制器（JWT 保护）
│   │   └── front/                   # 前台控制器（公开）
│   ├── dto/                         # 数据传输对象
│   ├── entity/                      # 数据库实体
│   ├── mapper/                      # MyBatis Plus Mapper
│   ├── security/                    # JWT 认证过滤器 + 工具类
│   └── service/                     # 业务接口 + 实现
│
├── src/main/resources/
│   ├── mapper/                      # XML 映射文件
│   ├── sql/init.sql                 # 数据库初始化脚本
│   └── application.properties       # 应用配置
│
├── pom.xml                          # Maven 依赖
└── .gitignore
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 8.0+（可选，用于缓存）
- Node.js 18+（前端构建）

### 1. 初始化数据库

```bash
# 登录 MySQL 后执行初始化脚本
mysql -u root -p < src/main/resources/sql/init.sql
```

脚本会自动创建 `edushare` 数据库、所有表结构，并插入初始数据。

### 2. 配置

编辑 `src/main/resources/application.properties`：

```properties
# 数据库配置（按实际修改）
spring.datasource.url=jdbc:mysql://localhost:3306/edushare?...
spring.datasource.username=root
spring.datasource.password=123456

# Redis 配置（可选，不配置时仍可使用，只是无缓存）
spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379
```

### 3. 启动后端

```bash
# 编译
./mvnw compile

# 启动（MySQL 必须已运行且数据库已初始化）
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package -DskipTests
java -jar target/EduShare-0.0.1-SNAPSHOT.jar
```

后端默认运行在 `http://localhost:8080`。

### 4. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行（热更新）
npm run dev

# 构建生产版本
npm run build
```

前端默认运行在 `http://localhost:5173`，开发模式已配置代理到后端 8080 端口。

### 5. 启动 Redis（可选，用于缓存）

```bash
redis-server redis.conf
# 或注册为 Windows 服务后自动启动
```

### 6. 访问系统

- **前台首页**：http://localhost:5173
- **后台管理**：http://localhost:5173/admin
- **默认管理员**：`******` / `******`

## 📖 功能模块

### 前台功能

| 模块 | 说明 |
|------|------|
| **首页** | Banner 轮播、分类导航、推荐资源（限4个）、热门资源、最新资源 |
| **资源列表** | 按分类筛选、关键词搜索、标签筛选、排序（最新/最热/推荐） |
| **资源详情** | 查看资源信息、复制网盘链接、推荐相关资源 |
| **公告** | 系统公告列表与详情 |
| **关于我们** | 管理员联系方式、系统介绍 |

### 后台功能

| 模块 | 说明 |
|------|------|
| **控制台** | 资源总数、分类数、标签数统计，最近资源列表 |
| **资源管理** | 资源 CRUD、分类筛选、关键词搜索、推荐/发布状态管理 |
| **批量导入** | 粘贴网盘资源文本，自动解析标题提取年级/科目/版本/标签，智能匹配分类 |
| **分类管理** | 树形分类结构管理（小学/初中/高中/中考/高考） |
| **标签管理** | 标签 CRUD，显示每个标签的资源数量 |
| **关键词规则** | 管理批量导入时的标题解析规则（标签匹配 + 教材版本识别） |
| **管理员管理** | 管理员账号管理、密码修改 |
| **公告管理** | 公告发布/撤回、编辑、删除 |
| **下载日志** | 查看资源下载记录，自动清理 90 天前的日志 |
| **联系方式** | 配置前台"关于我们"页面的联系方式 |

### 管理端 API 路由

所有管理端 API 以 `/api/admin/` 开头，需携带 JWT Token。

| 路由 | 功能 |
|------|------|
| `POST /api/admin/login` | 管理员登录 |
| `GET/POST/PUT/DELETE /api/admin/resource/**` | 资源 CRUD |
| `PATCH /api/admin/resource/{id}/recommend` | 切换推荐状态 |
| `POST /api/admin/import/parse` | 解析导入文本 |
| `POST /api/admin/import/batch` | 批量导入资源 |
| `GET/POST/PUT/DELETE /api/admin/category/**` | 分类管理 |
| `GET/POST/PUT/DELETE /api/admin/tag/**` | 标签管理 |
| `GET/POST/PUT/DELETE /api/admin/keyword-rule/**` | 关键词规则管理 |
| `GET/POST/PUT/DELETE /api/admin/announcement/**` | 公告管理 |
| `GET /api/admin/announcement/unread` | 未读公告数 |
| `GET /api/admin/download-log/page` | 下载日志分页 |
| `GET/PUT /api/admin/contact/**` | 联系方式管理 |
| `POST /api/admin/upload/image` | 图片上传 |

### 前台 API 路由

公开访问，无需认证。

| 路由 | 功能 |
|------|------|
| `GET /api/front/category/tree` | 获取分类树 |
| `GET /api/front/resource/page` | 资源分页查询 |
| `GET /api/front/resource/recommend` | 推荐资源列表 |
| `GET /api/front/resource/hot` | 热门资源列表 |
| `GET /api/front/resource/latest` | 最新资源列表 |
| `GET /api/front/resource/{id}` | 资源详情（含浏览+1） |
| `GET /api/front/tag/list` | 标签列表 |
| `GET /api/front/announcement/page` | 已发布公告分页 |
| `GET /api/front/announcement/detail/{id}` | 公告详情 |
| `GET /api/front/contact` | 系统联系方式 |

## 🔧 核心架构设计

### 分层架构

```
Controller → Service (接口 + 实现) → Mapper (MyBatis Plus BaseMapper) → MySQL
```

- **Controller**：接收请求参数校验，调用 Service，返回统一 Result
- **Service**：业务逻辑层，事务管理
- **Mapper**：数据访问层，MyBatis Plus 自动注入基础 CRUD

### 认证授权

- JWT 登录认证，Token 有效期 24 小时（可配置）
- `JwtAuthenticationFilter` 通过 `FilterRegistrationBean` 注册，仅拦截 `/api/admin/*` 路径
- 排除登录接口 `/api/admin/login`
- 管理员 ID 提取至 request attribute 供审计使用

### 批量导入智能解析

粘贴网盘文本后，系统自动解析每条资源：

1. **分隔文本块** — 按空行分割多条资源
2. **提取标题+链接** — 从每行中分离网盘链接和标题
3. **教材版本识别** — 匹配来自 `keyword_rule` 表的 VERSION 类型规则
4. **科目提取** — 从 `category` 表子分类名称动态构建正则匹配（排除试卷类）
5. **年级识别** — 支持中文数字（一~六年级）、阿拉伯数字（7~9年级）、年级范围（1-6年级）、独立年级（高一/初一）
6. **学段判定** — 根据年级数字确定学段：小学（≤6）、初中（7-9）、高中（10-12）
7. **分类匹配** — 根据学段+科目定位分类，无科目时匹配试卷分类或顶层分类
8. **标签自动打标** — 匹配来自 `keyword_rule` 表的 TAG 类型规则
9. **重复检测** — 根据网盘链接检查是否已在数据库中存在

> 关键词规则和分类均从数据库动态加载，管理员可在后台管理页面直接增删改，无需修改代码。

### 缓存策略

使用 Redis + Spring Cache 实现数据缓存：

| 缓存名称 | 内容 | 失效策略 |
|----------|------|----------|
| `category:tree` | 分类树 | 分类增删改时清除 |
| `category:all` | 全部分类列表 | 分类增删改时清除 |
| `resource:recommend` | 推荐资源 | 资源增删改/切换推荐时清除 |
| `resource:hot` | 热门资源 | 资源增删改时清除 |
| `resource:latest` | 最新资源 | 资源增删改时清除 |
| `resource:detail` | 资源详情 | 资源增删改时清除 |
| `tag:all` | 全部标签 | 标签增删改时清除 |
| `announcement:detail` | 公告详情 | 公告增删改/发布时清除 |
| `keywordRule:active` | 启用的关键词规则 | 规则增删改时清除 |

公告未读数量接口 `/api/front/announcement/unread-count` 不使用 Redis 缓存，直接实时查询已发布公告数量。该接口是轻量 `count` 查询，避免 `Long` 数值在 JSON 序列化后从缓存读出为字符串导致类型转换异常。

默认 TTL 1 小时。

### 日志自动清理

下载日志默认保留 90 天，每天凌晨 3 点自动清理过期数据。保留天数通过 `log.retention.days` 配置。

## 📝 注意事项

### Spring Boot 4 + MyBatis Plus 兼容性

MyBatis Plus 3.5.9 不官方支持 Spring Boot 4.x，需手动配置：

1. `pom.xml` 中排除 `mybatis-plus-spring-boot-autoconfigure`
2. 显式添加 `mybatis-spring:3.0.4`、`spring-tx`、`spring-boot-starter-jdbc`
3. `MyBatisPlusConfig` 中手动创建 `SqlSessionFactory` Bean
4. `@MapperScan` 写在启动类上且指定 `sqlSessionFactoryRef`

### 控制器 Bean 命名

前后台同名 Controller（如 `ResourceController`）通过显式 Bean 名称区分：

```java
@RestController("admin.resourceController")
@RestController("front.resourceController")
```

### 软删除

所有表使用 `is_deleted` 字段实现软删除（1=已删除，0=正常）。
MyBatis Plus 全局配置：`logic-delete-value=1`，`logic-not-delete-value=0`。

## 🧪 测试

```bash
# 运行全部测试
./mvnw test

# 运行单个测试
./mvnw test -Dtest=LearningResourceLibraryApplicationTests
```

测试使用 H2 内存数据库（MySQL 兼容模式），独立的 JWT Secret 和上传路径。

## 📄 数据库表结构

| 表名 | 说明 |
|------|------|
| `admin` | 管理员（支持多角色：系统管理员/普通管理员） |
| `category` | 分类（树形结构，支持多级，含层级字段 level） |
| `tag` | 标签 |
| `resource` | 资源（含网盘链接、分类关联、浏览/下载计数） |
| `resource_tag` | 资源-标签多对多关联 |
| `keyword_rule` | 关键词规则（TAG/VERSION 两种类型） |
| `download_log` | 下载日志（含资源ID、标题、IP、时间） |
| `announcement` | 系统公告 |

## 📜 开源协议

MIT License
