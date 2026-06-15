// noinspection JSUnusedGlobalSymbols

if (process.env.NO_COLOR) {
    delete process.env.NO_COLOR;
}

import {defineConfig, devices} from '@playwright/test';

const frontendPort = Number.parseInt(process.env.E2E_FRONTEND_PORT || '5173', 10);

export default defineConfig({
    testDir: './',
    timeout: 20_000,
    workers: 1,
    reporter: 'dot',
    use: {
        baseURL: `http://localhost:${frontendPort}`,
        trace: 'off',
        screenshot: 'off'
    },
    webServer: {
        command: `node lifecycle.js`,
        env: {
            ...process.env,
            SGC_PERFIL: 'e2e',
            SGC_LIFECYCLE_REUTILIZAR_EXISTENTE: 'off',
            VITE_FEEDBACK_WIDGET: 'true'
        },
        url: `http://localhost:${frontendPort}`,
        reuseExistingServer: false,
        timeout: 300 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },

    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop chrome'], channel: 'chromium-headless-shell' },
        },
        {
            name: 'firefox',
            use: { ...devices['Desktop Firefox'] },
        },
        {
            name: 'android',
            use: { ...devices['Pixel 10'] },
        },
        {
            name: 'ios',
            use: { ...devices['iPhone 17'] },
        },
    ],
});
