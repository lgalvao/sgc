import {defineConfig} from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import tsconfigPaths from 'vite-tsconfig-paths';
import {playwright} from '@vitest/browser-playwright';

export default defineConfig({
    plugins: [vue() as any, tsconfigPaths() as any],
    test: {
        browser: {
            enabled: true,
            provider: playwright(),
            instances: [
                {
                    browser: 'chromium',
                },
            ],
            headless: true,
            screenshotFailures: false,
        },
        include: ['src/visual-capture/capture.test.ts'],
    },
});
