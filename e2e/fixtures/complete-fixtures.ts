import {test as base} from './processo-fixtures.js';
import {resetDatabase} from '../hooks/hooks-limpeza.js';

/**
 * Fixture completa que combina:
 * - Autenticação (de auth-fixtures)
 * - Reset de database por arquivo
 *
 * Ideal para a maioria dos testes E2E que precisam de setup limpo e rápido.
 *
 * @example
 * ```typescript
 * import {test, expect} from './fixtures/complete-fixtures.js';
 *
 * test('Deve criar e editar processo', async ({page, autenticadoComoAdmin}) => {
 *   // Database resetada ao iniciar o arquivo, usuário logado!
 *   const descricao = 'Processo teste';
 *   await criarProcesso(page, {descricao, ...});
 * });
 * ```
 */
import * as fs from 'node:fs';

const arquivosSerialResetados = new Set<string>();

const test = base.extend<{
    resetAutomatico: void;
}>({
    resetAutomatico: [async ({request}, use, testInfo) => {
        
        
        const fileContent = fs.readFileSync(testInfo.file, 'utf-8');
        const ehSerial = fileContent.includes('test.describe.serial');

        if (ehSerial) {
            // No modo serial, resetamos apenas uma vez por arquivo (no primeiro teste)
            if (!arquivosSerialResetados.has(testInfo.file)) {
                await resetDatabase(request);
                arquivosSerialResetados.add(testInfo.file);
            }
        } else {
            // No modo normal, resetamos antes de CADA teste para garantir isolamento total
            await resetDatabase(request);
        }
        await use();
    }, {auto: true}]
});

export {expect} from './auth-fixtures.js';
export {test};
