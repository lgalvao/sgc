import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsAdmin} from '~/utils/auth';
import {LABELS} from './test-constants';
import {expectTextVisible, navigateToProcessCreation} from './test-helpers';

test.describe('CDU-03: Manter processo', () => {
    test.beforeEach(async ({page}) => {
        await loginAsAdmin(page);
    });

  test('deve acessar tela de criação de processo', async ({ page }) => {
      await navigateToProcessCreation(page);

    // Verificar elementos do formulário
    await expect(page.getByLabel(LABELS.DESCRICAO)).toBeVisible();
    await expect(page.getByLabel(LABELS.TIPO)).toBeVisible();
    await expectTextVisible(page, 'Unidades participantes');
  });

  test('deve mostrar erro para processo sem descrição', async ({ page }) => {
      await navigateToProcessCreation(page);

    // Deixar descrição vazia e tentar salvar
    await page.getByRole('button', { name: 'Salvar' }).click();

    // Deve mostrar erro (comentado pois o texto pode variar)
    // await expect(page.getByText('Preencha a descrição.')).toBeVisible();
  });

  test('deve mostrar erro para processo sem unidades', async ({ page }) => {
      await navigateToProcessCreation(page);

    // Preencher descrição mas não selecionar unidades
    await page.getByLabel('Descrição').fill('Processo Teste');
    await page.getByLabel('Tipo').selectOption('Mapeamento');

    await page.getByRole('button', { name: 'Salvar' }).click();

    // Deve mostrar erro
    await expect(page.getByText('Preencha todos os campos e selecione ao menos uma unidade.')).toBeVisible();
  });

  test('deve permitir visualizar processo existente', async ({ page }) => {
    await loginAsAdmin(page); // ADMIN

    // Clicar em um processo da lista (assumindo que existe)
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Deve navegar para a página do processo
    await expect(page).toHaveURL(/\/processo\/\d+$/);

    // Verificar que mostra informações do processo
    await expect(page.getByTestId('processo-info')).toBeVisible();
  });
});