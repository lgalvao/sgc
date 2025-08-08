// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './spec',
    fullyParallel: false,
    use: {baseURL: 'http://localhost:5173/',},
    projects: [{
        name: 'chromium',
        use: {...devices['Desktop Chrome']},
    }],
});
