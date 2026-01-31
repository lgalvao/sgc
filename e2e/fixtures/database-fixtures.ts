import {test as base} from './auth-fixtures.js';
import {resetDatabase} from '../hooks/hooks-limpeza.js';

/**
 * Fixture que garante reset de database antes de cada teste.
 *
 * Automatiza o padrão comum:
 * ```typescript
 * test.beforeAll(async ({request}) => await resetDatabase(request));
 * ```
 *
 * @example
 * ```typescript
 * import {test, expect} from './fixtures/database-fixtures.js';
 *
 * test('Deve criar processo em banco limpo', async ({page, autenticadoComoAdmin}) => {
 *   // Database já foi resetada automaticamente!
 *   await page.getByTestId('btn-painel-criar-processo').click();
 *   // ...
 * });
 * ```
 */
export const test = base;

let lastResetFile: string | undefined;

// Hook para reset de database (uma vez por arquivo)
test.beforeEach(async ({request}, testInfo) => {
    if (lastResetFile !== testInfo.file) {
        await resetDatabase(request);
        lastResetFile = testInfo.file;
    }
});

export {expect} from './auth-fixtures.js';
