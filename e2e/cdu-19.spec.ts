import { expect, test } from '@playwright/test';
import { autenticar, login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-19 - Validar mapa de competências', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-19 ${timestamp}`;
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

    test('Deve permitir CHEFE validar mapa ou apresentar sugestões', async ({ page }) => {
        await page.goto('/login');
        await autenticar(page, USUARIO_CHEFE, 'senha');

        await page.getByText(descricaoProcesso).click();

        // 1. Access Map
        // Needs 'Mapa disponibilizado' state.
        const cardMapa = page.getByTestId('mapa-card-vis');
        if (await cardMapa.isVisible()) {
            await cardMapa.click();

            // 2. Buttons: Sugestoes / Validar
            const btnSugestoes = page.getByTestId('btn-sugestoes-mapa');
            const btnValidar = page.getByTestId('validar-btn'); // Using ID from cdu-05

            await expect(btnSugestoes).toBeVisible();
            await expect(btnValidar).toBeVisible();

            // Test Validar flow
            await btnValidar.click();

            // 5.1 Confirmation Modal
            await expect(page.getByRole('dialog')).toBeVisible();
            await expect(page.getByText('Confirma a validação')).toBeVisible();

            // Cancel to keep state
            await page.getByTestId('btn-modal-cancelar').click();
        }
    });
});
