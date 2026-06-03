import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {abrirCardDiagnostico, preencherAutoavaliacaoCompleta} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-43 - Acompanhar diagnóstico da unidade', () => {
    test('CHEFE acessa o monitoramento e visualiza situação e ações dos servidores', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-43 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: UNIDADE,
            iniciar: true
        });

        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await abrirCardDiagnostico(page, 'card-subprocesso-diagnostico', /\/autoavaliacao/);
        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);
        await preencherAutoavaliacaoCompleta(page, codSubprocesso);
        await page.getByTestId('btn-concluir-autoavaliacao').click();
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao/concluir`) && res.ok()),
            page.getByTestId('btn-confirmar-concluir').click()
        ]);

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await abrirCardDiagnostico(page, 'card-subprocesso-monitoramento', /\/monitoramento/);

        await expect(page.getByRole('heading', {name: /Monitoramento do Diagnóstico/i})).toBeVisible();
        await expect(page.getByText('Servidores')).toBeVisible();
        await expect(page.getByText('Pendentes')).toBeVisible();
        await expect(page.getByTestId(`btn-manter-consenso-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeVisible();
        await expect(page.getByTestId(`btn-impossibilitar-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeEnabled();
        await expect(page.getByText('Autoavaliação concluída')).toBeVisible();
    });
});
