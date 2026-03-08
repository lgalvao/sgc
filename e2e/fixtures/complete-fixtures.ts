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
 *   const descricao = 'Processo Teste';
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
        // Detecta de forma mais agressiva se a suíte é serial.
        // O Playwright testInfo não tem metadata.serial de forma nativa na maioria das vezes.
        // Então usamos o titlePath, nome do arquivo ou verificamos se a primeira string do path (título da suíte) indica um fluxo serial.
        // CDUs que sabemos ser seriais (podemos listar caso a string falhe): CDU-05, CDU-06 (parcial), etc.
        // A melhor forma no contexto do Playwright é ver se o titlePath raiz (describe principal) foi marcado ou se é um teste que a gente SABE que não pode resetar.
        
        // Estratégia: No SGC, a maioria dos testes de 'revisao' (como CDU-05) ou fluxos longos usam .serial.
        // Verificamos a presença de 'describe.serial' no arquivo que o testInfo indica, mas como não podemos ler arquivo aqui de forma síncrona,
        // vamos usar o titlePath ou nome do arquivo.
        
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
