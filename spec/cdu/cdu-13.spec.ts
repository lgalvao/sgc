import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsGestor} from '~/utils/auth';

test.describe('CDU-13: Analisar cadastro de atividades e conhecimentos', () => {
  test('deve mostrar botões de análise como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo de mapeamento
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar na unidade subordinada
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar presença dos botões de análise
    await expect(page.getByRole('button', { name: 'Histórico de análise' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Registrar aceite' })).toBeVisible();
  });

  test('deve mostrar botões de análise como ADMIN', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar na unidade
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Atividades e conhecimentos').click();

    // Verificar presença dos botões de análise
    await expect(page.getByRole('button', { name: 'Histórico de análise' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Devolver para ajustes' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Homologar' })).toBeVisible();
  });

  test('deve mostrar histórico de análise', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Histórico de análise
    await page.getByRole('button', { name: 'Histórico de análise' }).click();

    // Verificar modal de histórico
    await expect(page.getByText('Histórico de análise')).toBeVisible();

    // Verificar tabela de histórico (se houver dados)
    const tabelaHistorico = page.locator('table');
    if (await tabelaHistorico.isVisible()) {
      await expect(tabelaHistorico.locator('thead')).toBeVisible();
    }
  });

  test('deve devolver cadastro para ajustes como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo
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

    // Cancelar operação
    await page.getByRole('button', { name: 'Cancelar' }).click();

    // Verificar que permanece na tela
    await expect(page.getByText('Atividades e conhecimentos')).toBeVisible();
  });

  test('deve registrar aceite como GESTOR', async ({ page }) => {
    // Login como GESTOR
    await loginAsGestor(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Registrar aceite
    await page.getByRole('button', { name: 'Registrar aceite' }).click();

    // Verificar modal de aceite
    await expect(page.getByText('Aceite')).toBeVisible();
    await expect(page.getByText('Confirma o aceite do cadastro de atividades?')).toBeVisible();

    // Confirmar aceite
    await page.getByRole('button', { name: 'Confirmar' }).click();

    // Verificar mensagem de sucesso
    await expect(page.getByText('Aceite registrado')).toBeVisible();

    // Verificar redirecionamento
    await page.waitForURL('**/painel');
  });

  test('deve homologar cadastro como ADMIN', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar para processo
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    const unidadeCard = page.locator('[data-testid="unidade-card"]').first();
    await unidadeCard.click();
    await page.getByText('Atividades e conhecimentos').click();

    // Clicar em Homologar
    await page.getByRole('button', { name: 'Homologar' }).click();

    // Verificar modal de homologação
    await expect(page.getByText('Homologação do cadastro de atividades e conhecimentos')).toBeVisible();
    await expect(page.getByText('Confirma a homologação do cadastro de atividades e conhecimentos?')).toBeVisible();

    // Confirmar homologação
    await page.getByRole('button', { name: 'Confirmar' }).click();

    // Verificar mensagem de sucesso
    await expect(page.getByText('Homologação efetivada')).toBeVisible();
  });
});