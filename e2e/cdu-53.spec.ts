import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoConcluidoFixture, criarProcessoDiagnosticoHomologadoFixture} from './fixtures/index.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('CDU-53 - Finalizar processo de diagnóstico', () => {
    const UNIDADE = 'ASSESSORIA_12';
    const DESCRICAO_PROCESSO = `Diagnóstico CDU-53 ${Date.now()}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoDiagnosticoHomologadoFixture(request, {
            descricao: DESCRICAO_PROCESSO,
            unidade: UNIDADE
        });
        expect(processo.codigo).toBeGreaterThan(0);
    });

    test('Cenários CDU-53: ADMIN finaliza processo de diagnóstico', async ({
                                                                               _resetAutomatico,
                                                                               page,
                                                                               _autenticadoComoAdmin
                                                                           }) => {
        await acessarDetalhesProcesso(page, DESCRICAO_PROCESSO);
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
        await expect(page.getByTestId('btn-processo-finalizar')).toBeEnabled();

        await page.getByTestId('btn-processo-finalizar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: TEXTOS.processo.FINALIZACAO_TITULO})).toBeVisible();
        await expect(modal.getByText('Essa ação encerrará o processo e notificará todas as unidades participantes.')).toBeVisible();
        await modal.getByTestId('btn-finalizar-processo-cancelar').click();
        await expect(modal).toBeHidden();

        await page.getByTestId('btn-processo-finalizar').click();
        await expect(modal).toBeVisible();
        await modal.getByTestId('btn-finalizar-processo-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText(TEXTOS.sucesso.PROCESSO_FINALIZADO, {exact: true})).toBeVisible();
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Finalização de processo de diagnóstico',
            tipo: 'Finalização de processo',
            trechoCorpo: 'Comunicamos a finalização do processo'
        });
    });
});

test.describe('CDU-53 - Processo de diagnóstico ainda não homologado', () => {
    test('ADMIN não consegue finalizar processo apenas concluído', async ({
                                                                              _resetAutomatico,
                                                                              request,
                                                                              page,
                                                                              _autenticadoComoAdmin
                                                                          }) => {
        const descricaoProcesso = `Diagnóstico CDU-52 pendente ${Date.now()}`;
        await criarProcessoDiagnosticoConcluidoFixture(request, {
            descricao: descricaoProcesso,
            unidade: 'ASSESSORIA_12'
        });

        await page.goto('/painel');
        await acessarDetalhesProcesso(page, descricaoProcesso);
        await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
        await expect(page.getByTestId('btn-processo-finalizar')).toBeEnabled();
        await page.getByTestId('btn-processo-finalizar').click();
        await expect(page.getByText('Não é possível finalizar o processo: há unidades não homologadas')).toBeVisible();
    });
});
