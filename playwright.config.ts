// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';
import * as path from "node:path";

export default defineConfig({
    testDir: './e2e',
    fullyParallel: false,
    workers: 1,

    timeout: 3000,
    reporter: "list",
    expect: {timeout: 1000},
    globalSetup: path.resolve(__dirname, './e2e/setup/setup-databases.ts'),
    globalTeardown: path.resolve(__dirname, './e2e/setup/global-teardown.ts'),
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],
    webServer: [
        {
            command: 'npm --prefix ./frontend run dev',
            url: 'http://localhost:5173',
            reuseExistingServer: !process.env.CI,
            stdout: 'pipe',
            stderr: 'pipe',
        },
        {
            command: './gradlew :backend:bootRunE2E --console=plain',
            url: 'http://localhost:10000/actuator/health',
            reuseExistingServer: !process.env.CI,
            stdout: 'pipe',
            stderr: 'pipe',
        }
    ],
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure'
    }
});