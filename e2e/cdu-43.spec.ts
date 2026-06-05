import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture} from './fixtures/index.js';
import {abrirAcaoConsensoDiagnostico} from './helpers/helpers-diagnostico.js';
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
        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE);
        await expect(page.getByRole('columnheader', {name: 'Servidor'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Situação'})).toBeVisible();
        await expect(page.getByTestId(`dropdown-acoes-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeVisible();
        await abrirAcaoConsensoDiagnostico(page, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));
        await expect(page.getByRole('heading', {name: /Avaliação de Consenso/i})).toBeVisible();
    });
});
