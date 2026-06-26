import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComConsensoCriadoFixture} from './fixtures/index.js';
import {login} from './helpers/helpers-auth.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {verificarToast} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-46 - Aprovar avaliação de consenso', () => {
    test('SERVIDOR aprova o consenso criado pela chefia', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-46 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComConsensoCriadoFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_SERVIDOR_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);

        const cardConsenso = page.getByTestId('card-subprocesso-consenso');
        await expect(cardConsenso).toBeVisible();
        await expect(cardConsenso).not.toHaveClass(/card-disabled/);
        await cardConsenso.click();
        await expect(page).toHaveURL(new RegExp(String.raw`/diagnostico/\d+/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`));
        await expect(page.locator('[data-testid^="consenso-chefia-importancia-"]')).toHaveCount(0);
        await expect(page.locator('[data-testid^="consenso-final-importancia-"]')).toHaveCount(0);

        const codSubprocesso = Number(new URL(page.url()).pathname.split('/')[2]);

        await expect(page.getByTestId('btn-aprovar-consenso')).toBeVisible();
        await page.getByTestId('btn-aprovar-consenso').click();
        await expect(page.getByRole('dialog')).toContainText(TEXTOS.diagnostico.MODAL_APROVAR_CONSENSO_MENSAGEM);
        await Promise.all([
            page.waitForResponse(res => res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/consenso/aprovar`) && res.ok()),
            page.getByTestId('btn-confirmar-aprovar-consenso').click()
        ]);

        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}/${UNIDADE}`));
        await verificarToast(page, 'Avaliação de consenso aprovada');

        await page.goto(`/diagnostico/${codSubprocesso}/${UNIDADE}/consenso/${TITULO_SERVIDOR_ASSESSORIA_12}`);
        await expect(page.getByText('A avaliação de consenso já foi aprovada.')).toBeVisible();
        await expect(page.getByTestId('btn-aprovar-consenso')).toBeDisabled();

        await login(page, '191919', 'senha');
        await verificarNotificacaoAdmin(page, {
            destinatario: UNIDADE,
            assunto: 'Avaliação de consenso',
            tipo: 'Consenso aprovado'
        });
    });
});
