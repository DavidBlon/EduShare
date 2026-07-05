<template>
  <div class="qrcode-page">
    <div class="page-header">
      <div class="header-left">
        <h2>资料群二维码</h2>
        <span class="header-desc">上传新图片将自动替换前台展示的微信群二维码</span>
      </div>
    </div>

    <div class="qrcode-layout">
      <!-- 上传区域 -->
      <el-card shadow="never" class="upload-card">
        <template #header>
          <span>上传新二维码</span>
        </template>

        <el-upload
          ref="uploadRef"
          drag
          accept="image/jpeg,image/png,image/gif,image/webp"
          :auto-upload="false"
          :limit="1"
          :on-change="onFileChange"
          :on-exceed="onExceed"
          class="upload-area"
        >
          <el-icon class="upload-icon" :size="48"><Plus /></el-icon>
          <div class="upload-text">
            <span>将二维码图片拖拽到此处，或 <em>点击选择</em></span>
          </div>
          <template #tip>
            <div class="upload-tip">
              支持 jpg/png/gif/webp 格式，建议使用正方形图片
            </div>
          </template>
        </el-upload>

        <div v-if="previewUrl" class="preview-row">
          <div class="preview-img-wrapper">
            <img :src="previewUrl" alt="预览" class="preview-img" />
          </div>
          <div class="preview-actions">
            <el-button type="primary" :loading="uploading" @click="handleUpload" size="large">
              <el-icon><Upload /></el-icon> 确认上传
            </el-button>
            <el-button @click="handleClear" :disabled="uploading" size="large">取消</el-button>
          </div>
        </div>
      </el-card>

      <!-- 当前二维码预览 -->
      <el-card shadow="never" class="current-card">
        <template #header>
          <span>当前前台展示</span>
        </template>
        <div class="current-qr-wrapper">
          <img
            :src="currentQrSrc"
            alt="当前二维码"
            class="current-qr"
            @error="onCurrentError"
          />
        </div>
        <p class="current-path">路径：/uploads/group-qrcode.png</p>
        <p class="current-hint">
          <el-icon><InfoFilled /></el-icon>
          上传新图片后将立即覆盖，前台自动生效
        </p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadQrCode } from '@/api/upload'

const currentQrSrc = '/uploads/group-qrcode.png'
const uploadRef = ref(null)
const uploading = ref(false)
const previewUrl = ref('')
const selectedFile = ref(null)

function onFileChange(uploadFile) {
  const rawFile = uploadFile.raw
  if (!rawFile) return

  // 校验文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(rawFile.type)) {
    ElMessage.warning('仅支持 jpg/png/gif/webp 格式的图片')
    uploadRef.value?.clearFiles()
    return
  }

  // 校验文件大小（10MB）
  if (rawFile.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 10MB')
    uploadRef.value?.clearFiles()
    return
  }

  selectedFile.value = rawFile
  previewUrl.value = URL.createObjectURL(rawFile)
}

function onExceed() {
  ElMessage.warning('每次只能上传一张图片')
}

function handleClear() {
  previewUrl.value = ''
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

async function handleUpload() {
  if (!selectedFile.value) return

  uploading.value = true
  try {
    await uploadQrCode(selectedFile.value)
    ElMessage.success('二维码更新成功！前台页面已自动生效')
    handleClear()
  } catch {
    // handled by interceptor
  } finally {
    uploading.value = false
  }
}

function onCurrentError() {
  // 首次部署时可能还没有上传过二维码，静默处理
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px;
}
.header-desc { font-size:13px; color:var(--text-secondary); }

.qrcode-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

/* ====== Upload Card ====== */
.upload-card :deep(.el-card__body) {
  padding: 24px;
}

.upload-area {
  width: 100%;
}

.upload-icon {
  color: var(--text-placeholder);
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: var(--text-secondary);
}
.upload-text em {
  color: var(--primary);
  font-style: normal;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 8px;
}

.preview-row {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.preview-img-wrapper {
  width: 160px;
  height: 160px;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--bg);
  flex-shrink: 0;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 8px;
}

/* ====== Current Card ====== */
.current-card :deep(.el-card__body) {
  padding: 24px;
  text-align: center;
}

.current-qr-wrapper {
  width: 200px;
  height: 200px;
  margin: 0 auto 16px;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--bg);
}

.current-qr {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.current-path {
  font-size: 12px;
  color: var(--text-placeholder);
  margin: 0 0 12px;
  word-break: break-all;
}

.current-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
  padding: 8px 12px;
  background: rgba(64, 158, 255, 0.06);
  border-radius: var(--radius);
}

/* ====== Responsive ====== */
@media (max-width: 768px) {
  .qrcode-layout {
    grid-template-columns: 1fr;
  }

  .preview-row {
    flex-direction: column;
    align-items: center;
  }

  .preview-actions {
    flex-direction: row;
  }

  .current-qr-wrapper {
    width: 160px;
    height: 160px;
  }
}
</style>
