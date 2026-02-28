/**
 * Índice central de fixtures E2E.
 *
 * Exporta todas as fixtures disponíveis para simplificar importações.
 *
 * ## Guia de Uso
 *
 * ### 1. Testes Simples (apenas autenticação)
 * ```typescript
 * import {test, expect} from './fixtures/auth-fixtures.js';
 *
 * test('Teste básico', async ({page, autenticadoComoAdmin}) => {
 *   // Apenas login automático
 * });
 * ```
 *
 * ### 2. Testes com Processo (criação + cleanup)
 * ```typescript
 * import {test, expect} from './fixtures/processo-fixtures.js';
 *
 * test('Teste com processo', async ({page, processoFixture, cleanup}) => {
 *   // Processo criado automaticamente!
 *   await page.goto(`/processo/${processoFixture.codigo}`);
 * });
 * ```
 *
 * ### 3. Testes Completos (database + auth + cleanup)
 * ```typescript
 * import {test, expect} from './fixtures/complete-fixtures.js';
 *
 * test('Teste completo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
 *   // Database resetada + login + cleanup configurado!
 *   await criarProcesso(page, {...});
 *   cleanupAutomatico.registrar(codigo);
 * });
 * ```
 *
 * ### 4. Database Reset Manual
 * ```typescript
 * import {test, expect} from './fixtures/database-fixtures.js';
 *
 * test('Com reset', async ({page, databaseResetada}) => {
 *   // Database limpa antes do teste
 * });
 * ```
 */

// Re-exportar todas as fixtures
export {test as authTest, expect} from './auth-fixtures.js';
export {test as databaseTest} from './database-fixtures.js';
export {test as processoTest, criarMultiplosProcessos} from './processo-fixtures.js';
export {test as completeTest} from './complete-fixtures.js';

// Re-exportar tipos úteis
export type {ProcessoContext} from './processo-fixtures.js';

// Re-exportar fixtures base
export * from './base.js';
export * from './fixtures-processos.js';
