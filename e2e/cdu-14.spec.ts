import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-14 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';

    test.beforeAll(async ({ browser }) => {
        // Setup: Create and start REVISION process
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

    test('Deve permitir analisar revisão (Impactos/Devolver/Homologar)', async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await page.getByText(descricaoProcesso).click();

        // Select Unit
        await page.getByRole('treegrid').getByText(UNIDADE_ALVO).click();

        // 5. Click Atividades card
        await page.getByTestId('atividades-card').click();

        // 6. Check buttons including Impactos
        await expect(page.getByTestId('btn-impactos-mapa')).toBeVisible();
        await expect(page.getByTestId('btn-historico-analise')).toBeVisible();
        await expect(page.getByTestId('btn-registrar-aceite-homologar')).toBeVisible();
    });
});
