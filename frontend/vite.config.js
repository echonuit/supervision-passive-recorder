import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Notre backend Javalin (PORT=7070 par défaut) : /api/capteurs, etc.
      '/api': 'http://localhost:7070',
      // Vigie-Chiro, pour éviter le blocage CORS du navigateur en développement.
      '/vigiechiro': {
        target: 'https://vigiechiro.herokuapp.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/vigiechiro/, ''),
      },
    },
  },
})
