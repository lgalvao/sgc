// noinspection JSUnusedGlobalSymbols

if (process.env.NO_COLOR) {
    delete process.env.NO_COLOR;
}

import {defineConfig, devices} from '@playwright/test';

const workers = 1;
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
        command: `node e2e/lifecycle.js`,
        url: `http://localhost:${frontendPort}`,
        reuseExistingServer: true,
        timeout: 120 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },

    projects: [{name: 'chromium', use: {...devices['Desktop Chrome'], channel: 'chromium-headless-shell'}}],
});
