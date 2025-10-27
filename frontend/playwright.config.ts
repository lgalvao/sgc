import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './e2e',

    // Critical: Run tests sequentially with no parallelism
    fullyParallel: false,
    workers: 1,

    // Disable retries to prevent cascading failures
    retries: 0,

    // Increase timeouts for resource-constrained environment
    timeout: 60000,
    expect: {
        timeout: 10000
    },

    // Only use Chromium, skip other browsers
    projects: [
        {
            name: 'chromium',
            use: {
                ...devices['Desktop Chrome'],
                // Reduce browser overhead
                launchOptions: {
                    args: [
                        '--disable-dev-shm-usage',
                        '--disable-blink-features=AutomationControlled',
                        '--no-sandbox',
                        '--disable-setuid-sandbox',
                        '--disable-gpu',
                        '--disable-software-rasterizer',
                        '--disable-extensions',
                        '--disable-background-networking',
                        '--disable-background-timer-throttling',
                        '--disable-backgrounding-occluded-windows',
                        '--disable-breakpad',
                        '--disable-component-extensions-with-background-pages',
                        '--disable-features=TranslateUI',
                        '--disable-ipc-flooding-protection',
                        '--disable-renderer-backgrounding',
                        '--force-color-profile=srgb',
                        '--metrics-recording-only',
                        '--mute-audio',
                        '--no-first-run',
                        '--safebrowsing-disable-auto-update',
                        '--single-process',
                    ]
                }
            },
        },
    ],

    // Playwright will start the frontend dev server, but the backend must be started manually.
    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173',
        timeout: 20000, // 20 seconds for Vite to start
        reuseExistingServer: !process.env.CI,
    },

    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure', // Keep traces for failed tests
        video: 'off', // Disable video recording
        screenshot: 'only-on-failure',
    },
});
