<template>
  <div class="category-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>分类管理</h2>
        <span class="header-desc">管理资源分类结构</span>
      </div>
      <n-button type="primary" @click="openAdd">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新增一级分类
      </n-button>
    </div>

    <n-card :bordered="false">
      <n-data-table
        ref="tableRef"
        :columns="columns"
        :data="categories"
        :loading="loading"
        :bordered="true"
        :single-line="false"
        children-key="children"
      />
    </n-card>

    <n-modal v-model:show="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" preset="card" style="width:500px" :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-placement="left">
        <n-form-item label="分类名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入分类名称" />
        </n-form-item>
        <n-form-item label="父级分类">
          <n-tree-select
            v-model:value="form.parentId"
            :options="categoryTree"
            :key-field="'id'"
            :label-field="'name'"
            :children-field="'children'"
            placeholder="不选则为顶级分类"
            clearable
          />
        </n-form-item>
        <n-form-item label="排序号">
          <n-input-number v-model:value="form.sort" :min="0" :max="999" />
        </n-form-item>
        <n-form-item label="层级" path="level">
          <n-select v-model:value="form.level" placeholder="请选择层级" :options="levelOptions" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.statusStr" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="可选" />
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
import { ref, reactive, h, onMounted, computed, nextTick } from 'vue'
import { useMessage } from 'naive-ui'
import { NTag, NButton, NPopconfirm } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { getAllCategories, addCategory, updateCategory, deleteCategory } from '@/api/category'

const message = useMessage()
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

const levelOptions = [
  { label: '小学', value: 1 },
  { label: '初中', value: 2 },
  { label: '高中', value: 3 },
  { label: '中考', value: 4 },
  { label: '高考', value: 5 }
]

const levelLabel = (level) => {
  const map = { 1: '小学', 2: '初中', 3: '高中', 4: '中考', 5: '高考' }
  return map[level] || `Level ${level}`
}

const levelTagType = (level) => {
  const map = { 1: 'default', 2: 'success', 3: 'warning', 4: 'error', 5: 'info' }
  return map[level] || 'default'
}

const columns = [
  { title: '分类名称', key: 'name', minWidth: 160, tree: true },
  { title: '层级', key: 'level', width: 120, align: 'center',
    render: (row) => h(NTag,
      { type: levelTagType(row.level), size: 'small' },
      { default: () => levelLabel(row.level) }
    )
  },
  { title: '排序', key: 'sort', width: 80, align: 'center' },
  { title: '状态', key: 'status', width: 80, align: 'center',
    render: (row) => h(NTag,
      { type: row.status === 1 ? 'success' : 'error', size: 'small' },
      { default: () => row.status === 1 ? '启用' : '禁用' }
    )
  },
  { title: '创建时间', key: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 280, fixed: 'right',
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
        default: () => '确定删除此分类？'
      }),
      h(NButton, {
        text: true, type: 'primary', size: 'small',
        style: 'margin-left:8px',
        onClick: () => openAddChild(row)
      }, { default: () => '添加子分类' })
    ]
  }
]

// Build tree for tree-select
const categoryTree = computed(() => {
  const build = (list) =>
    list.map(c => ({ id: c.id, name: c.name, children: c.children ? build(c.children) : undefined }))
  return build(categories.value)
})

onMounted(loadCategories)

async function loadCategories() {
  loading.value = true
  try {
    const res = await getAllCategories()
    const flat = res.data || []
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
      // Expand all rows
      // Naive UI data-table tree uses default-expand-all or row-key expand behavior
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
  try {
    await formRef.value.validate()
  } catch {
    return
  }

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
      message.success('更新成功')
    } else {
      await addCategory(data)
      message.success('新增成功')
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
    message.success('删除成功')
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
