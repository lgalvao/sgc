import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoConcluidoFixture} from './fixtures/index.js';
import {acessarDetalhesProcesso, obterAcaoBloco} from './helpers/helpers-processos.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {verificarAusenciaNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

test.describe.serial('CDU-52 - homologar diagnósticos em bloco', () => {
    const UNIDADE = 'ASSESSORIA_12';
    const DESCRICAO_PROCESSO = `Diagnóstico CDU-52 ${Date.now()}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoDiagnosticoConcluidoFixture(request, {
            descricao: DESCRICAO_PROCESSO,
            unidade: UNIDADE
        });
        expect(processo.codigo).toBeGreaterThan(0);
    });

    test('Cenários CDU-52: ADMIN homologa diagnósticos em bloco', async ({
        _resetAutomatico,
        page,
        _autenticadoComoAdmin
    }) => {
        await acessarDetalhesProcesso(page, DESCRICAO_PROCESSO);
        await expect(page.getByTestId('processo-info')).toBeVisible();

        const botaoHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-diagnosticos-bloco');
        await expect(botaoHomologar).toBeVisible();
        await expect(botaoHomologar).toBeEnabled();

        await botaoHomologar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await expect(modal.getByText('Homologação de diagnósticos em bloco')).toBeVisible();
        await expect(modal.getByText('Selecione as unidades cujos diagnósticos devem ser homologados')).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();
        await expect(modal.locator('tr', {hasText: UNIDADE})).toBeVisible();

        const checkboxes = modal.locator('input[type="checkbox"]');
        await checkboxes.first().uncheck();
        await expect(checkboxes).toHaveCount(2);
        await expect(checkboxes.nth(1)).not.toBeChecked();

        await modal.getByTestId('btn-acao-bloco-confirmar').click();
        await expect(modal.getByText('Selecione ao menos uma unidade.')).toBeVisible();

        await checkboxes.nth(1).check();
        await modal.getByTestId('btn-acao-bloco-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText('Diagnósticos homologados')).toBeVisible();

        await verificarAusenciaNotificacaoAdmin(page, {
            assunto: 'Diagnósticos homologados',
            tipo: 'Diagnóstico homologado'
        });
    });
});
