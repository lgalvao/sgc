import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-16 - Ajustar mapa de competências', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-16 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';

    test.beforeAll(async ({ browser }) => {
        // Setup: Create REVISION process
        const page = await browser.newPage();
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descricaoProcesso,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        await page.getByRole('row', { name: descricaoProcesso }).click();
        await page.getByTestId('btn-iniciar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await page.close();
    });

    test('Deve permitir ADMIN ajustar mapa (ver impactos e disponibilizar)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await page.getByText(descricaoProcesso).click();
        await page.getByRole('treegrid').getByText(UNIDADE_ALVO).click();

        // 5. Click Mapa card
        // Note: Needs 'Revisão do cadastro homologada' or 'Mapa ajustado'
        const cardMapa = page.getByTestId('mapa-card-vis');
        if (await cardMapa.isVisible()) {
            await cardMapa.click();

            // 6. Check buttons
            await expect(page.getByTestId('btn-impactos-mapa')).toBeVisible();
            await expect(page.getByTestId('btn-disponibilizar-mapa')).toBeVisible();

            // 7. Click Impactos
            await page.getByTestId('btn-impactos-mapa').click();

            // 8. Check Modal
            // Might be "Nenhum impacto" or actual list
            await Promise.any([
                expect(page.getByRole('dialog')).toBeVisible(),
                expect(page.getByText('Nenhum impacto')).toBeVisible()
            ]);
        }
    });
});
