<template>
  <div class="category-nav" v-if="categories.length">
    <div
      class="category-section"
      v-for="(section, idx) in categories"
      :key="section.id"
      :style="{ animationDelay: idx * 0.08 + 's' }"
    >
      <div class="category-header">
        <span class="category-main">
          <span class="category-icon" :style="{ background: iconColor(idx) }">
            {{ section.name.charAt(0) }}
          </span>
          {{ section.name }}
        </span>
        <router-link
          :to="{ path: '/resources', query: { categoryId: section.id } }"
          class="category-more"
        >
          更多 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      <div class="category-children" v-if="section.children && section.children.length">
        <router-link
          v-for="child in section.children"
          :key="child.id"
          :to="{ path: '/resources', query: { categoryId: child.id } }"
          class="category-child"
        >
          {{ child.name }}
        </router-link>
      </div>
    </div>
  </div>
  <div v-else-if="loading" class="category-loading">
    <el-skeleton :rows="3" animated />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategoryTree } from '@/api/category'

const categories = ref([])
const loading = ref(true)

const iconColors = [
  'linear-gradient(135deg, #409eff, #337ecc)',
  'linear-gradient(135deg, #67c23a, #529b2e)',
  'linear-gradient(135deg, #e6a23c, #d48806)',
  'linear-gradient(135deg, #f56c6c, #d03050)',
  'linear-gradient(135deg, #909399, #73767a)',
]

function iconColor(idx) {
  return iconColors[idx % iconColors.length]
}

onMounted(async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.category-nav {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.category-section {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.3s ease;
  animation: catFadeIn 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
}
.category-section:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

@keyframes catFadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 2px solid var(--bg);
}

.category-main {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.category-more {
  font-size: 13px;
  color: var(--primary);
  display: flex;
  align-items: center;
  gap: 2px;
  transition: var(--transition);
}
.category-more:hover {
  gap: 6px;
}

.category-children {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-child {
  padding: 6px 18px;
  border-radius: 20px;
  background: var(--bg);
  font-size: 14px;
  color: var(--text-regular);
  text-decoration: none;
  transition: var(--transition);
  font-weight: 500;
}
.category-child:hover {
  background: var(--primary-bg);
  color: var(--primary);
  transform: translateY(-1px);
}

.category-loading {
  padding: 20px;
}

@media (max-width: 768px) {
  .category-nav {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .category-section {
    padding: 16px;
  }
  .category-main {
    font-size: 15px;
  }
  .category-child {
    padding: 5px 14px;
    font-size: 13px;
  }
}
</style>
