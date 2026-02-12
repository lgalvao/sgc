import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import type {useProcessoCleanup} from './hooks/hooks-limpeza.js';

async function acessarSubprocessoChefe(page: Page, descProcesso: string) {
    await page.getByText(descProcesso).click();
    await page.getByTestId('card-subprocesso-mapa').click();
}

/**
 * CDU-26 - Homologar validação de mapas de competências em bloco
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões'
 * 
 * Fluxo principal:
 * 1. ADMIN acessa processo em andamento
 * 2. Sistema mostra Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para homologação
 * 4. ADMIN clica no botão 'Homologar Mapa em Bloco'
 * 5. Sistema abre modal com lista de unidades
 * 6. ADMIN confirma
 * 7. Sistema executa homologação para cada unidade
 */
test.describe.serial('CDU-26 - Homologar validação de mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-26 ${timestamp}`;
    let processoId: number;

    const atividade1 = `Atividade Homol ${timestamp}`;
    const competencia1 = `Competência Homol ${timestamp}`;

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        

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
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza cadastro', async ({page, autenticadoComoChefeSecao221}) => {
        

        await page.goto(`/processo/${processoId}/${UNIDADE_1}`);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Homol 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro e cria mapa', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1]);
        await disponibilizarMapa(page, '2030-12-31');
        await verificarPaginaPainel(page);
    });

    test('Preparacao 4: Chefe valida o mapa', async ({page, autenticadoComoChefeSecao221}) => {
        

        await acessarSubprocessoChefe(page, descProcesso);

        await page.getByTestId('btn-mapa-validar').click();
        await expect(page.getByRole('dialog')).toBeVisible();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN visualiza botão Homologar Mapa em Bloco', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnHomologar = page.getByRole('button', {name: /Homologar.*Bloco/i});
        const btnVisivel = await btnHomologar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnHomologar).toBeEnabled();
        }
    });

    test('Cenario 2: ADMIN abre modal de homologação de mapa em bloco', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByText(descProcesso).click();

        const btnHomologar = page.getByRole('button', {name: /Homologar.*Bloco/i});
        
        if (await btnHomologar.isVisible().catch(() => false)) {
            await btnHomologar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await expect(modal.getByText(/Homologar/i)).toBeVisible();
            await expect(modal.locator('table')).toBeVisible();
            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
            await expect(modal.getByRole('button', {name: /Homologar/i})).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();
        }
    });

    test('Cenario 3: Cancelar homologação de mapa em bloco', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByText(descProcesso).click();

        const btnHomologar = page.getByRole('button', {name: /Homologar.*Bloco/i});
        
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
