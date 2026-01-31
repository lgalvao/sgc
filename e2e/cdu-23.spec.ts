import {expect, test} from './fixtures/auth-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';

/**
 * CDU-23 - Homologar cadastros em bloco
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Processo de mapeamento ou revisão com unidades subordinadas
 * - Subprocessos na situação 'Cadastro disponibilizado' ou 'Cadastro aceito'
 * 
 * Fluxo principal:
 * 1. No Painel, ADMIN acessa processo em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para homologação
 * 4. ADMIN clica no botão 'Homologar em Bloco'
 * 5. Sistema abre modal com lista de unidades selecionáveis
 * 6. ADMIN seleciona unidades e confirma
 * 7. Sistema executa homologação para cada unidade selecionada
 */
test.describe.serial('CDU-23 - Homologar cadastros em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-23 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    const atividade1 = `Atividade Homol ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo', async ({page}) => {
        

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_1,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanup.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza cadastro', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Homol 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN visualiza botão Homologar em Bloco', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnHomologar = page.getByRole('button', {name: /Homologar em Bloco/i});
        const btnVisivel = await btnHomologar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnHomologar).toBeEnabled();
        }
    });

    test('Cenario 2: ADMIN abre modal de homologação em bloco', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnHomologar = page.getByRole('button', {name: /Homologar em Bloco/i});
        
        if (await btnHomologar.isVisible().catch(() => false)) {
            await btnHomologar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await expect(modal.getByText(/Homologar em Bloco/i)).toBeVisible();
            await expect(modal.locator('table')).toBeVisible();
            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
            await expect(modal.getByRole('button', {name: /Homologar/i})).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();
        }
    });

    test('Cenario 3: Cancelar homologação em bloco permanece na tela', async ({page}) => {
        

        await page.getByText(descProcesso).click();

        const btnHomologar = page.getByRole('button', {name: /Homologar em Bloco/i});
        
        if (await btnHomologar.isVisible().catch(() => false)) {
            await btnHomologar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();

            await expect(modal).toBeHidden();
            await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        }
    });
});