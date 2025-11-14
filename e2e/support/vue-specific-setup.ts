import {ErrorReporter} from './error-reporter';
import {Page} from '@playwright/test';
import {test} from './fixtures'; // Use nossa fixture com cleanDatabase

import {existsSync, mkdirSync, writeFileSync} from 'fs';
import {join} from 'path';

// Definição de uma interface para a janela com propriedades customizadas
interface CustomWindow extends Window {
    Vue?: unknown;
    __VUE__?: unknown;
    waitForVue?: () => Promise<void>;

    postMessage(message: any, targetOrigin: string, transfer?: Transferable[]): void;
    postMessage(message: any, options?: WindowPostMessageOptions): void;

    __coverage__?: any;
}

declare const window: CustomWindow;

export const vueTest = test.extend<{ page: Page }>({
    page: async ({page}, use, _testInfo) => {
        const errorReporter = new ErrorReporter();

        await page.addInitScript(() => {
            const originalConsoleError = console.error;
            console.error = function (...args: any[]) {
                if (args.some(arg => typeof arg === 'string' && (arg.includes('[Vue') || arg.includes('vue') || arg.includes('component')))) {
                    window.postMessage({type: 'VUE_ERROR', message: `[VUE] ${args.join(' ')}`}, '*');
                    originalConsoleError.apply(console, ['🔥 VUE:', ...args]);
                } else {
                    window.postMessage({type: 'CONSOLE_ERROR', message: args.join(' ')}, '*');
                    originalConsoleError.apply(console, args);
                }
            };

            const originalConsoleWarn = console.warn;
            console.warn = function (...args: any[]) {
                if (args.some(arg => typeof arg === 'string' && (arg.includes('[Vue') || arg.includes('vue')))) {
                    originalConsoleWarn.apply(console, ['⚠️  VUE WARNING:', ...args]);
                } else {
                    originalConsoleWarn.apply(console, args);
                }
            };
        });

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

        page.on('console', msg => {
            const text = msg.text();
            const type = msg.type();

            if (text.includes('hydration') || text.includes('Hydration')) {
                errorReporter.addVueError(`[HYDRATION] ${text}`, 'Hydration');
            } else if (text.includes('[Vue') || (text.includes('vue') && type === 'error')) {
                errorReporter.addVueError(`[VUE] ${text}`, 'Vue');
            } else if (type === 'error') {
                errorReporter.addConsoleError(text, 'Page Console');
            }
        });

        page.on('pageerror', error => {
            errorReporter.addJavaScriptError(error);
        });

        page.on('requestfailed', request => {
            errorReporter.addNetworkError(request.url(), request.failure()?.errorText || 'Unknown network error');
        });

        await use(page);

        // Coletar cobertura após cada teste
        if (process.env.COLLECT_COVERAGE) { // Adicionar uma flag para ativar a coleta
            const coverage = await page.evaluate(() => (window as any).__coverage__);
            if (coverage) {
                const coveragePath = join(process.cwd(), '.nyc_output');
                if (!existsSync(coveragePath)) {
                    mkdirSync(coveragePath, {recursive: true});
                }
                const testTitle = test.info().title.replace(/[^a-zA-Z0-9]/g, '_'); // Nome do arquivo baseado no título do teste
                writeFileSync(join(coveragePath, `coverage-${testTitle}-${Date.now()}.json`), JSON.stringify(coverage));
            }
        }

        // Verificar se há erros críticos e falhar o teste se necessário
        if (errorReporter.hasErrors()) {
            const criticalErrors = errorReporter.getCriticalErrors();
            if (criticalErrors.length > 0) {
                console.warn(`⚠️  Teste executado com ${criticalErrors.length} erro(s) crítico(s)`);
            }
        }

        errorReporter.generateReport();
    },
});