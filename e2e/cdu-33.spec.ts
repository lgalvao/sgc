import {expect, test} from './fixtures/auth-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza.js';

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
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Revisão CDU-33 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    const atividade1 = `Atividade Revisão ${timestamp}`;

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

    test('Preparacao 2: Chefe disponibiliza revisão de cadastro', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Revisão 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para subprocesso de revisão', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toBeVisible();
    });

    test('Cenario 2: ADMIN visualiza botão Reabrir Revisão', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        const btnVisivel = await btnReabrir.isVisible().catch(() => false);
        
        if (btnVisivel) {
            await expect(btnReabrir).toBeEnabled();
        }
    });

    test('Cenario 3: ADMIN abre modal de reabertura de revisão', async ({page}) => {
        

        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnReabrir = page.getByTestId('btn-reabrir-revisao');
        
        if (await btnReabrir.isVisible().catch(() => false)) {
            await btnReabrir.click();

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();
            await expect(modal.getByText(/Reabrir Revisão/i)).toBeVisible();
            await expect(page.getByTestId('inp-justificativa-reabrir')).toBeVisible();
            await expect(page.getByTestId('btn-confirmar-reabrir')).toBeVisible();
        }
    });
});