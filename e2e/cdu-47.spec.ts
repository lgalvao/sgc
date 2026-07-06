import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture, criarProcessoFinalizadoFixture} from './fixtures/index.js';
import {buscarCodSubprocessoDiagnostico} from './helpers/helpers-diagnostico.js';
import {login} from './helpers/helpers-auth.js';

const TITULO_CHEFE_ASSESSORIA_12 = '151515';
const TITULO_SERVIDOR_ASSESSORIA_12 = '242426';
const UNIDADE = 'ASSESSORIA_12';

test.describe('CDU-47 - Indicar impossibilidade de avaliação', () => {
    test('CHEFE informa justificativa e impossibilita a avaliação do servidor', async ({
                                                                                           _resetAutomatico,
                                                                                           page,
                                                                                           request
                                                                                       }) => {
        await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE,
            iniciar: true
        });

        const descricao = `Diagnóstico CDU-47 ${Date.now()}`;
        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_ASSESSORIA_12
        });

        await login(page, TITULO_CHEFE_ASSESSORIA_12, 'senha');
        await page.goto(`/processo/${processo.codigo}/${UNIDADE}`);
        const codSubprocesso = await buscarCodSubprocessoDiagnostico(page, processo.codigo, UNIDADE);
        const dropdownAcoes = page.getByTestId(`dropdown-acoes-${TITULO_SERVIDOR_ASSESSORIA_12}`);
        await expect(dropdownAcoes).toBeVisible();
        await dropdownAcoes.getByRole('button', {name: 'Ações'}).click();
        await page.locator('[role="menu"]:visible').getByTestId(`btn-impossibilitar-${TITULO_SERVIDOR_ASSESSORIA_12}`).click();
        const modal = page.getByRole('dialog');
        await expect(modal).toContainText('Indicar impossibilidade de avaliação');
        await expect(modal).toContainText('João Guilherme de Albuquerque Maranhão');

        await page.getByTestId('btn-confirmar-impossibilitar').click();
        await expect(modal).toContainText('A justificativa é obrigatória.');

        await page.getByTestId('textarea-justificativa-impossibilidade').fill('Servidor afastado durante todo o período da avaliação.');
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/subprocessos/${codSubprocesso}/diagnostico/avaliacoes/${TITULO_SERVIDOR_ASSESSORIA_12}/impossibilitar`)
                && res.ok()
            ),
            page.getByTestId('btn-confirmar-impossibilitar').click()
        ]);

        await expect(page.getByText('Avaliação impossibilitada', {exact: true})).toBeVisible();
        await dropdownAcoes.getByRole('button', {name: 'Ações'}).click();
        const menuAcoes = page.locator('[role="menu"]:visible');
        await expect(menuAcoes.getByTestId(`btn-impossibilitar-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeDisabled();
        await expect(menuAcoes.getByTestId(`btn-desfazer-impossibilidade-${TITULO_SERVIDOR_ASSESSORIA_12}`)).toBeEnabled();
    });
});
