import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
    plugins: [vue()],
    resolve: {alias: {'@': path.resolve(__dirname, './src')},},
    test: { // Adicionar esta seção para Vitest
        globals: true,
        environment: 'jsdom',
        include: ['src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'], // Incluir apenas arquivos .test.ts ou .spec.ts dentro de src/
        setupFiles: ['./vitest.setup.ts'], // Adicionar um arquivo de setup para mocks globais
    },
});