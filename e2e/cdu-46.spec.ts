import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';
import {buscarCodSubprocessoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-46 - Indicar impossibilidade de avaliação', () => {
    test('CHEFE informa justificativa e impossibilita a avaliação do servidor', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-46 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: UNIDADE,
            iniciar: true
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toHaveText(UNIDADE);
        await page.getByTestId(`dropdown-acoes-${TITULO_SERVIDOR_ASSESSORIA_12}`).getByRole('button', {name: 'Ações'}).click();

        await page.getByTestId(`btn-impossibilitar-${TITULO_SERVIDOR_ASSESSORIA_12}`).click();
        const modal = page.getByRole('dialog');
        await expect(modal).toContainText('Indicar impossibilidade de avaliação');
        await expect(modal).toContainText('Duff McKagan');

        await page.getByTestId('btn-confirmar-impossibilitar').click();
        await expect(modal).toContainText('A justificativa é obrigatória.');

        await page.getByTestId('textarea-justificativa-impossibilidade').fill('Servidor afastado durante todo o período da avaliação.');
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/avaliacoes/${TITULO_SERVIDOR_ASSESSORIA_12}/impossibilitar`)
                && res.ok()
            ),
            page.getByTestId('btn-confirmar-impossibilitar').click()
        ]);

        await expect(page.getByTestId('app-alert')).toContainText('Impossibilidade registrada');
        await expect(page.getByText('Avaliação impossibilitada', {exact: true})).toBeVisible();
        await expect(page.getByTestId(`btn-impossibilitar-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeDisabled();
    });
});
