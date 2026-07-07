<template>
  <div class="banner">
    <!-- Animated background gradient -->
    <div class="banner-bg" :style="{ background: `linear-gradient(135deg, ${bgStart}, ${bgEnd})` }"></div>

    <!-- Mesh grid texture overlay -->
    <div class="mesh-grid"></div>

    <!-- Animated gradient orbs -->
    <div class="banner-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
    </div>

    <!-- Animated floating particles -->
    <div class="banner-particles">
      <div
        v-for="i in 12"
        :key="i"
        :class="['particle', i % 3 === 0 ? 'particle-dot' : 'particle-circle']"
        :style="{
          width: particleSize(i) + 'px',
          height: particleSize(i) + 'px',
          left: particleLeft(i) + '%',
          top: particleTop(i) + '%',
          animationDelay: particleDelay(i) + 's',
          animationDuration: (5 + i * 0.8) + 's',
        }"
      ></div>
    </div>

    <!-- Sparkle stars -->
    <div class="sparkles">
      <div
        v-for="i in 6"
        :key="'s' + i"
        class="sparkle"
        :style="{
          left: sparkleLeft(i) + '%',
          top: sparkleTop(i) + '%',
          animationDelay: sparkleDelay(i) + 's',
        }"
      ></div>
    </div>

    <div class="banner-content">
      <div class="banner-title-row">
        <div class="title-logo">
          <img src="/logo.jpg" alt="小初学习资料圈" class="logo-img" />
        </div>
        <h1 class="banner-title">{{ title }}</h1>
      </div>
      <div class="banner-search" v-if="showSearch">
        <div class="search-glow"></div>
        <div class="search-wrapper">
          <n-input
            v-model:value="searchKeyword"
            placeholder=""
            clearable
            class="banner-input"
            :name="'search-input-home'"
            autocomplete="off"
            @keyup.enter="doSearch"
          >
          </n-input>
          <n-button @click="doSearch" class="banner-search-btn">
            <template #icon>
              <n-icon :size="20"><SearchOutline /></n-icon>
            </template>
          </n-button>
        </div>
      </div>
    </div>

    <!-- Bottom wave divider -->
    <div class="banner-wave">
      <svg viewBox="0 0 1440 80" preserveAspectRatio="none">
        <path d="M0,40 C360,80 720,0 1440,40 L1440,80 L0,80 Z" fill="var(--bg)"/>
      </svg>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { SearchOutline } from '@vicons/ionicons5'

const props = defineProps({
  title: { type: String, default: '小初学习资料圈' },
  showSearch: { type: Boolean, default: true },
  bgStart: { type: String, default: '#1a1a2e' },
  bgEnd: { type: String, default: '#1e3a5f' }
})

const router = useRouter()
const searchKeyword = ref('')

onMounted(() => {
  // 延迟清空，确保覆盖浏览器自动填充
  setTimeout(() => { searchKeyword.value = '' }, 200)
})

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/resources', query: { keyword: searchKeyword.value.trim() } })
  }
}

function particleSize(i) {
  const sizes = [70, 35, 90, 45, 25, 65, 50, 40, 80, 30, 55, 20]
  return sizes[i - 1]
}
function particleLeft(i) {
  const positions = [8, 82, 55, 22, 72, 3, 92, 38, 15, 68, 48, 88]
  return positions[i - 1]
}
function particleTop(i) {
  const positions = [15, 12, 75, 35, 85, 55, 25, 68, 45, 8, 60, 90]
  return positions[i - 1]
}
function particleDelay(i) {
  return i * 0.8
}

function sparkleLeft(i) {
  const positions = [15, 75, 35, 90, 52, 8]
  return positions[i - 1]
}
function sparkleTop(i) {
  const positions = [25, 18, 78, 48, 10, 65]
  return positions[i - 1]
}
function sparkleDelay(i) {
  return i * 1.5
}
</script>

<style scoped>
/* ====== Base ====== */
.banner {
  position: relative;
  overflow: hidden;
  padding: 80px 24px 0;
  text-align: center;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-bg {
  position: absolute;
  inset: 0;
  animation: bgShift 16s ease-in-out infinite alternate;
}

@keyframes bgShift {
  0%   { filter: hue-rotate(0deg) saturate(1); }
  50%  { filter: hue-rotate(8deg) saturate(1.1); }
  100% { filter: hue-rotate(-4deg) saturate(0.95); }
}

/* ====== Mesh Grid ====== */
.mesh-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    radial-gradient(circle, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
}

/* ====== Gradient Orbs ====== */
.banner-orbs {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.18;
  animation: orbFloat 16s ease-in-out infinite alternate;
}

.orb-1 {
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, #409eff, transparent);
  top: -250px;
  right: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #1e90ff, transparent);
  bottom: -250px;
  left: -150px;
  animation-delay: -5s;
  opacity: 0.12;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #06b6d4, transparent);
  top: 35%;
  left: 55%;
  animation-delay: -10s;
  opacity: 0.1;
}

.orb-4 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, #3b82f6, transparent);
  top: 60%;
  right: 25%;
  animation-delay: -7s;
  opacity: 0.08;
}

