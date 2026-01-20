import type {APIRequestContext} from '@playwright/test';

/**
 * Hook para gerenciar cleanup automático de processos criados durante testes E2E.
 *
 * Uso:
 * ```typescript
 * test.describe('Meus testes', () => {
 *     let cleanup: ReturnType<typeof useProcessoCleanup>;
 *
 *     test.beforeEach(() => {
 *         cleanup = useProcessoCleanup();
 *     });
 *
 *     test.afterEach(async ({ request }) => {
 *         await cleanup.limpar(request);
 *     });
 *
 *     test('Deve criar processo', async ({ page }) => {
 *         const processo = await criarProcesso(page, { ... });
 *         cleanup.registrar(processo.codigo); // Auto-cleanup ao final
 *     });
 * });
 * ```
 */
export function useProcessoCleanup() {
    const processosParaLimpar: number[] = [];

    return {
        /**
         * Registra um processo para ser removido ao final do teste
         */
        registrar: (codigo: number) => {
            if (!processosParaLimpar.includes(codigo)) processosParaLimpar.push(codigo);
        },

        /**
         * Remove todos os processos registrados
         */
        limpar: async (request: APIRequestContext) => {
            for (const codigo of processosParaLimpar) {
                try {
                    await request.post(`/e2e/processo/${codigo}/limpar`);
                } catch (error) {
                    console.warn(`Falha ao limpar processo ${codigo}:`, error);
                }
            }
            processosParaLimpar.length = 0;
        }
    };
}

/**
 * Hook para reset completo do banco de dados antes de todos os testes.
 *
 * Uso:
 * ```typescript
 * test.describe('Meus testes', () => {
 *     test.beforeAll(async ({ request }) => {
 *         await resetDatabase(request);
 *     });
 *
 *     test('Teste com banco limpo', async ({ page }) => {
 *         // Banco está no estado inicial do seed.sql
 *     });
 * });
 * ```
 */
export async function resetDatabase(request: APIRequestContext): Promise<void> {
    const response = await request.post('/e2e/reset-database');

    if (!response.ok()) {
        throw new Error(`Falha ao resetar banco de dados: ${response.status()} ${response.statusText()}`);
    }
}
