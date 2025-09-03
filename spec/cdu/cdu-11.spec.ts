import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsChefe, loginAsGestor, loginAsServidor} from '../utils/auth';

test.describe('CDU-11: Visualizar cadastro de atividades e conhecimentos', () => {
  test('deve visualizar cadastro como ADMIN', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Clicar no processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Como ADMIN, deve ver detalhes do processo e unidades subordinadas
    await expect(page.getByText('Detalhes do processo')).toBeVisible();

    // Clicar em uma unidade subordinada
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar no card Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar exibição dos dados
    await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();
    await expect(page.locator('table')).toBeVisible();

    // Verificar que não há botões de edição (modo visualização)
    await expect(page.getByRole('button', { name: 'Adicionar atividade' })).not.toBeVisible();
    await expect(page.getByRole('button', { name: 'Disponibilizar' })).not.toBeVisible();
  });

  test('deve visualizar cadastro como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Clicar no processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Como GESTOR, deve ver detalhes do processo
    await expect(page.getByText('Detalhes do processo')).toBeVisible();

    // Clicar em uma unidade subordinada
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar no card Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar exibição dos dados
    await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();
    await expect(page.locator('table')).toBeVisible();
  });

  test('deve visualizar cadastro como CHEFE', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Clicar no processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Como CHEFE, deve ir direto para detalhes do subprocesso
    await expect(page.getByText('Detalhes do subprocesso')).toBeVisible();

    // Clicar no card Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar exibição dos dados
    await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();
    await expect(page.locator('table')).toBeVisible();

    // Verificar que os dados da unidade estão visíveis
    await expect(page.getByText(/SESEL/)).toBeVisible(); // Sigla da unidade
  });

  test('deve visualizar cadastro como SERVIDOR', async ({ page }) => {
    // Login como SERVIDOR
    await loginAsServidor(page);

    // Clicar no processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Como SERVIDOR, deve ir direto para detalhes do subprocesso
    await expect(page.getByText('Detalhes do subprocesso')).toBeVisible();

    // Clicar no card Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar exibição dos dados
    await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();
    await expect(page.locator('table')).toBeVisible();

    // Verificar estrutura da tabela (atividades como cabeçalhos, conhecimentos como linhas)
    const table = page.locator('table');
    await expect(table.locator('thead')).toBeVisible();
    await expect(table.locator('tbody')).toBeVisible();
  });
});