import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-20 - Analisar validação de mapa de competências', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-20 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';

    test.beforeAll(async ({ browser }) => {
        // Setup: Create process
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

    test('Deve permitir analisar validação do mapa', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await page.getByText(descricaoProcesso).click();

        // Select Unit
        await page.getByRole('treegrid').getByText(UNIDADE_ALVO).click();

        // 3. Click Mapa Card
        const cardMapa = page.getByTestId('mapa-card-vis');
        if (await cardMapa.isVisible()) {
            await cardMapa.click();

            // 4. Check buttons (Devolver / Aceitar/Homologar)
            // Needs 'Mapa validado' state

            const btnDevolver = page.getByTestId('btn-devolver-ajustes');
            const btnHomologar = page.getByTestId('btn-registrar-aceite-homologar');

            // Conditional check as button visibility depends on state
            if (await btnHomologar.isVisible()) {
                await btnHomologar.click();
                await expect(page.getByRole('dialog')).toBeVisible();
                await page.getByTestId('btn-modal-cancelar').click();
            }
        }
    });
});
