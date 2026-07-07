<template>
  <div class="download-log">
    <div class="page-header">
      <div class="header-left">
        <h2>下载日志</h2>
        <span class="header-desc">记录用户的下载行为，包含IP地址和时间</span>
      </div>
    </div>

    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="logs"
        :loading="loading"
        :bordered="true"
        :single-line="false"
        size="small"
      />
      <div class="pagination-wrapper">
        <n-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :page-count="pageCount"
          :page-sizes="[10, 20, 50]"
          show-size-picker
          @update:page="loadLogs"
          @update:page-size="loadLogs"
        />
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, computed, onMounted } from 'vue'
import { NTag } from 'naive-ui'
import { getDownloadLogList } from '@/api/downloadLog'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const columns = [
  { title: '#', key: 'index', width: 55, align: 'center', render: (_, index) => index + 1 },
  { title: '资源名称', key: 'resourceTitle', minWidth: 200, ellipsis: { tooltip: true } },
  { title: '资源ID', key: 'resourceId', width: 80, align: 'center' },
  { title: 'IP 地址', key: 'ipAddress', width: 160, align: 'center',
    render: (row) => h(NTag, { size: 'small' }, { default: () => row.ipAddress || '—' })
  },
  { title: '下载时间', key: 'createdAt', width: 180, align: 'center' }
]

onMounted(loadLogs)

async function loadLogs() {
  loading.value = true
  try {
    const res = await getDownloadLogList({ page: page.value, pageSize: pageSize.value })
    logs.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
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
.header-desc { font-size:13px; color:var(--text-secondary); }
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
