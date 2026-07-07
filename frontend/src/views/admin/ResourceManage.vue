<template>
  <div class="resource-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>资源管理</h2>
        <span class="header-desc">管理所有资源</span>
      </div>
      <n-button type="primary" @click="openAdd">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新增资源
      </n-button>
    </div>

    <!-- Search Filters -->
    <n-card :bordered="false" class="filter-card">
      <div class="filter-row">
        <n-form-item label="关键词">
          <n-input v-model:value="query.keyword" placeholder="标题/简介" clearable style="width:200px;" />
        </n-form-item>
        <n-form-item label="分类">
          <n-tree-select
            v-model:value="query.categoryId"
            :options="categoryTree"
            :key-field="'id'"
            :label-field="'name'"
            :children-field="'children'"
            placeholder="全部分类"
            clearable
            style="width:180px;"
          />
        </n-form-item>
        <n-form-item label="排序">
          <n-select v-model:value="query.sortBy" placeholder="默认" clearable style="width:120px;"
            :options="[
              { label: '最新', value: 'new' },
              { label: '热门', value: 'hot' },
              { label: '推荐', value: 'recommend' }
            ]"
          />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" @click="loadResources">搜索</n-button>
          <n-button style="margin-left:8px" @click="resetQuery">重置</n-button>
        </n-form-item>
      </div>
    </n-card>

    <!-- Table -->
    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="resources"
        :loading="loading"
        :bordered="true"
        :single-line="false"
        size="small"
      />
      <div class="pagination-wrapper">
        <n-pagination
          v-model:page="query.page"
          v-model:page-size="query.pageSize"
          :page-count="pageCount"
          :page-sizes="[10, 20, 50]"
          show-size-picker
          show-quick-jumper
          @update:page="loadResources"
          @update:page-size="loadResources"
        />
      </div>
    </n-card>

    <!-- Add/Edit Dialog -->
    <n-modal v-model:show="dialogVisible" :title="isEdit ? '编辑资源' : '新增资源'" preset="card" style="width:700px" :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-placement="left">
        <n-form-item label="资源标题" path="title">
          <n-input v-model:value="form.title" placeholder="请输入资源标题" />
        </n-form-item>
        <n-form-item label="所属分类" path="categoryId">
          <n-tree-select
            v-model:value="form.categoryId"
            :options="categoryTree"
            :key-field="'id'"
            :label-field="'name'"
            :children-field="'children'"
            placeholder="请选择分类"
            style="width:100%"
          />
        </n-form-item>
        <n-form-item label="封面图片">
          <div class="cover-upload">
            <n-upload
              :default-upload="false"
              accept="image/*"
              :max="1"
              @before-upload="onBeforeUpload"
            >
              <img v-if="form.cover" :src="form.cover" class="cover-preview" />
              <div v-else class="cover-placeholder">
                <n-icon><AddOutline /></n-icon>
                <span>上传封面</span>
              </div>
            </n-upload>
            <n-button v-if="form.cover" text type="error" size="small" @click="form.cover = ''">移除</n-button>
          </div>
        </n-form-item>
        <n-form-item label="标签">
          <n-select v-model:value="form.tagIds" multiple placeholder="选择标签" :options="allTags.map(t => ({ label: t.name, value: t.id }))" />
        </n-form-item>
        <n-form-item label="网盘链接">
          <n-input v-model:value="form.netdiskLink" placeholder="百度网盘分享链接" />
        </n-form-item>
        <n-form-item label="提取码">
          <n-input v-model:value="form.netdiskCode" placeholder="百度网盘提取码" style="width:200px;" />
        </n-form-item>
        <div style="display:flex;gap:20px;">
          <n-form-item label="排序">
            <n-input-number v-model:value="form.sort" :min="0" :max="999" />
          </n-form-item>
          <n-form-item label="状态">
            <n-select v-model:value="form.status" style="width:120px;"
              :options="[{ label: '发布', value: 1 }, { label: '草稿', value: 0 }]"
            />
          </n-form-item>
          <n-form-item label="推荐">
            <n-select v-model:value="form.isRecommend" style="width:120px;"
              :options="[{ label: '推荐', value: 1 }, { label: '普通', value: 0 }]"
            />
          </n-form-item>
        </div>
        <n-form-item label="资源简介">
          <n-input v-model:value="form.description" type="textarea" :rows="4" placeholder="资源详细描述" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display:flex;justify-content:flex-end;gap:8px">
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, computed, onMounted, nextTick } from 'vue'
import { useMessage } from 'naive-ui'
import { NTag, NButton, NPopconfirm, NSwitch } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { getAdminResourcePage, getAdminResourceDetail, addResource, updateResource, deleteResource, toggleRecommend } from '@/api/resource'
import { getAllCategories } from '@/api/category'
import { getAdminTagList } from '@/api/tag'
import { uploadCover } from '@/api/upload'

const message = useMessage()
const loading = ref(false)
const resources = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const allTags = ref([])
const categoryTree = ref([])
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  categoryId: null,
  sortBy: null
})

const defaultForm = () => ({
  id: null,
  title: '',
  description: '',
  cover: '',
  categoryId: null,
  netdiskLink: '',
  netdiskCode: '',
  isRecommend: 0,
  status: 0,
  sort: 0,
  tagIds: []
})

const form = reactive(defaultForm())

const rules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类' }]
}

