// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testMatch: /.*\.spec\.ts/,
    timeout: 15000,
    testDir: './spec',
    fullyParallel: true,
    reporter: 'dot',

    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 60000,
    },
    use: {
        baseURL: 'http://localhost:5173/',
        trace: 'on-first-retry',
        actionTimeout: 10000,
        navigationTimeout: 10000,
    },
    projects: [{
        name: 'chromium',
        use: {...devices['Desktop Chrome']},
    }],
});
