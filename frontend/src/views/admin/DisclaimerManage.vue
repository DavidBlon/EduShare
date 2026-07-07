<template>
  <div class="disclaimer-page">
    <div class="page-header">
      <div class="header-left">
        <h2>免责声明设置</h2>
        <span class="header-desc">管理平台对外展示的免责声明内容</span>
      </div>
    </div>

    <n-card :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-placement="left">
        <n-collapse v-model:value="activeSections" display-directive="show">
          <n-collapse-item title="章节一" name="1">
            <n-form-item label="标题" path="section1Title">
              <n-input v-model:value="form.section1Title" placeholder="请输入章节标题" />
            </n-form-item>
            <n-form-item label="内容" path="section1Content">
              <n-input v-model:value="form.section1Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </n-form-item>
          </n-collapse-item>

          <n-collapse-item title="章节二" name="2">
            <n-form-item label="标题" path="section2Title">
              <n-input v-model:value="form.section2Title" placeholder="请输入章节标题" />
            </n-form-item>
            <n-form-item label="内容" path="section2Content">
              <n-input v-model:value="form.section2Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </n-form-item>
          </n-collapse-item>

          <n-collapse-item title="章节三" name="3">
            <n-form-item label="标题" path="section3Title">
              <n-input v-model:value="form.section3Title" placeholder="请输入章节标题" />
            </n-form-item>
            <n-form-item label="内容" path="section3Content">
              <n-input v-model:value="form.section3Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </n-form-item>
          </n-collapse-item>

          <n-collapse-item title="章节四" name="4">
            <n-form-item label="标题" path="section4Title">
              <n-input v-model:value="form.section4Title" placeholder="请输入章节标题" />
            </n-form-item>
            <n-form-item label="内容" path="section4Content">
              <n-input v-model:value="form.section4Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </n-form-item>
          </n-collapse-item>

          <n-collapse-item title="章节五" name="5">
            <n-form-item label="标题" path="section5Title">
              <n-input v-model:value="form.section5Title" placeholder="请输入章节标题" />
            </n-form-item>
            <n-form-item label="内容" path="section5Content">
              <n-input v-model:value="form.section5Content" type="textarea" :rows="3" placeholder="请输入章节内容" />
            </n-form-item>
          </n-collapse-item>
        </n-collapse>

        <n-divider />

        <n-form-item label="联系邮箱" path="contactEmail">
          <n-input v-model:value="form.contactEmail" placeholder="请输入侵权投诉联系邮箱">
            <template #prefix>
              <n-icon><ChatboxEllipsesOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="页脚声明" path="briefDisclaimer">
          <n-input v-model:value="form.briefDisclaimer" type="textarea" :rows="2" placeholder="请输入页脚显示的简短免责声明" />
        </n-form-item>

        <n-divider />

        <n-form-item>
          <div class="form-actions">
            <n-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
              保存设置
            </n-button>
            <n-button @click="handleReset" size="large">重置</n-button>
          </div>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { ChatboxEllipsesOutline } from '@vicons/ionicons5'
import { getDisclaimer, updateDisclaimer } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
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
  dialog.warning({
    title: '提示',
    content: '重置将丢失所有未保存的更改，确定吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      location.reload()
    }
  })
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
    message.success('免责声明已更新')
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
</style>
