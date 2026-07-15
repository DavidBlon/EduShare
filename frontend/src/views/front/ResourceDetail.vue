<template>
  <div class="detail-page">
    <div class="page-container">
      <!-- Loading -->
      <div v-if="loading" class="loading-area">
        <n-skeleton text :repeat="8" />
      </div>

      <!-- Error -->
      <div v-else-if="error" class="empty-area">
        <div class="empty-inner">
          <span class="empty-icon"><svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="8" y1="9" x2="8.01" y2="9" stroke-width="2"/><line x1="16" y1="9" x2="16.01" y2="9" stroke-width="2"/><line x1="9" y1="16" x2="15" y2="16"/></svg></span>
          <p>{{ error }}</p>
          <n-button @click="$router.back()" class="empty-btn">返回上一页</n-button>
        </div>
      </div>

      <!-- Content -->
      <template v-else-if="resource">
        <div class="detail-top">
          <!-- Breadcrumb -->
          <n-breadcrumb class="detail-breadcrumb">
            <n-breadcrumb-item>
              <router-link to="/">首页</router-link>
            </n-breadcrumb-item>
            <n-breadcrumb-item>
              <router-link to="/resources">资源库</router-link>
            </n-breadcrumb-item>
            <n-breadcrumb-item>{{ resource.title }}</n-breadcrumb-item>
          </n-breadcrumb>

          <!-- Back button -->
          <n-button quaternary class="back-btn" @click="$router.back()">
            <template #icon><n-icon><ArrowBackOutline /></n-icon></template>
            返回
          </n-button>
        </div>

        <div class="detail-wrapper">
          <!-- Main Info -->
          <div class="detail-main">
            <div class="detail-header">
              <h1 class="detail-title">{{ resource.title }}</h1>
              <div class="detail-meta">
                <span v-if="resource.categoryName">
                  <n-tag :bordered="false" color="#e6f1ed" class="detail-cat-tag">
                    {{ resource.categoryName }}
                  </n-tag>
                </span>
                <span class="meta-stat">
                  <n-icon><EyeOutline /></n-icon> {{ resource.viewCount || 0 }} 次浏览
                </span>
                <span class="meta-stat">
                  <n-icon><DownloadOutline /></n-icon> {{ resource.downloadCount || 0 }} 次下载
                </span>
                <span class="meta-date">
                  <n-icon><TimeOutline /></n-icon> {{ formatDate(resource.createdAt) }}
                </span>
              </div>
            </div>

            <!-- Cover -->
            <div class="detail-cover">
              <img
                :src="resource.cover || getTitleCover(resource.title, 800, 400)"
                :alt="resource.title"
                class="detail-image"
                @error="e => e.target.src = getTitleCover(resource.title, 800, 400)"
              />
            </div>

            <!-- Tags -->
            <div class="detail-tags" v-if="resource.tags && resource.tags.length">
              <span class="tag-label">标签：</span>
              <n-tag
                v-for="tag in resource.tags"
                :key="tag.id"
                type="success"
                :bordered="false"
                class="detail-tag"
              >
                {{ tag.name }}
              </n-tag>
            </div>

            <!-- Description -->
            <div class="detail-desc" v-if="resource.description">
              <h3>资源简介</h3>
              <div class="desc-content">{{ resource.description }}</div>
            </div>
          </div>

          <!-- Sidebar -->
          <aside class="detail-sidebar">
            <n-card :bordered="false" class="sidebar-card">
              <template #header>
                <div class="sidebar-card-header">
                  <n-icon><LinkOutline /></n-icon>
                  <span>网盘信息</span>
                </div>
              </template>

              <div class="netdisk-card">
                <div class="netdisk-field" v-if="netdiskInfo.netdiskLink">
                  <label>网盘链接</label>
                  <div class="field-value link-value">{{ netdiskInfo.netdiskLink }}</div>
                </div>
                <div class="netdisk-field" v-if="netdiskInfo.netdiskCode">
                  <label>提取码</label>
                  <div class="field-value code-value">
                    <span class="code-text">{{ showCode ? netdiskInfo.netdiskCode : '****' }}</span>
                    <n-button
                      quaternary
                      type="primary"
                      size="small"
                      @click="showCode = !showCode"
                      class="toggle-code"
                    >
                      {{ showCode ? '隐藏' : '显示' }}
                    </n-button>
                  </div>
                </div>

                <n-divider class="netdisk-divider" />

                <div class="netdisk-actions">
                  <template v-if="!netdiskInfo.netdiskLink">
                    <n-button
                      type="primary"
                      size="large"
                      class="action-btn primary-btn"
                      @click="getNetdisk"
                      :loading="netdiskLoading"
                    >
                      <template #icon><n-icon><LinkOutline /></n-icon></template>
                      查看网盘信息
                    </n-button>
                    <p class="netdisk-tip">点击查看网盘链接和提取码</p>
                  </template>

                  <template v-else>
                    <n-button type="primary" size="large" class="action-btn primary-btn" @click="copyLink">
                      <template #icon><n-icon><CopyOutline /></n-icon></template>
                      {{ linkCopied ? '已复制' : '复制链接' }}
                    </n-button>
                    <n-button size="large" class="action-btn" @click="copyCode">
                      <template #icon><n-icon><CopyOutline /></n-icon></template>
                      {{ codeCopied ? '已复制' : '复制提取码' }}
                    </n-button>
                    <n-button
                      size="large"
                      class="action-btn download-confirm-btn"
                      @click="handleConfirmDownload"
                      :loading="downloading"
                    >
                      <template #icon><n-icon><DownloadOutline /></n-icon></template>
                      跳转网盘
                    </n-button>
                  </template>
                </div>
              </div>
            </n-card>
          </aside>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  ArrowBackOutline,
  EyeOutline,
  DownloadOutline,
  TimeOutline,
  LinkOutline,
  CopyOutline
} from '@vicons/ionicons5'
import { getResourceDetail, getNetdiskInfo, confirmDownload } from '@/api/resource'
import { getTitleCover } from '@/utils/cover'

