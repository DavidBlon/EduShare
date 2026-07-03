<template>
  <div class="download-log">
    <div class="page-header">
      <div class="header-left">
        <h2>下载日志</h2>
        <span class="header-desc">记录用户的下载行为，包含IP地址和时间</span>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="logs" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="resourceTitle" label="资源名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="resourceId" label="资源ID" width="80" align="center" />
        <el-table-column prop="ipAddress" label="IP 地址" width="160" align="center">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.ipAddress || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下载时间" width="180" align="center" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDownloadLogList } from '@/api/downloadLog'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

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
