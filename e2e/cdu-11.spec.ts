import { expect, test } from '@playwright/test';
import { autenticar, login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-11 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';

    test.beforeAll(async ({ browser }) => {
        // Setup: Create and start process
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

    test('Deve visualizar cadastro de atividades e conhecimentos', async ({ page }) => {
        await page.goto('/login');
        // Any user profile with access can view, let's use CHEFE
        await autenticar(page, USUARIO_CHEFE, 'senha');

        // 1. Click process
        await page.getByText(descricaoProcesso).click();

        // 3.1 Exibe detalhes do subprocesso (Chefe goes direct)
        // 4. Click card Atividades
        await page.getByTestId('atividades-card').click();

        // 5. Verifica tela
        await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();

        // 6. Check header with unit info
        await expect(page.getByText(UNIDADE_ALVO)).toBeVisible();

        // Check for table structure (even if empty)
        // The spec implies a visual structure: activity description header, knowledge rows.
        // We can check for a container that would hold this.
        await expect(page.locator('.atividades-list, .atividade-card')).toBeVisible();
    });
});
