import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  define: {
    global: 'window'
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // ★ 新增：代理 WebSocket 请求
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,           // 开启 WebSocket 代理
        rewriteWsOrigin: true
      }
    }
  }
})