import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComConsensoCriadoFixture} from './fixtures/index.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {verificarToast} from './helpers/helpers-navegacao.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

// CDU-45: Painel → Detalhes do subprocesso → card Avaliação de consenso → aprova
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

        // CDU-45 passo 1: acessa a tela Detalhes do subprocesso
        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);

        // CDU-45 passo 2: aciona o card Avaliação de consenso (habilitado quando situação = CONSENSO_CRIADO)
        const cardConsenso = page.getByTestId('card-subprocesso-consenso');
        await expect(cardConsenso).toBeVisible();
        await expect(cardConsenso).not.toHaveClass(/card-disabled/);
        await cardConsenso.click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));

        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);

        // CDU-45 passos 3-4: tela Avaliação de consenso com botão Aprovar
        await expect(page.getByTestId('btn-aprovar-consenso')).toBeVisible();
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/consenso/aprovar`) && res.ok()),
            page.getByTestId('btn-aprovar-consenso').click()
        ]);

        // CDU-45 passo 8: redireciona para Detalhes do subprocesso com mensagem de sucesso
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}`));
        await verificarToast(page, 'Avaliação de consenso aprovada');

        // CDU-45 passos 6-7: notificação gerada para o responsável da unidade
        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Avaliação de consenso',
            tipo: 'Consenso aprovado'
        });
    });
});
