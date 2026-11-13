import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host : true,
    port: 5173,
    proxy: {
      '/vigiechiro': {
        target: 'https://vigiechiro.herokuapp.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/vigiechiro/, ''),
      },
      '/api': {
        target: 'http://localhost:7070',
        changeOrigin: true,
      }
    },
  },
})