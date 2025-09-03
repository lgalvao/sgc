import {expect, test} from '@playwright/test';
import {loginAsAdmin} from '../utils/auth';

test.describe('CDU-17: Disponibilizar mapa de competências', () => {
  test('deve acessar tela de edição e clicar em Disponibilizar', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar na unidade com mapa criado
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar no card Mapa de competências
    await page.getByText('Mapa de competências').click();

    // Verificar tela de edição
    await expect(page.getByText('Edição de mapa')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Disponibilizar' })).toBeVisible();

    // Clicar em Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();
  });

  test('deve mostrar erro quando competência não tem atividade associada', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Criar competência sem associar atividade (para teste)
    await page.getByRole('button', { name: 'Criar competência' }).click();
    await page.getByLabel('Descrição da competência').fill('Competência sem atividade');
    // Não selecionar nenhuma atividade
    await page.getByRole('button', { name: 'Salvar' }).click();

    // Tentar disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar mensagem de erro
    await expect(page.getByText(/competência.*sem associação|atividade.*associada/i)).toBeVisible();
  });

  test('deve mostrar erro quando atividade não tem competência associada', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Assumir que há atividades não associadas
    // Tentar disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar mensagem de erro
    await expect(page.getByText(/atividade.*sem.*competência|não.*associada/i)).toBeVisible();
  });

  test('deve abrir modal de disponibilização com validações OK', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo com mapa válido
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar modal
    await expect(page.getByText('Disponibilização do mapa de competências')).toBeVisible();

    // Verificar campos obrigatórios
    await expect(page.getByLabel('Data limite')).toBeVisible();
    await expect(page.getByLabel(/observações/i)).toBeVisible();

    // Verificar botões
    await expect(page.getByRole('button', { name: 'Disponibilizar' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Cancelar' })).toBeVisible();
  });

  test('deve cancelar disponibilização', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Cancelar
    await page.getByRole('button', { name: 'Cancelar' }).click();

    // Verificar que permanece na tela de edição
    await expect(page.getByText('Edição de mapa')).toBeVisible();
    await expect(page.getByText('Disponibilização do mapa de competências')).not.toBeVisible();
  });

  test('deve disponibilizar mapa com sucesso', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Preencher campos obrigatórios
    await page.getByLabel('Data limite').fill('2025-12-31');

    // Opcionalmente preencher observações
    await page.getByLabel(/observações/i).fill('Observações de teste para disponibilização');

    // Confirmar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar mensagem de sucesso
    await expect(page.getByText('Mapa de competências disponibilizado')).toBeVisible();

    // Verificar redirecionamento
    await page.waitForURL('**/painel');
  });

  test('deve validar campo data obrigatório', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Tentar confirmar sem preencher data
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar que modal permanece aberto (validação)
    await expect(page.getByText('Disponibilização do mapa de competências')).toBeVisible();

    // Verificar mensagem de erro ou que campo é obrigatório
    await expect(page.getByLabel('Data limite')).toHaveAttribute('required');
  });
});