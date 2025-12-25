import {test as base} from '@playwright/test';

export const test = base.extend({
    page: async ({ page }, use) => {
        // Listener para logs do console
        page.on('console', async msg => {
            const text = msg.text();
            // Filtrar logs de conexão do Vite para reduzir ruído
            if (text.includes('[vite] connecting...') || text.includes('[vite] connected.')) {
                return;
            }

            const type = msg.type();

            // Tenta expandir argumentos se forem objetos (ex: AxiosError)
            let expandedArgs = '';
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

            console.log(`[BROWSER ${type.toUpperCase()}] ${expandedArgs || text}`);
        });

        // Listener para erros não tratados da página
        page.on('pageerror', error => {
            console.error(`[BROWSER UNCAUGHT ERROR]`, error);
        });

        // Listener para falhas de rede (4xx, 5xx)
        page.on('response', async response => {
            if (response.status() >= 400) {
                let body = '';
                try {
                    body = await response.text();
                } catch (e) {
                    body = '[Erro ao ler corpo]';
                }

                console.log(`[NETWORK ERROR] ${response.status()} ${response.request().method()} ${response.url()}`);
                if (body && body.length < 2000) {
                    console.log(`[NETWORK BODY] ${body}`);
                } else if (body) {
                    console.log(`[NETWORK BODY] ${body.substring(0, 500)}...`);
                }
            }
        });

        await use(page);
    },
});

export { expect } from '@playwright/test';
