# 微信支付对接方案 — 按资源付费 (Native 扫码支付)

> 创建日期: 2026-07-04

## 背景

EduShare 教育资源共享平台目前资源免费查看，需要增加付费下载功能。用户可通过微信扫码支付购买单个资源，支付成功后查看网盘链接。

---

## 业务模型

**按资源付费**: 管理员为每个资源设置价格（单位：分），用户按需购买单个资源，支付成功后获取网盘链接和提取码。

---

## 架构决策

| 决策 | 选择 | 原因 |
|---|---|---|
| 付费模式 | 按资源付费 | 每个资源独立定价，最灵活 |
| 支付方式 | Native 扫码支付 | PC/移动端均适用，用户打开微信扫码 |
| 价格单位 | 分 (Long) | 避免浮点精度问题，DB 存 BIGINT |
| 支付客户端 | 接口 + Mock 模式 | 无商户号也可开发测试，配置文件切换 |
| 订单超时 | 30 分钟 | 二维码有效期，超时自动过期 |
| 前端轮询 | 3 秒间隔 | 查询订单状态直至支付成功/过期 |

---

## 数据表设计

### resource 表新增字段

```sql
ALTER TABLE `resource`
    ADD COLUMN `price` BIGINT DEFAULT 0 COMMENT '价格（单位：分，0表示免费）' AFTER `sort`;
```

### 新建 orders 表

```sql
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（唯一）',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `resource_title` VARCHAR(200) DEFAULT NULL COMMENT '资源标题（冗余）',
    `total_fee` BIGINT NOT NULL COMMENT '订单金额（单位：分）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待支付 PAID-已支付 EXPIRED-已过期 REFUNDED-已退款',
    `code_url` VARCHAR(500) DEFAULT NULL COMMENT '微信支付二维码URL',
    `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付订单号（微信侧）',
    `paid_at` DATETIME DEFAULT NULL COMMENT '支付时间',
    `expired_at` DATETIME NOT NULL COMMENT '订单过期时间',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '用户IP地址',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

---

## API 设计

### 前台 API（公开）

| 方法 | 路径 | 说明 | 请求体/参数 | 响应 |
|---|---|---|---|---|
| POST | `/api/front/order/create` | 创建订单 | `{resourceId: Long}` | `{orderNo, codeUrl, totalFee, status, expiredAt}` |
| GET | `/api/front/order/status/{orderNo}` | 查询订单状态 | 路径参数 | `{status, paidAt, netdiskLink?, netdiskCode?}` |
| GET | `/api/front/order/netdisk/{orderNo}` | 获取网盘信息 | 路径参数 | `{netdiskLink, netdiskCode}` |
| POST | `/api/front/order/notify` | 微信支付回调 | XML 通知 | XML 成功响应 |

### 管理端 API（JWT 保护）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/admin/order/page` | 订单分页列表 |
| GET | `/api/admin/order/{id}` | 订单详情 |

---

## 核心数据流

```
┌─ 管理员 ──────────────────────────────────────────────┐
│  管理后台 → 新增/编辑资源 → 设置价格(如 9.99 元)        │
│  → resource.price = 999 (分) 写入数据库                 │
└───────────────────────────────────────────────────────┘

┌─ 用户 ──────────────────────────────────────────────┐
│  资源详情页 → 看到价格和"立即购买"按钮                   │
│  → 点击购买 → POST /api/front/order/create            │
│  → 跳转支付页 /payment/{orderNo}                      │
│  → 显示二维码 → 打开微信扫一扫 → 支付                   │
│  → 页面自动显示网盘链接和提取码                          │
└───────────────────────────────────────────────────────┘

┌─ 后端 ──────────────────────────────────────────────┐
│  OrderService.createOrder():                          │
│    1. 校验资源存在/已发布/价格>0                       │
│    2. 生成订单号 ORD+yyyyMMddHHmmss+6位随机            │
│    3. 写入 orders 表 (status=PENDING, expired=30min)  │
│    4. 调用 WechatPayClient.prepay() 获取 code_url    │
│    5. 更新订单 code_url，返回 OrderVo                  │
│                                                       │
│  NotifyController (微信回调):                          │
│    1. 验证签名 (真实) 或跳过 (Mock)                    │
│    2. 更新订单 status=PAID, transactionId, paidAt    │
│                                                       │
│  订单状态查询: 每次检查 expiredAt, 自动过期             │
└───────────────────────────────────────────────────────┘

┌─ 前端 ──────────────────────────────────────────────┐
│  Payment.vue:                                        │
│    1. 渲染二维码 (vue-qrcode)                          │
│    2. 每3秒轮询 GET /api/front/order/status           │
│    3. PAID → 显示成功 + 网盘信息                      │
│    4. EXPIRED → 显示过期 + "重新购买"按钮              │
└───────────────────────────────────────────────────────┘
```

