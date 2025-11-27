import { test, expect } from './support/fixtures';
import { loginComoAdmin, loginComoGestor } from './helpers/auth';

/**
 * CDU-06: Detalhar processo
 * 
 * REWRITTEN: Uses API for setup and direct navigation.
 */
test.describe('CDU-06: Detalhar processo', () => {

    test('deve mostrar detalhes do processo para ADMIN', async ({ page }) => {
        const descricao = `Processo Detalhes ADMIN ${Date.now()}`;

        // 1. Setup via API: Create process
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

        // 2. Setup via API: Initiate process (to make it viewable in details mode)
        const initResponse = await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: {
                tipo: 'MAPEAMENTO',
                unidades: [2]
            }
        });
        expect(initResponse.ok(), `Falha ao iniciar processo: ${await initResponse.text()}`).toBeTruthy();

        // Verify process status via API
        const detailsResponse = await page.request.get(`http://localhost:10000/api/processos/${codProcesso}/detalhes`);
        const details = await detailsResponse.json();
        expect(details.situacao).toBe('EM_ANDAMENTO');
        expect(details.unidades.length).toBeGreaterThan(0);

        // 3. Login and Navigate directly to details page
        await loginComoAdmin(page);
        await page.goto(`/processo/${codProcesso}`);

        // 4. Verify Details
        await expect(page.getByTestId('processo-info')).toContainText(descricao);
        await expect(page.getByText('EM_ANDAMENTO')).toBeVisible();
        await expect(page.getByText('MAPEAMENTO')).toBeVisible();

        // 5. Verify Tree Table
        await expect(page.getByRole('table')).toBeVisible();
        // STIC should be visible in the tree (Name from data-minimal.sql)
        await expect(page.getByText('STIC - Secretaria de Informática e Comunicações')).toBeVisible();

        // 6. Verify Actions (ADMIN sees Finalizar)
        await expect(page.getByTestId('btn-finalizar-processo')).toBeVisible();
    });

    test('deve mostrar detalhes do processo para GESTOR participante', async ({ page }) => {
        const descricao = `Processo Detalhes GESTOR ${Date.now()}`;

        // 1. Setup via API: Create process with SEDESENV (ID 8) where Paulo Horta is GESTOR
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

        // 2. Setup via API: Initiate process
        const initResponse = await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: {
                tipo: 'MAPEAMENTO',
                unidades: [8]
            }
        });
        expect(initResponse.ok()).toBeTruthy();

        // 3. Login as GESTOR and Navigate
        await loginComoGestor(page);
        await page.goto(`/processo/${codProcesso}`);

        // 4. Verify Details
        await expect(page.getByTestId('processo-info')).toContainText(descricao);

        // 5. Verify Actions (GESTOR does NOT see Finalizar)
        await expect(page.getByTestId('btn-finalizar-processo')).not.toBeVisible();
    });

    test('deve exibir o botão "Iniciar Processo" quando o processo está em estado CRIADO', async ({ page }) => {
        const descricao = `Processo CRIADO ${Date.now()}`;

        // 1. Setup via API: Create process (do NOT initiate)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [2]
            }
        });
        expect(response.ok()).toBeTruthy();
        const processo = await response.json();
        const codProcesso = processo.codigo;

        // 2. Login and Navigate (Edit page because it's CRIADO)
        await loginComoAdmin(page);
        // Even if we try to go to details, logic might redirect or we go to edit explicitly
        // The test requirement says "exibir botão Iniciar Processo", which is on the Edit page (CadProcesso)
        await page.goto(`/processo/cadastro?codProcesso=${codProcesso}`);

        // 3. Verify Button
        await expect(page.getByTestId('btn-iniciar-processo')).toBeVisible();
        await expect(page.getByTestId('btn-finalizar-processo')).not.toBeVisible();
    });

    test('deve exibir o botão "Finalizar Processo" quando o processo está EM ANDAMENTO', async ({ page }) => {
        const descricao = `Processo EM ANDAMENTO ${Date.now()}`;

        // 1. Setup via API: Create and Initiate
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [2]
            }
        });
        const processo = await response.json();
        const codProcesso = processo.codigo;

        await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: { tipo: 'MAPEAMENTO', unidades: [2] }
        });

        // 2. Login and Navigate to Details
        await loginComoAdmin(page);
        await page.goto(`/processo/${codProcesso}`);

        // 3. Verify Buttons
        // Iniciar is on Edit page, Finalizar is on Details page
        await expect(page.getByTestId('btn-finalizar-processo')).toBeVisible();
        // We are on details page, so Iniciar button (from CadProcesso) shouldn't be here anyway,
        // but let's verify we are indeed on details page
        await expect(page.getByTestId('processo-info')).toBeVisible();
    });

    test('deve permitir clicar em unidade para ver detalhes do subprocesso', async ({ page }) => {
        const descricao = `Processo Navegacao ${Date.now()}`;

        // 1. Setup via API: Create and Initiate with STIC (ID 2)
        const response = await page.request.post('http://localhost:10000/api/processos', {
            data: {
                descricao: descricao,
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31T00:00:00',
                unidades: [2]
            }
        });
        const processo = await response.json();
        const codProcesso = processo.codigo;

        const initResponse = await page.request.post(`http://localhost:10000/api/processos/${codProcesso}/iniciar`, {
            data: { tipo: 'MAPEAMENTO', unidades: [2] }
        });
        expect(initResponse.ok()).toBeTruthy();

        // 2. Login and Navigate
        await loginComoAdmin(page);
        await page.goto(`/processo/${codProcesso}`);

        // 3. Click on STIC in the tree
        // The tree renders "STIC - Secretaria..."
        // We need to find the row and click it.
        // TreeRowItem has data-testid="tree-table-row-{id}"
        // STIC id is 2.

        // Wait for the row to be visible
        await expect(page.getByTestId('tree-table-row-2')).toBeVisible();
        await page.getByTestId('tree-table-row-2').click();

        // 4. Verify Navigation to Subprocesso
        // URL pattern: /processo/{codProcesso}/{sigla}
        await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}/STIC`));

        // Verify Subprocesso Header or content
        await expect(page.getByTestId('unidade-info')).toContainText('STIC');
        await expect(page.getByTestId('unidade-info')).toContainText('Secretaria de Informática e Comunicações');
    });
});
