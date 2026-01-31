import {test as base} from './auth-fixtures';
import {resetDatabase} from '../hooks/hooks-limpeza';

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
 * import {test, expect} from './fixtures/database-fixtures';
 *
 * test('Deve criar processo em banco limpo', async ({page, autenticadoComoAdmin}) => {
 *   // Database já foi resetada automaticamente!
 *   await page.getByTestId('btn-painel-criar-processo').click();
 *   // ...
 * });
 * ```
 */
export const test = base;

// Hook global para reset de database antes de todos os testes
test.beforeAll(async ({request}) => {
    await resetDatabase(request);
});

export {expect} from './auth-fixtures';
