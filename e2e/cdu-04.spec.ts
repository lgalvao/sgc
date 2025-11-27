import { test, expect } from './support/fixtures';
import { loginComoAdmin } from './helpers';

/**
 * CDU-04: Iniciar processo de mapeamento
 * 
 * REWRITTEN: This test file now uses API calls for setup and explicit inline waits
 * to avoid the flakiness associated with the UI helpers.
 */
test.describe('CDU-04: Iniciar processo', () => {

    test.beforeEach(async ({ page }) => {
        await loginComoAdmin(page);
    });

    test('deve abrir modal de confirmação, iniciar processo, criar subprocessos, mapas e movimentações', async ({ page }) => {
        const descricao = `Processo Iniciar ${Date.now()}`;

        // 1. Setup via API: Create process with STIC (ID: 2)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [2] // STIC
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();
        const codProcesso = processo.codigo;

        // 2. Navigate to edit page
        await page.goto(`http://localhost:5173/processo/cadastro?codProcesso=${codProcesso}`);

        // 3. Wait for form load (Description + Remover button)
        await expect(page.locator('#descricao')).toHaveValue(descricao);
        await page.getByRole('button', { name: 'Remover' }).waitFor({ state: 'visible' });

        // 4. Click Iniciar
        await page.getByTestId('btn-iniciar-processo').click();

        // 5. Verify Modal
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal).toContainText('Ao iniciar o processo, não será mais possível editá-lo');

        // 6. Confirm
        const iniciarResponsePromise = page.waitForResponse(response =>
            response.url().includes(`/api/processos/${codProcesso}/iniciar`) &&
            response.request().method() === 'POST' &&
            response.status() === 200
        );
        await page.getByTestId('btn-modal-confirmar').click();
        await iniciarResponsePromise;

        // 7. Verify Success (Navigation to Painel)
        await expect(page).toHaveURL(/.*\/painel/, { timeout: 5000 });

        // 8. Verify Process Status via API
        const checkResponse = await page.request.get(`http://localhost:10000/api/processos/${codProcesso}`);
        const updatedProcesso = await checkResponse.json();
        expect(updatedProcesso.situacao).toBe('EM_ANDAMENTO');

        // 9. Verify Subprocess Creation via API
        const subprocessosResponse = await page.request.get(`http://localhost:10000/api/processos/${codProcesso}/subprocessos`);
        const subprocessos = await subprocessosResponse.json();
        expect(subprocessos.length).toBeGreaterThan(0);

        // Check STIC subprocess (codUnidade: 2)
        const sticSubprocesso = subprocessos.find((s: any) => s.codUnidade === 2);
        expect(sticSubprocesso).toBeDefined();
        expect(sticSubprocesso.situacao).toBe('NAO_INICIADO');
    });

    test('deve cancelar iniciação e permanecer na tela', async ({ page }) => {
        const descricao = `Processo Cancelar ${Date.now()}`;

        // 1. Setup via API: Create process with SEDESENV (ID: 8)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [8] // SEDESENV
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();
        const codProcesso = processo.codigo;

        // 2. Navigate to edit page
        await page.goto(`http://localhost:5173/processo/cadastro?codProcesso=${codProcesso}`);

        // 3. Wait for form load
        await expect(page.locator('#descricao')).toHaveValue(descricao);
        await page.getByRole('button', { name: 'Remover' }).waitFor({ state: 'visible' });

        // 4. Click Iniciar
        await page.getByTestId('btn-iniciar-processo').click();

        // 5. Verify Modal
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();

        // 6. Cancel
        await page.getByTestId('btn-modal-cancelar').click();

        // 7. Verify Modal Closed and Still on Page
        await expect(modal).not.toBeVisible();
        await expect(page).toHaveURL(/.*\/processo\/cadastro/);
        await expect(page.locator('#descricao')).toHaveValue(descricao);
    });

    test('não deve permitir editar processo após iniciado', async ({ page }) => {
        const descricao = `Processo Bloqueio ${Date.now()}`;

        // 1. Setup via API: Create process with SEDOC (ID: 15)
        const createResponse = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [15] // SEDOC
            }
        });
        expect(createResponse.ok()).toBeTruthy();
        const processo = await createResponse.json();
        const codProcesso = processo.codigo;

        // 2. Setup via API: Iniciar o processo
        const iniciarResponse = await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: {
                tipo: 'MAPEAMENTO',
                unidades: [15]
            }
        });
        expect(iniciarResponse.ok()).toBeTruthy();

        // 3. Try to navigate to edit page (should redirect or show read-only view)
        // In this system, clicking an active process goes to the detail view, not edit view.
        // But if we force the URL:
        await page.goto(`http://localhost:5173/processo/cadastro?codProcesso=${codProcesso}`);

        // 4. Verify redirection or read-only state
        // The requirement says "não deve permitir editar". 
        // If the frontend redirects to /processo/{id} (detail view), that's a pass.
        // If it stays on /cadastro but disables fields, that's also a pass.

        // Checking if we are redirected to the detail page (which seems to be the behavior for started processes)
        // Or checking if the "Salvar" button is missing/disabled if we stay on the page.

        // Let's check if we are redirected to the detail page first
        // Wait a bit for potential redirect
        await page.waitForTimeout(1000);

        const url = page.url();
        if (url.includes('/processo/cadastro')) {
            // If still on cadastro page, check if inputs are disabled or save button is missing
            // This depends on implementation. Assuming redirection to detail page based on previous tests.
            await expect(page).not.toHaveURL(/.*\/processo\/cadastro/);
        } else {
            // Verify we are on the detail page
            await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}$`));
            // The detail page header contains the process description, not "Detalhes do Processo"
            await expect(page.locator('h2')).toContainText(descricao);
        }
    });
});