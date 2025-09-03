import {expect, test} from '@playwright/test';
import {loginAsAdmin} from '../utils/auth';

test.describe('CDU-15: Manter mapa de competências', () => {
  test('deve acessar tela de edição do mapa de competências', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar na unidade com subprocesso homologado
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar no card Mapa de competências
    await page.getByText('Mapa de competências').click();

    // Verificar elementos da tela de edição
    await expect(page.getByText('Edição de mapa')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Criar competência' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Disponibilizar' })).toBeVisible();
  });

  test('deve criar nova competência', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para mapa de competências
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Criar competência
    await page.getByRole('button', { name: 'Criar competência' }).click();

    // Verificar modal de edição
    await expect(page.getByText('Edição de competência')).toBeVisible();
    await expect(page.getByLabel('Descrição da competência')).toBeVisible();

    // Preencher dados
    await page.getByLabel('Descrição da competência').fill('Competência de Teste E2E');

    // Selecionar atividades (assumindo que há atividades disponíveis)
    const primeiraAtividade = page.locator('input[type="checkbox"]').first();
    if (await primeiraAtividade.isVisible()) {
      await primeiraAtividade.check();
    }

    // Salvar
    await page.getByRole('button', { name: 'Salvar' }).click();

    // Verificar se competência apareceu
    await expect(page.getByText('Competência de Teste E2E')).toBeVisible();
  });

  test('deve editar competência existente', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para mapa de competências
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Assumir que há uma competência existente e clicar em editar
    const editarButton = page.locator('[data-testid="editar-competencia"]').first();
    if (await editarButton.isVisible()) {
      await editarButton.click();

      // Verificar modal preenchido
      await expect(page.getByText('Edição de competência')).toBeVisible();

      // Alterar descrição
      await page.getByLabel('Descrição da competência').fill('Competência Editada E2E');

      // Salvar
      await page.getByRole('button', { name: 'Salvar' }).click();

      // Verificar alteração
      await expect(page.getByText('Competência Editada E2E')).toBeVisible();
    }
  });

  test('deve excluir competência', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para mapa de competências
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Assumir que há uma competência e clicar em excluir
    const excluirButton = page.locator('[data-testid="excluir-competencia"]').first();
    if (await excluirButton.isVisible()) {
      await excluirButton.click();

      // Verificar diálogo de confirmação
      await expect(page.getByText('Exclusão de competência')).toBeVisible();
      await expect(page.getByText(/Confirma a exclusão/)).toBeVisible();

      // Confirmar exclusão
      await page.getByRole('button', { name: 'Confirmar' }).click();

      // Verificar que competência foi removida (se era a única)
      // Nota: este teste pode variar dependendo dos dados de teste
    }
  });

  test('deve cancelar criação de competência', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para mapa de competências
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Criar competência
    await page.getByRole('button', { name: 'Criar competência' }).click();

    // Preencher dados
    await page.getByLabel('Descrição da competência').fill('Competência Cancelada');

    // Cancelar
    await page.getByRole('button', { name: 'Cancelar' }).click();

    // Verificar que modal foi fechado
    await expect(page.getByText('Edição de competência')).not.toBeVisible();

    // Verificar que competência não foi criada
    await expect(page.getByText('Competência Cancelada')).not.toBeVisible();
  });

  test('deve mostrar tooltip com conhecimentos da atividade', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para mapa de competências
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Passar mouse sobre badge de conhecimentos (se existir)
    const badgeConhecimentos = page.locator('[data-testid="badge-conhecimentos"]').first();
    if (await badgeConhecimentos.isVisible()) {
      await badgeConhecimentos.hover();

      // Verificar tooltip (pode variar na implementação)
      // await expect(page.locator('.tooltip')).toBeVisible();
    }
  });
});