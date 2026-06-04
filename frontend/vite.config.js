import { fileURLToPath, URL } from 'node:url'
import fs from 'node:fs'
import path from 'node:path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// 自动设备检测插件
// 手机/平板 UA → mobile.html
// 桌面 UA → index.html
function deviceDetectPlugin() {
  const mobileRE = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini|Mobile/i

  function isMobile(ua) {
    return mobileRE.test(ua)
  }

  function serveHtml(res, filePath) {
    const html = fs.readFileSync(filePath, 'utf-8')
    res.statusCode = 200
    res.setHeader('Content-Type', 'text/html')
    res.end(html)
  }

  return {
    name: 'device-detect',
    configureServer(server) {
      return () => {
        server.middlewares.use((req, res, next) => {
          const url = req.url || ''
          // API / 静态资源放行
          if (url.startsWith('/api') || url.startsWith('/uploads') || url.includes('.')) {
            return next()
          }
          const ua = req.headers['user-agent'] || ''
          if (isMobile(ua)) {
            return serveHtml(res, path.resolve(__dirname, 'mobile.html'))
          }
          next()
        })
      }
    }
  }
}

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    deviceDetectPlugin(),
  ],
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  build: {
    rollupOptions: {
      input: {
        main: fileURLToPath(new URL('./index.html', import.meta.url)),
        mobile: fileURLToPath(new URL('./mobile.html', import.meta.url)),
      }
    }
  }
})
