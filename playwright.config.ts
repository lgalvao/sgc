// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

const workers = Number.parseInt(process.env.E2E_WORKERS || '1', 10);
const frontendPort = Number.parseInt(process.env.E2E_FRONTEND_PORT || '5173', 10);

export default defineConfig({
    testDir: './e2e',
    timeout: 15_000,
    workers,
    expect: {timeout: 3_000},
    reporter: 'list',
    use: {
        baseURL: `http://localhost:${frontendPort}`,
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure'
    },
    webServer: {
        command: `env -u NO_COLOR WORKER_COUNT=${workers} node e2e/lifecycle.js`,
        url: `http://localhost:${frontendPort}`,
        reuseExistingServer: true,
        timeout: 120 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },

    projects: [{name: 'chromium', use: {...devices['Desktop Chrome'], channel: 'chromium-headless-shell'}}],
});
