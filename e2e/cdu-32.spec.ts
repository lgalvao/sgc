import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaHomologadoFixture, validarProcessoFixture} from './fixtures/index.js';
import {navegarParaSubprocesso, obterAcaoCabecalhoSubprocesso, verificarToast} from './helpers/helpers-navegacao.js';
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

        await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-cadastro')).toBeVisible();
        await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-cadastro')).toBeEnabled();

        // Cenario 3: Abrir modal e cancelar
        await (await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-cadastro')).click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Reabrir cadastro/i)).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();

        // Cenario 4: Validação sem justificativa
        await (await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-cadastro')).click();
        await page.getByTestId('btn-confirmar-reabrir').click();
        await expect(page.getByTestId('txt-reabertura-pendencia-justificativa')).toBeVisible();
        await page.getByTestId('inp-justificativa-reabrir').fill('Justificativa de teste');
        await expect(page.getByTestId('txt-reabertura-pendencia-justificativa')).toBeHidden();

        // Cenario 5: Confirmar reabertura
        await page.getByTestId('btn-confirmar-reabrir').click();

        await verificarToast(page, /Cadastro reaberto/i);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Cadastro reaberto/i);

        const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
            .locator('tr', {hasText: /Cadastro reaberto/i})
            .first();
        await expect(linhaMovimentacao).toBeVisible();
        await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}\s+\d{2}:\d{2}/);
        await expect(linhaMovimentacao).toContainText('ADMIN');
        await expect(linhaMovimentacao).toContainText(UNIDADE_1);
    });

    test('Cenário complementar: unidade alvo visualiza alerta de reabertura de cadastro no painel', async ({
                                                                                                               _resetAutomatico,
                                                                                                               page,
                                                                                                               _autenticadoComoChefeSecao221
                                                                                                           }) => {
        const tabelaAlertas = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertas).toBeVisible();
        await expect(tabelaAlertas).toContainText(descProcesso);
        await expect(tabelaAlertas).toContainText(/Cadastro de atividades reaberto/i);
        await expect(tabelaAlertas).toContainText(/\d{2}\/\d{2}\/\d{4}/);
    });

    test('Cenário complementar: unidade superior visualiza alerta de reabertura no painel', async ({
                                                                                                       _resetAutomatico,
                                                                                                       page,
                                                                                                       _autenticadoComoGestorCoord22
                                                                                                   }) => {
        const tabelaAlertas = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertas).toBeVisible();
        await expect(tabelaAlertas).toContainText(descProcesso);
        await expect(tabelaAlertas).toContainText(/Cadastro da unidade SECAO_221 reaberto/i);
        await expect(tabelaAlertas).toContainText(/ADMIN/i);
        await expect(tabelaAlertas).toContainText(/\d{2}\/\d{2}\/\d{4}/);
    });
});
