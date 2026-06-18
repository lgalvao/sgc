import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture} from './fixtures/index.js';
import {abrirAcaoConsensoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';
import {verificarAppAlert, verificarToast} from './helpers/helpers-navegacao.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const UNIDADE = 'ASSESSORIA_12';
const VALOR_CONSENSO_IMPORTANCIA = '4';

async function preencherTodosOsCamposDeConsenso(page: import('@playwright/test').Page, codSubprocesso: number): Promise<void> {
    const seletores = [
        '[data-testid^="consenso-chefia-importancia-"]',
        '[data-testid^="consenso-chefia-dominio-"]',
        '[data-testid^="consenso-final-importancia-"]',
        '[data-testid^="consenso-final-dominio-"]',
    ];

    for (const seletor of seletores) {
        const campos = page.locator(seletor);
        const total = await campos.count();
        for (let i = 0; i < total; i++) {
            const campo = campos.nth(i);
            if (await campo.isDisabled()) {
                continue;
            }
            if ((await campo.inputValue()) === '4') {
                continue;
            }
            await Promise.all([
                page.waitForResponse(res =>
                    res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`)
                    && res.request().method() === 'POST'
                    && res.ok()
                ),
                campo.selectOption('4')
            ]);
        }
    }
}

test.describe('CDU-45 - Manter avaliação de consenso', () => {
    test('CHEFE salva rascunho por autosave, valida na conclusão e só então libera o consenso para o servidor', async ({
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
        await expect(page.getByText(`João Guilherme de Albuquerque Maranhão - ${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeVisible();
        const seletorChefiaImportancia = page.locator('[data-testid^="consenso-chefia-importancia-"]').first();
        await expect(seletorChefiaImportancia).toBeVisible();
        await expect(page.locator('[data-testid^="consenso-chefia-dominio-"]').first()).toBeVisible();
        const seletorConsensoImportancia = page.locator('[data-testid^="consenso-final-importancia-"]').first();
        await expect(seletorConsensoImportancia).toBeVisible();
        await expect(page.locator('[data-testid^="consenso-final-dominio-"]').first()).toBeVisible();
        await expect(page.getByTestId('btn-aprovar-consenso')).toHaveCount(0);

        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            seletorChefiaImportancia.selectOption(VALOR_CONSENSO_IMPORTANCIA)
        ]);

        await expect(seletorConsensoImportancia).toBeEnabled();

        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`)
                && res.request().method() === 'POST'
                && res.ok()
            ),
            seletorConsensoImportancia.selectOption(VALOR_CONSENSO_IMPORTANCIA)
        ]);

        await page.goBack();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`));

        await abrirAcaoConsensoDiagnostico(page, TITULO_SERVIDOR_ASSESSORIA_12);
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));
        await expect(seletorConsensoImportancia).toHaveValue(VALOR_CONSENSO_IMPORTANCIA);
        await expect(page.locator('tbody tr').first().locator('span.valor-estatico').first()).toBeVisible();

        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('card-subprocesso-consenso')).toHaveClass(/card-disabled/);

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`);
        await page.getByTestId('btn-concluir-avaliacao').click();
        await verificarAppAlert(page, TEXTOS.diagnostico.ERRO_PREENCHIMENTO_CONSENSO_INCOMPLETO);

        await preencherTodosOsCamposDeConsenso(page, codSubprocesso);

        await Promise.all([
            page.waitForURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`)),
            page.getByTestId('btn-concluir-avaliacao').click(),
        ]);
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`));
        await verificarToast(page, TEXTOS.diagnostico.SUCESSO_CONSENSO_CRIADO);

        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            assunto: 'Avaliação de consenso criada',
            destinatario: TITULO_SERVIDOR_ASSESSORIA_12,
            tipo: 'Consenso disponível',
            situacao: 'PENDENTE',
            trechoCorpo: 'concluiu a avaliação de consenso no processo',
        });

        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('card-subprocesso-consenso')).not.toHaveClass(/card-disabled/);
    });
});
