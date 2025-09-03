import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsGestor} from '../utils/auth';

test.describe('CDU-14: Analisar revisão de cadastro de atividades e conhecimentos', () => {
  test('deve mostrar botões de análise da revisão como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar na unidade subordinada
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar presença dos botões de análise da revisão
    await expect(page.getByRole('button', { name: 'Impactos no mapa' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Histórico de análise' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Registrar aceite' })).toBeVisible();
  });

  test('deve mostrar botões de análise da revisão como ADMIN', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar na unidade
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar presença dos botões de análise da revisão
    await expect(page.getByRole('button', { name: 'Impactos no mapa' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Histórico de análise' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Homologar' })).toBeVisible();
  });

  test('deve acessar impactos no mapa da revisão', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Impactos no mapa
    await page.getByRole('button', { name: 'Impactos no mapa' }).click();

    // Verificar modal de impactos
    await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();

    // Fechar modal
    await page.getByRole('button', { name: 'Fechar' }).click();
    await expect(page.getByText('Impacto no Mapa de Competências')).not.toBeVisible();
  });

  test('deve devolver revisão para ajustes como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Devolver para ajustes
    await page.getByRole('button', { name: 'Devolver para ajustes' }).click();

    // Verificar modal de devolução
    await expect(page.getByText('Devolução')).toBeVisible();
    await expect(page.getByText('Confirma a devolução do cadastro para ajustes?')).toBeVisible();

    // Cancelar
    await page.getByRole('button', { name: 'Cancelar' }).click();
    await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();
  });

  test('deve registrar aceite da revisão como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Registrar aceite
    await page.getByRole('button', { name: 'Registrar aceite' }).click();

    // Verificar modal específico para revisão
    await expect(page.getByText('Aceite')).toBeVisible();
    await expect(page.getByText('Confirma o aceite da revisão do cadastro de atividades?')).toBeVisible();

    // Confirmar
    await page.getByRole('button', { name: 'Confirmar' }).click();

    // Verificar mensagem
    await expect(page.getByText('Aceite registrado')).toBeVisible();
    await page.waitForURL('**/painel');
  });

  test('deve homologar revisão sem impactos como ADMIN', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão sem impactos
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Homologar
    await page.getByRole('button', { name: 'Homologar' }).click();

    // Verificar modal específico para homologação sem impactos
    await expect(page.getByText('Homologação do mapa de competências')).toBeVisible();
    await expect(page.getByText('A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade')).toBeVisible();

    // Confirmar
    await page.getByRole('button', { name: 'Confirmar' }).click();

    // Verificar mensagem
    await expect(page.getByText('Homologação efetivada')).toBeVisible();
  });

  test('deve homologar revisão com impactos como ADMIN', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo de revisão com impactos
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Homologar
    await page.getByRole('button', { name: 'Homologar' }).click();

    // Verificar modal específico para homologação com impactos
    await expect(page.getByText('Homologação do cadastro de atividades e conhecimentos')).toBeVisible();
    await expect(page.getByText('Confirma a homologação do cadastro de atividades e conhecimentos?')).toBeVisible();

    // Confirmar
    await page.getByRole('button', { name: 'Confirmar' }).click();

    // Verificar mensagem
    await expect(page.getByText('Homologação efetivada')).toBeVisible();
  });
});