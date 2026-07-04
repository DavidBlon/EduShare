<template>
  <div class="resource-list-page">
    <div class="page-container main-section" style="padding-top:40px;">
      <div class="layout-wrapper">
        <!-- Mobile filter toggle -->
        <div class="mobile-filter-bar">
          <el-button @click="showFilter = !showFilter" :type="showFilter ? 'primary' : 'default'" style="width:100%;">
            <el-icon><Filter /></el-icon>
            {{ showFilter ? '收起筛选' : '展开筛选' }}
          </el-button>
        </div>

        <!-- Sidebar Filters -->
        <aside class="filter-sidebar" :class="{ 'filter-visible': showFilter }">
          <el-card shadow="never">
            <template #header>
              <span class="filter-title"><el-icon><Filter /></el-icon> 筛选条件</span>
            </template>

            <div class="filter-group">
              <h4>分类</h4>
              <div class="category-tree">
                <div v-for="cat in categories" :key="cat.id" class="cat-item">
                  <!-- 父级大类 -->
                  <div
                    class="cat-parent"
                    :class="{ active: query.categoryId === cat.id }"
                    @click="selectCategory(cat.id)"
                  >
                    <span
                      class="cat-toggle"
                      v-if="cat.children && cat.children.length"
                      @click.stop="toggleParent(cat.id)"
                    >
                      <el-icon :class="{ rotated: isExpanded(cat.id) }">
                        <CaretRight />
                      </el-icon>
                    </span>
                    <span class="cat-name">{{ cat.name }}</span>
                  </div>
                  <!-- 子级科目 -->
                  <div class="cat-children" v-if="cat.children && cat.children.length && isExpanded(cat.id)">
                    <div
                      v-for="child in cat.children"
                      :key="child.id"
                      class="cat-child-label"
                      :class="{ active: query.categoryId === child.id }"
                      @click="selectCategory(child.id)"
                    >
                      {{ child.name }}
                    </div>
                  </div>
                </div>
                <div
                  class="cat-label cat-all"
                  :class="{ active: !query.categoryId }"
                  @click="selectCategory(null)"
                >
                  全部分类
                </div>
              </div>
            </div>

            <el-divider />

            <div class="filter-group">
              <h4>标签</h4>
              <div class="tag-list">
                <el-tag
                  v-for="tag in tags"
                  :key="tag.id"
                  :type="query.tagId === tag.id ? 'primary' : 'info'"
                  effect="plain"
                  class="tag-item"
                  @click="selectTag(tag.id)"
                >
                  {{ tag.name }}
                </el-tag>
                <div v-if="!tags.length" class="no-items">暂无标签</div>
              </div>
            </div>

            <el-divider />

            <div class="filter-group">
              <h4>排序</h4>
              <el-radio-group v-model="query.sortBy" @change="doSearch">
                <el-radio-button value="">默认</el-radio-button>
                <el-radio-button value="new">最新</el-radio-button>
                <el-radio-button value="hot">热门</el-radio-button>
                <el-radio-button value="recommend">推荐</el-radio-button>
              </el-radio-group>
            </div>

            <el-button @click="resetFilters" class="reset-btn" style="width:100%;margin-top:12px;">
              重置筛选
            </el-button>
          </el-card>
        </aside>

        <!-- Main List -->
        <section class="resource-list">
          <!-- Search Bar -->
          <div class="search-bar">
            <el-input
              v-model="query.keyword"
              placeholder="搜索资源标题或简介..."
              clearable
              style="width:360px;"
              @keyup.enter="doSearch"
              @clear="doSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="doSearch">搜索</el-button>
          </div>

          <!-- Stats -->
          <div class="list-stats" v-if="total > 0">
            共找到 <strong>{{ total }}</strong> 个资源
          </div>

          <!-- Loading -->
          <div v-if="loading" class="loading-area">
            <el-skeleton :count="6" animated>
              <template #template>
                <div style="display:flex;gap:16px;padding:16px;margin-bottom:12px;background:white;border-radius:8px;">
                  <el-skeleton-item variant="image" style="width:160px;height:120px" />
                  <div style="flex:1">
                    <el-skeleton-item variant="h3" style="width:50%;margin-bottom:12px" />
                    <el-skeleton-item variant="text" style="width:80%;margin-bottom:8px" />
                    <el-skeleton-item variant="text" style="width:40%" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </div>

          <!-- Empty -->
          <el-empty v-else-if="!resources.length" description="暂无匹配的资源" />

          <!-- List -->
          <div v-else class="resource-items">
            <router-link
              v-for="item in resources"
              :key="item.id"
              :to="`/resource/${item.id}`"
              class="resource-item"
            >
              <div class="item-cover">
                <img
                  :src="item.cover || defaultCover"
                  :alt="item.title"
                  @error="e => e.target.src = defaultCover"
                />
              </div>
              <div class="item-info">
                <h3 class="item-title">{{ item.title }}</h3>
                <p class="item-desc" v-if="item.description">{{ item.description }}</p>
                <div class="item-meta">
                  <el-tag v-if="item.categoryName" size="small" effect="plain">{{ item.categoryName }}</el-tag>
                  <span class="item-stats">
                    <el-icon><View /></el-icon> {{ item.viewCount || 0 }}
                    <el-icon style="margin-left:12px;"><Download /></el-icon> {{ item.downloadCount || 0 }}
                  </span>
                  <span class="item-date">{{ formatDate(item.createdAt) }}</span>
                </div>
                <div class="item-tags" v-if="item.tags && item.tags.length">
                  <el-tag v-for="tag in item.tags.slice(0, 4)" :key="tag.id" size="small" type="success" effect="plain">
                    {{ tag.name }}
                  </el-tag>
                </div>
              </div>
            </router-link>
          </div>

          <!-- Pagination -->
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              v-model:current-page="query.page"
              v-model:page-size="query.pageSize"
              :total="total"
              :page-sizes="[10, 20, 30]"
              layout="total, sizes, prev, pager, next"
              small
              background
              :pager-count="mobile ? 3 : 7"
              @current-change="loadResources"
              @size-change="loadResources"
            />
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchResources } from '@/api/resource'
import { getCategoryTree } from '@/api/category'
import { getTagList } from '@/api/tag'

