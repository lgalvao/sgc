import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import istanbul from 'vite-plugin-istanbul'; // Importar o plugin

export default defineConfig({
    plugins: [
        vue(),
        istanbul({
            include: 'src/*',
            exclude: [
                'node_modules',
                'src/**/*.spec.ts',
                'src/**/*.test.ts',
                'src/mocks/**/*.json'
            ],
            extension: ['.js', '.ts', '.vue'],
            forceBuildInstrument: true,
        }),
    ],
    resolve: {alias: {'@': path.resolve(__dirname, './src')},},
    test: {
        globals: true,
        environment: 'jsdom',
        include: ['src/**/*.{test,spec}.{mjs,cjs,ts,mts,cts,jsx,tsx}'],
        exclude: ['spec/**/*'],
        setupFiles: ['./vitest.setup.ts'],
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html'],
            exclude: [
                'node_modules/',
                'spec/**',
                'tests/**',
                'src/**/*.spec.ts',
                'src/**/*.test.ts',
                'src/mocks/**/*.json',
                'src/main.ts',
                'src/router.ts'
            ],
        },
    },
    define: {
        __VUE_PROD_DEVTOOLS__: false,
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false
    }
});