<template>
  <div class="disclaimer-page">
    <div class="page-header">
      <div class="header-left">
        <h2>免责声明设置</h2>
        <span class="header-desc">管理平台对外展示的免责声明内容</span>
      </div>
    </div>

    <el-card shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-collapse v-model="activeSections">
          <!-- 章节 1 -->
          <el-collapse-item title="章节一" name="1">
            <el-form-item label="标题" prop="section1Title">
              <el-input v-model="form.section1Title" placeholder="请输入章节标题" />
            </el-form-item>
            <el-form-item label="内容" prop="section1Content">
              <el-input v-model="form.section1Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </el-form-item>
          </el-collapse-item>

          <!-- 章节 2 -->
          <el-collapse-item title="章节二" name="2">
            <el-form-item label="标题" prop="section2Title">
              <el-input v-model="form.section2Title" placeholder="请输入章节标题" />
            </el-form-item>
            <el-form-item label="内容" prop="section2Content">
              <el-input v-model="form.section2Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </el-form-item>
          </el-collapse-item>

          <!-- 章节 3 -->
          <el-collapse-item title="章节三" name="3">
            <el-form-item label="标题" prop="section3Title">
              <el-input v-model="form.section3Title" placeholder="请输入章节标题" />
            </el-form-item>
            <el-form-item label="内容" prop="section3Content">
              <el-input v-model="form.section3Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </el-form-item>
          </el-collapse-item>

          <!-- 章节 4 -->
          <el-collapse-item title="章节四" name="4">
            <el-form-item label="标题" prop="section4Title">
              <el-input v-model="form.section4Title" placeholder="请输入章节标题" />
            </el-form-item>
            <el-form-item label="内容" prop="section4Content">
              <el-input v-model="form.section4Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </el-form-item>
          </el-collapse-item>

          <!-- 章节 5 -->
          <el-collapse-item title="章节五" name="5">
            <el-form-item label="标题" prop="section5Title">
              <el-input v-model="form.section5Title" placeholder="请输入章节标题" />
            </el-form-item>
            <el-form-item label="内容" prop="section5Content">
              <el-input v-model="form.section5Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </el-form-item>
          </el-collapse-item>
        </el-collapse>

        <el-divider />

        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" placeholder="请输入侵权投诉联系邮箱">
            <template #prefix><el-icon><Message /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item label="页脚声明" prop="briefDisclaimer">
          <el-input v-model="form.briefDisclaimer" type="textarea" :rows="2" placeholder="请输入页脚显示的简短免责声明" />
        </el-form-item>

        <el-divider />

        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
              保存设置
            </el-button>
            <el-button @click="handleReset" size="large">重置</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDisclaimer, updateDisclaimer } from '@/api/admin'

const formRef = ref(null)
const submitting = ref(false)
const activeSections = ref(['1', '2', '3', '4', '5'])

const form = reactive({
  section1Title: '',
  section1Content: '',
  section2Title: '',
  section2Content: '',
  section3Title: '',
  section3Content: '',
  section4Title: '',
  section4Content: '',
  section5Title: '',
  section5Content: '',
  contactEmail: '',
  briefDisclaimer: ''
})

const rules = {
  contactEmail: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const res = await getDisclaimer()
    const d = res.data
    form.section1Title = d.section1Title || ''
    form.section1Content = d.section1Content || ''
    form.section2Title = d.section2Title || ''
    form.section2Content = d.section2Content || ''
    form.section3Title = d.section3Title || ''
    form.section3Content = d.section3Content || ''
    form.section4Title = d.section4Title || ''
    form.section4Content = d.section4Content || ''
    form.section5Title = d.section5Title || ''
    form.section5Content = d.section5Content || ''
    form.contactEmail = d.contactEmail || ''
    form.briefDisclaimer = d.briefDisclaimer || ''
  } catch {
    // handled
  }
})

function handleReset() {
  ElMessageBox.confirm('重置将丢失所有未保存的更改，确定吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    location.reload()
  }).catch(() => {})
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updateDisclaimer({
      section1Title: form.section1Title,
      section1Content: form.section1Content,
      section2Title: form.section2Title,
      section2Content: form.section2Content,
      section3Title: form.section3Title,
      section3Content: form.section3Content,
      section4Title: form.section4Title,
      section4Content: form.section4Content,
      section5Title: form.section5Title,
      section5Content: form.section5Content,
      contactEmail: form.contactEmail || null,
      briefDisclaimer: form.briefDisclaimer
    })
    ElMessage.success('免责声明已更新')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-header {
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

.form-actions {
  display: flex;
  gap: 12px;
}

:deep(.el-collapse-item__header) {
  font-weight: 600;
  font-size: 15px;
}
</style>
