<template>
  <div class="home-page">
    <!-- Banner -->
    <Banner />

    <!-- Recommended Resources -->
    <section class="section section-alt" ref="recommendSection">
      <div class="page-container">
        <h2 class="section-title">⭐ 推荐资源</h2>
        <div v-if="recommendResources.length" class="resource-grid">
          <ResourceCard
            v-for="(item, index) in recommendResources"
            :key="item.id"
            :resource="item"
            :style="{ animationDelay: index * 0.08 + 's' }"
            class="card-enter"
          />
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
    <section class="section" ref="hotSection">
      <div class="page-container">
        <h2 class="section-title">
          <el-icon style="color:#f56c6c"><TrendCharts /></el-icon>
          热门资源
        </h2>
        <div v-if="hotResources.length" class="resource-grid">
          <ResourceCard
            v-for="(item, index) in hotResources.slice(0, 4)"
            :key="item.id"
            :resource="item"
            :style="{ animationDelay: index * 0.08 + 's' }"
            class="card-enter"
          />
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
    <section class="section section-alt" ref="latestSection">
      <div class="page-container">
        <h2 class="section-title">
          <el-icon style="color:#409eff"><Clock /></el-icon>
          最新资源
        </h2>
        <div v-if="latestResources.length" class="resource-grid">
          <ResourceCard
            v-for="(item, index) in latestResources.slice(0, 4)"
            :key="item.id"
            :resource="item"
            :style="{ animationDelay: index * 0.08 + 's' }"
            class="card-enter"
          />
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
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

// Scroll reveal observer
const categorySection = ref(null)
const recommendSection = ref(null)
const hotSection = ref(null)
const latestSection = ref(null)

let observer = null

function observeSection(el) {
  if (!el || !observer) return
  observer.observe(el)
}

onMounted(async () => {
  // Set up IntersectionObserver for scroll reveal
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('section-visible')
          observer.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
  )

  observeSection(categorySection.value)
  observeSection(recommendSection.value)
  observeSection(hotSection.value)
  observeSection(latestSection.value)

  // Load data
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

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.section {
  padding: 60px 0;
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.6s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}
.section.section-visible {
  opacity: 1;
  transform: translateY(0);
}

.section-alt {
  background: var(--bg);
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

/* Card entrance animation */
.card-enter {
  animation: cardFadeIn 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 768px) {
  .section {
    padding: 32px 0;
  }
  .resource-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  .section-title {
    font-size: 20px;
    margin-bottom: 16px;
  }
}
</style>
