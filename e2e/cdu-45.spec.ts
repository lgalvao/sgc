import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComConsensoCriadoFixture} from './fixtures/index.js';
import {buscarCodSubprocessoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {verificarToast} from './helpers/helpers-navegacao.js';

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
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);
        await page.goto(`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`);
        await expect(page.getByTestId('btn-aprovar-consenso')).toBeVisible();
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/consenso/aprovar`) && res.ok()),
            page.getByTestId('btn-aprovar-consenso').click()
        ]);

        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}`));
        await verificarToast(page, 'Avaliação de consenso aprovada');

        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Avaliação de consenso',
            tipo: 'Consenso aprovado'
        });
    });
});
