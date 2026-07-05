<template>
  <div class="resource-import">
    <div class="page-header">
      <h2>批量导入资源</h2>
      <p class="page-desc">粘贴从网盘群复制的资源信息，系统自动解析并批量导入。</p>
    </div>

    <!-- Step 1: 粘贴文本 -->
    <el-card class="import-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>第一步：粘贴导入文本</span>
          <el-tooltip content="每条资源之间用空行隔开" placement="top">
            <el-icon class="hint-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </template>

      <el-input
        v-model="rawText"
        type="textarea"
        :rows="10"
        placeholder="在此粘贴资源信息，例如：&#10;&#10;语文1-6年级《阳光同学 暑假衔接》26秋&#10;https://pan.quark.cn/s/aec027398f5b&#10;&#10;数学1-6年《阳光同学 暑假衔接》人教 26年&#10;https://pan.quark.cn/s/ad88bbd8737b"
      />

      <div class="action-bar">
        <el-button
          type="primary"
          :loading="parsing"
          :disabled="!rawText.trim()"
          @click="handleParse"
        >
          <el-icon><DataAnalysis /></el-icon>
          解析文本
        </el-button>
        <el-button @click="rawText = ''; parsedList = []" :disabled="!rawText">
          清空
        </el-button>
      </div>
    </el-card>

    <!-- Step 2: 预览结果（导入成功后替换为结果卡片） -->
    <el-card v-if="parsedList.length > 0 && !hasImported" class="import-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>第二步：确认解析结果（可编辑）</span>
          <el-tag :type="importCount > 0 ? 'success' : 'warning'" effect="plain">
            共 {{ parsedList.length }} 条，待导入 {{ importCount }} 条
            <template v-if="dupCount">，重复 {{ dupCount }} 条</template>
          </el-tag>
        </div>
      </template>

      <el-alert
        v-if="parsedList.length > successCount"
        title="部分资源未能自动匹配分类，请手动选择分类后再导入"
        type="warning"
        show-icon
        :closable="false"
        class="mb-16"
      />

      <el-table :data="parsedList" stripe border max-height="600" style="width: 100%">
        <el-table-column type="index" label="#" width="50" fixed />

        <el-table-column prop="title" label="资源标题" min-width="240">
          <template #default="{ row }">
            <div class="title-cell">
              <el-input v-model="row.title" size="small" :disabled="row.alreadyExists" />
              <el-tag v-if="row.alreadyExists" type="warning" size="small" effect="dark" class="exists-tag">
                已存在
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="netdiskLink" label="网盘链接" min-width="260">
          <template #default="{ row }">
            <el-input v-model="row.netdiskLink" size="small" placeholder="可选" :disabled="row.alreadyExists" />
          </template>
        </el-table-column>

        <el-table-column prop="netdiskCode" label="提取码" width="100">
          <template #default="{ row }">
            <el-input v-model="row.netdiskCode" size="small" placeholder="可选" :disabled="row.alreadyExists" />
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="匹配分类" width="160">
          <template #default="{ row }">
            <template v-if="row.alreadyExists">
              <el-tag type="warning" effect="plain" size="small">{{ row.categoryName }}</el-tag>
            </template>
            <el-tag v-else-if="row.categoryId" type="success" effect="plain" size="small">
              {{ row.categoryName }}
            </el-tag>
            <el-select
              v-else
              v-model="row.categoryId"
              placeholder="选择分类"
              size="small"
              style="width: 100%"
              @change="val => updateCategoryName(row, val)"
            >
              <el-option
                v-for="item in categoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <div v-if="row.parseError || row.categoryMatchMessage" class="match-message">
              {{ row.parseError || row.categoryMatchMessage }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column prop="subject" label="科目" width="70" />

        <el-table-column prop="tagIds" label="标签" min-width="140">
          <template #default="{ row }">
            <el-select
              v-model="row.tagIds"
              multiple
              placeholder="可选"
              size="small"
              style="width: 100%"
              :disabled="row.alreadyExists"
            >
              <el-option
                v-for="tag in tagOptions"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ $index }">
            <el-button
              v-if="!parsedList[$index].alreadyExists"
              text
              type="danger"
              size="small"
              @click="parsedList.splice($index, 1)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="action-bar">
        <el-button
          type="primary"
          size="large"
          :loading="importing"
          :disabled="importCount === 0 || hasImported"
          @click="handleBatchImport"
        >
          <el-icon><Upload /></el-icon>
          批量导入（{{ importCount }} 条）
        </el-button>
        <el-button @click="parsedList = []; hasImported = false">重新导入</el-button>
      </div>
    </el-card>

    <!-- 导入结果（直接替换第二步） -->
    <el-card v-if="importResult" class="import-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>导入结果</span>
        </div>
      </template>
      <el-result
        icon="success"
        :title="`成功导入 ${importResult.imported} 条资源`"
        :sub-title="getImportSubtitle()"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/admin/resource')">
            前往资源管理
          </el-button>
          <el-button @click="importResult = null; rawText = ''; parsedList = []; hasImported = false">
            继续导入
          </el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { parseImportText, batchImportResources } from '@/api/import'
