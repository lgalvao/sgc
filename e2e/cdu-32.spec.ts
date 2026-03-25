import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaHomologadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';
import {navegarParaSubprocesso, verificarAppAlert} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';

/**
 * CDU-32 - Reabrir cadastro
 */
test.describe.serial('CDU-32 - Reabrir cadastro', () => {
    const UNIDADE_1 = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-32 ${timestamp}`;

    test('Setup UI', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoMapaHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1,
            diasLimite: 30
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('Cenários CDU-32: ADMIN reabre cadastro', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {

        // Cenario 1 & 2: Navegação e visualização do botão
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);

        const btnReabrir = page.getByTestId('btn-reabrir-cadastro');
        await expect(btnReabrir).toBeVisible();
        await expect(btnReabrir).toBeEnabled();

        // Cenario 3: Abrir modal e cancelar
        await btnReabrir.click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Reabrir cadastro/i)).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();

        // Cenario 4: Botão confirmar desabilitado sem justificativa
        await btnReabrir.click();
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled();
        await page.getByTestId('inp-justificativa-reabrir').fill('Justificativa de teste');
        await expect(page.getByTestId('btn-confirmar-reabrir')).toBeEnabled();

        // Cenario 5: Confirmar reabertura
        await page.getByTestId('btn-confirmar-reabrir').click();

        await verificarAppAlert(page, /Cadastro reaberto/i);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de cadastro/i);
    });
});
