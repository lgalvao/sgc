import '../spec/global.d.ts';

// tests/vue-specific-setup.js - Setup específico para Vue 3
import {test as base} from '@playwright/test'; // Adicionar importação de Page

export const vueTest = base.extend({
    page: async ({page}, use) => {
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
                    originalConsoleError.apply(console, ['🔥 VUE:', ...args]);
                } else {
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
                hydrationErrors.push(text);
                console.error(`💧 VUE HYDRATION ERROR: ${text}`);
            }
            // Outros erros do Vue
            else if (text.includes('[Vue') || (text.includes('vue') && msg.type() === 'error')) {
                vueErrors.push(text);
                console.error(`🔥 VUE ERROR: ${text}`);
            }
        });

        await use(page);

        // Relatório específico do Vue
        if (vueErrors.length > 0 || hydrationErrors.length > 0) {
            console.error('\n🎯 RELATÓRIO ESPECÍFICO VUE 3');
            console.error('='.repeat(50));

            if (hydrationErrors.length > 0) {
                console.error(`\n💧 ERROS DE HIDRATAÇÃO (${hydrationErrors.length}):`);
                hydrationErrors.forEach((error, i) => {
                    console.error(`  ${i + 1}. ${error}`);
                });
                console.error('\n💡 DICA: Erros de hidratação geralmente indicam:');
                console.error('   - Diferenças entre servidor e cliente');
                console.error('   - Componentes que dependem de APIs do navegador');
                console.error('   - Estados reativos não sincronizados');
            }

            if (vueErrors.length > 0) {
                console.error(`\n🔥 OUTROS ERROS VUE (${vueErrors.length}):`);
                vueErrors.forEach((error, i) => {
                    console.error(`  ${i + 1}. ${error}`);
                });
            }

            console.error('\n' + '='.repeat(50) + '\n');
        }
    },
});