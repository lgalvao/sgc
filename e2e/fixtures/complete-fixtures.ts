import {test as base} from './processo-fixtures.js';
import {resetDatabase, useProcessoCleanup} from '../hooks/hooks-limpeza.js';

/**
 * Fixture completa que combina:
 * - Autenticação (de auth-fixtures)
 * - Reset de database
 * - Cleanup automático de processos
 *
 * Ideal para a maioria dos testes E2E que precisam de setup completo.
 *
 * @example
 * ```typescript
 * import {test, expect} from './fixtures/complete-fixtures.js';
 *
 * test('Deve criar e editar processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
 *   // Database resetada, usuário logado, cleanup automático!
 *   const descricao = 'Processo Teste';
 *   await criarProcesso(page, {descricao, ...});
 *   
 *   // Capturar código para cleanup
 *   const codigo = await capturarCodigoProcesso(page);
 *   cleanupAutomatico.registrar(codigo);
 * });
 * ```
 */

// Adicionar reset de database como beforeEach (uma vez por arquivo)
const test = base;

let lastResetFile: string | undefined;

test.beforeEach(async ({request}, testInfo) => {
    // Garante que o banco seja resetado apenas na primeira vez que o arquivo de teste é executado
    // Se o worker for reiniciado (ex: após timeout), lastResetFile estará undefined e resetará novamente.
    if (lastResetFile !== testInfo.file) {
        await resetDatabase(request);
        lastResetFile = testInfo.file;
    }
});

// Extend para adicionar cleanup automático
export const testWithCleanup = test.extend<{
    cleanupAutomatico: ReturnType<typeof useProcessoCleanup>;
}>({
    // Cleanup automático configurado para cada teste
    cleanupAutomatico: async ({}, use, testInfo) => {
        const cleanup = useProcessoCleanup();
        
        // Use
        await use(cleanup);
        
        // Teardown: limpar processos registrados
        const {request} = await import('@playwright/test');
        const ctx = await request.newContext();
        await cleanup.limpar(ctx);
        await ctx.dispose();
    }
});

// Export alias
export {testWithCleanup as test};
export {expect} from './auth-fixtures.js';