import { getCategoryTree } from '@/api/category'
import { getTagList } from '@/api/tag'
import { ElMessage } from 'element-plus'
import { QuestionFilled, DataAnalysis, Upload } from '@element-plus/icons-vue'

const rawText = ref('')
const parsedList = ref([])
const parsing = ref(false)
const importing = ref(false)
const importResult = ref(null)
const hasImported = ref(false) // 防止重复导入

// 分类选项（扁平化树为选择器选项）
const categoryOptions = ref([])

// 标签选项
const tagOptions = ref([])

const successCount = computed(() =>
  parsedList.value.filter(r => r.categoryId != null).length
)

// 可导入数量（有分类且不重复）
const importCount = computed(() =>
  parsedList.value.filter(r => r.categoryId != null && !r.alreadyExists).length
)

// 重复数量
const dupCount = computed(() =>
  parsedList.value.filter(r => r.alreadyExists).length
)

onMounted(async () => {
  await Promise.all([loadCategories(), loadTags()])
})

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    // 扁平化分类树为 el-option 数据
    const flat = []
    function walk(list, prefix) {
      list.forEach(c => {
        const label = prefix ? prefix + ' / ' + c.name : c.name
        flat.push({ value: c.id, label })
        if (c.children && c.children.length) {
          walk(c.children, label)
        }
      })
    }
    walk(res.data || [], '')
    categoryOptions.value = flat
  } catch {
    ElMessage.error('加载分类列表失败')
  }
}

async function loadTags() {
  try {
    const res = await getTagList()
    tagOptions.value = res.data || []
  } catch {
    ElMessage.error('加载标签列表失败')
  }
}

async function handleParse() {
  if (!rawText.value.trim()) return
  parsing.value = true
  try {
    const res = await parseImportText(rawText.value)
    parsedList.value = (res.data || []).map(item => ({
      ...item,
      // 确保 tagIds 是数组
      tagIds: item.tagIds || []
    }))
    if (parsedList.value.length === 0) {
      ElMessage.warning('未能从文本中解析出有效资源，请检查格式')
      return
    }
  } catch {
    // Error already handled by interceptor
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
  if (importResult.value.skippedDup > 0) parts.push(`自动跳过 ${importResult.value.skippedDup} 条已有资源`)
  if (importResult.value.skippedNoCategory > 0) parts.push(`跳过 ${importResult.value.skippedNoCategory} 条未分类资源`)
  if (parts.length === 0) return '可前往资源管理查看或编辑'
  return parts.join('，') + '，可前往资源管理查看或编辑'
}

async function handleBatchImport() {
  const uncategorizedCount = parsedList.value.filter(r => r.categoryId == null && !r.alreadyExists).length
  const dupCountVal = parsedList.value.filter(r => r.alreadyExists).length
  const validList = parsedList.value.filter(r => r.categoryId != null && !r.alreadyExists)

  if (validList.length === 0) {
    ElMessage.warning('没有可导入的资源')
    return
  }

  importing.value = true
  try {
    await batchImportResources(validList)
    importResult.value = {
      imported: validList.length,
      skippedDup: dupCountVal,
      skippedNoCategory: uncategorizedCount
    }
    hasImported.value = true
    const parts = [`成功导入 ${validList.length} 条`]
    if (dupCountVal > 0) parts.push(`跳过 ${dupCountVal} 条重复`)
    if (uncategorizedCount > 0) parts.push(`跳过 ${uncategorizedCount} 条未分类`)
    ElMessage.success(parts.join('，'))
  } catch {
    // Error already handled by interceptor
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
  color: var(--text-secondary);
  cursor: pointer;
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

.title-cell .el-input {
  flex: 1;
}

.exists-tag {
  flex-shrink: 0;
}

.match-message {
  margin-top: 4px;
  color: #e6a23c;
  font-size: 12px;
  line-height: 1.3;
}
</style>
