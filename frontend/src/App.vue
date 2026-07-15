<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { zhCN, dateZhCN } from 'naive-ui'

const themeOverrides = {
  common: {
    primaryColor: '#0f6d70',
    primaryColorHover: '#084b4e',
    primaryColorPressed: '#063f42',
    primaryColorSuppl: '#0f6d70',
    infoColor: '#0f6d70',
    successColor: '#36775d',
    warningColor: '#c17b25',
    errorColor: '#b84e44'
  }
}
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}
</style>
