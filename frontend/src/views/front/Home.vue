<template>
  <div class="home-page">
    <!-- Banner -->
    <Banner />

    <!-- Recommended Resources -->
    <section class="section section-alt" ref="recommendSection">
      <div class="page-container">
        <h2 class="section-title">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="#f59e0b" stroke="#f59e0b" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          推荐资源
        </h2>
        <div v-if="recommendResources.length" class="resource-grid">
          <ResourceCard
            v-for="(item, index) in recommendResources"
            :key="item.id"
            :resource="item"
            :style="{ animationDelay: index * 0.08 + 's' }"
            class="card-enter"
          />
        </div>
        <div v-else-if="!recommendLoading" class="empty-state">
          <span class="empty-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22 6 12 13 2 6"/></svg></span>
          <p class="empty-text">暂无推荐资源</p>
        </div>
        <div v-if="recommendLoading" class="resource-grid">
          <div v-for="i in 4" :key="i" class="skeleton-card">
            <n-skeleton :height="180" class="skeleton-img" />
            <div class="skeleton-body">
              <n-skeleton text :repeat="2" />
              <n-skeleton text style="width: 40%; margin-top: 8px;" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Resources -->
    <section class="section" ref="hotSection">
      <div class="page-container">
        <h2 class="section-title">
          <n-icon color="#f56c6c"><TrendingUpOutline /></n-icon>
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
        <div v-else-if="!hotLoading" class="empty-state">
          <span class="empty-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/></svg></span>
          <p class="empty-text">暂无热门资源</p>
        </div>
        <div v-if="hotLoading" class="resource-grid">
          <div v-for="i in 4" :key="i" class="skeleton-card">
            <n-skeleton :height="180" class="skeleton-img" />
            <div class="skeleton-body">
              <n-skeleton text :repeat="2" />
              <n-skeleton text style="width: 40%; margin-top: 8px;" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Latest Resources -->
    <section class="section section-alt" ref="latestSection">
      <div class="page-container">
        <h2 class="section-title">
          <n-icon color="#409eff"><TimeOutline /></n-icon>
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
        <div v-else-if="!latestLoading" class="empty-state">
          <span class="empty-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/><line x1="8" y1="10" x2="16" y2="10"/><line x1="8" y1="14" x2="12" y2="14"/></svg></span>
          <p class="empty-text">暂无最新资源</p>
        </div>
        <div v-if="latestLoading" class="resource-grid">
          <div v-for="i in 4" :key="i" class="skeleton-card">
            <n-skeleton :height="180" class="skeleton-img" />
            <div class="skeleton-body">
              <n-skeleton text :repeat="2" />
              <n-skeleton text style="width: 40%; margin-top: 8px;" />
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import Banner from '@/components/front/Banner.vue'
import ResourceCard from '@/components/front/ResourceCard.vue'
import { TrendingUpOutline, TimeOutline } from '@vicons/ionicons5'
import { getRecommendResources, getHotResources, getLatestResources } from '@/api/resource'

const recommendResources = ref([])
const hotResources = ref([])
const latestResources = ref([])
const recommendLoading = ref(true)
const hotLoading = ref(true)
const latestLoading = ref(true)

// Scroll reveal observer
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

/* Empty state */
.empty-state {
  text-align: center;
  padding: 48px 20px;
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
}
.empty-icon {
  display: block;
  margin-bottom: 12px;
}
.empty-icon svg {
  display: block;
  margin: 0 auto;
}
.empty-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* Skeleton card */
.skeleton-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow);
}
.skeleton-img {
  border-radius: 0 !important;
}
.skeleton-body {
  padding: 16px;
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
