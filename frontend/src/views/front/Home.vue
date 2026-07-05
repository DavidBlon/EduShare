<template>
  <div class="home-page">
    <!-- Banner -->
    <Banner />

    <!-- Category Navigation -->
    <section class="section category-section">
      <div class="page-container">
        <h2 class="section-title">资源分类</h2>
        <CategoryNav />
      </div>
    </section>

    <!-- Recommended Resources -->
    <section class="section">
      <div class="page-container">
        <h2 class="section-title">推荐资源</h2>
        <div v-if="recommendResources.length" class="resource-grid">
          <ResourceCard v-for="item in recommendResources" :key="item.id" :resource="item" />
        </div>
        <el-empty v-else-if="!recommendLoading" description="暂无推荐资源" />
        <div v-if="recommendLoading" class="resource-grid">
          <el-skeleton :count="4" animated>
            <template #template>
              <div style="padding:0">
                <el-skeleton-item variant="image" style="width:100%;height:180px" />
                <div style="padding:16px">
                  <el-skeleton-item variant="h3" style="width:60%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:80%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:40%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </section>

    <!-- Hot Resources -->
    <section class="section section-alt">
      <div class="page-container">
        <h2 class="section-title">热门资源</h2>
        <div v-if="hotResources.length" class="resource-grid">
          <ResourceCard v-for="item in hotResources.slice(0, 4)" :key="item.id" :resource="item" />
        </div>
        <el-empty v-else-if="!hotLoading" description="暂无热门资源" />
        <div v-if="hotLoading" class="resource-grid">
          <el-skeleton :count="4" animated>
            <template #template>
              <div style="padding:0">
                <el-skeleton-item variant="image" style="width:100%;height:180px" />
                <div style="padding:16px">
                  <el-skeleton-item variant="h3" style="width:60%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:80%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:40%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </section>

    <!-- Latest Resources -->
    <section class="section">
      <div class="page-container">
        <h2 class="section-title">最新资源</h2>
        <div v-if="latestResources.length" class="resource-grid">
          <ResourceCard v-for="item in latestResources.slice(0, 4)" :key="item.id" :resource="item" />
        </div>
        <el-empty v-else-if="!latestLoading" description="暂无最新资源" />
        <div v-if="latestLoading" class="resource-grid">
          <el-skeleton :count="4" animated>
            <template #template>
              <div style="padding:0">
                <el-skeleton-item variant="image" style="width:100%;height:180px" />
                <div style="padding:16px">
                  <el-skeleton-item variant="h3" style="width:60%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:80%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:40%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Banner from '@/components/front/Banner.vue'
import CategoryNav from '@/components/front/CategoryNav.vue'
import ResourceCard from '@/components/front/ResourceCard.vue'
import { getRecommendResources, getHotResources, getLatestResources } from '@/api/resource'

const recommendResources = ref([])
const hotResources = ref([])
const latestResources = ref([])
const recommendLoading = ref(true)
const hotLoading = ref(true)
const latestLoading = ref(true)

onMounted(async () => {
  try {
    const [recRes, hotRes, latestRes] = await Promise.all([
      getRecommendResources(4),
      getHotResources(4),
      getLatestResources(4)
    ])
    recommendResources.value = recRes.data || []
    hotResources.value = hotRes.data || []
    latestResources.value = latestRes.data || []
  } catch {
    // handled by interceptor
  } finally {
    recommendLoading.value = false
    hotLoading.value = false
    latestLoading.value = false
  }
})
</script>

<style scoped>
.section {
  padding: 50px 0;
}

.section-alt {
  background: var(--bg);
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

@media (max-width: 768px) {
  .section {
    padding: 24px 0;
  }
  .resource-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  .section-title {
    font-size: 18px;
    margin-bottom: 16px;
  }
}
</style>
