import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5176,
        proxy: {
            // In questa fase NON proxiemo le route applicative come /login o /user/*:
            // sono gestite dal router React nel frontend.
            // Se in futuro vorrai usare percorsi relativi per le API (es. /api/*),
            // potrai aggiungere solo quel prefisso qui.
            // Esempio:
            // '/api': {
            //   target: 'http://localhost:8080',
            //   changeOrigin: true,
            //   secure: false,
            // },
        },
    },
})
