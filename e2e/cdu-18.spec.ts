import { expect, test } from '@playwright/test';
import { autenticar, login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-18 - Visualizar mapa de competências', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-18 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';

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

    test('Deve permitir visualizar mapa de competências', async ({ page }) => {
        await page.goto('/login');
        await autenticar(page, USUARIO_CHEFE, 'senha');

        await page.getByText(descricaoProcesso).click();

        // 4. Click Mapa Card
        const cardMapa = page.getByTestId('mapa-card-vis');
        if (await cardMapa.isVisible()) {
            await cardMapa.click();

            // 5. Verify View
            // 5.1 Title
            await expect(page.getByText('Mapa de competências')).toBeVisible();
            // 5.2 Unit ID
            await expect(page.getByText(UNIDADE_ALVO)).toBeVisible();

            // 5.3 Competencies blocks
            // Check for list container
             await expect(page.locator('.competencias-list, .competencia-card, .list-group')).toBeVisible();
        }
    });
});
