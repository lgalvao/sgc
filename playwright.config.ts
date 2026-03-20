// noinspection JSUnusedGlobalSymbols

if (process.env.NO_COLOR) {
    delete process.env.NO_COLOR;
}

import {defineConfig, devices} from '@playwright/test';

const workers = 1;
const frontendPort = Number.parseInt(process.env.E2E_FRONTEND_PORT || '5173', 10);

export default defineConfig({
    testDir: './e2e',
    timeout: 20_000,
    workers,
    expect: {timeout: 4_000},
    reporter: 'list',
    use: {
        baseURL: `http://localhost:${frontendPort}`,
        trace: 'off',
        screenshot: 'off'
    },
    webServer: {
        command: `node e2e/lifecycle.js`,
        url: `http://localhost:${frontendPort}`,
        reuseExistingServer: true,
        timeout: 120 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },

    projects: [{name: 'chromium', use: {...devices['Desktop chrome'], channel: 'chromium-headless-shell'}}],
});
