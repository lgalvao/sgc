import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture} from './fixtures/index.js';
import {abrirAcaoConsensoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const UNIDADE = 'ASSESSORIA_12';
const VALOR_CONSENSO_IMPORTANCIA = '4';

test.describe('CDU-45 - Manter avaliação de consenso', () => {
    test('CHEFE mantém consenso por autosave e reencontra o valor salvo ao reabrir a avaliação', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-45 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId(`dropdown-acoes-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeVisible();

        await abrirAcaoConsensoDiagnostico(page, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));

        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);
        const seletorConsensoImportancia = page.locator('[data-testid^="consenso-final-importancia-"]').first();
        await expect(seletorConsensoImportancia).toBeVisible();

        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            seletorConsensoImportancia.selectOption(VALOR_CONSENSO_IMPORTANCIA)
        ]);

        await expect.poll(async () => await page.evaluate(async ({codigo, titulo}) => {
            const resposta = await fetch(`/api/diagnosticos/subprocessos/${codigo}/consenso/${titulo}`, {credentials: 'include'});
            if (!resposta.ok) return null;
            const dados = await resposta.json();
            return String(dados.competencias[0]?.consensoImportancia ?? '');
        }, {
            codigo: codSubprocesso,
            titulo: TITULO_SERVIDOR_ASSESSORIA_12
        })).toBe(VALOR_CONSENSO_IMPORTANCIA);

        await page.goBack();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`));

        await abrirAcaoConsensoDiagnostico(page, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));
        await expect(seletorConsensoImportancia).toHaveValue(VALOR_CONSENSO_IMPORTANCIA);
    });
});
