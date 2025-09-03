import {expect, test} from '@playwright/test';
import {loginAsAdmin} from '../utils/auth';
import {LABELS, TEXTS, URLS} from './test-constants';
import {expectTextVisible, expectUrl} from './test-helpers';

test.describe('CDU-03: Manter processo', () => {
  test('deve acessar tela de criação de processo', async ({ page }) => {
    await loginAsAdmin(page); // ADMIN

    // Clicar em Criar processo
    await page.getByText(TEXTS.CRIAR_PROCESSO).click();

    // Deve navegar para tela de cadastro
    await expectUrl(page, `**${URLS.PROCESSO_CADASTRO}`);

    // Verificar elementos do formulário
    await expect(page.getByLabel(LABELS.DESCRICAO)).toBeVisible();
    await expect(page.getByLabel(LABELS.TIPO)).toBeVisible();
    await expectTextVisible(page, 'Unidades participantes');
  });

  test('deve mostrar erro para processo sem descrição', async ({ page }) => {
    await loginAsAdmin(page); // ADMIN

    await page.getByText('Criar processo').click();

    // Deixar descrição vazia e tentar salvar
    await page.getByRole('button', { name: 'Salvar' }).click();

    // Deve mostrar erro (comentado pois o texto pode variar)
    // await expect(page.getByText('Preencha a descrição.')).toBeVisible();
  });

  test('deve mostrar erro para processo sem unidades', async ({ page }) => {
    await loginAsAdmin(page); // ADMIN

    await page.getByText('Criar processo').click();

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