---

## 后端包结构设计

```
com.wb.learningresourcelibrary
├── payment/                          # ★ 新增包 - 支付相关
│   ├── WechatPayProperties.java      # 微信支付配置属性
│   ├── WechatPayClient.java          # 支付客户端接口
│   ├── PrepayRequest.java            # 预支付请求 DTO
│   ├── PrepayResponse.java           # 预支付响应 DTO
│   ├── MockWechatPayClient.java      # Mock 客户端（dev环境）
│   └── WechatPayNativeClient.java    # 真实微信支付客户端（prod环境）
├── controller/
│   ├── front/
│   │   └── OrderController.java      # ★ 新建 - 前台订单 API
│   │   └── NotifyController.java     # ★ 新建 - 微信支付回调
│   │   └── ResourceController.java   # ★ 修改 - 返回 price 字段
│   └── admin/
│       └── OrderController.java      # ★ 新建 - 管理端订单管理
├── entity/
│   ├── Order.java                    # ★ 新建 - 订单实体
│   └── Resource.java                 # ★ 修改 - 增加 price 字段
├── dto/
│   ├── OrderCreateDto.java           # ★ 新建 - 创建订单 DTO
│   └── ResourceDto.java              # ★ 修改 - 增加 price 字段
├── vo/
│   └── OrderVo.java                  # ★ 新建 - 订单视图对象
├── mapper/
│   ├── OrderMapper.java              # ★ 新建 - 订单 Mapper
│   └── (ResourceMapper.java 不变)
└── service/
    ├── OrderService.java             # ★ 新建 - 订单服务接口
    └── impl/
        ├── OrderServiceImpl.java     # ★ 新建 - 订单服务实现
        └── (ResourceServiceImpl.java # ☆ 修改 - 处理 price 字段)
```

---

## 完整文件清单

### 新建文件（后端 12 个）

| # | 文件路径 | 说明 |
|---|---|---|
| 1 | `entity/Order.java` | 订单实体，MyBatis Plus 注解 |
| 2 | `mapper/OrderMapper.java` | 订单 Mapper，继承 BaseMapper |
| 3 | `service/OrderService.java` | 订单服务接口 |
| 4 | `service/impl/OrderServiceImpl.java` | 订单服务实现（核心业务逻辑） |
| 5 | `dto/OrderCreateDto.java` | 创建订单请求 DTO |
| 6 | `vo/OrderVo.java` | 订单视图对象（返回前端） |
| 7 | `payment/WechatPayProperties.java` | 微信支付配置 (@ConfigurationProperties) |
| 8 | `payment/WechatPayClient.java` | 支付客户端接口 |
| 9 | `payment/PrepayRequest.java` | 预支付请求参数 |
| 10 | `payment/PrepayResponse.java` | 预支付响应结果 |
| 11 | `payment/MockWechatPayClient.java` | Mock 客户端（@ConditionalOnProperty） |
| 12 | `payment/WechatPayNativeClient.java` | 真实微信支付客户端（占位桩） |
| 13 | `controller/front/OrderController.java` | 前台订单 API |
| 14 | `controller/front/NotifyController.java` | 微信支付回调处理 |
| 15 | `controller/admin/OrderController.java` | 管理端订单管理 |

### 新建文件（前端 2 个）

| # | 文件路径 | 说明 |
|---|---|---|
| 16 | `api/order.js` | 订单 API 封装 |
| 17 | `views/front/Payment.vue` | 支付页面（二维码+轮询+结果展示） |

### 修改文件（后端 5 个）

