import { createApp } from 'vue'
import naive from 'naive-ui'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 自托管 Noto Serif SC（避免 Windows 回退到 SimSun），unicode-range 分包按需加载
import '@fontsource/noto-serif-sc/400.css'
import '@fontsource/noto-serif-sc/600.css'
import '@fontsource/noto-serif-sc/700.css'
import './assets/styles/global.css'

const app = createApp(App)

app.use(naive)
app.use(createPinia())
app.use(router)
app.mount('#app')
