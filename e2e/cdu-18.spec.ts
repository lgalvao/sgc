import { test, expect } from '@playwright/test';
import { login, USUARIOS } from './helpers/helpers-auth';

test.describe('CDU-18: Visualizar mapa de competências', () => {

    test.beforeEach(async ({ page }) => {
        // Admin login
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
    });

    test('Deve visualizar mapa de competências corretamente', async ({ page }) => {
        // 1. Navigate to Panel
        await page.goto('/painel');

        // 2. Click on 'Processo em Andamento' (from seed)
        // Wait for table row to appear
        await page.getByRole('row', { name: 'Processo em Andamento' }).click();

        // 3. Admin view: details of process.
        // Wait for TreeTable to load and expand
        // Look for 'ASSESSORIA_11'
        await expect(page.getByText('ASSESSORIA_11')).toBeVisible();
        await page.getByText('ASSESSORIA_11').click();

        // 4. Subprocess view. Verify we are on Subprocess page.
        // Title usually contains unit name
        await expect(page.getByText('Detalhes do Subprocesso')).toBeVisible();
        await expect(page.getByText('ASSESSORIA_11')).toBeVisible();

        // Click on 'Mapa de Competências'
        // Likely a card or a button. "Mapa de Competências"
        await page.getByText('Mapa de Competências').click();

        // 5. Verify Map View
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        // Check for unit name again
        await expect(page.getByText('ASSESSORIA_11')).toBeVisible();

        // Verify seeded content from import.sql
        // Competencia: 'Competencia Técnica 1'
        // Atividade: 'Atividade 1'
        // Conhecimento: 'Conhecimento 1'
        await expect(page.getByText('Competencia Técnica 1')).toBeVisible();
        await expect(page.getByText('Atividade 1')).toBeVisible();
        await expect(page.getByText('Conhecimento 1')).toBeVisible();
    });
});
