import {test as base} from '@playwright/test';
// Use the project's centralized logger for consistent formatting
import logger from '../../frontend/src/utils/logger.js';
import {useProcessoCleanup} from '../hooks/hooks-limpeza.js';
import * as fs from 'node:fs';
import * as path from 'node:path';

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

export const test = base.extend<{
    cleanupAutomatico: ReturnType<typeof useProcessoCleanup>;
}>({
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

    page: async ({page}, use, testInfo) => {
        let ultimoRuidoAutenticacaoDetalhesEm = 0;
        const logs: string[] = [];

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

            const logEntry = `[${type.toUpperCase()}] ${expandedArgs || text}`;
            logs.push(logEntry);

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
            const stack = error && error.stack ? error.stack : error;
            logs.push(`[UNCAUGHT ERROR] ${stack}`);
            logger.error(`[BROWSER UNCAUGHT ERROR] ${stack}`);
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

                const networkLog = `[NETWORK ERROR] ${response.status()} ${response.request().method()} ${response.url()}`;
                logs.push(networkLog);
                logger.warn(networkLog);
                
                if (body && body.length < 2000) {
                    logs.push(`[NETWORK BODY] ${body}`);
                    logger.info(`[NETWORK BODY] ${body}`);
                } else if (body) {
                    logs.push(`[NETWORK BODY] ${body.substring(0, 500)}...`);
                    logger.info(`[NETWORK BODY] ${body.substring(0, 500)}...`);
                }
            }
        });

        await use(page);

        // Se o teste falhar, gera o arquivo error-context.md
        if (testInfo.status !== testInfo.expectedStatus && testInfo.status !== 'skipped') {
            const contextContent = [
                `# Contexto de Erro: ${testInfo.title}`,
                '',
                `**URL:** ${page.url()}`,
                `**Título:** ${await page.title()}`,
                '',
                '## Logs do Navegador',
                '```',
                ...logs,
                '```',
                '',
                '## HTML Page snapshot (Body)',
                '```html',
                await page.evaluate(() => document.body.innerHTML.substring(0, 5000)),
                '```'
            ].join('\n');

            if (!fs.existsSync(testInfo.outputDir)) {
                fs.mkdirSync(testInfo.outputDir, {recursive: true});
            }
            const filePath = path.join(testInfo.outputDir, 'error-context.md');
            fs.writeFileSync(filePath, contextContent);
            
            // Registra o arquivo como um anexo para aparecer no report do Playwright
            testInfo.attachments.push({
                name: 'error-context',
                contentType: 'text/markdown',
                path: filePath
            });
        }
    },

    cleanupAutomatico: async ({request}, use) => {
        const cleanup = useProcessoCleanup();
        await use(cleanup);
        await cleanup.limpar(request);
    },
});

export {expect} from '@playwright/test';
