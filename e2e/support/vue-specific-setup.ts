import { Page } from '@playwright/test';
import { test } from './fixtures'; // Use nossa fixture com cleanDatabase
import { existsSync, mkdirSync, writeFileSync } from 'fs';
import { join } from 'path';

// Definição de uma interface para a janela com propriedades customizadas
interface CustomWindow extends Window {
    Vue?: unknown;
    __VUE__?: unknown;
    waitForVue?: () => Promise<void>;
    __coverage__?: any;
}

declare const window: CustomWindow;

export const vueTest = test.extend<{ page: Page }>({
    page: async ({ page }, use, _testInfo) => {

        await page.addInitScript(() => {
            window.waitForVue = () => {
                return new Promise<void>((resolve) => {
                    if (window.Vue || window.__VUE__) {
                        resolve();
                    } else {
                        const checkVue = setInterval(() => {
                            if (window.Vue || window.__VUE__) {
                                clearInterval(checkVue);
                                resolve();
                            }
                        }, 100);
                    }
                });
            };
        });

        // Forward console logs from the browser to the terminal
        // Forward console logs from the browser to the terminal, filtering out noise
        page.on('console', msg => {
            const text = msg.text();
            if (msg.type() === 'debug' || text.includes('[vite]')) {
                return;
            }
            console.log(`[Browser ${msg.type()}] ${text}`);
        });

        // Forward unhandled exceptions to the terminal
        page.on('pageerror', err => {
            console.error(`[Browser PageError] ${err.message}`);
            console.error(err.stack);
        });

        await use(page);

        // Coletar cobertura após cada teste
        if (process.env.COLLECT_COVERAGE) { // Adicionar uma flag para ativar a coleta
            const coverage = await page.evaluate(() => (window as any).__coverage__);
            if (coverage) {
                const coveragePath = join(process.cwd(), '.nyc_output');
                if (!existsSync(coveragePath)) {
                    mkdirSync(coveragePath, { recursive: true });
                }
                const testTitle = test.info().title.replace(/[^a-zA-Z0-9]/g, '_'); // Nome do arquivo baseado no título do teste
                writeFileSync(join(coveragePath, `coverage-${testTitle}-${Date.now()}.json`), JSON.stringify(coverage));
            }
        }
    },
});