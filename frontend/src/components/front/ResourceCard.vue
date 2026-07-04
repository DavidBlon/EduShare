<template>
  <router-link :to="`/resource/${resource.id}`" class="resource-card">
    <div class="card-cover">
      <img
        :src="resource.cover ? resource.cover : defaultCover"
        :alt="resource.title"
        loading="lazy"
        @error="e => e.target.src = defaultCover"
        @load="e => e.target.classList.add('loaded')"
        class="blur-load"
      />
      <div class="card-status" v-if="resource.status === 0">
        <el-tag size="small" type="info">草稿</el-tag>
      </div>
    </div>
    <div class="card-body">
      <h3 class="card-title" :title="resource.title">{{ resource.title }}</h3>
      <p class="card-desc" v-if="resource.description">{{ truncate(resource.description, 60) }}</p>
      <div class="card-meta">
        <span class="meta-category" v-if="resource.categoryName">
          <el-tag size="small" effect="plain">{{ resource.categoryName }}</el-tag>
        </span>
        <span class="meta-stats">
          <el-icon><View /></el-icon> {{ resource.viewCount || 0 }}
          <el-icon style="margin-left:8px;"><Download /></el-icon> {{ resource.downloadCount || 0 }}
        </span>
      </div>
      <div class="card-tags" v-if="resource.tags && resource.tags.length">
        <el-tag
          v-for="tag in resource.tags.slice(0, 3)"
          :key="tag.id"
          size="small"
          type="success"
          effect="plain"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </div>
  </router-link>
</template>

<script setup>
defineProps({
  resource: {
    type: Object,
    required: true
  }
})

const defaultCover = '/default-cover.svg'

function truncate(text, len) {
  return text.length > len ? text.slice(0, len) + '...' : text
}
</script>

<style scoped>
.resource-card {
  display: block;
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow);
  transition: var(--transition);
  cursor: pointer;
  text-decoration: none;
  color: inherit;
}
.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.card-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: var(--bg);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease, opacity 0.5s ease, filter 0.5s ease;
}

/* Blur-up lazy loading */
.card-cover img.blur-load {
  filter: blur(20px);
  opacity: 0.4;
}

.card-cover img.blur-load.loaded {
  filter: blur(0);
  opacity: 1;
}

.resource-card:hover .card-cover img {
  transform: scale(1.08);
}

.card-status {
  position: absolute;
  top: 8px;
  right: 8px;
}

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
}

.card-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 12px;
  line-height: 1.5;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
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
