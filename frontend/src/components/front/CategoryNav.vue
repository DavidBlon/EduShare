<template>
  <div class="category-nav" v-if="categories.length">
    <div class="category-section" v-for="section in categories" :key="section.id">
      <div class="category-header">
        <span class="category-main">{{ section.name }}</span>
        <router-link :to="{ path: '/resources', query: { categoryId: section.id } }" class="category-more">
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
  padding: 20px;
  box-shadow: var(--shadow);
  transition: var(--transition);
}
.category-section:hover {
  box-shadow: var(--shadow-hover);
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--bg);
}

.category-main {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

.category-more {
  font-size: 13px;
  color: var(--primary);
  display: flex;
  align-items: center;
  gap: 2px;
}

.category-children {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-child {
  padding: 6px 16px;
  border-radius: 20px;
  background: var(--bg);
  font-size: 14px;
  color: var(--text-regular);
  text-decoration: none;
  transition: var(--transition);
}
.category-child:hover {
  background: rgba(64, 158, 255, 0.1);
  color: var(--primary);
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
    padding: 4px 12px;
    font-size: 13px;
  }
}
</style>
