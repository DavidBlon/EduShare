<template>
  <div class="category-manage">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h2>分类管理</h2>
        <span class="header-desc">管理资源分类结构</span>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增一级分类
      </el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never">
      <el-table ref="tableRef" :data="categories" row-key="id" :tree-props="{ children: 'children' }" v-loading="loading" border>
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="level" label="层级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" effect="plain" size="small">
              {{ levelLabel(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除此分类？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
            <el-button text type="primary" size="small" @click="openAddChild(row)">添加子分类</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', disabled: (d) => isEdit && d.id === form.id }"
            placeholder="不选则为顶级分类"
            check-strictly
            clearable
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="层级" prop="level">
          <el-select v-model="form.level" placeholder="请选择层级" style="width:100%">
            <el-option :value="1" label="小学" />
            <el-option :value="2" label="初中" />
            <el-option :value="3" label="高中" />
            <el-option :value="4" label="中考" />
            <el-option :value="5" label="高考" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusStr" active-text="启用" inactive-text="禁用" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选" />
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
import { getAllCategories, addCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const tableRef = ref(null)

const defaultForm = () => ({
  id: null,
  name: '',
  parentId: null,
  sort: 0,
  level: 1,
  statusStr: 1,
  description: ''
})

const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// Build tree for tree-select
const categoryTree = computed(() => {
  const build = (list) => {
    return list.map(c => ({
      ...c,
      id: c.id,
      name: c.name,
      children: c.children ? build(c.children) : undefined
    }))
  }
  return build(categories.value)
})

function levelLabel(level) {
  const map = { 1: '小学', 2: '初中', 3: '高中', 4: '中考', 5: '高考' }
  return map[level] || `Level ${level}`
}

function levelTagType(level) {
  const map = { 1: '', 2: 'success', 3: 'warning', 4: 'danger', 5: 'info' }
  return map[level] || ''
}

onMounted(loadCategories)

async function loadCategories() {
  loading.value = true
  try {
    // We use the admin list which returns flat data, then build tree manually
    const res = await getAllCategories()
    const flat = res.data || []

    // Build tree for display
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
    categories.value = tree
    nextTick(() => {
      tableRef.value?.toggleAllExpansion()
    })
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.parentId = row.parentId && row.parentId !== 0 ? row.parentId : null
  form.sort = row.sort
  form.level = row.level
  form.statusStr = row.status
  form.description = row.description
  dialogVisible.value = true
}

function openAddChild(row) {
  isEdit.value = false
  Object.assign(form, defaultForm())
  form.parentId = row.id
  form.level = row.level
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      name: form.name,
      parentId: form.parentId || 0,
      sort: form.sort,
      level: form.level,
      status: form.statusStr,
      description: form.description
    }
    if (isEdit.value) {
      data.id = form.id
      await updateCategory(data)
      ElMessage.success('更新成功')
    } else {
      await addCategory(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadCategories()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteCategory(id)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch {
    // handled by interceptor
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

.header-desc {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
