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
                    ]
                }
            },
        },
    ],

    // Let Playwright manage the backend lifecycle
    webServer: {
        command: 'cd /app && JAVA_OPTS="-Xmx512m -Xms256m" ./gradlew :backend:bootRun --args="--spring.profiles.active=jules"',
        url: 'http://localhost:8080',
        timeout: 120000, // 2 minutes for Spring Boot to start
        reuseExistingServer: false,
        stdout: 'pipe',
        stderr: 'pipe',
    },

    use: {
        baseURL: 'http://localhost:8080',
        trace: 'off', // Disable tracing to save resources
        video: 'off', // Disable video recording
        screenshot: 'only-on-failure',
    },
});
