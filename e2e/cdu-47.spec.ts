import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {abrirAcaoCapacitacaoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';
const VALOR_CAPACITACAO = 'EC';

test.describe('CDU-47 - Preencher situação de capacitação', () => {
    test('CHEFE preenche a matriz de capacitação com autosave e reencontra o valor salvo ao reabrir', async ({
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
        await expect(page.getByTestId(`dropdown-acoes-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeVisible();

        await abrirAcaoCapacitacaoDiagnostico(page, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/situacao-capacitacao`));

        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);
        const seletorCapacitacao = page.locator(`[data-testid^="ocupacao-${TITULO_SERVIDOR_ASSESSORIA_12}-"]`).first();
        const testIdCapacitacao = await seletorCapacitacao.getAttribute('data-testid');
        await expect(seletorCapacitacao).toBeVisible();

        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/ocupacoes-criticas`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            seletorCapacitacao.selectOption(VALOR_CAPACITACAO)
        ]);

        await expect.poll(async () => await page.evaluate(async ({codigo, titulo}) => {
            const resposta = await fetch(`/api/diagnosticos/subprocessos/${codigo}/unidade`, {credentials: 'include'});
            if (!resposta.ok) return null;
            const dados = await resposta.json();
            return dados.ocupacoesCriticas.find((item: {servidorTitulo: string; situacaoCapacitacao: string | null}) =>
                item.servidorTitulo === titulo && item.situacaoCapacitacao === 'EC'
            )?.situacaoCapacitacao ?? null;
        }, {
            codigo: codSubprocesso,
            titulo: TITULO_SERVIDOR_ASSESSORIA_12,
        })).toBe(VALOR_CAPACITACAO);

        await page.goBack();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`));

        await abrirAcaoCapacitacaoDiagnostico(page, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/situacao-capacitacao`));
        await expect(page.getByTestId(testIdCapacitacao!)).toHaveValue(VALOR_CAPACITACAO);
    });
});
