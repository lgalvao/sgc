import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComConsensoCriadoFixture} from './fixtures/index.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-45 - Aprovar avaliação de consenso', () => {
    test('SERVIDOR aprova o consenso criado pela chefia', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-45 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComConsensoCriadoFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        const cardConsensoServidor = page.getByTestId('card-subprocesso-consenso');
        await expect(cardConsensoServidor).toBeVisible();
        await cardConsensoServidor.click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));
        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);
        await expect(page.getByTestId('btn-aprovar-consenso')).toBeVisible();
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/consenso/aprovar`) && res.ok()),
            page.getByTestId('btn-aprovar-consenso').click()
        ]);

        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}(?:\\?.*)?$`));

        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Avaliação de consenso',
            tipo: 'Consenso aprovado'
        });
    });
});