const columns = [
  { title: '#', key: 'index', width: 55, align: 'center', render: (_, index) => index + 1 },
  { title: '标题', key: 'title', minWidth: 200, ellipsis: { tooltip: true } },
  { title: '分类', key: 'categoryName', width: 120, align: 'center',
    render: (row) => h(NTag, { size: 'small' }, { default: () => row.categoryName || '—' })
  },
  { title: '状态', key: 'status', width: 80, align: 'center',
    render: (row) => h(NTag,
      { type: row.status === 1 ? 'success' : 'info', size: 'small' },
      { default: () => row.status === 1 ? '发布' : '草稿' }
    )
  },
  { title: '推荐', key: 'isRecommend', width: 70, align: 'center',
    render: (row) => h(NSwitch, {
      value: row.isRecommend === 1,
      loading: row._recommendLoading,
      loading: row._recommendLoading,
      'onUpdate:value': () => handleToggleRecommend(row)
    })
  },
  { title: '浏览', key: 'viewCount', width: 70, align: 'center' },
  { title: '下载', key: 'downloadCount', width: 70, align: 'center' },
  { title: '标签', key: 'tags', width: 180,
    render: (row) => {
      if (row.tags && row.tags.length) {
        return row.tags.slice(0, 3).map(t => h(NTag, { size: 'small', type: 'success', style: 'margin:1px 2px' }, { default: () => t.name }))
      }
      return h('span', { style: 'color:#c0c4cc' }, '—')
    }
  },
  { title: '创建时间', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 160, fixed: 'right',
    render: (row) => [
      h(NButton, {
        text: true, type: 'primary', size: 'small',
        onClick: () => openEdit(row)
      }, { default: () => '编辑' }),
      h(NPopconfirm, {
        onPositiveClick: () => handleDelete(row.id)
      }, {
        trigger: () => h(NButton, {
          text: true, type: 'error', size: 'small',
          style: 'margin-left:8px'
        }, { default: () => '删除' }),
        default: () => '确定删除此资源？'
      })
    ]
  }
]

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([
    getAllCategories(),
    getAdminTagList()
  ])

  const flat = catRes.data || []
  const tree = []
  const map = {}
  flat.forEach(c => { map[c.id] = { ...c, children: [] } })
  flat.forEach(c => {
    if (c.parentId && c.parentId !== 0 && map[c.parentId]) {
      map[c.parentId].children.push(map[c.id])
    } else {
      tree.push(map[c.id])
    }
  })
  categoryTree.value = tree
  allTags.value = tagRes.data || []
  loadResources()
})

function resetQuery() {
  query.keyword = ''
  query.categoryId = null
  query.sortBy = null
  query.page = 1
  loadResources()
}

async function loadResources() {
  loading.value = true
  try {
    const params = { page: query.page, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.sortBy) params.sortBy = query.sortBy
    const res = await getAdminResourcePage(params)
    resources.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.restoreValidation())
}

async function openEdit(row) {
  isEdit.value = true
  Object.assign(form, defaultForm())
  form.id = row.id
  try {
    const res = await getAdminResourceDetail(row.id)
    const d = res.data
    form.title = d.title
    form.description = d.description || ''
    form.cover = d.cover || ''
    form.categoryId = d.categoryId
    form.netdiskLink = d.netdiskLink || ''
    form.netdiskCode = d.netdiskCode || ''
    form.isRecommend = d.isRecommend
    form.status = d.status
    form.sort = d.sort || 0
    form.tagIds = (d.tags || []).map(t => t.id)
    dialogVisible.value = true
    nextTick(() => formRef.value?.restoreValidation())
  } catch {
    message.error('获取资源详情失败')
  }
}

function onBeforeUpload({ file }) {
  const rawFile = file?.file
  if (rawFile) handleUpload(rawFile)
  return false // 不让 n-upload 加入内部列表，避免 :max=1 阻塞后续选择
}

async function handleUpload(file) {
  try {
    const res = await uploadCover(file)
    form.cover = res.data.url
    message.success('封面上传成功')
  } catch {
    // handled by interceptor
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const data = {
      title: form.title,
      description: form.description,
      cover: form.cover,
      categoryId: form.categoryId,
      netdiskLink: form.netdiskLink,
      netdiskCode: form.netdiskCode,
      isRecommend: form.isRecommend,
      status: form.status,
      sort: form.sort,
      tagIds: form.tagIds
    }
    if (isEdit.value) {
      data.id = form.id
      await updateResource(data)
      message.success('更新成功')
    } else {
      await addResource(data)
      message.success('新增成功')
    }
    dialogVisible.value = false
    await loadResources()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteResource(id)
    message.success('删除成功')
    await loadResources()
  } catch {
    // handled
  }
}

async function handleToggleRecommend(row) {
  row._recommendLoading = true
  try {
    await toggleRecommend(row.id)
    row.isRecommend = row.isRecommend === 1 ? 0 : 1
    message.success(row.isRecommend === 1 ? '已设为推荐' : '已取消推荐')
  } catch {
    // handled
  } finally {
    row._recommendLoading = false
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px;
}
.header-desc { font-size:13px; color:var(--text-secondary); }

.filter-card {
  margin-bottom: 20px;
}
.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cover-preview {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--border);
}

.cover-placeholder {
  width: 120px;
  height: 80px;
  border: 1px dashed var(--border);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 12px;
  gap: 4px;
  transition: var(--transition);
}
.cover-placeholder:hover {
  border-color: var(--primary);
  color: var(--primary);
}
</style>
