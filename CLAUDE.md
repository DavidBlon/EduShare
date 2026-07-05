# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Compile
./mvnw compile

# Run tests
./mvnw test

# Run a single test
./mvnw test -Dtest=LearningResourceLibraryApplicationTests

# Start development server (MySQL must be running with edushare DB initialized)
./mvnw spring-boot:run

# Package
./mvnw clean package -DskipTests
```

## Project Overview

EduShare — an educational resource sharing platform (网盘资源分享平台). Spring Boot 4.0.7 backend, Vue 3 frontend (separate repo).

## Architecture

**Layered structure:** Controller → Service (interface + impl) → Mapper (MyBatis Plus BaseMapper) → MySQL

**Package layout:**
```
com.wb.learningresourcelibrary
├── common/
│   ├── config/          # MyBatisPlusConfig, WebMvcConfig
│   ├── constant/        # Constants.java — app-wide constants
│   ├── exception/       # BusinessException, GlobalExceptionHandler
│   └── result/          # Result<T> unified API response
├── controller/
│   ├── admin/           # admin.*Controller — JWT-protected, /api/admin/*
│   └── front/           # front.*Controller — public, /api/front/*
├── dto/                 # Data Transfer Objects (incoming request params)
├── entity/              # DOs mapped to DB tables (MyBatis Plus @TableName)
├── mapper/              # MyBatis Plus BaseMapper interfaces
├── security/            # JwtUtil, JwtAuthenticationFilter
└── service/             # Service interfaces + impl/
```

## Critical Architecture Decisions

### Spring Boot 4.0.7 + MyBatis Plus 3.5.9 Compatibility

This is the most important constraint. MyBatis Plus 3.5.9 does NOT officially support Spring Boot 4.x. The autoconfigure module references classes that moved or were removed. Manual configuration is required:

1. **Exclude autoconfigure from mybatis-plus dependency** in `pom.xml`
2. **Add explicit dependencies:** `mybatis-spring:3.0.4`, `spring-tx`, `spring-boot-starter-jdbc` (Boot 4 moved DataSource autoconfig out of spring-boot-autoconfigure into this starter)
3. **Manual `MybatisSqlSessionFactoryBean` config** in `MyBatisPlusConfig.java` — set DataSource, mapper locations, type aliases, GlobalConfig (logic delete + auto-fill)
4. **`@MapperScan` on `@SpringApplication` class** pointing to `com.wb.learningresourcelibrary.mapper` with `sqlSessionFactoryRef = "sqlSessionFactory"`

### Controller Naming

Every `@RestController` has an **explicit bean name** to avoid conflicts between admin and front controllers with same class names:
```java
@RestController("admin.resourceController")   // vs
@RestController("front.resourceController")
```

### Authentication

- JWT filter registered via `FilterRegistrationBean` (not `@Component` + `@Order`) to control URL patterns precisely
- Filter intercepts `/api/admin/*`, skips `/api/admin/login`
- Admin ID extracted from token (set as request attribute) for audit/logging

## Key Patterns

### API Response
Use `Result<T>` consistently:
```java
return Result.success(data);                        // 200 with data
return Result.success();                            // 200 no data
return Result.success("操作成功");                   // 200 with message (returns Result<Void>)
return Result.failed("错误信息");                     // 400 with message  
return Result.error(500, "服务器错误");               // 500
```

### Error Handling
`BusinessException` extends `RuntimeException` — throw with reason:
```java
throw BusinessException.notFound("资源不存在");
throw BusinessException.failed("参数错误");
```

### Admin Controller Pattern
```java
@RestController("admin.xxxController")
@RequestMapping("/api/admin/xxx")
@RequiredArgsConstructor
public class XxxController {
    private final XxxService xxxService;
    
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid XxxDto dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute(Constants.ADMIN_ID_ATTR);
        xxxService.add(dto);
        return Result.success("添加成功");
    }
}
```

### Soft Delete
MyBatis Plus `@TableLogic` on entity `isDeleted` field. Config: `logicDeleteValue=1`, `logicNotDeleteValue=0`.

### Auto-fill Timestamps
`MetaObjectHandler` auto-fills `createdAt`/`updatedAt` (insert) and `updatedAt` (update) on entities.

### Many-to-Many (Resource ↔ Tag)
Join table `tag_resource(resource_id, tag_id)`. Custom `ResourceTagMapper.xml` with `deleteByResourceId` for batch reassignment (delete all + re-insert).

### Pagination with Custom Query
`ResourceMapper.xml` defines `selectResourceWithCategory` with LEFT JOIN on category, optional keyword search, tagId filter, and sort modes (new/hot/recommend).

## Database

- **MySQL 8** with utf8mb4
- Run `src/main/resources/sql/init.sql` to create DB/tables/seed data
- Default admin: `admin` / `admin123`
- Tables: `admin`, `category` (tree via parent_id), `tag`, `resource`, `tag_resource`

## Testing

- H2 in-memory database in MySQL compatibility mode
- `@ActiveProfiles("test")` loads `src/test/resources/application-test.properties`
- Separate JWT secret (`test-secret-key-...`) for test isolation
- Upload path redirected to `target/test-uploads`

## Deployment

当用户说「部署」「上线」「重启」「推送服务器」「迭代」时，必须调用 Skill `edushare-deploy` 并按流程执行。

## Git Workflow

当用户说「推送」「拉取」「提交」「Git」「同步代码」「上传代码」等关键词或涉及两端代码同步操作时，必须调用 Skill `edushare-git` 并按规范区分本地/服务器环境执行。
