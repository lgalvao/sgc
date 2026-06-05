import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {buscarCodSubprocessoDiagnostico, preencherAutoavaliacaoCompleta} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-42 - Realizar autoavaliação', () => {
    test('SERVIDOR preenche a autoavaliação, conclui e gera notificação', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-42 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: UNIDADE,
            iniciar: true,
            diasLimite: 30
        });

        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);
        await page.goto(`/diagnostico/${codSubprocesso}/${UNIDADE}/autoavaliacao`);
        await expect(page.getByRole('heading', {name: /Autoavaliação de Competências/i})).toBeVisible();

        await preencherAutoavaliacaoCompleta(page, codSubprocesso);

        await page.getByTestId('btn-concluir-autoavaliacao').click();
        await expect(page.getByRole('dialog')).toContainText('Confirma a conclusão da autoavaliação?');
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao/concluir`) && res.ok()),
            page.getByTestId('btn-confirmar-concluir').click()
        ]);

        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/autoavaliacao`));
        await expect(page.getByTestId('app-alert')).toContainText('Autoavaliação concluída');

        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Autoavaliação',
            tipo: 'Autoavaliação concluída'
        });
    });
});
