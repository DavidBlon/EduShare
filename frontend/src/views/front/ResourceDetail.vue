<template>
  <div class="detail-page">
    <div class="page-container">
      <!-- Loading -->
      <div v-if="loading" class="loading-area">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- Error -->
      <el-empty v-else-if="error" :description="error">
        <el-button type="primary" @click="$router.back()">返回上一页</el-button>
      </el-empty>

      <!-- Content -->
      <template v-else-if="resource">
        <div class="detail-top">
          <!-- Breadcrumb -->
          <el-breadcrumb separator="/" class="detail-breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/resources' }">资源库</el-breadcrumb-item>
            <el-breadcrumb-item>{{ resource.title }}</el-breadcrumb-item>
          </el-breadcrumb>

          <!-- Back button -->
          <el-button text class="back-btn" @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
        </div>

        <div class="detail-wrapper">
          <!-- Main Info -->
          <div class="detail-main">
            <div class="detail-header">
              <h1 class="detail-title">{{ resource.title }}</h1>
              <div class="detail-meta">
                <span v-if="resource.categoryName">
                  <el-tag effect="plain" color="#e8f4ff" class="detail-cat-tag">
                    {{ resource.categoryName }}
                  </el-tag>
                </span>
                <span class="meta-stat">
                  <el-icon><View /></el-icon> {{ resource.viewCount || 0 }} 次浏览
                </span>
                <span class="meta-stat">
                  <el-icon><Download /></el-icon> {{ resource.downloadCount || 0 }} 次下载
                </span>
                <span class="meta-date">
                  <el-icon><Clock /></el-icon> {{ formatDate(resource.createdAt) }}
                </span>
              </div>
            </div>

            <!-- Cover -->
            <div class="detail-cover" v-if="resource.cover">
              <el-image
                :src="resource.cover"
                :alt="resource.title"
                fit="contain"
                lazy
                :preview-src-list="[resource.cover]"
                class="detail-image"
              />
            </div>

            <!-- Tags -->
            <div class="detail-tags" v-if="resource.tags && resource.tags.length">
              <span class="tag-label">标签：</span>
              <el-tag
                v-for="tag in resource.tags"
                :key="tag.id"
                type="success"
                effect="plain"
                class="detail-tag"
              >
                {{ tag.name }}
              </el-tag>
            </div>

            <!-- Description -->
            <div class="detail-desc" v-if="resource.description">
              <h3>资源简介</h3>
              <div class="desc-content">{{ resource.description }}</div>
            </div>
          </div>

          <!-- Sidebar -->
          <aside class="detail-sidebar">
            <el-card shadow="never" class="sidebar-card">
              <div class="sidebar-card-header">
                <el-icon><Link /></el-icon>
                <span>网盘信息</span>
              </div>

              <div class="netdisk-card">
                <div class="netdisk-field" v-if="netdiskInfo.netdiskLink">
                  <label>网盘链接</label>
                  <div class="field-value link-value">{{ netdiskInfo.netdiskLink }}</div>
                </div>
                <div class="netdisk-field" v-if="netdiskInfo.netdiskCode">
                  <label>提取码</label>
                  <div class="field-value code-value">
                    <span class="code-text">{{ showCode ? netdiskInfo.netdiskCode : '****' }}</span>
                    <el-button
                      text
                      type="primary"
                      size="small"
                      @click="showCode = !showCode"
                      class="toggle-code"
                    >
                      {{ showCode ? '隐藏' : '显示' }}
                    </el-button>
                  </div>
                </div>

                <el-divider class="netdisk-divider" />

                <div class="netdisk-actions">
                  <template v-if="!netdiskInfo.netdiskLink">
                    <el-button
                      type="primary"
                      size="large"
                      class="action-btn primary-btn"
                      @click="getNetdisk"
                      :loading="netdiskLoading"
                    >
                      <el-icon><Link /></el-icon>
                      查看网盘信息
                    </el-button>
                    <p class="netdisk-tip">点击查看网盘链接和提取码</p>
                  </template>

                  <template v-else>
                    <el-button type="primary" size="large" class="action-btn primary-btn" @click="copyLink">
                      <el-icon><CopyDocument /></el-icon>
                      {{ linkCopied ? '已复制' : '复制链接' }}
                    </el-button>
                    <el-button size="large" class="action-btn" @click="copyCode">
                      <el-icon><CopyDocument /></el-icon>
                      {{ codeCopied ? '已复制' : '复制提取码' }}
                    </el-button>
                  </template>
                </div>
              </div>
            </el-card>

            <!-- Share -->
            <el-card shadow="never" class="sidebar-card share-card">
              <div class="sidebar-card-header">
                <el-icon><Share /></el-icon>
                <span>分享</span>
              </div>
              <div class="share-actions">
                <el-button text @click="copyCurrentUrl">
                  <el-icon><Link /></el-icon> 复制链接
                </el-button>
              </div>
            </el-card>
          </aside>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getResourceDetail, getNetdiskInfo } from '@/api/resource'
import { ElMessage } from 'element-plus'

const route = useRoute()
const resource = ref(null)
const loading = ref(true)
const error = ref('')

const netdiskInfo = reactive({
  netdiskLink: '',
  netdiskCode: ''
})
const netdiskLoading = ref(false)
const showCode = ref(false)
const linkCopied = ref(false)
const codeCopied = ref(false)

