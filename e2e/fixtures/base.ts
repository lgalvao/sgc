import {test as base} from '@playwright/test';
// Use the project's centralized logger for consistent formatting
import logger from '../../frontend/src/utils/logger.js';

function obterBaseUrlWorker(_workerIndex: number): string {
    const portaFrontend = Number.parseInt(process.env.E2E_FRONTEND_PORT || '5173', 10);
    return `http://localhost:${portaFrontend}`;
}

function ehErroEsperadoAutenticacao(url: string, status?: number): boolean {
    return status === 401 && /\/api\/usuarios\/autorizar$/.test(url);
}

function ehErroEsperadoImportacaoDuplicada(url: string, status?: number, method?: string, body?: string): boolean {
    return status === 422
        && method === 'POST'
        && /\/api\/subprocessos\/\d+\/importar-atividades$/.test(url)
        && !!body
        && body.includes('"code":"VALIDACAO"')
        && body.includes('já existentes no cadastro');
}

function ehRuidoAutenticacaoEmDetalhes(url: string, status?: number, method?: string): boolean {
    return status === 401
        && method === 'GET'
        && /\/api\/processos\/\d+\/detalhes$/.test(url);
}

export const test = base.extend({
    context: async ({browser}, use, testInfo) => {
        const baseURL = obterBaseUrlWorker(testInfo.parallelIndex);
        const context = await browser.newContext({
            baseURL,
            extraHTTPHeaders: {
                'x-e2e-worker': String(testInfo.parallelIndex)
            }
        });
        await use(context);
        await context.close();
    },

    request: async ({playwright}, use, testInfo) => {
        const baseURL = obterBaseUrlWorker(testInfo.parallelIndex);
        const request = await playwright.request.newContext({
            baseURL,
            extraHTTPHeaders: {
                'x-e2e-worker': String(testInfo.parallelIndex)
            }
        });
        await use(request);
        await request.dispose();
    },

        page: async ({page}, use) => {
        let ultimoRuidoAutenticacaoDetalhesEm = 0;

        // Listener para logs do console
        page.on('console', async msg => {
            const text = msg.text();
            const locationUrl = msg.location().url || '';
            // Filtrar logs de conexão do Vite para reduzir ruído
            if (text.includes('[vite] connecting...') || text.includes('[vite] connected.')) {
                return;
            }

            if (text.includes('Failed to load resource: the server responded with a status of 401')
                && /\/api\/usuarios\/autorizar$/.test(locationUrl)) {
                return;
            }

            if (text.includes('Failed to load resource: the server responded with a status of 422')
                && /\/api\/subprocessos\/\d+\/importar-atividades$/.test(locationUrl)) {
                return;
            }

            if (text.includes('Failed to load resource: the server responded with a status of 401')
                && /\/api\/processos\/\d+\/detalhes$/.test(locationUrl)) {
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

            if (type === 'error'
                && text.includes('%cerror')
                && Date.now() - ultimoRuidoAutenticacaoDetalhesEm < 2_000
                && (expandedArgs.includes('"name":"AxiosError"') || expandedArgs === 'null')) {
                return;
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
                if (ehErroEsperadoAutenticacao(response.url(), response.status())) {
                    return;
                }

                let body: string;
                try {
                    body = await response.text();
                } catch (e) {
                    body = '[Erro ao ler corpo]';
                }

                if (ehErroEsperadoImportacaoDuplicada(response.url(), response.status(), response.request().method(), body)) {
                    logger.info(`[NETWORK EXPECTED] ${response.status()} ${response.request().method()} ${response.url()}`);
                    return;
                }

                if (ehRuidoAutenticacaoEmDetalhes(response.url(), response.status(), response.request().method())) {
                    ultimoRuidoAutenticacaoDetalhesEm = Date.now();
                    logger.info(`[NETWORK EXPECTED] ${response.status()} ${response.request().method()} ${response.url()}`);
                    return;
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

export {expect} from '@playwright/test';
