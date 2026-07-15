<template>
  <div class="resource-list-page">
    <div class="page-container main-section">
      <div class="layout-wrapper">
        <!-- Mobile filter toggle -->
        <div class="mobile-filter-bar">
          <n-button
            @click="showFilter = !showFilter"
            :type="showFilter ? 'primary' : 'default'"
            class="filter-toggle-btn"
          >
            <template #icon><n-icon><FunnelOutline /></n-icon></template>
            {{ showFilter ? '收起筛选' : '展开筛选' }}
          </n-button>
        </div>

        <!-- Sidebar Filters -->
        <transition name="slide-fade">
          <aside v-if="!mobile || showFilter" class="filter-sidebar" :class="{ 'filter-mobile': mobile }">
            <n-card :bordered="false" class="filter-card-custom">
              <template #header>
                <span class="filter-title">
                  <n-icon><FunnelOutline /></n-icon>
                  筛选条件
                </span>
              </template>

              <div class="filter-group">
                <h4>分类</h4>
                <div class="category-tree">
                  <div
                    class="cat-all"
                    :class="{ active: !query.categoryId }"
                    @click="selectCategory(null)"
                  >
                    全部分类
                  </div>
                  <div v-for="cat in categories" :key="cat.id" class="cat-item">
                    <!-- Parent -->
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
                        <n-icon :class="{ rotated: isExpanded(cat.id) }">
                          <CaretForwardOutline />
                        </n-icon>
                      </span>
                      <span class="cat-name">{{ cat.name }}</span>
                      <span class="cat-count" v-if="cat.children">{{ cat.children.length }}</span>
                    </div>
                    <!-- Children -->
                    <transition name="fade">
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
                    </transition>
                  </div>
                </div>
              </div>

              <n-divider />

              <div class="filter-group">
                <h4>标签</h4>
                <div class="tag-list">
                  <n-tag
                    v-for="tag in tags"
                    :key="tag.id"
                    :type="query.tagIds.includes(tag.id) ? 'primary' : 'default'"
                    :color="query.tagIds.includes(tag.id) ? { text: '#0f6d70', border: '#79afaa', color: '#e6f1ed' } : undefined"
                    class="tag-item"
                    @click="selectTag(tag.id)"
                  >
                    {{ tag.name }}
                  </n-tag>
                  <div v-if="!tags.length" class="no-items">暂无标签</div>
                </div>
              </div>

              <n-divider />

              <div class="filter-group">
                <h4>排序</h4>
                <n-radio-group v-model:value="query.sortBy" @update:value="doSearch" class="sort-group">
                  <n-radio-button value="">默认</n-radio-button>
                  <n-radio-button value="new">最新</n-radio-button>
                  <n-radio-button value="hot">热门</n-radio-button>
                  <n-radio-button value="recommend">推荐</n-radio-button>
                </n-radio-group>
              </div>

              <n-button @click="resetFilters" class="reset-btn">
                <template #icon><n-icon><RefreshOutline /></n-icon></template>
                重置筛选
              </n-button>
            </n-card>
          </aside>
        </transition>

        <!-- Main List -->
        <section class="resource-list">
          <!-- Search Bar -->
          <div class="search-bar">
            <n-input
              v-model:value="query.keyword"
              placeholder="搜索资源标题或简介..."
              clearable
              class="search-input"
              @keyup.enter="doSearch"
              @clear="doSearch"
            >
              <template #prefix>
                <n-icon><SearchOutline /></n-icon>
              </template>
            </n-input>
            <n-button type="primary" @click="doSearch" class="search-btn">
              <template #icon><n-icon><SearchOutline /></n-icon></template>
              搜索
            </n-button>
          </div>

          <!-- Active Filters -->
          <div class="active-filters" v-if="query.categoryId || query.tagIds.length || query.keyword || query.sortBy">
            <span class="active-label">当前筛选：</span>
            <n-tag
              v-if="query.keyword"
              closable
              @close="query.keyword = ''; doSearch()"
              :bordered="false"
            >
              关键词：{{ query.keyword }}
            </n-tag>
            <n-tag
              v-if="query.categoryId"
              closable
              @close="query.categoryId = null; doSearch()"
              type="success"
              :bordered="false"
            >
              {{ getCategoryName(query.categoryId) }}
            </n-tag>
            <n-tag
              v-for="tid in query.tagIds"
              :key="tid"
              closable
              @close="removeTag(tid)"
              type="warning"
              :bordered="false"
            >
              {{ getTagName(tid) }}
            </n-tag>
            <n-tag
              v-if="query.sortBy"
              closable
              @close="query.sortBy = ''; doSearch()"
              type="info"
              :bordered="false"
            >
              {{ sortLabels[query.sortBy] }}
            </n-tag>
          </div>

          <!-- Stats -->
          <div class="list-stats" v-if="total > 0">
            共找到 <strong>{{ total }}</strong> 个资源
          </div>

          <!-- Loading -->
          <div v-if="loading" class="loading-area">
            <div v-for="i in 4" :key="i" class="skeleton-item">
              <div class="skeleton-row">
                <n-skeleton :width="180" :height="130" class="skeleton-cover" />
                <div class="skeleton-info">
                  <n-skeleton text style="width: 50%; margin-bottom: 12px" />
                  <n-skeleton text style="width: 80%; margin-bottom: 8px" />
                  <n-skeleton text style="width: 30%" />
                </div>
              </div>
            </div>
          </div>

          <!-- Empty -->
          <div v-else-if="!resources.length" class="empty-area">
            <div class="empty-inner">
              <span class="empty-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>
              <p>暂无匹配的资源</p>
            </div>
          </div>

          <!-- List -->
          <div v-else class="resource-items">
            <router-link
              v-for="(item, idx) in resources"
              :key="item.id"
              :to="`/resource/${item.id}`"
              class="resource-item"
              :style="{ animationDelay: idx * 0.06 + 's' }"
            >
              <div class="item-cover">
                <img
                  :src="item.cover || getTitleCover(item.title)"
                  :alt="item.title"
                  loading="lazy"
                  @error="e => e.target.src = getTitleCover(item.title)"
                  @load="e => e.target.classList.add('loaded')"
                  class="blur-load"
                />
              </div>
              <div class="item-info">
                <h3 class="item-title">{{ item.title }}</h3>
                <p class="item-desc" v-if="item.description">{{ item.description }}</p>
                <div class="item-meta">
                  <n-tag v-if="item.categoryName" size="small" :bordered="false" class="item-category">
                    {{ item.categoryName }}
                  </n-tag>
                  <span class="item-stats">
                    <n-icon><EyeOutline /></n-icon> {{ item.viewCount || 0 }}
                    <n-icon style="margin-left:12px;"><DownloadOutline /></n-icon> {{ item.downloadCount || 0 }}
                  </span>
                  <span class="item-date">{{ formatDate(item.createdAt) }}</span>
                </div>
                <div class="item-tags" v-if="item.tags && item.tags.length">
                  <n-tag
                    v-for="tag in item.tags.slice(0, 4)"
                    :key="tag.id"
                    size="small"
                    type="success"
                    :bordered="false"
                  >
                    {{ tag.name }}
                  </n-tag>
                </div>
              </div>
              <n-icon class="item-arrow"><ChevronForwardOutline /></n-icon>
            </router-link>
          </div>

          <!-- Pagination -->
          <div class="pagination-wrapper" v-if="total > 0">
            <n-pagination
              v-model:page="query.page"
              v-model:page-size="query.pageSize"
              :page-count="pageCount"
              :page-sizes="[10, 20, 30]"
              show-size-picker
              @update:page="loadResources"
              @update:page-size="loadResources"
            />
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, onActivated, onDeactivated, defineOptions, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  FunnelOutline,
  SearchOutline,
  EyeOutline,
  DownloadOutline,
  ChevronForwardOutline,
  CaretForwardOutline,
  RefreshOutline
} from '@vicons/ionicons5'
import { searchResources } from '@/api/resource'
import { getCategoryTree } from '@/api/category'
import { getTagList } from '@/api/tag'
import { getTitleCover } from '@/utils/cover'

