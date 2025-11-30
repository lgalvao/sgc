import { expect, test } from '@playwright/test';
import { autenticar, login, USUARIOS } from './helpers/auth';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    const timestamp = Date.now();
    const descricaoProcesso = `Processo CDU-08 ${timestamp}`;
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

    test('Deve permitir adicionar atividades e conhecimentos', async ({ page }) => {
        await page.goto('/login');
        await autenticar(page, USUARIO_CHEFE, 'senha');

        // Navigate to subprocess
        await page.getByText(descricaoProcesso).click();

        // 1. Acessa o card "Atividades e conhecimentos"
        await page.getByTestId('atividades-card').click();

        // 2. O sistema apresenta a lista de atividades (inicialmente vazia ou com importadas)
        // 3. Adicionar Atividade
        const nomeAtividade = `Atividade Nova ${timestamp}`;
        await page.getByTestId('input-nova-atividade').fill(nomeAtividade);
        await page.getByTestId('btn-adicionar-atividade').click();

        // Verify it appears
        await expect(page.getByText(nomeAtividade)).toBeVisible();

        // 4. Adicionar Conhecimento à Atividade
        // Find the card for the activity we just added
        const activityCard = page.locator('.atividade-card').filter({ hasText: nomeAtividade });

        const nomeConhecimento = `Conhecimento Novo ${timestamp}`;
        await activityCard.getByTestId('input-novo-conhecimento').fill(nomeConhecimento);
        await activityCard.getByTestId('btn-adicionar-conhecimento').click();

        // Verify knowledge appears
        await expect(activityCard.getByText(nomeConhecimento)).toBeVisible();

        // 5. Editar/Excluir (Optional verification)
        // Check for edit/delete buttons availability
        await expect(activityCard.getByTestId('btn-excluir-atividade')).toBeVisible();
        await expect(activityCard.getByTestId('btn-excluir-conhecimento')).toBeVisible();

        // 6. Concluir cadastro
        // Note: The button might be "Disponibilizar cadastro" or similar on the main subprocess page,
        // or a "Salvar" equivalent here.
        // In cdu-05 we saw user going back and then "Disponibilizar Mapa".
        // But for activities only, cdu-08 says "Concluir cadastro" which might trigger status change.
        // Assuming there is a back button to return to subprocess details.
        await page.getByTestId('btn-voltar').click();
    });
});
