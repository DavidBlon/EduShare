<template>
  <div class="qrcode-page">
    <div class="page-header">
      <div class="header-left">
        <h2>资料群二维码</h2>
        <span class="header-desc">上传新图片将自动替换前台展示的微信群二维码</span>
      </div>
    </div>

    <div class="qrcode-layout">
      <n-card :bordered="false" class="upload-card">
        <template #header>
          <span>上传新二维码</span>
        </template>

        <n-upload
          ref="uploadRef"
          accept="image/jpeg,image/png,image/gif,image/webp"
          :default-upload="false"
          :max="1"
          @change="onFileChange"
        >
          <n-upload-dragger>
            <div style="margin-bottom: 8px;">
              <n-icon :size="48" color="var(--text-placeholder)"><AddOutline /></n-icon>
            </div>
            <div class="upload-text">
              <span>将二维码图片拖拽到此处，或 <em>点击选择</em></span>
            </div>
          </n-upload-dragger>
        </n-upload>
        <div class="upload-tip">支持 jpg/png/gif/webp 格式，建议使用正方形图片</div>

        <div v-if="previewUrl" class="preview-row">
          <div class="preview-img-wrapper">
            <img :src="previewUrl" alt="预览" class="preview-img" />
          </div>
          <div class="preview-actions">
            <n-button type="primary" :loading="uploading" @click="handleUpload" size="large">
              <template #icon><n-icon><CloudUploadOutline /></n-icon></template>
              确认上传
            </n-button>
            <n-button @click="handleClear" :disabled="uploading" size="large">取消</n-button>
          </div>
        </div>
      </n-card>

      <n-card :bordered="false" class="current-card">
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
          <n-icon><InformationCircleOutline /></n-icon>
          上传新图片后将立即覆盖，前台自动生效
        </p>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import { AddOutline, CloudUploadOutline, InformationCircleOutline } from '@vicons/ionicons5'
import { uploadQrCode } from '@/api/upload'

const message = useMessage()
const currentQrSrc = ref(`/uploads/group-qrcode.png?t=${Date.now()}`)
const uploadRef = ref(null)
const uploading = ref(false)
const previewUrl = ref('')
const selectedFile = ref(null)

function onFileChange({ file }) {
  const rawFile = file?.file || file
  if (!rawFile) return

  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(rawFile.type)) {
    message.warning('仅支持 jpg/png/gif/webp 格式的图片')
    return
  }
  if (rawFile.size > 10 * 1024 * 1024) {
    message.warning('文件大小不能超过 10MB')
    return
  }

  selectedFile.value = rawFile
  previewUrl.value = URL.createObjectURL(rawFile)
}

function handleClear() {
  previewUrl.value = ''
  selectedFile.value = null
  uploadRef.value?.clear()
}

async function handleUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  try {
    await uploadQrCode(selectedFile.value)
    message.success('二维码更新成功！前台页面已自动生效')
    handleClear()
    currentQrSrc.value = `/uploads/group-qrcode.png?t=${Date.now()}`
  } catch {
    // handled by interceptor
  } finally {
    uploading.value = false
  }
}

function onCurrentError() {
  // silently handle missing image
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
  background: rgba(99, 102, 241, 0.06);
  border-radius: var(--radius);
}

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