defineOptions({ name: 'ResourceList' })

const route = useRoute()
const router = useRouter()
const scrollTop = ref(0)

const categories = ref([])
const tags = ref([])
const resources = ref([])
const total = ref(0)
const loading = ref(false)
const showFilter = ref(false)
const mobile = ref(false)

const expandedParents = ref(new Set())

const sortLabels = { new: '最新', hot: '热门', recommend: '推荐' }
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))

const query = reactive({
  page: 1,
  pageSize: 10,
  categoryId: null,
  keyword: '',
  tagIds: [],
  sortBy: ''
})

function getCategoryName(id) {
  for (const cat of categories.value) {
    if (cat.id === id) return cat.name
    if (cat.children) {
      const child = cat.children.find(c => c.id === id)
      if (child) return child.name
    }
  }
  return '分类#' + id
}

function getTagName(id) {
  const tag = tags.value.find(t => t.id === id)
  return tag ? tag.name : '标签#' + id
}

function checkMobile() {
  mobile.value = window.innerWidth < 768
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)

  // Read query params
  if (route.query.keyword) query.keyword = route.query.keyword
  if (route.query.categoryId) query.categoryId = Number(route.query.categoryId)
  if (route.query.tagIds) {
    query.tagIds = String(route.query.tagIds).split(',').map(Number).filter(Boolean)
  }

  const [catRes, tagRes] = await Promise.all([
    getCategoryTree(),
    getTagList()
  ])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []

  loadResources()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

