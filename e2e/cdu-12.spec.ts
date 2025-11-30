import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-12 - Verificar impactos no mapa de competências', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-12 ${timestamp}`;
    const UNIDADE_ALVO = 'ASSESSORIA_21';

    test.beforeAll(async ({ browser }) => {
        // Setup: Create REVISION process
        const page = await browser.newPage();
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descricaoProcesso,
            tipo: 'REVISAO', // Must be Revision
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        await page.getByRole('row', { name: descricaoProcesso }).click();
        await page.getByTestId('btn-iniciar-processo').click();
        await page.getByTestId('btn-modal-confirmar').click();
        await page.close();
    });

    test('Deve acessar verificação de impactos', async ({ page }) => {
        // Using ADMIN as it has access in 'Revisão do cadastro homologada' or 'Mapa Ajustado'
        // OR as CHEFE if 'Revisão do cadastro em andamento'.
        // Let's use CHEFE as the process just started.

        await page.goto('/login');
        // Chefe of ASSESSORIA_21
        const USUARIO_CHEFE = '777777';
        await login(page, USUARIO_CHEFE, 'senha');

        await page.getByText(descricaoProcesso).click();

        // 3. Access via 'Atividades e conhecimentos' card
        await page.getByTestId('atividades-card').click();

        // 4. Click 'Impactos no mapa' button
        const btnImpactos = page.getByTestId('btn-impactos-mapa');
        await expect(btnImpactos).toBeVisible();
        await btnImpactos.click();

        // 6. If no divergence, shows message
        // Since we just started and likely have no previous map or no changes,
        // it might say "Nenhum impacto" or "Mapa vigente não encontrado" if no baseline exists.
        // We'll check for the modal or the message.

        const modal = page.getByRole('dialog');
        const message = page.getByText('Nenhum impacto no mapa da unidade');

        await Promise.any([
            expect(modal).toBeVisible(),
            expect(message).toBeVisible()
        ]);

        // If modal appears, close it
        if (await modal.isVisible()) {
            await modal.getByRole('button', { name: 'Fechar' }).click();
        }
    });
});