| # | 文件路径 | 修改内容 |
|---|---|---|
| 18 | `src/main/resources/sql/init.sql` | 新增 resource.price 字段 + orders 表 DDL |
| 19 | `entity/Resource.java` | 增加 `private Long price` 字段 |
| 20 | `dto/ResourceDto.java` | 增加 `private Long price` 字段 |
| 21 | `resources/mapper/ResourceMapper.xml` | resultMap 增加 price 映射 |
| 22 | `service/impl/ResourceServiceImpl.java` | setResourceFields() 处理 price |
| 23 | `controller/front/ResourceController.java` | 返回 price 字段 |
| 24 | `application.yml` | 增加 wechat.pay.* 配置项 |

### 修改文件（前端 3 个）

| # | 文件路径 | 修改内容 |
|---|---|---|
| 25 | `views/admin/ResourceManage.vue` | 表单增加价格输入框 |
| 26 | `views/front/ResourceDetail.vue` | 显示价格和"立即购买"按钮 |
| 27 | `router/index.js` | 添加 `/payment/:orderNo` 路由 |
| 28 | `package.json` | 增加 `vue-qrcode` 依赖 |

---

## 配置项设计

```yaml
# application.yml 新增
wechat:
  pay:
    mock: true                          # true=Mock模式(开发测试)，false=真实支付
    app-id:                             # 微信小程序/公众号 AppID
    mch-id:                             # 商户号 ID
    api-key:                            # API 密钥
    api-v3-key:                         # API v3 密钥
    private-key-path:                   # 商户私钥证书路径
    merchant-serial:                    # 商户证书序列号
    notify-url: http://example.com/api/front/order/notify  # 支付回调地址
```

---

## 实现步骤（按依赖顺序）

### Phase 1: 数据库
- [ ] `init.sql` — 新增 `resource.price` 字段
- [ ] `init.sql` — 新建 `orders` 表 DDL

### Phase 2: 后端 — Resource 价格字段（4 files）
- [ ] `entity/Resource.java` — +`price` 字段
- [ ] `dto/ResourceDto.java` — +`price` 字段
- [ ] `mapper/ResourceMapper.xml` — resultMap +price
- [ ] `service/impl/ResourceServiceImpl.java` — 处理 price
- [ ] `controller/front/ResourceController.java` — 返回 price

### Phase 3: 后端 — 订单核心（6 files）
- [ ] `entity/Order.java` — 订单实体
- [ ] `mapper/OrderMapper.java` — 订单 Mapper
- [ ] `service/OrderService.java` — 订单服务接口
- [ ] `service/impl/OrderServiceImpl.java` — 订单服务实现
- [ ] `dto/OrderCreateDto.java` — 创建订单 DTO
- [ ] `vo/OrderVo.java` — 订单视图对象

### Phase 4: 后端 — 微信支付客户端（5 files）
- [ ] `payment/WechatPayProperties.java` — 配置属性
- [ ] `payment/WechatPayClient.java` — 客户端接口
- [ ] `payment/PrepayRequest.java` + `PrepayResponse.java` — 内部 DTO
- [ ] `payment/MockWechatPayClient.java` — Mock 实现
- [ ] `payment/WechatPayNativeClient.java` — 真实实现（桩）
- [ ] `application.yml` — 配置项

### Phase 5: 后端 — Controller（3 files）
- [ ] `controller/front/OrderController.java`
- [ ] `controller/front/NotifyController.java`
- [ ] `controller/admin/OrderController.java`

### Phase 6: 前端 — 管理端（1 file）
- [ ] `views/admin/ResourceManage.vue` — 价格输入框

### Phase 7: 前端 — 支付（4 files）
- [ ] `api/order.js` — API 封装
- [ ] `views/front/Payment.vue` — 支付页面
- [ ] `views/front/ResourceDetail.vue` — 价格 + 购买按钮
- [ ] `router/index.js` — 支付路由
- [ ] `package.json` — +vue-qrcode

### Phase 8: 测试验证
- [ ] `OrderServiceTest.java` — 单元测试
- [ ] 编译验证: `mvn clean test`
- [ ] 前端构建: `npm run build`

---

## 微信支付商户号申请

开始开发前需申请微信支付商户号（如未申请，可用 Mock 模式先行开发）：

