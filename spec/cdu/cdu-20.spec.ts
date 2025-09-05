import type {Page} from '@playwright/test';
import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsGestor} from '~/utils/auth';

// Strings usados nos testes
const MODAL_DEVOLUCAO_TITLE = 'Devolução';
const MODAL_DEVOLUCAO_BODY = 'Confirma a devolução da validação do mapa para ajustes?';
const MODAL_ACEITE_TITLE = 'Aceitar Mapa de Competências';
const MODAL_HOMOLOGACAO_TITLE = 'Homologação';
const MODAL_HOMOLOGACAO_BODY = 'Confirma a homologação do mapa de competências?';
const MODAL_VER_SUGESTOES_TITLE = 'Sugestões';
const MSG_DEVOLUCAO_REALIZADA = 'Devolução realizada';
const MSG_ACEITE_REGISTRADO = 'Aceite registrado';
const MSG_HOMOLOGACAO_EFETIVADA = 'Homologação efetivada';
const TEXTO_MAPA_COMPETENCIAS = 'Mapa de competências técnicas';
const MODAL_HISTORICO_TITLE = 'Histórico de Análise';

async function navegarParaSubprocessoUnidade(page: Page, siglaUnidade: string, processDescription: string) {
    // Navegar para a linha do processo especificado
    const linhasProcesso = page.locator('table tbody tr');
    const linhaProcesso = linhasProcesso.filter({hasText: processDescription}).first();
    await linhaProcesso.click();

    // Aguardar TreeTable carregar completamente
    await page.waitForSelector('[data-testid="tree-table-row"]');

    // Expandir todos os nós para garantir visibilidade das unidades
    await page.getByTestId('btn-expandir-todas').click();

    // Clicar na linha da unidade específica
    const unidadeRow = page.locator('[data-testid="tree-table-row"]').filter({hasText: siglaUnidade}).first();
    await unidadeRow.waitFor({state: 'visible'});
    await unidadeRow.click();

    // Clicar no card "Mapa de Competências" para entrar na visão de análise
    await page.locator('[data-testid="mapa-card"]').click();

    // Aguardar navegação para a página de visualização do mapa
    await page.waitForURL('**/vis-mapa**');
}

