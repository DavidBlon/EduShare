<template>
  <div class="resource-import">
    <div class="page-header">
      <h2>批量导入资源</h2>
      <p class="page-desc">粘贴从网盘群复制的资源信息，系统自动解析并批量导入。</p>
    </div>

    <n-card :bordered="false" class="import-card">
      <template #header>
        <div class="card-header">
          <span>第一步：粘贴导入文本</span>
          <n-tooltip placement="top" trigger="hover">
            <template #trigger>
              <n-icon class="hint-icon" color="var(--text-secondary)"><HelpCircleOutline /></n-icon>
            </template>
            每条资源之间用空行隔开
          </n-tooltip>
        </div>
      </template>

      <n-input
        v-model:value="rawText"
        type="textarea"
        :rows="10"
        placeholder="在此粘贴资源信息，例如：&#10;&#10;语文1-6年级《阳光同学 暑假衔接》26秋&#10;https://pan.quark.cn/s/xxx&#10;&#10;数学1-6年《阳光同学 暑假衔接》人教 26年&#10;https://pan.quark.cn/s/yyy"
      />

      <div class="action-bar">
        <n-button
          type="primary"
          :loading="parsing"
          :disabled="!rawText.trim()"
          @click="handleParse"
        >
          <template #icon><n-icon><AnalyticsOutline /></n-icon></template>
          解析文本
        </n-button>
        <n-button @click="rawText = ''; parsedList = []" :disabled="!rawText">
          清空
        </n-button>
      </div>
    </n-card>

    <n-card v-if="parsedList.length > 0 && !hasImported" :bordered="false" class="import-card">
      <template #header>
        <div class="card-header">
          <span>第二步：确认解析结果（可编辑）</span>
          <n-tag :type="importCount > 0 ? 'success' : 'warning'" size="small">
            共 {{ parsedList.length }} 条，待导入 {{ importCount }} 条
            <template v-if="dupCount">，重复 {{ dupCount }} 条</template>
          </n-tag>
        </div>
      </template>

      <n-alert
        v-if="parsedList.length > successCount"
        title="部分资源未能自动匹配分类，请手动选择分类后再导入"
        type="warning"
        :bordered="false"
        class="mb-16"
      />

      <n-data-table
        :columns="tableColumns"
        :data="parsedList"
        :bordered="true"
        :single-line="false"
        size="small"
        :max-height="600"
      />

      <div class="action-bar">
        <n-button
          type="primary"
          size="large"
          :loading="importing"
          :disabled="importCount === 0 || hasImported"
          @click="handleBatchImport"
        >
          <template #icon><n-icon><CloudUploadOutline /></n-icon></template>
          批量导入（{{ importCount }} 条）
        </n-button>
        <n-button @click="parsedList = []; hasImported = false">重新导入</n-button>
      </div>
    </n-card>

    <n-card v-if="importResult" :bordered="false" class="import-card">
      <template #header>
        <div class="card-header">
          <span>导入结果</span>
        </div>
      </template>
      <n-result status="success" :title="'成功导入 ' + importResult.imported + ' 条资源'">
        <template #default>
          <p>{{ getImportSubtitle() }}</p>
        </template>
        <template #footer>
          <n-button type="primary" @click="$router.push('/admin/resource')">
            前往资源管理
          </n-button>
          <n-button style="margin-left:8px" @click="importResult = null; rawText = ''; parsedList = []; hasImported = false">
            继续导入
          </n-button>
        </template>
      </n-result>
    </n-card>
  </div>
</template>

