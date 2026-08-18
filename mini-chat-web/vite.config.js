import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  define: {
    // 将 global 定义为浏览器全局对象
    global: 'globalThis',
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})
