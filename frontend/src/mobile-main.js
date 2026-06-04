import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './mobile/router'
import App from './mobile/App.vue'
import './assets/styles/theme.css'

const app = createApp(App)
app.use(createPinia())
app.use(ElementPlus)
app.use(router)
app.mount('#app')
