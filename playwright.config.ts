// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true, // Habilitado: testes rodam em paralelo
    timeout: 10000,
    reporter: "dot",
    expect: {timeout: 5000},
    workers: undefined, // Permite ao Playwright determinar o número de workers
    globalSetup: require.resolve('./e2e/setup/setup-databases'),
    globalTeardown: require.resolve('./e2e/setup/setup-databases'),
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],

    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173',
        reuseExistingServer: !process.env.CI,
        cwd: './frontend'
    },

    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure'
    }
});