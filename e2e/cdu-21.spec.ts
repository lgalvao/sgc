import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import type {useProcessoCleanup} from './hooks/hooks-limpeza.js';

async function acessarSubprocessoChefe(page: Page, descProcesso: string) {
    await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
    await page.getByTestId('card-subprocesso-mapa').click();
}

test.describe.serial('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-21 ${timestamp}`;
    let processoId: number;

    // Atividades e competências para os testes
    const atividade1 = `Atividade 1 ${timestamp}`;
    const atividade2 = `Atividade 2 ${timestamp}`;
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    // ========================================================================
    // PREPARAÇÃO - Criar mapa homologado para ADMIN finalizar processo
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo\/cadastro\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro', async ({page, autenticadoComoChefeSecao221}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento 1A');

        await adicionarAtividade(page, atividade2);
        await adicionarConhecimento(page, atividade2, 'Conhecimento 2A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    });

    test('Preparacao 4: Admin cria competências e disponibiliza mapa', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await navegarParaMapa(page);

        await criarCompetencia(page, competencia1, [atividade1]);
        await criarCompetencia(page, competencia2, [atividade2]);

        await disponibilizarMapa(page, '2030-12-31');

        await verificarPaginaPainel(page);
        await expect(page.getByText(/Mapa disponibilizado/i)).toBeVisible();
    });

    test('Preparacao 5: Chefe valida o mapa', async ({page, autenticadoComoChefeSecao221}) => {
        

        await acessarSubprocessoChefe(page, descProcesso);

        await page.getByTestId('btn-mapa-validar').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText(/Mapa validado/i).first()).toBeVisible();
    });

    test('Preparacao 6: Gestor registra aceite do mapa', async ({page, autenticadoComoGestorCoord22}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 7: Admin homologa o mapa', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await verificarPaginaPainel(page);

        // Verificar que mapa foi homologado
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Mapa homologado/i);
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-21
    // ========================================================================

    test('Cenario 1: ADMIN navega para detalhes do processo', async ({page, autenticadoComoAdmin}) => {
        // CDU-21: Passos 1-2
        

        // Passo 1: ADMIN clica no processo
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        // Passo 2: Sistema exibe tela Detalhes do processo
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        // Botão Finalizar visível
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test('Cenario 2: ADMIN cancela finalização - permanece na tela', async ({page, autenticadoComoAdmin}) => {
        // CDU-21: Passo 6.1
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        // Passo 3: Clicar em Finalizar processo
        await page.getByTestId('btn-processo-finalizar').click();

        // Passo 6: Modal de confirmação
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Confirma a finalização/i)).toBeVisible();

        // Passo 6.1: Cancelar
        await page.getByTestId('btn-finalizar-processo-cancelar').click();

        // Permanece na tela de detalhes do processo
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
    });

    test('Cenario 3: ADMIN finaliza processo com sucesso', async ({page, autenticadoComoAdmin}) => {
        // CDU-21: Passos 7-10
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        // Passo 3: Clicar em Finalizar processo
        await page.getByTestId('btn-processo-finalizar').click();

        // Passo 6: Modal de confirmação
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        // Passo 7: ADMIN escolhe Confirmar
        await page.getByTestId('btn-finalizar-processo-confirmar').click();

        // Passo 10: Redireciona para Painel com mensagem de sucesso
        await verificarPaginaPainel(page);
        await expect(page.getByText(/Processo finalizado/i)).toBeVisible();

        // Verificar que processo não aparece mais no painel ativo (foi finalizado)
        // (Processo finalizado não aparece na lista de processos ativos)
    });
});
