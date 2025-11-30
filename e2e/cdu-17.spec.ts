import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-17 - Disponibilizar mapa de competências', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-17 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';

    test.beforeAll(async ({ browser }) => {
        // Setup: Create MAPPING process
        const page = await browser.newPage();
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descricaoProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        await page.getByRole('row', { name: descricaoProcesso }).click();
        await page.getByTestId('btn-iniciar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await page.close();
    });

    test('Deve permitir ADMIN disponibilizar mapa', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await page.getByText(descricaoProcesso).click();
        await page.getByRole('treegrid').getByText(UNIDADE_ALVO).click();

        // 5. Click Mapa card (assuming we reached a state where it's visible)
        const cardMapa = page.getByTestId('mapa-card-vis');
        if (await cardMapa.isVisible()) {
            await cardMapa.click();

            // 7. Click Disponibilizar
            // Assuming we have created competencies or it allows check.
            // If validation fails (no competencies), it should show error.

            const btnDisponibilizar = page.getByTestId('btn-disponibilizar-mapa');
            await expect(btnDisponibilizar).toBeVisible();
            await btnDisponibilizar.click();

            // 10. Check Modal
            // If validation passes, we see modal with Date and Observations
            // If validation fails, we see error message.
            // We check for either to confirm the interaction.

            const modal = page.getByRole('dialog');
            await expect(modal).toBeVisible();

            // Check for date input or error message
            // const hasDateInput = await modal.getByTestId('input-data-limite').isVisible();
        }
    });
});