@keyframes orbFloat {
  0%   { transform: translate(0, 0) scale(1); }
  33%  { transform: translate(50px, -40px) scale(1.15); }
  66%  { transform: translate(-30px, 50px) scale(0.9); }
  100% { transform: translate(40px, -30px) scale(1.08); }
}

/* ====== Particles ====== */
.banner-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.particle {
  position: absolute;
  animation: particleFloat 7s ease-in-out infinite alternate;
}

.particle-circle {
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.particle-dot {
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.05);
}

@keyframes particleFloat {
  0%   { transform: translateY(0) scale(1); opacity: 0.2; }
  50%  { transform: translateY(-40px) scale(1.3); opacity: 0.5; }
  100% { transform: translateY(15px) scale(0.7); opacity: 0.15; }
}

/* ====== Sparkles ====== */
.sparkles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.sparkle {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: white;
  animation: sparkleAnim 3s ease-in-out infinite;
  box-shadow: 0 0 6px rgba(255, 255, 255, 0.6), 0 0 12px rgba(255, 255, 255, 0.3);
}

@keyframes sparkleAnim {
  0%, 100% { opacity: 0; transform: scale(0); }
  50%      { opacity: 0.7; transform: scale(1); }
}

/* ====== Content Area ====== */
.banner-content {
  position: relative;
  z-index: 2;
  max-width: 680px;
  margin: 0 auto;
  animation: fadeInUp 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  padding-bottom: 80px;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to   { opacity: 1; transform: translateY(0); }
}


/* ====== Title ====== */
.banner-title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 36px;
}

.title-logo {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.15);
  flex-shrink: 0;
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.15);
}

.title-logo .logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-title {
  font-size: 44px;
  font-weight: 800;
  color: white;
  margin: 0;
  letter-spacing: 2px;
  line-height: 1.2;
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.3);
}

/* ====== Search Box ====== */
.banner-search {
  max-width: 580px;
  margin: 0 auto;
  position: relative;
}

/* Outer glow ring */
.search-glow {
  position: absolute;
  inset: -4px;
  border-radius: 54px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.3), rgba(51, 126, 204, 0.3), rgba(64, 158, 255, 0.1));
  filter: blur(12px);
  opacity: 0.6;
  animation: glowPulse 3s ease-in-out infinite alternate;
  pointer-events: none;
}

@keyframes glowPulse {
  0%   { opacity: 0.4; transform: scale(0.98); }
  100% { opacity: 0.8; transform: scale(1.02); }
}

.search-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: stretch;
  border-radius: 50px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  background: linear-gradient(135deg, #409eff, #337ecc);
}

.banner-input {
  flex: 1;
}

.banner-input :deep(.n-input-wrapper) {
  border-radius: 0 !important;
  background: rgba(255, 255, 255, 0.92) !important;
  backdrop-filter: blur(12px);
  padding-left: 24px !important;
  height: 56px;
  border: none !important;
  transition: all 0.3s ease;
}
.banner-input :deep(.n-input-wrapper):hover {
  background: rgba(255, 255, 255, 0.97) !important;
}
.banner-input :deep(.n-input--focus) .n-input-wrapper {
  background: rgba(255, 255, 255, 0.98) !important;
}
.banner-input :deep(.n-input__input) {
  font-size: 16px;
  height: 56px;
  line-height: 56px;
  color: #1a1a2e;
}
.banner-input :deep(.n-input__input)::placeholder {
  color: rgba(26, 26, 46, 0.35);
}

/* Search Button */
.banner-search-btn {
  height: 56px !important;
  border-radius: 0 !important;
  padding: 0 20px !important;
  font-size: 16px !important;
  background: linear-gradient(135deg, #409eff, #337ecc) !important;
  border: none !important;
  border-left: none !important;
  outline: none !important;
  box-shadow: none !important;
  color: white !important;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.banner-search-btn :deep(.n-button__border) {
  display: none !important;
}

.banner-search-btn:not(:disabled):hover {
  background: linear-gradient(135deg, #337ecc, #409eff) !important;
  box-shadow: 0 4px 24px rgba(64, 158, 255, 0.4);
}
.banner-search-btn:not(:disabled):active {
  transform: scale(0.98);
}

/* ====== Bottom Wave ====== */
.banner-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 80px;
  pointer-events: none;
  z-index: 1;
}

.banner-wave svg {
  width: 100%;
  height: 100%;
  display: block;
}

/* ====== Responsive ====== */
@media (max-width: 768px) {
  .banner {
    padding: 50px 16px 0;
    min-height: 280px;
  }

  .banner-wave {
    height: 40px;
  }

  .banner-title-row {
    gap: 12px;
    margin-bottom: 28px;
  }

  .title-logo {
    width: 44px;
    height: 44px;
  }

  .banner-title {
    font-size: 26px;
  }

  .banner-input :deep(.n-input-wrapper) {
    height: 48px;
  }
  .banner-input :deep(.n-input__input) {
    height: 48px;
    font-size: 15px;
  }

  .banner-search-btn {
    height: 48px !important;
    padding: 0 20px !important;
    font-size: 15px !important;
  }

  .banner-search {
    max-width: 100%;
  }

  .search-glow {
    display: none;
  }

  .mesh-grid {
    background-size: 24px 24px;
  }
}
</style>
