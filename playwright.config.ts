// noinspection JSUnusedGlobalSymbols

import {defineConfig} from '@playwright/test';

export default defineConfig({
    testDir: './e2e/tests',
    use: {
        video: 'on',
        baseURL: 'http://localhost:8080',
    },
}); 