onActivated(() => {
  // 每次激活时重新读取路由参数，覆盖缓存中的旧值
  let changed = false
  if (route.query.keyword && route.query.keyword !== query.keyword) {
    query.keyword = route.query.keyword
    changed = true
  }
  if (route.query.categoryId) {
    const catId = Number(route.query.categoryId)
    if (catId !== query.categoryId) { query.categoryId = catId; changed = true }
  }
  if (route.query.tagIds) {
    const tagIds = String(route.query.tagIds).split(',').map(Number).filter(Boolean).sort()
    const currentTagIds = [...query.tagIds].sort()
    if (JSON.stringify(tagIds) !== JSON.stringify(currentTagIds)) { query.tagIds = tagIds; changed = true }
  }
  if (changed) {
    query.page = 1
    loadResources()
  }

  // 恢复滚动位置
  if (scrollTop.value > 0) {
    window.scrollTo({ top: scrollTop.value, behavior: 'auto' })
  }
})

onDeactivated(() => {
  scrollTop.value = window.scrollY
})

function toggleParent(id) {
  const set = new Set(expandedParents.value)
  if (set.has(id)) set.delete(id)
  else set.add(id)
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
  const idx = query.tagIds.indexOf(id)
  if (idx > -1) {
    query.tagIds.splice(idx, 1)
  } else {
    query.tagIds.push(id)
  }
  query.page = 1
  loadResources()
}

function removeTag(id) {
  const idx = query.tagIds.indexOf(id)
  if (idx > -1) {
    query.tagIds.splice(idx, 1)
    query.page = 1
    loadResources()
  }
}

function doSearch() {
  query.page = 1
  loadResources()
}

