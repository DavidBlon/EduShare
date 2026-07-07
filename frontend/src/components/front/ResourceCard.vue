<template>
  <router-link :to="`/resource/${resource.id}`" class="resource-card" @mouseenter="hover = true" @mouseleave="hover = false">
    <div class="card-cover">
      <img
        :src="resource.cover || defaultCover()"
        :alt="resource.title"
        loading="lazy"
        class="cover-img"
        :class="{ loaded: coverLoaded }"
        @error="onError"
        @load="coverLoaded = true"
      />
      <!-- Gradient overlay on hover -->
      <div class="cover-overlay" :class="{ visible: hover }"></div>

      <!-- Category badge -->
      <span class="card-category-badge" v-if="resource.categoryName">
        {{ resource.categoryName }}
      </span>

      <!-- Status (draft) -->
      <div class="card-status" v-if="resource.status === 0">
        <n-tag size="small" type="info">草稿</n-tag>
      </div>

      <!-- View count on hover -->
      <div class="card-stats-overlay" :class="{ visible: hover }">
        <span><n-icon><EyeOutline /></n-icon> {{ resource.viewCount || 0 }}</span>
        <span><n-icon><DownloadOutline /></n-icon> {{ resource.downloadCount || 0 }}</span>
      </div>
    </div>

    <div class="card-body">
      <h3 class="card-title" :title="resource.title">{{ resource.title }}</h3>
      <p class="card-desc" v-if="resource.description">{{ truncate(resource.description, 60) }}</p>
      <div class="card-meta">
        <span class="meta-stats">
          <n-icon><EyeOutline /></n-icon> {{ resource.viewCount || 0 }}
          <n-icon style="margin-left:8px;"><DownloadOutline /></n-icon> {{ resource.downloadCount || 0 }}
        </span>
      </div>
      <div class="card-tags" v-if="resource.tags && resource.tags.length">
        <n-tag
          v-for="tag in (hover ? resource.tags.slice(0, 4) : resource.tags.slice(0, 3))"
          :key="tag.id"
          size="small"
          type="success"
          :bordered="false"
        >
          {{ tag.name }}
        </n-tag>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { ref } from 'vue'
import { EyeOutline, DownloadOutline } from '@vicons/ionicons5'
import { getTitleCover } from '@/utils/cover'

const props = defineProps({
  resource: { type: Object, required: true }
})

const hover = ref(false)
const coverLoaded = ref(false)

function defaultCover() {
  return getTitleCover(props.resource.title, 400, 300)
}

function onError(e) {
  e.target.src = defaultCover()
  coverLoaded.value = true
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '…' : text
}
</script>

<style scoped>
.resource-card {
  display: block;
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow);
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  will-change: transform;
}
.resource-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

/* ====== Cover ====== */
.card-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: var(--bg);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.5s ease,
              filter 0.5s ease;
  filter: blur(20px);
  opacity: 0.4;
  transform: scale(1);
}
.cover-img.loaded {
  filter: blur(0);
  opacity: 1;
}

.resource-card:hover .cover-img {
  transform: scale(1.08);
}

/* Gradient overlay */
.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(0, 0, 0, 0.4));
  opacity: 0;
  transition: opacity 0.35s ease;
}
.cover-overlay.visible {
  opacity: 1;
}

/* Category badge */
.card-category-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  font-size: 12px;
  color: white;
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* Status */
.card-status {
  position: absolute;
  top: 10px;
  right: 10px;
}

/* Stats overlay on hover */
.card-stats-overlay {
  position: absolute;
  bottom: 10px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.card-stats-overlay.visible {
  opacity: 1;
  transform: translateY(0);
}
.card-stats-overlay span {
  display: flex;
  align-items: center;
  gap: 4px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

/* ====== Body ====== */
.card-body {
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.25s ease;
}
.resource-card:hover .card-title {
  color: var(--primary);
}

.card-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 12px;
  line-height: 1.5;
}

.card-meta {
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

@media (max-width: 768px) {
  .card-cover {
    height: 140px;
  }
  .card-body {
    padding: 12px;
  }
  .card-title {
    font-size: 14px;
  }
}
</style>
