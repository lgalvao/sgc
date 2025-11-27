import { test, expect } from './support/fixtures';
import { loginComoAdmin, loginComo } from './helpers';

/**
 * CDU-05: Iniciar processo de revisão
 * 
 * REWRITTEN: This test file now uses API calls for setup and explicit inline waits
 * to avoid the flakiness associated with the UI helpers.
 */
test.describe('CDU-05: Iniciar processo de revisão', () => {
    test.beforeEach(async ({ page }) => {
        await loginComoAdmin(page);
    });

    test('deve exibir modal de confirmação ao clicar em Iniciar processo', async ({ page }) => {
        const descricao = `Processo Modal ${Date.now()}`;

        // 1. Setup via API: Create process with SESEL (ID: 10)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'REVISAO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [10] // SESEL
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
        await expect(modal).toContainText('Ao iniciar o processo, não será mais possível editá-lo');
    });

    test('deve cancelar iniciação do processo ao clicar em Cancelar no modal', async ({ page }) => {
        const descricao = `Processo Cancelar ${Date.now()}`;

        // 1. Setup via API: Create process with SESEL (ID: 10)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'REVISAO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [10] // SESEL
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

        // 5. Cancel
        await page.getByTestId('btn-modal-cancelar').click();

        // 6. Verify Modal Closed and Still on Page
        await expect(page.locator('.modal.show')).not.toBeVisible();
        await expect(page).toHaveURL(/.*\/processo\/cadastro/);
        await expect(page.locator('#descricao')).toHaveValue(descricao);
    });

    test('deve iniciar processo de revisão e mudar situação para EM_ANDAMENTO', async ({ page }) => {
        const descricao = `Processo Iniciar ${Date.now()}`;

        // 1. Setup via API: Create process with SEDIA (ID: 9)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'REVISAO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [9] // SEDIA
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

        // 4. Click Iniciar and Confirm
        await page.getByTestId('btn-iniciar-processo').click();
        const iniciarResponsePromise = page.waitForResponse(response =>
            response.url().includes(`/api/processos/${codProcesso}/iniciar`) &&
            response.request().method() === 'POST' &&
            response.status() === 200
        );
        await page.getByTestId('btn-modal-confirmar').click();
        await iniciarResponsePromise;

        // 5. Verify Success (Navigation to Painel)
        await expect(page).toHaveURL(/.*\/painel/, { timeout: 5000 });

        // 6. Verify Process Status via API
        const checkResponse = await page.request.get(`http://localhost:10000/api/processos/${codProcesso}`);
        const updatedProcesso = await checkResponse.json();
        expect(updatedProcesso.situacao).toBe('EM_ANDAMENTO');
    });

    test('deve criar subprocessos para unidades participantes ao iniciar processo', async ({ page }) => {
        const descricao = `Processo Subprocessos ${Date.now()}`;

        // 1. Setup via API: Create process with SESEL (ID: 10)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'REVISAO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [10] // SESEL
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();
        const codProcesso = processo.codigo;

        // 2. Initiate Process via API
        const iniciarResponse = await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: {
                tipo: 'REVISAO',
                unidades: [10]
            }
        });
        expect(iniciarResponse.ok()).toBeTruthy();

        // 3. Verify Subprocess Creation via API
        const subprocessosResponse = await page.request.get(`http://localhost:10000/api/processos/${codProcesso}/subprocessos`);
        const subprocessos = await subprocessosResponse.json();
        expect(subprocessos.length).toBeGreaterThan(0);

        // Check SESEL subprocess (codUnidade: 10)
        const seselSubprocesso = subprocessos.find((s: any) => s.codUnidade === 10);
        expect(seselSubprocesso).toBeDefined();
        expect(seselSubprocesso.situacao).toBe('NAO_INICIADO');
        expect(seselSubprocesso.dataLimiteEtapa1).toContain('2025-12-31');
    });

    test('deve criar alertas para unidades participantes ao iniciar processo', async ({ page }) => {
        const descricao = `Processo Alertas ${Date.now()}`;

        // 1. Setup via API: Create process with SESEL (ID: 10)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'REVISAO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [10] // SESEL
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();
        const codProcesso = processo.codigo;

        // 2. Initiate Process via API
        const iniciarResponse = await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: {
                tipo: 'REVISAO',
                unidades: [10]
            }
        });
        if (!iniciarResponse.ok()) {
            console.log('Iniciar Processo Failed:', await iniciarResponse.text());
        }
        expect(iniciarResponse.ok()).toBeTruthy();

        // 3. Verify Alerts via API
        // The original test used `loginComo(page, USUARIOS.CHEFE_TESTE)`.
        // Let's use the helper for login but inline the verification.

        // Note: USUARIOS constant is not imported. Let's define the user here.
        // Log in as Ana Paula Souza (ID: 1) - Servidor of SESEL
        const SERVIDOR_SESEL = {
            titulo: '1',
            nome: 'Ana Paula Souza',
            senha: '123'
        };

        await loginComo(page, SERVIDOR_SESEL);

        // Check for alert in the table
        // Check for alert in the table
        // Check for alert in the table
        await expect(page.getByText(descricao).first()).toBeVisible({ timeout: 5000 });
        await expect(page.getByText(descricao)).toBeVisible();
    });

    test('deve preservar dados do processo após iniciação (somente leitura)', async ({ page }) => {
        const descricao = `Processo ReadOnly ${Date.now()}`;

        // 1. Setup via API: Create process with SESEL (ID: 10)
        const createResponse = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'REVISAO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [10] // SESEL
            }
        });
        expect(createResponse.ok()).toBeTruthy();
        const processo = await createResponse.json();
        const codProcesso = processo.codigo;

        // 2. Setup via API: Initiate process
        const iniciarResponse = await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: {
                tipo: 'REVISAO',
                unidades: [10]
            }
        });
        if (!iniciarResponse.ok()) {
            console.log('Iniciar Processo Failed:', await iniciarResponse.text());
        }
        expect(iniciarResponse.ok()).toBeTruthy();

        // 3. Try to navigate to edit page
        await page.goto(`http://localhost:5173/processo/cadastro?codProcesso=${codProcesso}`);

        // 4. Verify redirection to detail page
        await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}$`));
        await expect(page.locator('h2')).toContainText(descricao);

        // 5. Verify "Iniciar processo" button is NOT present
        await expect(page.getByTestId('btn-iniciar-processo')).not.toBeVisible();
    });
});
