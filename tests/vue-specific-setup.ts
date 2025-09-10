import '../spec/global.d.ts';
import {ErrorReporter} from './utils/error-reporter';
import {Page, test as base} from '@playwright/test';

// Definição de uma interface para a janela com propriedades customizadas
interface CustomWindow extends Window {
    Vue?: unknown;
    __VUE__?: unknown;
    waitForVue?: () => Promise<void>;

    postMessage(message: any, targetOrigin: string, transfer?: Transferable[]): void;

    postMessage(message: any, options?: WindowPostMessageOptions): void;
}

declare const window: CustomWindow;

export const vueTest = base.extend<{ page: Page }>({
    page: async ({page}, use) => {
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

        errorReporter.generateReport();
    },
});