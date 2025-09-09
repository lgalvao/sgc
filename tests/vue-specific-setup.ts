import '../spec/global.d.ts';
import {ErrorReporter} from './utils/error-reporter';

// tests/vue-specific-setup.js - Setup específico para Vue 3
import {test as base} from '@playwright/test'; // Adicionar importação de Page

export const vueTest = base.extend({
    page: async ({page}, use) => {
        // Instanciar ErrorReporter
        const errorReporter = new ErrorReporter();

        // Intercepta erros específicos do Vue 3
        await page.addInitScript(() => {
            // Captura erros do Vue antes mesmo da página carregar
            const originalConsoleError = console.error;
            console.error = function (...args) {
                // Marca erros como vindos do Vue para fácil identificação
                if (args.some(arg =>
                    typeof arg === 'string' &&
                    (arg.includes('[Vue') || arg.includes('vue') || arg.includes('component'))
                )) {
                    errorReporter.addConsoleError(`[VUE] ${args.join(' ')}`, 'Console');
                    originalConsoleError.apply(console, ['🔥 VUE:', ...args]);
                } else {
                    errorReporter.addConsoleError(args.join(' '), 'Console');
                    originalConsoleError.apply(console, args);
                }
            };

            // Intercepta warnings do Vue
            const originalConsoleWarn = console.warn;
            console.warn = function (...args) {
                if (args.some(arg =>
                    typeof arg === 'string' &&
                    (arg.includes('[Vue') || arg.includes('vue'))
                )) {
                    originalConsoleWarn.apply(console, ['⚠️  VUE WARNING:', ...args]);
                } else {
                    originalConsoleWarn.apply(console, args);
                }
            };
        });

        // Aguarda Vue estar pronto antes de qualquer interação
        /* eslint-disable @typescript-eslint/no-explicit-any */
        await page.addInitScript(() => {
            (window as any).waitForVue = () => {
                return new Promise((resolve: (value: void) => void) => { // Tipar resolve
                    if ((window as any).Vue || (window as any).__VUE__) {
                    } else {
                        const checkVue = setInterval(() => {
                            if ((window as any).Vue || (window as any).__VUE__) {
                                clearInterval(checkVue);
                                resolve();
                            }
                        }, 100);
                    }
                });
            };
        });
        /* eslint-enable @typescript-eslint/no-explicit-any */

        const vueErrors: string[] = []; // Tipar
        const hydrationErrors: string[] = []; // Tipar

        page.on('console', msg => {
            const text = msg.text();

            // Detecta erros de hidratação do Vue 3
            if (text.includes('hydration') || text.includes('Hydration')) {
                errorReporter.addVueError(`[HYDRATION] ${text}`, 'Hydration');
            }
            // Outros erros do Vue
            else if (text.includes('[Vue') || (text.includes('vue') && msg.type() === 'error')) {
                errorReporter.addVueError(`[VUE] ${text}`, 'Vue');
            }
            // Captura erros gerais de console que não são do Vue
            else if (msg.type() === 'error') {
                errorReporter.addConsoleError(text, 'Page Console');
            }
        });

        // Captura erros de JavaScript não tratados na página
        page.on('pageerror', error => {
            errorReporter.addJavaScriptError(error);
        });

        await use(page);

        // Gerar relatório de erros no final do teste
        errorReporter.generateReport();
    },
});