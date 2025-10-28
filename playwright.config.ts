import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: false,
    workers: 1,
    retries: 0,
    timeout: 5000,
    expect: {
        timeout: 1000
    },
    projects: [
        {
            name: 'chromium',
            use: {
                ...devices['Desktop Chrome']
            },
        },
    ],

    webServer: {
        command: 'npm run dev --prefix frontend',
        url: 'http://localhost:5173',
        timeout: 20000,
        reuseExistingServer: true
    },

    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure',
        video: 'off',
        screenshot: 'only-on-failure',
    },
});
