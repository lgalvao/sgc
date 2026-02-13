import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {homologarCadastroRevisaoComImpacto} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

/**
 * CDU-33 - Reabrir revisão de cadastro
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Processo de revisão com cadastro já revisado/disponibilizado
 * 
 * Fluxo principal:
 * 1. ADMIN acessa subprocesso de revisão
 * 2. ADMIN seleciona opção "Reabrir Revisão"
 * 3. Sistema solicita justificativa
 * 4. ADMIN preenche justificativa e confirma
 * 5. Sistema altera situação e envia notificações
 */
test.describe.serial('CDU-33 - Reabrir revisão de cadastro', () => {
const UNIDADE_1 = 'ASSESSORIA_12';
const UNIDADE_CRIACAO = 'ASSESSORIA_12';

    const timestamp = Date.now();
    const descProcesso = `Revisão CDU-33 ${timestamp}`;
    let processoId: number;

    const atividade1 = `Atividade Revisão ${timestamp}`;

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_CRIACAO,
            expandir: ['SECRETARIA_1']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = Number.parseInt(new RegExp(/\/processo(?:\/cadastro)?\/(\d+)/).exec(page.url())?.[1] || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe disponibiliza revisão de cadastro', async ({page, autenticadoComoChefeAssessoria12}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Revisão 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: ADMIN homologa revisão de cadastro', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroRevisaoComImpacto(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para subprocesso de revisão', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão de cadastro homologada/i);
    });

    test('Cenario 2: ADMIN visualiza botão Reabrir Revisão', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();
    });

    test('Cenario 3: ADMIN abre modal de reabertura de revisão', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        await btnReabrir.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Reabrir Revisão/i)).toBeVisible();
        await expect(page.getByTestId('inp-justificativa-reabrir')).toBeVisible();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();
    });

    test('Cenario 4: ADMIN confirma reabertura da revisão', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('btn-reabrir-revisao').click();

        await page.getByTestId('inp-justificativa-reabrir').fill('Necessário revisar inconsistências apontadas.');
        await page.getByTestId('btn-confirmar-reabrir').click();

        await expect(page.getByText(/Revisão de cadastro reaberta|Revisão reaberta/i).first()).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão de cadastro em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de revisão de cadastro/i);
    });
});
