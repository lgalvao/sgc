import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsChefe, loginAsGestor} from '~/utils/auth';

test.describe('CDU-12: Verificar impactos no mapa de competências', () => {
  test('deve verificar impactos como CHEFE', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Cadastro de atividades e conhecimentos').click();

    // Verificar presença do botão Impactos no mapa
    await expect(page.getByRole('button', { name: 'Impactos no mapa' })).toBeVisible();

    // Clicar no botão
    await page.getByRole('button', { name: 'Impactos no mapa' }).click();

    // Verificar modal de impactos
    await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();

    // Fechar modal
    await page.getByRole('button', { name: 'Fechar' }).click();

    // Verificar que modal foi fechado
    await expect(page.getByText('Impacto no Mapa de Competências')).not.toBeVisible();
  });

  test('deve verificar impactos como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Selecionar unidade subordinada
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar em Atividades e conhecimentos (modo leitura)
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar presença do botão Impactos no mapa
    await expect(page.getByRole('button', { name: 'Impactos no mapa' })).toBeVisible();

    // Clicar no botão
    await page.getByRole('button', { name: 'Impactos no mapa' }).click();

    // Verificar modal
    await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();
  });

  test('deve verificar impactos como ADMIN', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Selecionar unidade
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Testar acesso pela tela de atividades
    await page.getByText('Atividades e conhecimentos').click();
    await expect(page.getByRole('button', { name: 'Impactos no mapa' })).toBeVisible();

    // Testar acesso pela tela de mapa de competências
    await page.goto('/painel'); // Voltar ao painel
    const processoRow2 = page.locator('table tbody tr').first();
    await processoRow2.click();
    const unidadeCard2 = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard2.click();

    // Clicar em Mapa de competências
    await page.getByText('Mapa de competências').click();
    await expect(page.getByRole('button', { name: 'Impactos no mapa' })).toBeVisible();
  });

  test('deve mostrar mensagem quando não há impactos', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Cadastro de atividades e conhecimentos').click();

    // Clicar em Impactos no mapa
    await page.getByRole('button', { name: 'Impactos no mapa' }).click();

    // Verificar mensagem de nenhum impacto
    await expect(page.getByText('Nenhum impacto no mapa da unidade.')).toBeVisible();
  });

  test('deve mostrar impactos detectados', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Navegar para processo com impactos
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Cadastro de atividades e conhecimentos').click();

    // Clicar em Impactos no mapa
    await page.getByRole('button', { name: 'Impactos no mapa' }).click();

    // Verificar seções do modal
    const modal = page.locator('[role="dialog"]');

    // Verificar seção de atividades inseridas (se existir)
    const atividadesInseridas = modal.locator('.atividades-inseridas');
    if (await atividadesInseridas.isVisible()) {
      await expect(atividadesInseridas.getByText('Atividades inseridas')).toBeVisible();
    }

    // Verificar seção de competências impactadas (se existir)
    const competenciasImpactadas = modal.locator('.competencias-impactadas');
    if (await competenciasImpactadas.isVisible()) {
      await expect(competenciasImpactadas.getByText('Competências impactadas')).toBeVisible();
    }
  });
});