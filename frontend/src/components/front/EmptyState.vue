<template>
  <div class="empty-state">
    <span class="empty-dash" aria-hidden="true">&mdash;</span>
    <p>{{ text }}</p>
    <div v-if="$slots.default" class="empty-action"><slot /></div>
  </div>
</template>
<script setup>
defineProps({ text: { type: String, default: '暂无内容' } })
</script>
<style scoped>
.empty-state{display:grid;min-height:238px;place-content:center;color:#87918b;text-align:center;background:rgba(255,253,248,.55);border:1px dashed var(--border)}.empty-dash{color:#c39659;font-size:36px;line-height:1}.empty-state p{margin:8px 0 0;font:13px 'PingFang SC','Microsoft YaHei',sans-serif}.empty-action{margin-top:18px}
</style>
