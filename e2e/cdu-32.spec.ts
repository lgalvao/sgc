import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {homologarCadastroMapeamento} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

/**
 * CDU-32 - Reabrir cadastro
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Subprocesso com cadastro já disponibilizado ou aceito
 * 
 * Fluxo principal:
 * 1. ADMIN acessa subprocesso da unidade
 * 2. ADMIN seleciona opção "Reabrir cadastro"
 * 3. Sistema solicita justificativa
 * 4. ADMIN preenche justificativa e confirma
 * 5. Sistema altera situação e envia notificações
 */
test.describe.serial('CDU-32 - Reabrir cadastro', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-32 ${timestamp}`;
    let processoId: number;

    const atividade1 = `Atividade Reabrir ${timestamp}`;

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

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo(?:\/cadastro)?\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza cadastro', async ({page, autenticadoComoChefeSecao221}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Reabrir 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: ADMIN homologa cadastro', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroMapeamento(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para subprocesso disponibilizado', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        await expect(page.getByTestId('subprocesso-header__txt-situacao'))
            .toHaveText(/Cadastro homologado/i);
    });

    test('Cenario 2: ADMIN visualiza botão Reabrir cadastro', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();
    });

    test('Cenario 3: ADMIN abre modal de reabertura de cadastro', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        await btnReabrir.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Reabrir cadastro/i)).toBeVisible();
        await expect(page.getByTestId('inp-justificativa-reabrir')).toBeVisible();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();
    });

    test('Cenario 4: Botão confirmar desabilitado sem justificativa', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        await btnReabrir.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled();
        await page.getByTestId('inp-justificativa-reabrir').fill('Justificativa de teste');
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeEnabled();
    });

    test('Cenario 5: ADMIN confirma reabertura de cadastro', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('btn-reabrir-cadastro').click();

        await page.getByTestId('inp-justificativa-reabrir').fill('Ajustes necessários identificados na revisão.');
        await page.getByTestId('btn-confirmar-reabrir').click();

        await expect(page.getByText(/Cadastro reaberto/i).first()).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de cadastro/i);
    });
});
