import { test as base } from '@playwright/test';

export const test = base.extend({
    page: async ({ page }, use) => {
        // Listener para logs do console
        page.on('console', msg => {
            const text = msg.text();
            // Filtrar logs de conexão do Vite para reduzir ruído
            if (text.includes('[vite] connecting...') || text.includes('[vite] connected.')) {
                return;
            }
            const type = msg.type();
            console.log(`[BROWSER ${type.toUpperCase()}] ${text}`);
        });

        // Listener para erros não tratados da página
        page.on('pageerror', error => {
            console.error(`[BROWSER UNCAUGHT ERROR]`, error);
        });

        await use(page);
    },
});

export { expect } from '@playwright/test';
