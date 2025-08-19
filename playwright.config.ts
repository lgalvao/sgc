// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './spec',
    fullyParallel: false,
    use: {baseURL: 'http://localhost:5173/',},
    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 5000,
    },
    projects: [{
        name: 'chromium',
        use: {...devices['Desktop Chrome']},
    }],
});
