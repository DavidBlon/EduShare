---
name: edushare-git
description: EduShare 本地开发 / 服务器端 Git 推拉完整规范 — 隐私配置隔离、两端环境区分、推拉流程标准化
---

# EduShare Git 推拉规范（本地 ↔ 服务器）

## 设备区分

| 设备 | 路径 | 职责 |
|------|------|------|
| **本地开发端** | 个人电脑 | 日常功能迭代，无线上密码，推纯净业务代码 |
| **线上服务器** | `/root/LearningResourceLibrary` | 存放线上生产配置，负责部署上线，紧急修复 |

**仓库分支**：`fix`（唯一生产分支）

---

## 一、首次配置（两端各自执行一次）

### 1.1 Git 合并策略

```bash
git config --global pull.rebase false
```

### 1.2 Git 凭据持久化（免重复输入密码）

```bash
git config --global credential.helper store
```

### 1.3 绑定本地 fix 分支与远程 fix 分支

```bash
git branch --set-upstream-to=origin/fix fix
```

### 1.4 解除 Git 对配置文件的追踪（已执行则跳过）

```bash
git rm --cached src/main/resources/application.properties
git commit -m "chore: 屏蔽本地隐私配置，禁止上传密码文件"
git push
```

---

## 二、本地开发端流程

### 日常推送

```bash
git pull                                 # 拉取最新代码
git add .
git commit -m "feat: 新增/优化xxx功能"
git push                                 # 推送纯净业务代码
```

### 规则

- 本地 **禁止存放线上真实数据库/Redis 密码**，仅用测试配置
- 本地只负责业务代码迭代，不维护线上生产环境配置

---

## 三、服务器端流程

### 标准安全推送（线上紧急修改）

```bash
cd /root/LearningResourceLibrary
git stash                                # 暂存线上本地修改
git pull                                 # 拉取远程最新代码
git stash pop                            # 恢复线上本地修改
git add .
git commit -m "fix: 线上紧急修复xxx问题"
git push                                 # 安全推送，配置永不上传
```

### 拉取本地开发者的代码（上线部署）

```bash
cd /root/LearningResourceLibrary
git stash
git pull
git stash pop
# 然后执行 edushare-deploy skill 的后续步骤（前端构建、后端打包、重启）
```

---

## 四、报错应急

| 错误 | 解决 |
|------|------|
| `fetch first`（服务器） | `git stash` → `git pull` → `git stash pop` → `git push` |
| `fetch first`（本地） | `git pull` → `git push` |
| 分支分叉 `divergent` | `git pull origin fix --no-rebase` → `git push` |
| `git stash pop` 冲突 | 手动编辑冲突文件 → `git add .` 标记解决 |

### 单人仓库强制同步（仅应急）

```bash
git push origin fix --force-with-lease
```

---

## 五、核心原则

1. **远程 GitHub 仓库**：只存纯净业务代码，无任何隐私配置
2. **线上服务器**：独占 `application.properties` 生产配置，Git 无视、不跟踪、不冲突
3. **本地开发端**：无生产配置，只同步公共业务代码
4. **环境配置物理隔离**，互不覆盖

### 验证命令

```bash
git status
```

看不到 `application.properties` 文件变更 = 屏蔽成功。
