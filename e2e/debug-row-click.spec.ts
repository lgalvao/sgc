import { test, expect } from '@playwright/test';
import { USUARIOS } from './support/usuarios';
import { autenticar } from './support/auth';

test('Debug ADMIN row click', async ({ page }) => {
    const timestamp = Date.now();
    const descricaoMapeamento = `Mapeamento Debug ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';

    // Setup: Admin creates and starts process
    await autenticar(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

    // Create process via API
    const processoResponse = await page.request.post('/api/processos', {
        data: {
            descricao: descricaoMapeamento,
            tipo: 'MAPEAMENTO',
            unidades: [UNIDADE_ALVO],
            dataLimiteEtapa1: '2025-12-30'
        }
    });
    const processo = await processoResponse.json();

    // Start process via API
    await page.request.post(`/api/processos/${processo.codigo}/iniciar`);

    // Go to Painel and click on process
    await page.goto('/painel');
    await expect(page).toHaveURL(/\/painel/);

    // Click on process - should show ProcessoView for ADMIN
    await page.getByText(descricaoMapeamento).click();
    await page.waitForURL(/\/processo\/\d+$/);

    // Take screenshot
    await page.screenshot({ path: 'test-results/admin-processo-view.png', fullPage: true });

    // Check that row exists
    const row = page.getByRole('row', { name: new RegExp(UNIDADE_ALVO) });
    await expect(row).toBeVisible({ timeout: 5000 });

    console.log('Row found, checking attributes...');

    // Get row element to check clickable attribute
    const rowHandle = await row.elementHandle();
    const dataTestId = await rowHandle?.getAttribute('data-testid');
    console.log('Row data-testid:', dataTestId);

    // Try clicking
    console.log('Attempting to click row...');
    await row.click();

    // Wait a bit to see if navigation happens
    await page.waitForTimeout(2000);

    // Take another screenshot
    await page.screenshot({ path: 'test-results/after-row-click.png', fullPage: true });

    console.log('Final URL:', page.url());
});
