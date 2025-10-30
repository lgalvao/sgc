// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    timeout: 30000,  // 30s for full test timeout (was 10s)
    expect: {timeout: 15000},  // 15s for assertions (was 5s)
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],
    webServer: {
        command: 'cd frontend ; npm run dev',
        url: 'http://localhost:5173',
        timeout: 20000,
        reuseExistingServer: true
    },
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure'
    }
});