// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    timeout: 7000,
    testDir: './spec',
    fullyParallel: true,

    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 60000,
    },
    use: {
        baseURL: 'http://localhost:5173/',
        trace: 'on-first-retry',
        actionTimeout: 5000,
        navigationTimeout: 5000,
    },
    projects: [{
        name: 'chromium',
        use: {...devices['Desktop Chrome']},
    }],
});