const message = useMessage()
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
    message.success('网盘信息已获取')
  } catch {
    message.error('获取网盘信息失败')
  } finally {
    netdiskLoading.value = false
  }
}

const downloading = ref(false)

async function handleConfirmDownload() {
  downloading.value = true
  try {
    await confirmDownload(route.params.id)
    // 刷新详情更新下载量显示
    const detailRes = await getResourceDetail(route.params.id)
    resource.value = detailRes.data
    message.success('下载已确认')
    // 跳转到网盘链接
    if (netdiskInfo.netdiskLink) {
      window.open(netdiskInfo.netdiskLink, '_blank')
    }
  } catch {
    message.error('确认失败')
  } finally {
    downloading.value = false
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
    message.success('链接已复制')
    setTimeout(() => { linkCopied.value = false }, 2000)
  } catch {
    message.error('复制失败，请手动复制')
  }
}

async function copyCode() {
  try {
    await copyToClipboard(netdiskInfo.netdiskCode)
    codeCopied.value = true
    message.success('提取码已复制')
    setTimeout(() => { codeCopied.value = false }, 2000)
  } catch {
    message.error('复制失败，请手动复制')
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

.detail-cat-tag {
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
  background: var(--paper);
  border-radius: 0;
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
  border-radius: 0;
  object-fit: contain;
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
  background: var(--paper);
  border: 1px solid var(--border-light);
  border-radius: 0;
  padding: 28px;
  box-shadow: var(--shadow-sm);
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
  background: var(--accent);
  border-radius: 0;
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
  border: 1px solid var(--border-light);
  border-radius: 0;
  overflow: hidden;
  background: var(--paper);
  box-shadow: var(--shadow-sm);
}

.sidebar-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: var(--primary-dark);
  background: #eef4ef;
  border-bottom: 1px solid #d7e4dd;
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
  padding: 9px 12px;
  background: #f5f1e8;
  border: 1px solid var(--border-light);
  border-radius: 0;
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
  color: var(--primary-dark);
}
.toggle-code:hover {
  background: var(--primary-bg);
  color: var(--primary-dark);
}

.netdisk-divider {
  margin: 16px 0;
  --n-color: var(--border-light);
}

.netdisk-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  width: 100%;
  min-height: 46px;
  border-radius: 0;
  font-size: 14px;
  font-weight: 600;
  justify-content: center;
  letter-spacing: .04em;
  transition: background var(--transition-fast), border-color var(--transition-fast), color var(--transition-fast), transform var(--transition-fast);
}

:deep(.primary-btn.n-button) {
  padding: 12px 14px;
  color: #fffdf8;
  background: var(--primary);
  border-color: var(--primary);
  box-shadow: 3px 3px 0 rgba(8, 75, 78, .16);
}
:deep(.primary-btn.n-button:hover),
:deep(.primary-btn.n-button:focus) {
  color: #fffdf8;
  background: var(--primary-dark);
  border-color: var(--primary-dark);
  box-shadow: 4px 4px 0 rgba(8, 75, 78, .18);
  transform: translate(-1px, -1px);
}
:deep(.primary-btn.n-button:active) {
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 rgba(8, 75, 78, .16);
}
:deep(.action-btn:not(.primary-btn):not(.download-confirm-btn).n-button) {
  color: var(--primary-dark);
  background: var(--paper);
  border-color: #a8c7bf;
}
:deep(.action-btn:not(.primary-btn):not(.download-confirm-btn).n-button:hover) {
  color: var(--primary-dark);
  background: var(--primary-bg);
  border-color: var(--primary);
}
:deep(.download-confirm-btn.n-button) {
  color: #8a571b;
  background: #fbefd9;
  border: 1px dashed var(--accent);
}
:deep(.download-confirm-btn.n-button:hover) {
  color: #724514;
  background: var(--accent-soft);
  border-color: #b87622;
}

.netdisk-tip {
  font-size: 12px;
  color: var(--text-placeholder);
  margin: 0;
  text-align: center;
}

/* Empty area */
.empty-area {
  text-align: center;
  padding: 80px 20px;
  background: var(--paper);
  border: 1px solid var(--border-light);
  border-radius: 0;
  box-shadow: var(--shadow-sm);
}
.empty-inner {
  text-align: center;
}
.empty-icon {
  display: block;
  margin-bottom: 12px;
}
.empty-icon svg {
  display: block;
  margin: 0 auto;
}
.empty-area p {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0 0 20px;
}
.empty-btn {
  border-radius: 8px;
}

/* Loading */
.loading-area {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow);
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
