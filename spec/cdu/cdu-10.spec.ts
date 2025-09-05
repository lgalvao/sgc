import {expect, test} from '@playwright/test';
import {loginAsChefe} from '~/utils/auth';

test.describe('CDU-10: Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
  test('deve disponibilizar revisão do cadastro com sucesso', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Navegar para subprocesso de revisão em andamento
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Cadastro de atividades e conhecimentos').click();

    // Verificar se estamos na tela correta
    await expect(page.getByText('Cadastro de atividades e conhecimentos')).toBeVisible();

    // Clicar no botão Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar diálogo de confirmação específico para revisão
    await expect(page.getByText('Disponibilização da revisão do cadastro')).toBeVisible();
    await expect(page.getByText('Confirma a finalização da revisão e a disponibilização do cadastro?')).toBeVisible();

    // Confirmar a ação
    await page.getByRole('button', { name: 'Confirmar' }).click();

    // Verificar mensagem de sucesso específica para revisão
    await expect(page.getByText('Revisão do cadastro de atividades disponibilizada')).toBeVisible();

    // Verificar redirecionamento para painel
    await page.waitForURL('**/painel');
  });

  test('deve mostrar erro quando atividade não tem conhecimento associado na revisão', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Navegar para subprocesso de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Cadastro de atividades e conhecimentos').click();

    // Adicionar atividade sem conhecimento (se necessário para o teste)
    await page.getByLabel('Nova atividade').fill('Atividade sem conhecimento na revisão');
    await page.getByRole('button', { name: 'Adicionar atividade' }).click();

    // Tentar disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Verificar mensagem de erro
    await expect(page.getByText(/atividade.*precisando.*conhecimento/i)).toBeVisible();

    // Verificar que permanece na mesma tela
    await expect(page.getByText('Cadastro de atividades e conhecimentos')).toBeVisible();
  });

  test('deve cancelar disponibilização da revisão', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Navegar para subprocesso de revisão
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar em Atividades e conhecimentos
    await page.getByText('Cadastro de atividades e conhecimentos').click();

    // Clicar no botão Disponibilizar
    await page.getByRole('button', { name: 'Disponibilizar' }).click();

    // Cancelar a ação
    await page.getByRole('button', { name: 'Cancelar' }).click();

    // Verificar que permanece na mesma tela
    await expect(page.getByText('Cadastro de atividades e conhecimentos')).toBeVisible();

    // Verificar que não há mensagem de sucesso
    await expect(page.getByText('Revisão do cadastro de atividades disponibilizada')).not.toBeVisible();
  });
});