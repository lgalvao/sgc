import {test as base} from '@playwright/test';
// Use the project's centralized logger for consistent formatting
import logger from '../../frontend/src/utils/logger.js';

export const test = base.extend({
    page: async ({ page }, use) => {
        // Listener para logs do console
        page.on('console', async msg => {
            const text = msg.text();
            // Filtrar logs de conexão do Vite para reduzir ruído
            if (text.includes('[vite] connecting...') || text.includes('[vite] connected.')) {
                return;
            }

            const type = String(msg.type());

            // Tenta expandir argumentos se forem objetos (ex: AxiosError)
            let expandedArgs: string;
            try {
                const args = await Promise.all(msg.args().map(arg => arg.jsonValue().catch(() => null)));
                expandedArgs = args.map(a => {
                    if (a && typeof a === 'object') {
                        // Se for um erro do Axios, tenta extrair detalhes úteis
                        if (a.code === 'ERR_BAD_REQUEST' || a.isAxiosError || (a.config && a.response)) {
                            return JSON.stringify({
                                message: a.message,
                                url: a.config?.url,
                                method: a.config?.method,
                                status: a.response?.status,
                                responseData: a.response?.data
                            });
                        }
                        return JSON.stringify(a);
                    }
                    return String(a);
                }).join(' ');
            } catch (e) {
                expandedArgs = text; // Fallback
            }

            // Map Playwright console types to logger methods
            if (type === 'error') {
                logger.error(`[BROWSER ${type.toUpperCase()}] ${expandedArgs || text}`);
            } else if (type === 'warning' || type === 'warn') {
                logger.warn(`[BROWSER ${type.toUpperCase()}] ${expandedArgs || text}`);
            } else {
                logger.info(`[BROWSER ${type.toUpperCase()}] ${expandedArgs || text}`);
            }
        });

        // Listener para erros não tratados da página
        page.on('pageerror', error => {
            logger.error(`[BROWSER UNCAUGHT ERROR] ${error && error.stack ? error.stack : error}`);
        });

        // Listener para falhas de rede (4xx, 5xx)
        page.on('response', async response => {
            if (response.status() >= 400) {
                let body: string;
                try {
                    body = await response.text();
                } catch (e) {
                    body = '[Erro ao ler corpo]';
                }

                logger.warn(`[NETWORK ERROR] ${response.status()} ${response.request().method()} ${response.url()}`);
                if (body && body.length < 2000) {
                    logger.info(`[NETWORK BODY] ${body}`);
                } else if (body) {
                    logger.info(`[NETWORK BODY] ${body.substring(0, 500)}...`);
                }
            }
        });

        await use(page);
    },
});

export { expect } from '@playwright/test';
