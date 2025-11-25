// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: false,
    workers: 1,
    timeout: 10000,
    reporter: "line",
    expect: {timeout: 5000},
    globalSetup: require.resolve('./e2e/setup/setup-databases'),
    globalTeardown: require.resolve('./e2e/setup/global-teardown'),
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],

    webServer: [
        {
            command: 'npm --prefix ./frontend run dev',
            url: 'http://localhost:5173',
            reuseExistingServer: !process.env.CI,
        },
        {
            command: './gradlew :backend:bootRunE2E',
            url: 'http://localhost:10000/actuator/health',
            reuseExistingServer: !process.env.CI,
            timeout: 120 * 1000,
        }
    ],
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure'
    }
});