import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {abrirCardDiagnostico, preencherAutoavaliacaoCompleta, preencherConsensoMinimo} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-44 - Manter avaliação de consenso', () => {
    test('CHEFE abre a avaliação de consenso, preenche e volta para a lista', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-44 ${Date.now()}`;
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
        await page.getByTestId(`btn-manter-consenso-${TITULO_SERVIDOR_ASSESSORIA_12}`).click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));
        await expect(page.getByRole('heading', {name: /Avaliação de Consenso/i})).toBeVisible();

        await preencherConsensoMinimo(page, codSubprocesso, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page.getByText('Salvo automaticamente')).toBeVisible();

        await page.getByRole('button', {name: /^Voltar$/i}).click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/monitoramento`));
    });
});
