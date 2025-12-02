import { expect, test } from '@playwright/test';
import { login, USUARIOS } from './helpers/auth';
import { criarProcesso, verificarProcessoNaTabela } from './helpers/processo-helpers';

test.describe('CDU-04 - Iniciar processo de mapeamento', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
    });

    test('Deve iniciar um processo com sucesso', async ({ page }) => {
        const descricao = `Processo para Iniciar - ${Date.now()}`;

        // 1. Cria processo em estado 'Criado'
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11'
        });

        // 2. Entra na edição
        await page.getByText(descricao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Aguarda carregamento dos dados
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricao);

        // 3. Clica em Iniciar
        await page.getByTestId('btn-processo-iniciar').click();

        // 4. Verifica Modal
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();

        // 5. Cancela
        await page.getByTestId('btn-mdl-iniciar-cancelar').click();
        await expect(modal).not.toBeVisible();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // 6. Inicia novamente e Confirma
        await page.getByTestId('btn-processo-iniciar').click();
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-mdl-iniciar-confirmar').click();

        // 7. Verifica redirecionamento e Status
        await expect(page).toHaveURL(/\/painel/);

        await verificarProcessoNaTabela(page, {
            descricao: descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
    });
});
