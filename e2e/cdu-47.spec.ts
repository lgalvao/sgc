import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {abrirCardDiagnostico, preencherPrimeiraSituacaoCapacitacao} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-47 - Preencher situação de capacitação', () => {
    test('CHEFE preenche situação de capacitação com autossalvamento', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-47 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: UNIDADE,
            iniciar: true
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await abrirCardDiagnostico(page, 'card-subprocesso-ocupacoes', /\/situacao-capacitacao/);

        await expect(page.getByRole('heading', {name: /Situação de Capacitação/i})).toBeVisible();
        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);
        await preencherPrimeiraSituacaoCapacitacao(page, codSubprocesso, 'EC');

        await expect(page.getByText('Salvo automaticamente')).toBeVisible();
        await expect(page.getByTestId('btn-concluir-diagnostico')).toBeVisible();
    });
});
