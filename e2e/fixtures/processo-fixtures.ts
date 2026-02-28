import type {Page} from '@playwright/test';
import {test as base} from './auth-fixtures.js';
import {criarProcesso} from '../helpers/helpers-processos.js';
import {useProcessoCleanup} from '../hooks/hooks-limpeza.js';
import logger from "../../frontend/src/utils/logger.js";

/**
 * Contexto de fixture com processo criado automaticamente
 */
export interface ProcessoContext {
    /** Processo criado automaticamente no beforeEach */
    processoFixture: {
        codigo: number;
        descricao: string;
    };
    /** Cleanup registrado automaticamente */
    cleanup: ReturnType<typeof useProcessoCleanup>;
}

/**
 * Fixtures de processo para eliminar duplicação de setup/cleanup em testes E2E.
 *
 * Automatiza:
 * - Reset de database no beforeAll
 * - Criação de processo padrão no beforeEach
 * - Registro automático para cleanup
 * - Limpeza automática no afterEach
 *
 * @example
 * ```typescript
 * import {test, expect} from './fixtures/processo-fixtures.js';
 *
 * test('Deve editar processo', async ({page, processoFixture}) => {
 *   // Já tem um processo criado e registrado para cleanup!
 *   await page.goto(`/processo/cadastro?codProcesso=${processoFixture.codigo}`);
 *   await page.getByTestId('inp-processo-descricao').fill('Nova descrição');
 *   await page.getByTestId('btn-processo-salvar').click();
 * });
 * ```
 */
export const test = base.extend<ProcessoContext>({
    processoFixture: async ({page, autenticadoComoAdmin}, use, testInfo) => {
        const descricao = `Fixture ${testInfo.title} - ${Date.now()}`;

        await page.getByTestId('btn-painel-criar-processo').click();
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        // Capturar código do processo
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await page.waitForURL(/processo\/cadastro\?codProcesso=\d+/);
        const url = new URL(page.url());
        const codigo = parseInt(url.searchParams.get('codProcesso') || '0');

        if (codigo === 0) {
            throw new Error('Falha ao capturar código do processo criado');
        }

        await page.goto('/painel');

        // Use fixture
        await use({codigo, descricao});

        // Cleanup automático via endpoint E2E
        try {
            const response = await page.request.post(`/e2e/processo/${codigo}/limpar`);
            if (!response.ok()) {
                logger.warn(`Falha no cleanup do processo ${codigo}: ${response.status()}`);
            }
        } catch (error) {
            logger.warn(`Erro no cleanup do processo ${codigo}: ${error}`);
        }
    },

    cleanup: async ({}, use) => {
        const cleanup = useProcessoCleanup();
        await use(cleanup);
    }
});

export {expect} from './auth-fixtures.js';

/**
 * Helper para criar múltiplos processos em um teste
 *
 * @param page - Página do Playwright
 * @param count - Número de processos a criar
 * @param options - Opções para criação (tipo, unidade, etc.)
 * @returns Array com códigos e descrições dos processos criados
 */
export async function criarMultiplosProcessos(
    page: Page,
    count: number,
    options: {
        tipo?: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO';
        unidade?: string;
        diasLimite?: number;
    } = {}
): Promise<Array<{ codigo: number; descricao: string }>> {
    const processos: Array<{ codigo: number; descricao: string }> = [];
    const timestamp = Date.now();

    for (let i = 0; i < count; i++) {
        const descricao = `Processo Múltiplo ${i + 1} - ${timestamp}`;

        await page.getByTestId('btn-painel-criar-processo').click();
        await criarProcesso(page, {
            descricao,
            tipo: options.tipo || 'MAPEAMENTO',
            diasLimite: options.diasLimite || 30,
            unidade: options.unidade || 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await page.waitForURL(/processo\/cadastro\?codProcesso=\d+/);
        const url = new URL(page.url());
        const codigo = parseInt(url.searchParams.get('codProcesso') || '0');

        if (codigo > 0) {
            processos.push({codigo, descricao});
        }

        await page.goto('/painel');
    }

    return processos;
}
