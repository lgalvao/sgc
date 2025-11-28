import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 2 : 0,
    workers: process.env.CI ? 1 : undefined,
    reporter: 'dot',
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'on-first-retry',
    },
    webServer: {
        command: 'node e2e/lifecycle.js',
        url: 'http://localhost:5173',
        reuseExistingServer: !process.env.CI,
        timeout: 300 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],
});