function resetFilters() {
  query.categoryId = null
  query.tagIds = []
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
    if (!params.tagIds || !params.tagIds.length) delete params.tagIds
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
.main-section{padding:42px 0 78px}.layout-wrapper{display:flex;gap:28px;align-items:flex-start}.filter-sidebar{width:272px;flex-shrink:0;position:sticky;top:calc(var(--header-height) + 24px)}.filter-card-custom{background:var(--paper);border:1px solid var(--border);border-radius:0;box-shadow:0 12px 28px rgba(45,62,55,.055);overflow:hidden}.filter-card-custom :deep(.n-card-header){padding:19px 20px 15px;border-bottom:1px solid var(--border-light)}.filter-card-custom :deep(.n-card__content){padding:19px 20px 20px}.filter-card-custom :deep(.n-divider){margin:18px 0;border-color:var(--border-light)}.filter-title{display:flex;align-items:center;gap:8px;color:var(--ink);font-size:17px;font-weight:700}.filter-title :deep(.n-icon){color:var(--accent)}.filter-group{margin-bottom:4px}.filter-group h4{margin:0 0 10px;color:#81908a;font:600 10px/1.2 'PingFang SC','Microsoft YaHei',sans-serif;letter-spacing:.13em;text-transform:uppercase}.category-tree{display:flex;flex-direction:column;gap:2px}.cat-all,.cat-parent{border-left:2px solid transparent;border-radius:0;cursor:pointer;transition:background var(--transition-fast),color var(--transition-fast),border-color var(--transition-fast)}.cat-all{padding:8px 10px;color:var(--text-regular);font:14px/1.4 'PingFang SC','Microsoft YaHei',sans-serif}.cat-all:hover,.cat-all.active{color:var(--primary-dark);border-left-color:var(--accent);background:#edf4ef}.cat-all.active{font-weight:600}.cat-parent{display:flex;align-items:center;gap:4px;padding:8px 10px;color:var(--ink);font:600 14px/1.4 'PingFang SC','Microsoft YaHei',sans-serif;user-select:none}.cat-parent:hover,.cat-parent.active{color:var(--primary-dark);border-left-color:#72a399;background:#edf4ef}.cat-toggle{display:inline-flex;align-items:center;justify-content:center;width:18px;height:18px;flex-shrink:0;color:var(--text-secondary);font-size:12px}.cat-toggle .n-icon{transition:transform .2s ease}.cat-toggle .n-icon.rotated{transform:rotate(90deg)}.cat-name{flex:1}.cat-count{padding:0 6px;color:#84918b;background:#f0eee6;border-radius:10px;font:11px/18px 'PingFang SC','Microsoft YaHei',sans-serif}.cat-child-label{padding:6px 10px 6px 32px;color:var(--text-regular);border-left:2px solid transparent;border-radius:0;cursor:pointer;font:13px/1.4 'PingFang SC','Microsoft YaHei',sans-serif;transition:background var(--transition-fast),color var(--transition-fast),border-color var(--transition-fast)}.cat-child-label:hover,.cat-child-label.active{color:var(--primary-dark);border-left-color:#c79a5c;background:#f4f7f0}.cat-child-label.active{font-weight:600}.cat-children{margin-bottom:2px}.tag-list{display:flex;flex-wrap:wrap;gap:7px}.tag-item{cursor:pointer;transition:transform var(--transition-fast)}.tag-item:hover{transform:translateY(-1px)}.no-items{color:var(--text-secondary);font:13px 'PingFang SC','Microsoft YaHei',sans-serif}.sort-group{display:flex}.sort-group :deep(.n-radio-button){--n-button-text-color:var(--text-secondary);--n-button-border:#d9d4c8;--n-button-color:var(--paper);--n-button-text-color-active:#fffdf8;--n-button-color-active:#0f6d70;--n-button-border-active:#0f6d70}.sort-group :deep(.n-radio-button__label){padding:7px 12px;font-size:12px}.reset-btn{width:100%;margin-top:18px;color:#697974;border-color:#d6d4ca;border-radius:0}.reset-btn:hover{color:var(--primary-dark);border-color:#93b5aa;background:#eff5f0}.resource-list{min-width:0;flex:1}.search-bar{display:flex;gap:10px;margin-bottom:17px}.search-input{flex:1}.search-input :deep(.n-input-wrapper){height:46px;border:1px solid #d5d8cf;border-radius:0;background:rgba(255,253,248,.76);box-shadow:none}.search-input :deep(.n-input-wrapper:hover),.search-input :deep(.n-input-wrapper:focus-within){border-color:#75a59c;box-shadow:0 0 0 3px rgba(15,109,112,.08)}.search-btn{height:46px;padding:0 22px;border-radius:0;background:var(--primary)!important;border-color:var(--primary)!important;color:#fffdf8!important;--n-color:var(--primary);--n-color-hover:var(--primary-dark);--n-color-pressed:#063f42;--n-color-focus:var(--primary-dark);--n-color-disabled:#79afaa;--n-border:1px solid var(--primary);--n-border-hover:1px solid var(--primary-dark);--n-border-pressed:1px solid #063f42;--n-border-focus:1px solid var(--primary-dark);--n-text-color:#fffdf8;--n-text-color-hover:#fffdf8;--n-text-color-pressed:#fffdf8;--n-text-color-focus:#fffdf8;--n-ripple-color:rgba(255,253,248,.32)}.search-btn:hover,.search-btn:focus{background:var(--primary-dark)!important;border-color:var(--primary-dark)!important;color:#fffdf8!important}.search-btn:active{background:#063f42!important;border-color:#063f42!important;color:#fffdf8!important}.active-filters{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:19px;padding:11px 14px;background:#eef4ef;border-left:3px solid var(--accent)}.active-label{color:#64746f;font:13px 'PingFang SC','Microsoft YaHei',sans-serif;white-space:nowrap}.list-stats{margin-bottom:14px;color:var(--text-secondary);font:13px 'PingFang SC','Microsoft YaHei',sans-serif}.list-stats strong{color:var(--primary-dark);font-weight:700}.resource-items{display:flex;flex-direction:column;gap:13px}.resource-item{position:relative;display:flex;gap:21px;padding:17px;background:var(--paper);border:1px solid var(--border-light);border-radius:0;box-shadow:0 7px 18px rgba(46,61,55,.035);color:inherit;overflow:hidden;text-decoration:none;animation:listItemIn .35s ease forwards;opacity:0;transition:transform var(--transition),border-color var(--transition),box-shadow var(--transition)}.resource-item::before{position:absolute;top:0;bottom:0;left:0;width:3px;content:'';background:var(--accent);transform:scaleY(0);transform-origin:bottom;transition:transform var(--transition)}.resource-item:hover{color:inherit;border-color:#b8cec5;box-shadow:var(--shadow-hover);transform:translateY(-3px)}.resource-item:hover::before{transform:scaleY(1)}@keyframes listItemIn{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}.item-cover{width:180px;height:130px;flex-shrink:0;overflow:hidden;background:#e5ebe4;border-radius:0}.item-cover img{width:100%;height:100%;object-fit:cover;transition:transform .45s ease,opacity .5s ease,filter .5s ease}.item-cover img.blur-load{filter:blur(16px);opacity:.4}.item-cover img.blur-load.loaded{filter:blur(0);opacity:1}.resource-item:hover .item-cover img{transform:scale(1.05)}.item-info{min-width:0;flex:1}.item-title{margin:0 24px 8px 0;color:var(--ink);font-size:19px;font-weight:700;line-height:1.4;transition:color var(--transition-fast)}.resource-item:hover .item-title{color:var(--primary-dark)}.item-desc{display:-webkit-box;margin:0 0 13px;overflow:hidden;color:var(--text-secondary);font:13px/1.65 'PingFang SC','Microsoft YaHei',sans-serif;-webkit-box-orient:vertical;-webkit-line-clamp:2}.item-meta{display:flex;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:8px;color:var(--text-secondary);font:12px 'PingFang SC','Microsoft YaHei',sans-serif}.item-category{color:#276864!important;background:#e6f1ed!important}.item-stats{display:flex;align-items:center;gap:4px}.item-date{margin-left:auto;font-size:11px;color:#9a9e94}.item-tags{display:flex;flex-wrap:wrap;gap:5px}.item-arrow{position:absolute;top:50%;right:17px;color:#6b8c84;font-size:17px;opacity:0;transform:translateY(-50%);transition:opacity var(--transition-fast),transform var(--transition-fast)}.resource-item:hover .item-arrow{opacity:1;transform:translateY(-50%) translateX(4px)}.pagination-wrapper{display:flex;justify-content:center;margin-top:34px}.pagination-wrapper :deep(.n-pagination-item--active){color:#fffdf8!important;background:var(--primary)!important;border-color:var(--primary)!important}.skeleton-item{margin-bottom:13px}.skeleton-row{display:flex;gap:16px;padding:17px;background:var(--paper);border:1px solid var(--border-light);border-radius:0}.skeleton-cover{flex-shrink:0}.skeleton-info{display:flex;flex:1;flex-direction:column;justify-content:center}.empty-area{padding:64px 20px;text-align:center;background:var(--paper);border:1px dashed var(--border);border-radius:0;box-shadow:none}.empty-icon{display:block;margin-bottom:8px;color:#8a9d95}.empty-icon svg{display:block;margin:0 auto}.empty-area p{margin:0;color:var(--text-secondary);font:14px 'PingFang SC','Microsoft YaHei',sans-serif}.mobile-filter-bar{display:none}@media(max-width:768px){.main-section{padding:22px 0 48px}.layout-wrapper{flex-direction:column;gap:12px}.mobile-filter-bar{display:block;width:100%}.filter-toggle-btn{width:100%;height:42px;border-radius:0}.filter-sidebar{width:100%;position:static}.filter-sidebar.filter-mobile{display:block}.search-bar{gap:8px}.search-btn{padding:0 15px}.search-btn span{display:inline}.item-cover{width:100px;height:80px}.resource-item{gap:12px;padding:12px}.item-title{margin:0 0 6px;font-size:15px}.item-desc,.item-date{display:none}.item-meta{gap:8px;margin-top:12px;margin-bottom:0;font-size:11px}.item-arrow{display:none}.pagination-wrapper{justify-content:flex-start;overflow-x:auto;padding-bottom:8px}.pagination-wrapper :deep(.n-pagination){white-space:nowrap}}
</style>
