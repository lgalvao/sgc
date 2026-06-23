import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture,
    criarProcessoDiagnosticoProntoParaConcluirFixture,
} from './fixtures/index.js';
import {buscarCodSubprocessoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-49 - Concluir diagnóstico da unidade', () => {
    test('CHEFE recebe mensagem quando ainda houver pendências no diagnóstico', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-49 pendências ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/concluir/validacao`)
                && res.request().method() === 'GET'
                && res.status() >= 400
            ),
            page.getByTestId('btn-concluir-diagnostico-cabecalho').click()
        ]);
        await expect(page.getByTestId('app-alert')).toContainText(/Ainda existem avaliações .* situações de capacitação pendentes\./);
        await expect(page.getByRole('dialog')).toHaveCount(0);
    });

    test('CHEFE conclui o diagnóstico pronto e o submete para análise', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-49 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoProntoParaConcluirFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);

        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/concluir/validacao`)
                && res.request().method() === 'GET'
                && res.ok()
            ),
            page.getByTestId('btn-concluir-diagnostico-cabecalho').click()
        ]);
        await expect(page.getByRole('dialog')).toContainText(TEXTOS.diagnostico.MODAL_CONCLUIR_DIAG_MENSAGEM);
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/concluir`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            page.getByTestId('btn-confirmar-concluir-diagnostico-cabecalho').click()
        ]);

        await expect(page).toHaveURL(/\/painel/);

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText('Conclusão de diagnóstico');

        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: 'SECRETARIA_1',
            assunto: `Diagnóstico da unidade ${UNIDADE} submetido para análise`,
            tipo: 'Diagnóstico concluído',
            trechoCorpo: descricao
        });
    });
});
