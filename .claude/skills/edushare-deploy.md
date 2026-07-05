---
name: edushare-deploy
description: EduShare 生产服务器部署流程 — 从拉取代码、前端构建、后端打包到重启服务的完整操作指南（fix 分支）
---

# EduShare 服务器部署流程（fix 分支）

## 环境

- 项目路径：`/root/LearningResourceLibrary`
- 远程仓库：`origin = https://github.com/DavidBlon/EduShare.git`
- 线上分支：`fix`（服务器生产分支）
- 部署架构：Vue 前端独立目录 + SpringBoot 一体化 Jar 包 + systemd 托管后端 + Nginx 分发静态资源
- 特殊场景：服务器本地存在自定义后端代码，拉取远程代码时不能覆盖本地修改

## 首次前置配置（仅一次）

### Git 凭据持久化 & 分支绑定

```bash
git config --global credential.helper store
git branch --set-upstream-to=origin/fix fix
```

### systemd 服务配置

`/etc/systemd/system/edushare.service` 已配置 `-Xms512m -Xmx800m MemoryMax=1000M`（2核2G 服务器），无需重复操作。

## 日常部署流程（按顺序执行）

### Step 1: 拉取最新代码（保护本地修改）

```bash
cd /root/LearningResourceLibrary
git stash            # 暂存本地修改
git pull             # 拉取远程最新代码
git stash pop        # 恢复本地修改
```

> `git stash pop` 冲突时，手动编辑冲突文件后执行 `git add .` 标记解决。

### Step 2: 前端构建

```bash
cd frontend
npm install
npm run build
cd ..
```

### Step 3: 后端打包

```bash
mvn clean package -DskipTests
```

产物：`target/EduShare-0.0.1-SNAPSHOT.jar`

### Step 4: 重启后端

```bash
systemctl stop edushare
systemctl start edushare
systemctl status edushare    # 确认无报错
```

### Step 5: 重载 Nginx

```bash
nginx -s reload
```

### Step 6: 验证

- 浏览器 Ctrl+F5 强制刷新
- 查看日志：`journalctl -u edushare -f`

## 服务器本地代码推送到 GitHub

```bash
cd /root/LearningResourceLibrary
git add .
git commit -m "fix: 本次修改说明"
git push
```

> 已配置 `credential.helper store`，首次输入令牌后无需重复输入。

## 高频问题

| 问题 | 解决 |
|------|------|
| 拉取提示本地文件将被覆盖 | `git stash` → `git pull` → `git stash pop` |
| 前端打包后页面无变化 | 确认 dist 更新时间；必须执行 `mvn package` 并重启后端；`nginx -s reload` |
| `git push` 报 `[rejected]` | `git pull` 合并远程代码后重推，禁止 `--force` |
| 后端启动失败 | `journalctl -u edushare -f` 查看日志；检查 `upload/` 目录权限；确认 Jar 包存在 |

## 禁止操作

- ❌ 禁止 `git push --force`
- ❌ 禁止提交 `upload/`、`target/`、`*.log`、`*.hprof` 至 GitHub
- ❌ 仅改前端时跳过 `mvn package`（一体化 Jar 不打包不生效）
- ❌ 直接 `kill` Java 进程，统一用 `systemctl stop edushare`