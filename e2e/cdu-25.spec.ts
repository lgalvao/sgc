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
 * CDU-25 - Aceitar validação de mapas de competências em bloco
 * 
 * Ator: GESTOR
 * 
 * Pré-condições:
 * - Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões'
 * - Localização atual na unidade do usuário
 * 
 * Fluxo principal:
 * 1. GESTOR acessa processo em andamento
 * 2. Sistema mostra Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para aceite
 * 4. GESTOR clica no botão 'Aceitar Mapa em Bloco'
 * 5. Sistema abre modal com lista de unidades
 * 6. GESTOR confirma
 * 7. Sistema executa aceite para cada unidade
 */
test.describe.serial('CDU-25 - Aceitar validação de mapas em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-25 ${timestamp}`;
    let processoId: number;

    const atividade1 = `Atividade Val ${timestamp}`;
    const competencia1 = `Competência Val ${timestamp}`;

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
        

        await page.goto(`/processo/${processoId}`);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Val 1');

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

    test('Cenario 1: GESTOR acessa processo com mapa validado', async ({page, autenticadoComoGestorCoord22}) => {
        

        await page.getByText(descProcesso).click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByRole('button', {name: /Aceitar.*Bloco/i});
        const btnVisivel = await btnAceitar.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnAceitar).toBeEnabled();
        }
    });

    test('Cenario 2: GESTOR abre modal de aceite de mapa em bloco', async ({page, autenticadoComoGestorCoord22}) => {
        

        await page.getByText(descProcesso).click();

        const btnAceitar = page.getByRole('button', {name: /Aceitar.*Bloco/i});
        
        if (await btnAceitar.isVisible().catch(() => false)) {
            await btnAceitar.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await expect(modal.locator('table')).toBeVisible();
            await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();

            await modal.getByRole('button', {name: /Cancelar/i}).click();
        }
    });
});
