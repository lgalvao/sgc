// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';
import {vueTest} from './tests/vue-specific-setup';

export default defineConfig({
    testMatch: /.*\.spec\.ts/,
    timeout: 10000,
    testDir: './spec',
    fullyParallel: true,
    reporter: "dot",
    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 120 * 1000,
    },
    use: {
        baseURL: 'http://localhost:5173/',
        trace: 'on-first-retry',
        actionTimeout: 2000,
        navigationTimeout: 2000,
    },
    projects: [{
        name: 'chromium',
        use: {
            ...devices['Desktop Chrome'],
            ...vueTest,
        },
    }]
});
