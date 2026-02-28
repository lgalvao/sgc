import {expect, test} from '../fixtures/base.js';
import {login, USUARIOS} from '../helpers/helpers-auth.js';
import {resetDatabase} from '../hooks/hooks-limpeza.js';

test.describe('UX-001 - Botões de modais', () => {
    test.beforeEach(async ({request}) => {
        await resetDatabase(request);
    });

    test('deve manter ordem Cancelar -> Confirmar no modal de iniciar processo', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await page.getByTestId('btn-painel-criar-processo').click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        await page.getByTestId('inp-processo-descricao').fill(`Processo UX Modal ${Date.now()}`);
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

        const dataLimite = new Date();
        dataLimite.setDate(dataLimite.getDate() + 30);
        await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);

        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        await page.getByTestId('btn-arvore-expand-COORD_11').click();
        await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').check();

        await page.getByTestId('btn-processo-iniciar').click();

        const modal = page.getByRole('dialog', {name: 'Iniciar processo'});
        await expect(modal).toBeVisible();
        const botoesRodape = modal.locator('.modal-footer button');

        await expect(botoesRodape).toHaveCount(2);
        await expect(botoesRodape.nth(0)).toContainText('Cancelar');
        await expect(botoesRodape.nth(1)).toContainText('Confirmar');
        await expect(modal.getByTestId('btn-iniciar-processo-cancelar')).toBeVisible();
        await expect(modal.getByTestId('btn-iniciar-processo-confirmar')).toBeVisible();
        await expect(botoesRodape.nth(0)).toHaveClass(/btn-secondary/);
        await expect(botoesRodape.nth(1)).toHaveClass(/btn-primary/);
    });
});
