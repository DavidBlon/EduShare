<template>
  <div class="resource-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>资源管理</h2>
        <span class="header-desc">管理所有资源</span>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增资源
      </el-button>
    </div>

    <!-- Search Filters -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="query" inline>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="标题/简介" clearable style="width:200px;" />
        </el-form-item>
        <el-form-item label="分类">
          <el-tree-select
            v-model="query.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="全部分类"
            clearable
            check-strictly
            style="width:180px;"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="query.sortBy" placeholder="默认" clearable style="width:120px;">
            <el-option label="最新" value="new" />
            <el-option label="热门" value="hot" />
            <el-option label="推荐" value="recommend" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadResources">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <el-table :data="resources" v-loading="loading" border stripe style="width:100%">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.categoryName || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="70" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isRecommend === 1"
              :loading="row._recommendLoading"
              size="small"
              @click.stop
              @change="handleToggleRecommend(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
        <el-table-column prop="downloadCount" label="下载" width="70" align="center" />
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <template v-if="row.tags && row.tags.length">
              <el-tag v-for="t in row.tags.slice(0, 3)" :key="t.id" size="small" type="success" effect="plain" style="margin:1px 2px;">
                {{ t.name }}
              </el-tag>
            </template>
            <span v-else style="color:#c0c4cc;">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除此资源？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="loadResources"
          @size-change="loadResources"
        />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑资源' : '新增资源'" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="资源标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择分类"
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="cover-upload">
            <el-upload
              :show-file-list="false"
              :http-request="handleUpload"
              accept="image/*"
            >
              <img v-if="form.cover" :src="form.cover" class="cover-preview" />
              <div v-else class="cover-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
            <el-button v-if="form.cover" text type="danger" size="small" @click="form.cover = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%">
            <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="网盘链接">
          <el-input v-model="form.netdiskLink" placeholder="百度网盘分享链接" />
        </el-form-item>
        <el-form-item label="提取码">
          <el-input v-model="form.netdiskCode" placeholder="百度网盘提取码" style="width:200px;" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option :value="1" label="发布" />
                <el-option :value="0" label="草稿" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推荐">
              <el-select v-model="form.isRecommend" style="width:100%">
                <el-option :value="1" label="推荐" />
                <el-option :value="0" label="普通" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="资源简介">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="资源详细描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminResourcePage, getAdminResourceDetail, addResource, updateResource, deleteResource, toggleRecommend } from '@/api/resource'
import { getAllCategories } from '@/api/category'
import { getAdminTagList } from '@/api/tag'
import { uploadCover } from '@/api/upload'

const loading = ref(false)
const resources = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const allTags = ref([])

// Category tree
const categoryTree = ref([])

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
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

onMounted(async () => {
  // Load categories and tags
  const [catRes, tagRes] = await Promise.all([
    getAllCategories(),
    getAdminTagList()
  ])

  // Build tree
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
  nextTick(() => formRef.value?.clearValidate())
}

async function openEdit(row) {
  isEdit.value = true
  Object.assign(form, defaultForm())
  form.id = row.id

  // Load full detail
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
    nextTick(() => formRef.value?.clearValidate())
  } catch {
    ElMessage.error('获取资源详情失败')
  }
}

async function handleUpload(uploadOption) {
  try {
    const res = await uploadCover(uploadOption.file)
    form.cover = res.data.url
    ElMessage.success('封面上传成功')
  } catch {
    // handled by interceptor
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

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
      ElMessage.success('更新成功')
    } else {
      await addResource(data)
      ElMessage.success('新增成功')
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
    ElMessage.success('删除成功')
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
    ElMessage.success(row.isRecommend === 1 ? '已设为推荐' : '已取消推荐')
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
