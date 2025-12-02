// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    timeout: 300_000,
    expect: {timeout: 30_000},
    forbidOnly: !!process.env.CI,
    workers: 1,
    reporter: 'dot',
    use: {
        baseURL: 'http://localhost:5173'
    },
    webServer: {
        command: 'node e2e/lifecycle.js',
        url: 'http://localhost:5173',
        reuseExistingServer: !process.env.CI,
        timeout: 300 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome'], channel: 'chromium-headless-shell'}}],
});
