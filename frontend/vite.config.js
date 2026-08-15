import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    watch: {
      // 忽略编辑器产生的临时文件，避免 EBUSY 崩溃
      ignored: ['**/*.tmp', '**/*.tmpdir/**']
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
