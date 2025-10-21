// noinspection JSUnusedGlobalSymbols
import {defineConfig, devices} from '@playwright/test';
import {vueTest} from '~/support/vue-specific-setup';

export default defineConfig({
    testMatch: /.*\.spec\.ts/,
    timeout: 60000, // Aumentado para 60 segundos
    testDir: './e2e',
    fullyParallel: true,
    reporter: "dot",
    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 120 * 1000,
    },
    use: {
        baseURL: 'http://localhost:5173/',
        trace: 'on-first-retry',
        actionTimeout: 2000,
        navigationTimeout: 30000, // Aumentado para 30 segundos
    },
    projects: [{
        name: 'chromium',
        use: {
            ...devices['Desktop Chrome'],
            ...vueTest,
        },
    }]
});
