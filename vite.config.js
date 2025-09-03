import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
    plugins: [vue()],
    resolve: {alias: {'@': path.resolve(__dirname, './src')},},
    test: {
        globals: true,
        environment: 'jsdom',
        include: ['src/**/*.{test,spec}.{mjs,cjs,ts,mts,cts,jsx,tsx}'], // Incluir apenas arquivos .test.ts ou .spec.ts dentro de src/
        exclude: ['spec/**/*'], // Excluir diretório spec/ que contém testes E2E do Playwright
        setupFiles: ['./vitest.setup.ts'], // Adicionar um arquivo de setup para mocks globais
    },
    // Configuração específica para testes com Vue
    define: {
        __VUE_PROD_DEVTOOLS__: false,
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false
    }
});