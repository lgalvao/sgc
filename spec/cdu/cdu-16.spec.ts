import {expect, test} from '@playwright/test';
import {loginAsAdmin} from '~/utils/auth';

test.describe('CDU-16: Ajustar mapa de competências', () => {
  test('deve acessar tela de ajuste do mapa de competências', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar na unidade com subprocesso de revisão homologado
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar no card Mapa de competências
    await page.getByText('Mapa de competências').click();

    // Verificar elementos da tela de edição para revisão
    await expect(page.getByText('Edição de mapa')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Impactos no mapa' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Disponibilizar' })).toBeVisible();
  });

  test('deve verificar impactos antes de ajustar mapa', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Impactos no mapa
    await page.getByRole('button', { name: 'Impactos no mapa' }).click();

    // Verificar modal de impactos
    await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();

    // Fechar modal
    await page.getByRole('button', { name: 'Fechar' }).click();
    await expect(page.getByText('Impacto no Mapa de Competências')).not.toBeVisible();
  });

  test('deve ajustar mapa após verificar impactos', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Verificar impactos primeiro
    await page.getByRole('button', { name: 'Impactos no mapa' }).click();
    await page.getByRole('button', { name: 'Fechar' }).click();

    // Fazer ajustes no mapa (editar competência existente)
    const editarButton = page.locator('[data-testid="editar-competencia"]').first();
    if (await editarButton.isVisible()) {
      await editarButton.click();

      // Alterar descrição baseada nos impactos
      await page.getByLabel('Descrição da competência').fill('Competência Ajustada por Impactos E2E');

      // Salvar
      await page.getByRole('button', { name: 'Salvar' }).click();

      // Verificar alteração
      await expect(page.getByText('Competência Ajustada por Impactos E2E')).toBeVisible();
    }
  });

  test('deve criar nova competência durante ajuste', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Clicar em Criar competência
    await page.getByRole('button', { name: 'Criar competência' }).click();

    // Preencher dados
    await page.getByLabel('Descrição da competência').fill('Nova Competência de Ajuste E2E');

    // Selecionar atividades não associadas (conforme requisito)
    const atividadesNaoAssociadas = page.locator('input[type="checkbox"]:not(:checked)');
    if (await atividadesNaoAssociadas.first().isVisible()) {
      await atividadesNaoAssociadas.first().check();
    }

    // Salvar
    await page.getByRole('button', { name: 'Salvar' }).click();

    // Verificar criação
    await expect(page.getByText('Nova Competência de Ajuste E2E')).toBeVisible();
  });

  test('deve associar atividades não associadas durante ajuste', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Verificar se há atividades não associadas
    // Este teste assume que o sistema indica atividades não associadas
    const atividadesNaoAssociadas = page.locator('[data-testid="atividade-nao-associada"]');

    if (await atividadesNaoAssociadas.first().isVisible()) {
      // Criar competência para associar
      await page.getByRole('button', { name: 'Criar competência' }).click();
      await page.getByLabel('Descrição da competência').fill('Competência para Atividades Não Associadas');

      // Associar atividades não associadas
      await atividadesNaoAssociadas.first().check();

      // Salvar
      await page.getByRole('button', { name: 'Salvar' }).click();

      // Verificar que não há mais atividades não associadas
      await expect(page.locator('[data-testid="atividade-nao-associada"]')).not.toBeVisible();
    }
  });

  test('deve finalizar ajuste e disponibilizar mapa', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Mapa de competências').click();

    // Após ajustes, clicar em Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar que segue para o fluxo de disponibilização
    // (Este teste pode variar dependendo da implementação específica)
    await expect(page.getByText(/disponibiliza|mapa.*disponibilizado/i)).toBeVisible();
  });
});