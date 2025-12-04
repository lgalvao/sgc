// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    timeout: 30_000, // Aumentado para fixtures via API
    workers: process.env.CI ? 1 : 2, // Paralelização habilitada localmente
    fullyParallel: true,
    expect: {timeout: 5_000}, // Aumentado de 2s para 5s
    forbidOnly: !!process.env.CI,
    reporter: [
        ['dot'],
        ['json', { outputFile: 'test-results/results.json' }],
        ['html', { open: 'never' }]
    ],
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure'
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
