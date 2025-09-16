// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';
import {vueTest} from './tests/vue-specific-setup';
import path from 'path'; // Importar path
import {fileURLToPath} from 'url'; // Importar fileURLToPath

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default defineConfig({
    testMatch: /.*\.spec\.ts/,
    timeout: 12000,
    testDir: './spec',
    workers: 15,
    fullyParallel: true,
    reporter: "dot",
    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:5173/',
        reuseExistingServer: true,
        timeout: 60000,
    },
    use: {
        baseURL: 'http://localhost:5173/',
        trace: 'on-first-retry',
        actionTimeout: 7000,
        navigationTimeout: 7000,
    },
    projects: [{
        name: 'chromium',
        use: {
            ...devices['Desktop Chrome'],
            ...vueTest,
        },
    }],
    globalTeardown: path.resolve(__dirname, './tests/global-teardown.ts'), // Usar path.resolve
});
