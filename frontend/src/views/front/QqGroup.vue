<template>
  <div class="qq-group-page">
    <div class="page-container" style="padding-top: 40px;">
      <div class="group-layout">
        <!-- Left: QR Code Card -->
        <div class="qr-card">
          <div class="qr-header">
            <el-icon :size="22" style="color:#409eff"><Connection /></el-icon>
            <span>扫码加群</span>
          </div>
          <div class="qr-image-wrapper">
            <el-image
              src="/uploads/group-qrcode.png"
              alt="微信群二维码"
              :preview-src-list="['/uploads/group-qrcode.png']"
              :initial-index="0"
              fit="contain"
              hide-on-click-modal
              preview-teleported
              @load="onQrLoad"
              :class="['qr-image', { loaded: qrLoaded }]"
              style="width:100%;height:100%"
            >
              <template #loading>
                <div v-if="!qrLoaded" class="qr-placeholder" style="position:static;height:100%;min-height:200px">
                  <el-icon :size="48" class="qr-loading-icon"><Loading /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="qr-footer">
            <p class="qr-tip">长按或扫描上方二维码加入微信群</p>
            <p class="qr-sub-tip">如二维码过期，请联系网站管理员更新</p>
          </div>
        </div>

        <!-- Right: Group Info -->
        <div class="group-info">
          <section class="info-section">
            <h2 class="info-title">
              <el-icon :size="20"><InfoFilled /></el-icon>
              关于资料群
            </h2>
            <p class="info-text">
              小初学习资料圈微信群是一个面向家长、教师和教育工作者的免费交流平台。
              在这里，你可以与其他成员一起探讨学习方法、分享教育资源、获取最新资料更新通知。
            </p>
          </section>

          <section class="info-section">
            <h2 class="info-title">
              <el-icon :size="20"><Bell /></el-icon>
              群内福利
            </h2>
            <div class="benefit-list">
              <div class="benefit-item" v-for="item in benefits" :key="item.title">
                <div class="benefit-icon" :style="{ background: item.color }">
                  <el-icon :size="20"><component :is="item.icon" /></el-icon>
                </div>
                <div class="benefit-body">
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.desc }}</p>
                </div>
              </div>
            </div>
          </section>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const qrLoaded = ref(false)

function onQrLoad(e) {
  qrLoaded.value = true
  e.target.classList.add('loaded')
}

const benefits = [
  {
    icon: 'Bell',
    title: '资源更新通知',
    desc: '第一时间获取平台最新上架的资源信息，不再错过任何精品资料。',
    color: 'linear-gradient(135deg, #409eff, #337ecc)'
  },
  {
    icon: 'ChatLineSquare',
    title: '学习经验交流',
    desc: '与众多家长和老师交流辅导经验、学习方法，共同进步。',
    color: 'linear-gradient(135deg, #67c23a, #529b2e)'
  },
  {
    icon: 'Document',
    title: '独家资料分享',
    desc: '群内不定期分享独家整理的学科资料、真题试卷等额外福利。',
    color: 'linear-gradient(135deg, #e6a23c, #cf9236)'
  },
]
</script>

<style scoped>
.qq-group-page {
  min-height: 60vh;
  padding-bottom: 60px;
}

/* ========== Layout ========== */
.group-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 32px;
  margin-top: 40px;
  align-items: start;
}

/* ========== QR Card ========== */
.qr-card {
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  overflow: hidden;
  position: sticky;
  top: calc(var(--header-height) + 24px);
}

.qr-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 24px 0;
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

.qr-image-wrapper {
  position: relative;
  margin: 20px 24px;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--bg);
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.qr-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  transition: opacity 0.6s ease, filter 0.6s ease;
}

/* Blur-up lazy loading for QR code */
.qr-image:not(.loaded) {
  filter: blur(24px);
  opacity: 0.3;
  transform: scale(1.1);
}
.qr-image.loaded {
  filter: blur(0);
  opacity: 1;
  transform: scale(1);
}

.qr-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  pointer-events: none;
}

.qr-loading-icon {
  color: var(--text-placeholder);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.qr-footer {
  padding: 0 24px 24px;
  text-align: center;
}

.qr-tip {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 4px;
}

.qr-sub-tip {
  font-size: 12px;
  color: var(--text-placeholder);
  margin: 0;
}

/* ========== Group Info ========== */
.group-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-section {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 28px 32px;
  box-shadow: var(--shadow);
}

.info-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px;
  padding-bottom: 14px;
  border-bottom: 2px solid var(--bg);
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-text {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  margin: 0;
}

/* ========== Benefit List ========== */
.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.benefit-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius);
  background: var(--bg);
  transition: var(--transition);
}
.benefit-item:hover {
  transform: translateX(4px);
  box-shadow: var(--shadow);
}

.benefit-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.benefit-body h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.benefit-body p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}

/* ========== Responsive ========== */
@media (max-width: 768px) {
  .group-layout {
    grid-template-columns: 1fr;
    gap: 20px;
    margin-top: 20px;
  }

  .qr-card {
    position: static;
  }

  .qr-image-wrapper {
    margin: 16px;
    max-width: 280px;
    margin: 16px auto;
  }

  .info-section {
    padding: 20px;
  }

  .benefit-item {
    padding: 14px;
  }

  .info-title {
    font-size: 18px;
  }
}
</style>