const route = useRoute()
const router = useRouter()
const defaultCover = '/default-cover.svg'

const categories = ref([])
const tags = ref([])
const resources = ref([])
const total = ref(0)
const loading = ref(false)
const showFilter = ref(false)
const mobile = ref(false)

// 控制各大类（小学/初中/高中/中考/高考）下面科目的展开状态
const expandedParents = ref(new Set())

const query = reactive({
  page: 1,
  pageSize: 10,
  categoryId: null,
  keyword: '',
  tagId: null,
  sortBy: ''
})

function checkMobile() {
  mobile.value = window.innerWidth < 768
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)

  // Read query params from URL
  if (route.query.keyword) query.keyword = route.query.keyword
  if (route.query.categoryId) query.categoryId = Number(route.query.categoryId)
  if (route.query.tagId) query.tagId = Number(route.query.tagId)

  // Load filters
  const [catRes, tagRes] = await Promise.all([
    getCategoryTree(),
    getTagList()
  ])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []

  // Load resources
  loadResources()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

function toggleParent(id) {
  const set = new Set(expandedParents.value)
  if (set.has(id)) {
    set.delete(id)
  } else {
    set.add(id)
  }
  expandedParents.value = set
}

function isExpanded(id) {
  return expandedParents.value.has(id)
}

function selectCategory(id) {
  query.categoryId = id
  query.page = 1
  loadResources()
}

function selectTag(id) {
  query.tagId = query.tagId === id ? null : id
  query.page = 1
  loadResources()
}

function doSearch() {
  query.page = 1
  loadResources()
}

function resetFilters() {
  query.categoryId = null
  query.tagId = null
  query.keyword = ''
  query.sortBy = ''
  query.page = 1
  loadResources()
}