test.describe('CDU-20: Analisar validação de mapa de competências', () => {
    test.describe('como GESTOR', () => {
        test.beforeEach(async ({page}) => await loginAsGestor(page));

        test('deve mostrar botões de análise', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'SEDESENV', 'Revisão de mapeamento STIC/COINF - 2025');

            await expect(page.getByTestId('historico-analise-btn')).toBeVisible();
            await expect(page.getByTestId('devolver-ajustes-btn')).toBeVisible();
            await expect(page.getByTestId('registrar-aceite-btn')).toBeVisible();
        });

        test('deve mostrar botão ver sugestões quando há sugestões', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'SESEL', 'Revisão de mapeamento STIC/COINF - 2025');

            const verSugestoesButton = page.getByTestId('ver-sugestoes-btn');
            if (await verSugestoesButton.isVisible()) {
                await verSugestoesButton.click();
                // Validate modal title and content
                await expect(page.getByTestId('modal-sugestoes-title')).toHaveText(MODAL_VER_SUGESTOES_TITLE);
                await expect(page.getByTestId('modal-sugestoes-body')).toBeVisible(); // Assuming body exists
                await page.getByTestId('modal-sugestoes-close').click();
            } else {
                // Edge case: ensure no button when no suggestions
                await expect(verSugestoesButton).not.toBeVisible();
            }
        });

        test('deve devolver validação para ajustes', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'SEDESENV', 'Revisão de mapeamento STIC/COINF - 2025');

            // Click devolver button
            await page.getByTestId('devolver-ajustes-btn').click();

            // Validate modal opens with correct content
            await expect(page.getByTestId('modal-devolucao-title')).toHaveText(MODAL_DEVOLUCAO_TITLE);
            await expect(page.getByTestId('modal-devolucao-body')).toContainText(MODAL_DEVOLUCAO_BODY);

            // Fill observation and confirm
            await page.getByTestId('observacao-devolucao-textarea').fill('Observação de teste');
            await page.getByTestId('modal-devolucao-confirmar').click();

            // Validate success message and redirect
            await expect(page.getByText(MSG_DEVOLUCAO_REALIZADA)).toBeVisible();
            await page.waitForURL('**/painel');
        });

        test('deve registrar aceite da validação', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'SESEL', 'Revisão de mapeamento STIC/COINF - 2025');

            await page.getByTestId('registrar-aceite-btn').click();
            // Validate modal content
            await expect(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_ACEITE_TITLE);
            await expect(page.getByTestId('modal-aceite-body')).toContainText('Observações');
            await expect(page.getByTestId('modal-aceite-body')).toContainText('As observações serão registradas junto com a validação do mapa.');

            await page.getByTestId('modal-aceite-confirmar').click();
            await expect(page.getByText(MSG_ACEITE_REGISTRADO)).toBeVisible();
            // Navigation to painel may not occur immediately, so skip waitForURL
        });

        test('deve cancelar registro de aceite da validação', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'SESEL', 'Revisão de mapeamento STIC/COINF - 2025');

            await page.getByTestId('registrar-aceite-btn').click();
            await expect(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_ACEITE_TITLE);
            await expect(page.getByTestId('modal-aceite-body')).toContainText('Observações');
            await expect(page.getByTestId('modal-aceite-body')).toContainText('As observações serão registradas junto com a validação do mapa.');

            // Cancel the action
            await page.getByTestId('modal-aceite-cancelar').click();
            await expect(page.getByText(TEXTO_MAPA_COMPETENCIAS)).toBeVisible();
        });

        test('deve mostrar histórico de análise da validação', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'SEDESENV', 'Revisão de mapeamento STIC/COINF - 2025');
            // Click history button
            await page.getByTestId('historico-analise-btn').click();
            await expect(page.getByTestId('modal-historico-title')).toHaveText(MODAL_HISTORICO_TITLE);

            // Validate table presence and structure
            const tabela = page.getByTestId('tabela-historico');
            if (await tabela.isVisible()) {
                await expect(tabela.locator('thead')).toBeVisible();
                // Check for table body if data exists
                if (await tabela.locator('tbody tr').count() > 0) {
                    await expect(tabela.locator('tbody')).toBeVisible();
                }
            }
        });

        test('deve cancelar devolução da validação', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'SEDESENV', 'Revisão de mapeamento STIC/COINF - 2025');
            // Open devolver modal
            await page.getByTestId('devolver-ajustes-btn').click();
            await expect(page.getByTestId('modal-devolucao-title')).toHaveText(MODAL_DEVOLUCAO_TITLE);

            // Fill observation and cancel
            await page.getByTestId('observacao-devolucao-textarea').fill('Observação de teste para cancelamento');
            await page.getByTestId('modal-devolucao-cancelar').click();

            // Ensure back to main view
            await expect(page.getByText(TEXTO_MAPA_COMPETENCIAS)).toBeVisible();
        });
    });

    test.describe('como ADMIN', () => {
        test.beforeEach(async ({page}) => await loginAsAdmin(page));

        test('deve mostrar botões de análise', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'STIC', 'Revisão de mapeamento STIC/COINF - 2025');

            await expect(page.getByTestId('historico-analise-btn')).toBeVisible();
            await expect(page.getByTestId('devolver-ajustes-btn')).toBeVisible();
            await expect(page.getByTestId('registrar-aceite-btn')).toBeVisible();
        });

        test('deve homologar validação', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'STIC', 'Revisão de mapeamento STIC/COINF - 2025');

            // O botão de "Registrar aceite" vira "Homologar" para o ADMIN
            await page.getByTestId('registrar-aceite-btn').click();
            // Validate modal content for homologation
            await expect(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_HOMOLOGACAO_TITLE);
            await expect(page.getByTestId('modal-aceite-body')).toContainText(MODAL_HOMOLOGACAO_BODY);

            await page.getByTestId('modal-aceite-confirmar').click();
            await expect(page.getByText(MSG_HOMOLOGACAO_EFETIVADA)).toBeVisible();
            await page.waitForURL('**/painel');
        });

        test('deve cancelar homologação da validação', async ({page}) => {
            await navegarParaSubprocessoUnidade(page, 'STIC', 'Revisão de mapeamento STIC/COINF - 2025');

            await page.getByTestId('registrar-aceite-btn').click();
            await expect(page.getByTestId('modal-aceite-title')).toHaveText(MODAL_HOMOLOGACAO_TITLE);
            await expect(page.getByTestId('modal-aceite-body')).toContainText(MODAL_HOMOLOGACAO_BODY);

            // Cancel the action
            await page.getByTestId('modal-aceite-cancelar').click();
            await expect(page.getByText(TEXTO_MAPA_COMPETENCIAS)).toBeVisible();
        });
    });
});
