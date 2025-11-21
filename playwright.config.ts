// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    timeout: 10000,
    reporter: "dot",
    expect: {timeout: 5000},
    globalSetup: require.resolve('./e2e/setup/setup-databases'),
    globalTeardown: require.resolve('./e2e/setup/setup-databases'),
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],

    webServer: {
        command: 'npm --prefix ./frontend run dev',
        url: 'http://localhost:5173',
        reuseExistingServer: !process.env.CI,
    },
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure'
    }
});