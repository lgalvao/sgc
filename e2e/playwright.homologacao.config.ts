import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Carrega as variáveis do arquivo .env.hom na raiz do projeto
dotenv.config({ path: path.resolve(__dirname, '../.env.hom') });

const baseURL = process.env.URL_ACESSO_HOM;

export default defineConfig({
    testDir: './homologacao',
    timeout: 90_000,
    expect: {
        timeout: 10_000,
    },
    fullyParallel: false,
    workers: 1,
    reporter: [
        ['list'],
        ['html', { outputFolder: 'playwright-report-hom', open: 'never' }]
    ],
    use: {
        baseURL: baseURL,
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure',
        ignoreHTTPSErrors: true,
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],
});
