import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture} from './fixtures/index.js';
import {abrirCardDiagnostico, preencherConsensoMinimo} from './helpers/helpers-diagnostico.js';
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
        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await abrirCardDiagnostico(page, 'card-subprocesso-monitoramento', /\/monitoramento/);
        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);
        await page.getByTestId(`btn-manter-consenso-${TITULO_SERVIDOR_ASSESSORIA_12}`).click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));
        await expect(page.getByRole('heading', {name: /Avaliação de Consenso/i})).toBeVisible();

        await preencherConsensoMinimo(page, codSubprocesso, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page.getByText('Salvo automaticamente')).toBeVisible();

        await page.getByRole('button', {name: /^Voltar$/i}).click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/monitoramento`));
    });
});
