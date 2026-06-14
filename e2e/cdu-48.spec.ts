import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComConsensoCriadoFixture} from './fixtures/index.js';
import {abrirAcaoCapacitacaoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';
const VALOR_CAPACITACAO = 'EC';

test.describe('CDU-48 - Preencher situações de capacitação', () => {
    test('CHEFE preenche a situação de capacitação do servidor selecionado com autosave e reencontra o valor salvo ao reabrir', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-48 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComConsensoCriadoFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('card-subprocesso-situacoes-capacitacao')).toBeVisible();

        await abrirAcaoCapacitacaoDiagnostico(page);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/situacao-capacitacao`));
        await expect(page.getByRole('heading', {name: /Situação de capacitação/i})).toBeVisible();
        await expect(page.getByTestId('lista-servidores-situacao-capacitacao')).toBeVisible();
        await page.getByTestId(`btn-selecionar-servidor-situacao-capacitacao-${TITULO_SERVIDOR_ASSESSORIA_12}`).click();
        await expect(page.getByTestId('detalhes-servidor-situacao-capacitacao')).toContainText(TITULO_SERVIDOR_ASSESSORIA_12);

        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);
        const seletorCapacitacao = page.locator(`[data-testid^="situacao-${TITULO_SERVIDOR_ASSESSORIA_12}-"]`).first();
        const testIdCapacitacao = await seletorCapacitacao.getAttribute('data-testid');
        await expect(seletorCapacitacao).toBeVisible();

        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/situacoes-capacitacao`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            seletorCapacitacao.selectOption(VALOR_CAPACITACAO)
        ]);

        await expect.poll(async () => await page.evaluate(async ({codigo, titulo}) => {
            const resposta = await fetch(`/api/subprocessos/${codigo}/diagnostico/unidade`, {credentials: 'include'});
            if (!resposta.ok) return null;
            const dados = await resposta.json();
            return dados.situacoesCapacitacao.find((item: {servidorTitulo: string; situacaoCapacitacao: string | null}) =>
                item.servidorTitulo === titulo && item.situacaoCapacitacao === 'EC'
            )?.situacaoCapacitacao ?? null;
        }, {
            codigo: codSubprocesso,
            titulo: TITULO_SERVIDOR_ASSESSORIA_12,
        })).toBe(VALOR_CAPACITACAO);

        await page.goBack();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`));

        await abrirAcaoCapacitacaoDiagnostico(page);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/situacao-capacitacao`));
        await page.getByTestId(`btn-selecionar-servidor-situacao-capacitacao-${TITULO_SERVIDOR_ASSESSORIA_12}`).click();
        await expect(page.getByTestId(testIdCapacitacao!)).toHaveValue(VALOR_CAPACITACAO);
    });
});
