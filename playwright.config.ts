// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    retries: 0,
    timeout: 15000,
    expect: {timeout: 5000},
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],
    webServer: {
        command: 'cd frontend ; npm run dev',
        url: 'http://localhost:5173',
        timeout: 20000,
        reuseExistingServer: true
    },
    use: {baseURL: 'http://localhost:5173'}
});