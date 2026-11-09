import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    // overwrite default .html entry
    // input : '/path/to/main.js',
    server: {
        proxy: {
            '/vigiechiro': {
                target: 'https://vigiechiro.herokuapp.com',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/vigiechiro/, ''),
            },
            // TODO : This is only for local hosting fix before release
            '/api': 'http://localhost:7070'
        },
    },
})