<script setup>
import { ref, computed, h, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { NTag, NSelect, NInput, NButton } from 'naive-ui'
import { HelpCircleOutline, AnalyticsOutline, CloudUploadOutline } from '@vicons/ionicons5'
import { parseImportText, batchImportResources } from '@/api/import'
import { getCategoryTree } from '@/api/category'
import { getTagList } from '@/api/tag'

const message = useMessage()
const rawText = ref('')
const parsedList = ref([])
const parsing = ref(false)
const importing = ref(false)
const importResult = ref(null)
const hasImported = ref(false)
const categoryOptions = ref([])
const tagOptions = ref([])

const successCount = computed(() =>
  parsedList.value.filter(r => r.categoryId != null).length
)
const importCount = computed(() =>
  parsedList.value.filter(r => r.categoryId != null && !r.alreadyExists).length
)
const dupCount = computed(() =>
  parsedList.value.filter(r => r.alreadyExists).length
)

const tableColumns = [
  { title: '#', key: 'index', width: 50, fixed: 'left', render: (_, index) => index + 1 },
  { title: '资源标题', key: 'title', minWidth: 240,
    render: (row) => {
      const cells = []
      if (row.alreadyExists) {
        cells.push(h('div', { class: 'title-cell' }, [
          h(NInput, { value: row.title, size: 'small', disabled: true, style: 'flex:1' }),
          h(NTag, { type: 'warning', size: 'small', style: 'flex-shrink:0' }, { default: () => '已存在' })
        ]))
      } else {
        cells.push(h(NInput, {
          value: row.title,
          size: 'small',
          disabled: row.alreadyExists,
          'onUpdate:value': (v) => { row.title = v }
        }))
      }
      return cells
    }
  },
  { title: '网盘链接', key: 'netdiskLink', minWidth: 260,
    render: (row) => h(NInput, {
      value: row.netdiskLink, size: 'small', placeholder: '可选',
      disabled: row.alreadyExists,
      'onUpdate:value': (v) => { row.netdiskLink = v }
    })
  },
  { title: '提取码', key: 'netdiskCode', width: 100,
    render: (row) => h(NInput, {
      value: row.netdiskCode, size: 'small', placeholder: '可选',
      disabled: row.alreadyExists,
      'onUpdate:value': (v) => { row.netdiskCode = v }
    })
  },
  { title: '匹配分类', key: 'categoryName', width: 160,
    render: (row) => {
      if (row.alreadyExists) {
        return h(NTag, { type: 'warning', size: 'small' }, { default: () => row.categoryName })
      }
      if (row.categoryId) {
        return h(NTag, { type: 'success', size: 'small' }, { default: () => row.categoryName })
      }
      return h(NSelect, {
        value: row.categoryId, size: 'small', placeholder: '选择分类',
        options: categoryOptions.value,
        'onUpdate:value': (val) => { row.categoryId = val; updateCategoryName(row, val) }
      })
    }
  },
  { title: '年级', key: 'grade', width: 80 },
  { title: '科目', key: 'subject', width: 70 },
  { title: '标签', key: 'tagIds', minWidth: 140,
    render: (row) => h(NSelect, {
      value: row.tagIds, size: 'small', placeholder: '可选', multiple: true,
      disabled: row.alreadyExists,
      options: tagOptions.value.map(t => ({ label: t.name, value: t.id })),
      'onUpdate:value': (val) => { row.tagIds = val }
    })
  },
  { title: '操作', key: 'action', width: 80, fixed: 'right',
    render: (row, index) => {
      if (!row.alreadyExists) {
        return h(NButton, {
          text: true, type: 'error', size: 'small',
          onClick: () => { parsedList.value.splice(index, 1) }
        }, { default: () => '移除' })
      }
      return null
    }
  }
]

onMounted(async () => {
  await Promise.all([loadCategories(), loadTags()])
})

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    const flat = []
    function walk(list, prefix) {
      list.forEach(c => {
        const label = prefix ? prefix + ' / ' + c.name : c.name
        flat.push({ value: c.id, label })
        if (c.children && c.children.length) walk(c.children, label)
      })
    }
    walk(res.data || [], '')
    categoryOptions.value = flat
  } catch {
    message.error('加载分类列表失败')
  }
}

async function loadTags() {
  try {
    const res = await getTagList()
    tagOptions.value = res.data || []
  } catch {
    message.error('加载标签列表失败')
  }
}

async function handleParse() {
  if (!rawText.value.trim()) return
  parsing.value = true
  try {
    const res = await parseImportText(rawText.value)
    parsedList.value = (res.data || []).map(item => ({
      ...item,
      tagIds: item.tagIds || []
    }))
    if (parsedList.value.length === 0) {
      message.warning('未能从文本中解析出有效资源，请检查格式')
    }
  } catch {
    // handled by interceptor
  } finally {
    parsing.value = false
  }
}

function updateCategoryName(row, categoryId) {
  const found = categoryOptions.value.find(o => o.value === categoryId)
  row.categoryName = found ? found.label : ''
}

function getImportSubtitle() {
  if (!importResult.value) return ''
  const parts = []
  if (importResult.value.skippedDup > 0) parts.push('自动跳过 ' + importResult.value.skippedDup + ' 条已有资源')
  if (importResult.value.skippedNoCategory > 0) parts.push('跳过 ' + importResult.value.skippedNoCategory + ' 条未分类资源')
  if (parts.length === 0) return '可前往资源管理查看或编辑'
  return parts.join('，') + '，可前往资源管理查看或编辑'
}

async function handleBatchImport() {
  const validList = parsedList.value.filter(r => r.categoryId != null && !r.alreadyExists)
  if (validList.length === 0) {
    message.warning('没有可导入的资源')
    return
  }
  importing.value = true
  try {
    await batchImportResources(validList)
    importResult.value = {
      imported: validList.length,
      skippedDup: dupCount.value,
      skippedNoCategory: parsedList.value.filter(r => r.categoryId == null && !r.alreadyExists).length
    }
    hasImported.value = true
    const parts = ['成功导入 ' + validList.length + ' 条']
    if (dupCount.value > 0) parts.push('跳过 ' + dupCount.value + ' 条重复')
    if (importResult.value.skippedNoCategory > 0) parts.push('跳过 ' + importResult.value.skippedNoCategory + ' 条未分类')
    message.success(parts.join('，'))
  } catch {
    // handled by interceptor
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.resource-import {
  max-width: 1200px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  margin: 0 0 8px;
  font-size: 22px;
}
.page-desc {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}
.import-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hint-icon {
  cursor: pointer;
  margin-left: 6px;
}
.action-bar {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
.mb-16 {
  margin-bottom: 16px;
}
.title-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
