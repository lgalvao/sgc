import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

/**
 * CDU-27 - Alterar data limite de subprocesso
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Unidade participante com subprocesso iniciado e ainda não finalizado
 * 
 * Fluxo principal:
 * 1. ADMIN acessa processo ativo e clica em uma unidade
 * 2. Sistema mostra tela Detalhes do subprocesso
 * 3. ADMIN clica no botão 'Alterar data limite'
 * 4. Sistema abre modal com campo de data preenchido
 * 5. ADMIN altera a data e confirma
 * 6. Sistema atualiza e envia notificação
 */
test.describe.serial('CDU-27 - Alterar data limite de subprocesso', () => {
    const UNIDADE_1 = 'SECAO_221';
    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-27 ${timestamp}`;
    let processoId: number;

    // ========================================================================
    // PREPARAÇÃO
    // ========================================================================

    test('Preparacao: Admin cria e inicia processo', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        

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

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para detalhes do subprocesso', async ({page, autenticadoComoAdmin}) => {
        // CDU-27: Passos 1-2
        

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        // Verificar que está na página do subprocesso
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toBeVisible();
    });

    test('Cenario 2: ADMIN altera data limite e recebe confirmação', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnAlterarData = page.getByTestId('btn-alterar-data-limite');
        await expect(btnAlterarData).toBeVisible();
        await expect(btnAlterarData).toBeEnabled();
        await btnAlterarData.click();

        const inputData = page.getByTestId('input-nova-data-limite');
        await expect(inputData).toBeVisible();
        const novaData = new Date();
        novaData.setDate(novaData.getDate() + 7);
        const yyyy = novaData.getFullYear();
        const mm = String(novaData.getMonth() + 1).padStart(2, '0');
        const dd = String(novaData.getDate()).padStart(2, '0');
        await inputData.fill(`${yyyy}-${mm}-${dd}`);

        await page.getByTestId('btn-modal-confirmar').click();
        await expect(page.getByText(/Data limite alterada com sucesso/i).first()).toBeVisible();
        await expect(inputData).toBeHidden();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toBeVisible();
    });
});
