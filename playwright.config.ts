// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    timeout: 20_000,
    workers: 1,
    expect: {timeout: 5_000},
    reporter: 'line',
    use: {
        baseURL: 'http://localhost:5173',
        screenshot: 'only-on-failure'
    },
    webServer: {
        command: 'node e2e/lifecycle.js',
        url: 'http://localhost:5173',
        reuseExistingServer: true,
        timeout: 300 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome'], channel: 'chromium-headless-shell'}}],
});
