<template>
  <div class="banner" :style="{ background: `linear-gradient(135deg, ${bgStart}, ${bgEnd})` }">
    <div class="banner-content">
      <h1 class="banner-title">{{ title }}</h1>
      <p class="banner-subtitle">{{ subtitle }}</p>
      <div class="banner-search" v-if="showSearch">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索你需要的教育资源..."
          size="large"
          clearable
          @keyup.enter="doSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="doSearch" style="border-radius:0 8px 8px 0;">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>
    <div class="banner-decoration">
      <div class="circle c1"></div>
      <div class="circle c2"></div>
      <div class="circle c3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  title: { type: String, default: '韩米智途 - 教育资源共享平台' },
  subtitle: { type: String, default: '汇聚优质教育资源，助力每一位学习者' },
  showSearch: { type: Boolean, default: true },
  bgStart: { type: String, default: '#1a1a2e' },
  bgEnd: { type: String, default: '#16213e' }
})

const router = useRouter()
const searchKeyword = ref('')

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/resources', query: { keyword: searchKeyword.value.trim() } })
  }
}
</script>

<style scoped>
.banner {
  position: relative;
  overflow: hidden;
  padding: 80px 20px;
  text-align: center;
}

.banner-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
}

.banner-title {
  font-size: 40px;
  font-weight: 800;
  color: white;
  margin: 0 0 16px;
  letter-spacing: 1px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.banner-subtitle {
  font-size: 18px;
  color: rgba(255,255,255,0.75);
  margin: 0 0 32px;
  line-height: 1.6;
}

.banner-search {
  max-width: 560px;
  margin: 0 auto;
}

.banner-search :deep(.el-input__wrapper) {
  border-radius: 8px 0 0 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.banner-search :deep(.el-button) {
  height: 100%;
  font-size: 15px;
}

/* Decoration circles */
.banner-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  background: white;
}

.c1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
}

.c2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -80px;
}

.c3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 60%;
  transform: translate(-50%, -50%);
}

@media (max-width: 768px) {
  .banner-title {
    font-size: 24px;
  }
  .banner-subtitle {
    font-size: 14px;
    margin-bottom: 24px;
  }
  .banner {
    padding: 40px 16px;
  }
  .banner-search {
    max-width: 100%;
  }
  .banner-search :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
  .banner-search :deep(.el-button) {
    font-size: 14px;
    padding: 8px 14px;
  }
}
</style>
