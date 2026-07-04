<template>
  <div class="detail-page">
    <div class="page-container">
      <!-- Loading -->
      <div v-if="loading" class="loading-area" style="padding:80px 0;text-align:center;">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- Error -->
      <el-empty v-else-if="error" :description="error">
        <el-button type="primary" @click="$router.back()">返回上一页</el-button>
      </el-empty>

      <!-- Content -->
      <template v-else-if="resource">
        <!-- Breadcrumb -->
        <el-breadcrumb separator="/" class="detail-breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/resources' }">资源库</el-breadcrumb-item>
          <el-breadcrumb-item>{{ resource.title }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="detail-wrapper">
          <!-- Main Info -->
          <div class="detail-main">
            <div class="detail-header">
              <h1 class="detail-title">{{ resource.title }}</h1>
              <div class="detail-meta">
                <span v-if="resource.categoryName">
                  <el-tag effect="plain">{{ resource.categoryName }}</el-tag>
                </span>
                <span class="meta-stat">
                  <el-icon><View /></el-icon> {{ resource.viewCount || 0 }} 次浏览
                </span>
                <span class="meta-stat">
                  <el-icon><Download /></el-icon> {{ resource.downloadCount || 0 }} 次下载
                </span>
                <span class="meta-date">{{ formatDate(resource.createdAt) }}</span>
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
                style="max-height:400px;width:100%;"
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
            <el-card shadow="never">
              <template #header>
                <span class="sidebar-title"><el-icon><Link /></el-icon> 网盘信息</span>
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
                    <el-button text type="primary" @click="showCode = !showCode">
                      {{ showCode ? '隐藏' : '显示' }}
                    </el-button>
                  </div>
                </div>

                <el-divider />

                <div class="netdisk-actions">
                  <el-button
                    type="primary"
                    size="large"
                    class="copy-btn"
                    @click="getNetdisk"
                    :loading="netdiskLoading"
                    :disabled="!!netdiskInfo.netdiskLink"
                  >
                    <el-icon><Link /></el-icon>
                    {{ netdiskInfo.netdiskLink ? '复制网盘链接' : '查看网盘信息' }}
                  </el-button>
                  <p class="netdisk-tip" v-if="!netdiskInfo.netdiskLink">
                    点击查看网盘链接和提取码
                  </p>
                  <div v-else class="copy-actions">
                    <el-button @click="copyLink" :class="{ copied: linkCopied }">
                      {{ linkCopied ? '已复制' : '复制链接' }}
                    </el-button>
                    <el-button @click="copyCode" :class="{ copied: codeCopied }">
                      {{ codeCopied ? '已复制' : '复制提取码' }}
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </aside>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
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
  } catch (e) {
    error.value = '获取资源详情失败'
  } finally {
    loading.value = false
  }
})

async function getNetdisk() {
  if (netdiskInfo.netdiskLink) return // Already loaded
  netdiskLoading.value = true
  try {
    const res = await getNetdiskInfo(route.params.id)
    netdiskInfo.netdiskLink = res.data.netdiskLink
    netdiskInfo.netdiskCode = res.data.netdiskCode
    // Refresh detail to update download count
    const detailRes = await getResourceDetail(route.params.id)
    resource.value = detailRes.data
    ElMessage.success('网盘信息已获取')
  } catch {
    ElMessage.error('获取网盘信息失败')
  } finally {
    netdiskLoading.value = false
  }
}

// 兼容 HTTP 环境的剪贴板写入（navigator.clipboard 需要 HTTPS）
function copyToClipboard(text) {
  // 优先使用 Clipboard API（HTTPS 环境）
  if (navigator.clipboard && window.isSecureContext) {
    return navigator.clipboard.writeText(text)
  }
  // 降级方案：textarea + execCommand（兼容 HTTP）
  return new Promise((resolve, reject) => {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      const successful = document.execCommand('copy')
      if (successful) resolve()
      else reject(new Error('execCommand copy failed'))
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
    ElMessage.success('链接已复制到剪贴板')
    setTimeout(() => { linkCopied.value = false }, 2000)
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

async function copyCode() {
  try {
    await copyToClipboard(netdiskInfo.netdiskCode)
    codeCopied.value = true
    ElMessage.success('提取码已复制到剪贴板')
    setTimeout(() => { codeCopied.value = false }, 2000)
  } catch {
    ElMessage.error('复制失败，请手动复制')
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

.detail-breadcrumb {
  margin-bottom: 24px;
}

.detail-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.detail-main {
  flex: 1;
  min-width: 0;
}

.detail-header {
  margin-bottom: 24px;
}

.detail-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.meta-stat {
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

.detail-desc {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow);
}

.detail-desc h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--bg);
}

.desc-content {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  white-space: pre-wrap;
}

/* Sidebar */
.detail-sidebar {
  width: 340px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 30px);
}

.sidebar-title {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.netdisk-field {
  margin-bottom: 16px;
}

.netdisk-field label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
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
}

.code-value {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-text {
  font-size: 20px;
  letter-spacing: 4px;
  font-weight: 700;
  color: var(--primary);
}

.netdisk-actions {
  text-align: center;
}

.copy-btn {
  width: 100%;
  font-size: 16px;
  padding: 16px;
}

.netdisk-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 8px 0 0;
}

.copy-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.copy-actions .el-button {
  flex: 1;
}

.copy-actions .copied {
  background: #67c23a;
  color: white;
  border-color: #67c23a;
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
    font-size: 20px;
    line-height: 1.3;
  }
  .detail-meta {
    font-size: 13px;
    gap: 10px;
  }
  .detail-desc {
    padding: 16px;
  }
  .detail-breadcrumb {
    margin-bottom: 16px;
    font-size: 13px;
  }
  .netdisk-actions .copy-btn {
    font-size: 14px;
    padding: 12px;
  }
  .copy-actions {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
