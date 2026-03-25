import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaSubprocesso, verificarAppAlert} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

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

    function calcularNovaDataIso(dias: number): string {
        const novaData = new Date();
        novaData.setDate(novaData.getDate() + dias);
        const yyyy = novaData.getFullYear();
        const mm = String(novaData.getMonth() + 1).padStart(2, '0');
        const dd = String(novaData.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1,
            iniciar: true
        });
        validarProcessoFixture(processo, descProcesso);
    });


    test('Cenario 1: ADMIN navega para detalhes do subprocesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CDU-27: Passos 1-2


        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        // Verificar que está na página do subprocesso
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toBeVisible();
    });

    test('Cenario 2: ADMIN pode cancelar a alteração da data limite sem persistir mudanças', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnAlterarData = page.getByTestId('btn-alterar-data-limite');
        await expect(btnAlterarData).toBeVisible();
        await expect(btnAlterarData).toBeEnabled();
        await btnAlterarData.click();

        const modal = page.getByRole('dialog');
        await expect(modal.getByRole('heading', {name: TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE})).toBeVisible();

        const inputData = page.getByTestId('input-nova-data-limite');
        await expect(inputData).toBeVisible();
        const dataInicialModal = await inputData.inputValue();
        await expect(inputData).toHaveValue(/\d{4}-\d{2}-\d{2}/);

        await inputData.fill(calcularNovaDataIso(7));

        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();

        await btnAlterarData.click();
        await expect(page.getByTestId('input-nova-data-limite')).toHaveValue(dataInicialModal);
        await modal.getByRole('button', {name: /Cancelar/i}).click();
    });

    test('Cenario 3: ADMIN altera data limite e recebe confirmação', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnAlterarData = page.getByTestId('btn-alterar-data-limite');
        await btnAlterarData.click();

        const inputData = page.getByTestId('input-nova-data-limite');
        await expect(inputData).toHaveValue(/\d{4}-\d{2}-\d{2}/);

        await inputData.fill(calcularNovaDataIso(7));

        await page.getByTestId('btn-modal-confirmar').click();
        await verificarAppAlert(page, /Data limite alterada/i);
        await expect(inputData).toBeHidden();
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toBeVisible();
    });
});
