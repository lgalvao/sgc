import {defineConfig, devices} from '@playwright/test';
import {vueTest} from '~/support/vue-specific-setup';

// noinspection JSUnusedGlobalSymbols
export default defineConfig({
    testMatch: /.*\.spec\.ts/,
    timeout: 12000,
    testDir: './e2e',
    fullyParallel: true,
    reporter: "dot",
    globalSetup: './e2e/support/global-setup.ts',
    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 120 * 1000,
    },
    use: {
        baseURL: 'http://localhost:5173/',
        trace: 'on-first-retry',
        actionTimeout: 3000,
        navigationTimeout: 3000,
    },
    projects: [{
        name: 'chromium',
        use: {
            ...devices['Desktop Chrome'],
            ...vueTest,
        },
    }]
});
