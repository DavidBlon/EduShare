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

> **`git stash pop` 冲突时**：不要手动编辑冲突文件，按以下规则快速解决：
> 
> - **前端文件（`.vue`、`.js`、`.css` 等）**：用远程版本覆盖（本地前端修改已提交到 GitHub，不保留本地）
>   ```bash
>   git checkout origin/fix -- frontend/src/views/front/xxx.vue
>   ```
>   > 如果 `--theirs`/`--ours` 分不清哪个才是远程版本，直接用 `git checkout origin/fix -- <file>` 最保险。
> - **`application.properties`**：保留服务器版本（数据库密码、配置不同）
>   ```bash
>   git checkout --ours src/main/resources/application.properties
>   ```
>   > 注意：rebase 场景下 `--ours` 和 `--theirs` 含义互换，哪个不行换另一个。
> - **标记解决并提交**
>   ```bash
>   git add .
>   git commit -m "fix: 解决合并冲突"
>   ```

### Step 2: 前端构建（仅改前端时必须执行）

```bash
cd frontend
npm install
npm run build
cd ..
```

> ⚠️ **只 `nginx -s reload` 不会更新前端！** Nginx 分发的是 `dist/` 目录下的静态文件，必须 `npm run build` 重新生成 dist，Nginx 重载才会生效。
>
> 验证 dist 文件是否更新：`ls -la frontend/dist/assets/Home-*.js`

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
| `git stash pop` 报冲突 | 前端文件 `checkout --theirs`，`application.properties` `checkout --ours`，然后 `git add .` + `git commit` |
| 前端打包后页面无变化 | 先确认 `dist/` 文件是否更新（`ls -la frontend/dist/assets/Home-*.js`）；仅改前端时不需要 `mvn package`，但必须 `npm run build` |
| 修改前端后只 `nginx -s reload` 没变化 | **必须先 `npm run build`**，Nginx 只分发 `dist/` 里的文件，不构建永远不会更新 |
| `--theirs`/`--ours` 分不清哪个是远程 | 直接用 `git checkout origin/fix -- <file>` 拿远程版本 |
| `git push` 报 `[rejected]` | `git pull` 合并远程代码后重推，禁止 `--force` |
| 后端启动失败 | `journalctl -u edushare -f` 查看日志；检查 `upload/` 目录权限；确认 Jar 包存在 |

## 禁止操作

- ❌ 禁止 `git push --force`
- ❌ 禁止提交 `upload/`、`target/`、`*.log`、`*.hprof` 至 GitHub
- ❌ 仅改前端时跳过 `mvn package`（一体化 Jar 不打包不生效）
- ❌ 直接 `kill` Java 进程，统一用 `systemctl stop edushare`