async function loadResources() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.categoryId) delete params.categoryId
    if (!params.tagId) delete params.tagId
    if (!params.keyword) delete params.keyword
    if (!params.sortBy) delete params.sortBy
    const res = await searchResources(params)
    resources.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function formatDate(date) {
  if (!date) return ''
  return date.slice(0, 10)
}
</script>

<style scoped>
.main-section {
  padding: 30px 0;
}

.layout-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* Sidebar */
.filter-sidebar {
  width: 260px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 30px);
}

.filter-title {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-group {
  margin-bottom: 8px;
}

.filter-group h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px;
}

.category-tree {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* 父级大类行 */
.cat-parent {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  transition: var(--transition);
  user-select: none;
}
.cat-parent:hover {
  background: rgba(64, 158, 255, 0.08);
  color: var(--primary);
}
.cat-parent.active {
  background: rgba(64, 158, 255, 0.1);
  color: var(--primary);
}

/* 展开/收起的箭头 */
.cat-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-secondary);
  transition: var(--transition);
}
.cat-toggle .el-icon {
  transition: transform 0.25s ease;
}
.cat-toggle .el-icon.rotated {
  transform: rotate(90deg);
}

.cat-name {
  flex: 1;
}

/* 子级科目 */
.cat-child-label {
  padding: 6px 10px 6px 32px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-regular);
  transition: var(--transition);
}
.cat-child-label:hover {
  background: rgba(64, 158, 255, 0.08);
  color: var(--primary);
}
.cat-child-label.active {
  background: rgba(64, 158, 255, 0.1);
  color: var(--primary);
  font-weight: 600;
}

.cat-children {
  margin-bottom: 2px;
}

.cat-all {
  margin-top: 4px;
}

.cat-label,
.cat-all {
  padding: 7px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-regular);
  transition: var(--transition);
}
.cat-label:hover,
.cat-all:hover {
  background: rgba(64, 158, 255, 0.08);
  color: var(--primary);
}
.cat-label.active,
.cat-all.active {
  background: rgba(64, 158, 255, 0.1);
  color: var(--primary);
  font-weight: 600;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  cursor: pointer;
}

.no-items {
  font-size: 13px;
  color: var(--text-secondary);
}

/* Main list */
.resource-list {
  flex: 1;
  min-width: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.list-stats {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.resource-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-item {
  display: flex;
  gap: 20px;
  background: white;
  border-radius: var(--radius-lg);
  padding: 16px;
  box-shadow: var(--shadow);
  transition: var(--transition);
  text-decoration: none;
  color: inherit;
}
.resource-item:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.item-cover {
  width: 180px;
  height: 130px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg);
}
.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.resource-item:hover .item-cover img {
  transform: scale(1.05);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.item-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.item-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-date {
  margin-left: auto;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .layout-wrapper {
    flex-direction: column;
  }
  .mobile-filter-bar {
    display: block !important;
    margin-bottom: 12px;
  }
  .filter-sidebar {
    width: 100%;
    position: static;
    display: none;
  }
  .filter-sidebar.filter-visible {
    display: block;
  }
  .search-bar {
    flex-direction: column;
  }
  .search-bar .el-input {
    width: 100% !important;
  }
  .item-cover {
    width: 100px;
    height: 80px;
  }
  .resource-item {
    gap: 12px;
    padding: 12px;
  }
  .item-title {
    font-size: 15px;
  }
  .item-desc {
    display: none;
  }
  .item-date {
    display: none;
  }
  .item-meta {
    gap: 8px;
    font-size: 12px;
  }
  .main-section {
    padding-top: 20px !important;
  }
  .pagination-wrapper {
    overflow-x: auto;
    justify-content: flex-start;
    padding-bottom: 8px;
  }
  .pagination-wrapper :deep(.el-pagination) {
    white-space: nowrap;
  }
}

.mobile-filter-bar {
  display: none;
}
</style>
