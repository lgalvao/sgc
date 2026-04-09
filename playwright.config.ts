// noinspection JSUnusedGlobalSymbols

if (process.env.NO_COLOR) {
    delete process.env.NO_COLOR;
}

import {defineConfig, devices} from '@playwright/test';

const frontendPort = Number.parseInt(process.env.E2E_FRONTEND_PORT || '5173', 10);

export default defineConfig({
    testDir: './e2e',
    timeout: 15_000,
    workers: 1,
    reporter: 'list',
    use: {
        baseURL: `http://localhost:${frontendPort}`,
        trace: 'off',
        screenshot: 'off'
    },
    webServer: {
        command: `node e2e/lifecycle.js`,
        env: {
            ...process.env,
            SGC_LIFECYCLE_PROFILE: 'e2e'
        },
        url: `http://localhost:${frontendPort}`,
        reuseExistingServer: true,
        timeout: 300 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },

    projects: [{name: 'chromium', use: {...devices['Desktop chrome'], channel: 'chromium-headless-shell'}}],
});
