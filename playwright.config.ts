// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';
import {vueTest} from './e2e/support/vue-specific-setup';

export default defineConfig({
    testMatch: /.*\.spec\.ts/,
    timeout: 12000,
    testDir: './e2e',
    fullyParallel: true,
    reporter: "dot",
    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 120 * 1000,
    },
    globalSetup: './e2e/support/global-setup.ts',
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