1. **申请条件**: 已认证的微信服务号/小程序（需企业资质）
2. **申请入口**: [pay.weixin.qq.com](https://pay.weixin.qq.com/)
3. **所需材料**:
   - 营业执照（彩色扫描件）
   - 法人身份证
   - 对公账户信息
   - 经营场景证明
4. **申请后获取**:
   - `appid` — 绑定的小程序/公众号 AppID
   - `mchid` — 商户号
   - API 密钥 — 在商户平台设置
   - 商户证书 — 下载并配置私钥

> **开发阶段完全可以使用 `wechat.pay.mock=true` 进行全流程开发和测试，无需真实商户号。**

---

## 验证方案

1. **单元测试** (8 个用例):
   - `createOrder_success`: 有效资源，验证订单创建成功，状态 PENDING
   - `createOrder_resourceNotFound`: 资源不存在 → BusinessException
   - `createOrder_freeResource`: 免费资源 → BusinessException
   - `createOrder_draftResource`: 未发布资源 → BusinessException
   - `getOrderStatus_pending`: 查询待支付订单
   - `getOrderStatus_expired`: 已过期订单返回 EXPIRED
   - `getNetdisk_paid`: 支付后可获取网盘信息
   - `getNetdisk_unpaid`: 未支付 → BusinessException

2. **Mock 模式验证** (`wechat.pay.mock=true`):
   - 创建订单 → 返回 mock code_url
   - 前端显示二维码 → 轮询状态
   - 模拟支付 → 状态更新为 PAID → 显示网盘链接

3. **回归验证**:
   - 免费资源不受影响，直接显示网盘信息
   - 现有 API 格式不变（新增字段不破坏兼容性）
   - 管理端 CRUD 正常运行

---

## 关键代码设计要点

### OrderServiceImpl.createOrder() 核心逻辑

```java
@Transactional(rollbackFor = Exception.class)
public OrderVo createOrder(OrderCreateDto dto) {
    // 1. 校验资源
    Resource resource = resourceService.getById(dto.getResourceId());
    if (resource == null || resource.getIsDeleted() == 1)
        throw BusinessException.notFound("资源不存在或已下架");
    if (resource.getStatus() != 1)
        throw BusinessException.badRequest("资源未发布，暂不可购买");
    if (resource.getPrice() == null || resource.getPrice() <= 0)
        throw BusinessException.badRequest("免费资源无需购买");

    // 2. 创建订单
    Order order = new Order();
    order.setOrderNo(generateOrderNo());
    order.setResourceId(resource.getId());
    order.setResourceTitle(resource.getTitle());
    order.setTotalFee(resource.getPrice());
    order.setStatus(OrderStatus.PENDING.name());
    order.setExpiredAt(LocalDateTime.now().plusMinutes(30));
    orderMapper.insert(order);

    // 3. 调用微信支付获取二维码
    PrepayResponse prepayRes = wechatPayClient.prepay(
        new PrepayRequest(order.getOrderNo(), resource.getTitle(), resource.getPrice())
    );
    order.setCodeUrl(prepayRes.getCodeUrl());
    orderMapper.updateById(order);

    return toOrderVo(order);
}
```

### MockWechatPayClient

```java
@Component
@ConditionalOnProperty(name = "wechat.pay.mock", havingValue = "true", matchIfMissing = true)
public class MockWechatPayClient implements WechatPayClient {
    public PrepayResponse prepay(PrepayRequest request) {
        String mockCodeUrl = "mock://" + IdUtil.fastSimpleUUID();
        return new PrepayResponse(mockCodeUrl);
    }
}
```

### Payment.vue 轮询机制

```js
const pollTimer = ref(null)

function startPolling() {
  pollTimer.value = setInterval(async () => {
    const res = await getOrderStatus(props.orderNo)
    if (res.data.status === 'PAID') {
      clearInterval(pollTimer.value)
      // 显示成功 + 网盘信息
    } else if (res.data.status === 'EXPIRED') {
      clearInterval(pollTimer.value)
      // 显示过期 + 重新购买
    }
  }, 3000)
}

onUnmounted(() => {
  if (pollTimer.value) clearInterval(pollTimer.value)
})
```

---

## 注意事项

1. **价格精度**: 数据库中存 `BIGINT`（单位：分），后端用 `Long`，前端显示时除以 100（保留两位小数）
2. **订单号唯一性**: `ORD + yyyyMMddHHmmss + 6位随机数`，配合数据库唯一索引
3. **幂等处理**: 微信回调可能重复通知，事务内加 `status` 判断防止重复更新
4. **Mock 模式**: 开发环境自动启用，生产环境需配置真实参数后关闭
5. **无用户系统**: 当前平台无用户注册，订单仅通过 `order_no` 关联，后续接入用户体系后扩展