onMounted(async () => {
  const id = route.params.id
  if (!id) {
    error.value = '资源ID不存在'
    loading.value = false
    return
  }
  try {
    const res = await getResourceDetail(id)
    resource.value = res.data
    if (!res.data) {
      error.value = '资源不存在或已删除'
    }
  } catch {
    error.value = '获取资源详情失败'
  } finally {
    loading.value = false
  }
})

async function getNetdisk() {
  if (netdiskInfo.netdiskLink) return
  netdiskLoading.value = true
  try {
    const res = await getNetdiskInfo(route.params.id)
    netdiskInfo.netdiskLink = res.data.netdiskLink
    netdiskInfo.netdiskCode = res.data.netdiskCode
    // Refresh to update download count
    const detailRes = await getResourceDetail(route.params.id)
    resource.value = detailRes.data
    ElMessage.success('网盘信息已获取')
  } catch {
    ElMessage.error('获取网盘信息失败')
  } finally {
    netdiskLoading.value = false
  }
}

function copyToClipboard(text) {
  if (navigator.clipboard && window.isSecureContext) {
    return navigator.clipboard.writeText(text)
  }
  return new Promise((resolve, reject) => {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy') ? resolve() : reject(new Error('copy failed'))
    } catch (e) {
      reject(e)
    } finally {
      document.body.removeChild(textarea)
    }
  })
}

async function copyLink() {
  try {
    await copyToClipboard(netdiskInfo.netdiskLink)
    linkCopied.value = true
    ElMessage.success('链接已复制')
    setTimeout(() => { linkCopied.value = false }, 2000)
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

async function copyCode() {
  try {
    await copyToClipboard(netdiskInfo.netdiskCode)
    codeCopied.value = true
    ElMessage.success('提取码已复制')
    setTimeout(() => { codeCopied.value = false }, 2000)
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

async function copyCurrentUrl() {
  try {
    await copyToClipboard(window.location.href)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

function formatDate(date) {
  if (!date) return ''
  return date.slice(0, 10)
}
</script>

<style scoped>
.detail-page {
  padding: 30px 0;
}

.detail-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.detail-breadcrumb {
  font-size: 14px;
}

.back-btn {
  font-size: 14px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.back-btn:hover {
  color: var(--primary);
}

.detail-wrapper {
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

.detail-main {
  flex: 1;
  min-width: 0;
  animation: fadeInUp 0.5s ease;
}

.detail-header {
  margin-bottom: 24px;
}

.detail-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 14px;
  line-height: 1.35;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.detail-cat-tag :deep(.el-tag__content) {
  color: var(--primary);
  font-weight: 500;
}

.meta-stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-date {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-cover {
  background: var(--bg);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
  padding: 20px;
  border: 1px solid var(--border-light);
}

.detail-image {
  max-height: 400px;
  width: 100%;
  border-radius: 8px;
}

.detail-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tag-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.detail-tag {
  transition: var(--transition);
}
.detail-tag:hover {
  transform: translateY(-1px);
}

.detail-desc {
  background: white;
  border-radius: var(--radius-lg);
  padding: 28px;
  box-shadow: var(--shadow);
}

.detail-desc h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px;
  padding-bottom: 14px;
  border-bottom: 2px solid var(--bg);
  display: flex;
  align-items: center;
  gap: 8px;
}
.detail-desc h3::before {
  content: '';
  width: 3px;
  height: 18px;
  background: var(--primary-gradient);
  border-radius: 2px;
}

.desc-content {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  white-space: pre-wrap;
}

/* ====== Sidebar ====== */
.detail-sidebar {
  width: 340px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 30px);
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: fadeInUp 0.5s ease 0.1s both;
}

.sidebar-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.sidebar-card :deep(.el-card__body) {
  padding: 0;
}

.sidebar-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
  padding: 18px 20px;
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(135deg, #f7f8fa, #f0f2f5);
}

.netdisk-card {
  padding: 20px;
}

.netdisk-field {
  margin-bottom: 16px;
}

.netdisk-field label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  font-weight: 500;
}

.field-value {
  font-size: 14px;
  color: var(--text-primary);
  word-break: break-all;
}

.link-value {
  padding: 8px 12px;
  background: var(--bg);
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
}

.code-value {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-text {
  font-size: 22px;
  letter-spacing: 5px;
  font-weight: 700;
  color: var(--primary);
  font-family: 'Courier New', monospace;
}

.toggle-code {
  flex-shrink: 0;
}

.netdisk-divider {
  margin: 16px 0;
}

.netdisk-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  width: 100%;
  border-radius: 8px;
  font-size: 15px;
  justify-content: center;
}

.primary-btn {
  padding: 14px;
  font-weight: 600;
}

.netdisk-tip {
  font-size: 12px;
  color: var(--text-placeholder);
  margin: 0;
  text-align: center;
}

/* Share card */
.share-card .share-actions {
  padding: 16px 20px;
}
.share-actions .el-button {
  width: 100%;
  justify-content: flex-start;
  font-size: 14px;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .detail-page {
    padding: 16px 0;
  }
  .detail-wrapper {
    flex-direction: column;
  }
  .detail-sidebar {
    width: 100%;
    position: static;
  }
  .detail-title {
    font-size: 22px;
  }
  .detail-meta {
    font-size: 13px;
    gap: 10px;
  }
  .detail-desc {
    padding: 20px;
  }
  .detail-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .detail-breadcrumb {
    font-size: 13px;
  }
  .detail-cover {
    padding: 12px;
  }
}
</style>
