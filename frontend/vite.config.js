import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/vigiechiro': {
                target: 'https://vigiechiro.herokuapp.com',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/vigiechiro/, ''),
            },
        },
    },
})