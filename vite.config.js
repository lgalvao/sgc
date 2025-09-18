import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import istanbul from 'vite-plugin-istanbul';

export default defineConfig({
    plugins: [
        vue(),
        istanbul({
            include: 'src/**/*.{ts,vue}',
            exclude: ['node_modules', 'tests', 'spec'],
            extension: ['.ts', '.vue']
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
            provider: 'istanbul',
            reporter: ['json'],
            reportsDirectory: '.nyc_output_unit',
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
    },
    build: {
        sourcemap: true, // Explicitly enable sourcemaps to hide the message
    },
});