import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5176,
        proxy: {
            // Inoltra le richieste che iniziano con /user al backend
            '/user': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            },
            // Inoltra le richieste di login (usato per i redirect di Spring Security)
            '/login': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            },
        },
    },
})
