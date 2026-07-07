import { createApp } from 'vue'
import naive from 'naive-ui'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/styles/global.css'

const app = createApp(App)

app.use(naive)
app.use(createPinia())
app.use(router)
app.mount('#app')
