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

const arquivosResetados = new Set<string>();

// Extend para adicionar cleanup automático
const test = base.extend<{
    cleanupAutomatico: ReturnType<typeof useProcessoCleanup>;
    resetPorArquivo: void;
}>({
    resetPorArquivo: [async ({request}, use, testInfo) => {
        if (!arquivosResetados.has(testInfo.file)) {
            await resetDatabase(request);
            arquivosResetados.add(testInfo.file);
        }
        await use();
    }, {auto: true}],
    // Cleanup automático configurado para cada teste
    cleanupAutomatico: async ({request}, use) => {
        const cleanup = useProcessoCleanup();

        // Use
        await use(cleanup);

        // Limpar após o teste
        await cleanup.limpar(request);
    }
});

export {expect} from './auth-fixtures.js';
export {test};
