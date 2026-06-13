import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {navegarParaDiagnosticoUnidade} from './helpers/helpers-navegacao.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

test.describe('CDU-41 - Iniciar processo de diagnóstico', () => {
    test('ADMIN inicia diagnóstico, cria subprocesso e notificação inicial', async ({
        _resetAutomatico,
        page,
        request,
        _autenticadoComoAdmin
    }) => {
        const descricao = `Diagnóstico CDU-41 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: 'ASSESSORIA_12',
            iniciar: false,
            diasLimite: 30
        });

        await page.goto(`/processo/cadastro?codProcesso=${processo.codigo}`);
        await expect(page.getByTestId('btn-processo-iniciar-rodape')).toBeVisible();

        await page.getByTestId('btn-processo-iniciar-rodape').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toContainText('Ao iniciar o processo, não será mais possível editá-lo ou removê-lo');
        await modal.getByRole('button', {name: /^Cancelar$/i}).click();
        await expect(modal).toBeHidden();

        await page.getByTestId('btn-processo-iniciar-rodape').click();
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/processos/${processo.codigo}/iniciar`) && res.ok()),
            page.getByTestId('btn-iniciar-processo-confirmar').click()
        ]);

        await expect(page).toHaveURL(/\/painel/);

        await page.goto(`/processo/${processo.codigo}`);
        await navegarParaDiagnosticoUnidade(page, 'ASSESSORIA_12');
        await expect(page.getByRole('heading', {name: 'Análise do Diagnóstico da Unidade'})).toBeVisible();
        await expect(page.getByText('ASSESSORIA_12', {exact: true})).toBeVisible();
        await expect(page.getByTestId('btn-historico-analise-unidade')).toBeVisible();
        await expect(page.getByText('Competência x Servidor', {exact: true})).toBeVisible();
        await expect(page.getByText('Servidores e Consenso', {exact: true})).toBeVisible();
        await expect(page.getByText('Histórico de Movimentações', {exact: true})).toBeVisible();
        await expect(page.getByRole('listitem').filter({hasText: 'Duff McKagan'})).toBeVisible();
        await expect(page.getByRole('button', {name: /Duff McKagan242426/i})).toBeVisible();

        await verificarNotificacaoAdmin(page, {
            destinatario: 'ASSESSORIA_12',
            assunto: 'Início de processo de diagnóstico',
            tipo: 'Início do processo',
            trechoCorpo: descricao
        });

        await verificarNotificacaoAdmin(page, {
            destinatario: 'SECRETARIA_1',
            assunto: 'Início de processo de diagnóstico em unidades subordinadas',
            tipo: 'Início do processo',
            trechoCorpo: descricao
        });
    });
});
