import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
    plugins: [
        vue(),
    ],
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:10000',
                changeOrigin: true
            }
        }
    },
    resolve: {alias: {'@': path.resolve(__dirname, './src')},},
    test: {
        globals: true,
        environment: 'jsdom',
        include: ['src/**/*.{test,spec}.{mjs,cjs,ts,mts,cts,jsx,tsx}'],
        exclude: ['spec/**/*'],
        setupFiles: ['./vitest.setup.ts'],
        coverage: {
            provider: 'v8',
            reporter: ['json', 'html', 'text'],
            reportsDirectory: 'coverage',
            include: ['src/**/*.{ts,vue}'],
            exclude: [
                'node_modules/',
                'spec/**',
                'tests/**',
                'src/**/*.spec.ts',
                'src/**/*.test.ts',
                'src/mocks/**/*.json',
                'src/main.ts',
                'src/router.ts',
                'src/views/**'
            ],
            all: false,
        },
    },
    define: {
        __VUE_PROD_DEVTOOLS__: false,
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false
    },
    build: {
        sourcemap: true
    },
});