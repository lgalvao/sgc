import {expect, test} from '@playwright/test';
import {loginAsChefe} from '~/utils/auth';

test.describe('CDU-08: Manter cadastro de atividades e conhecimentos', () => {
  test('deve adicionar atividade e conhecimento', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    // Navegar para subprocesso
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    // Clicar no card Atividades
    await page.getByTestId('atividades-card').click();

    // Adicionar atividade
    await page.getByLabel('Nova atividade').fill('Atividade Teste E2E');
    await page.getByRole('button', { name: 'Adicionar atividade' }).click();

    // Verificar se apareceu
    await expect(page.getByText('Atividade Teste E2E')).toBeVisible();

    // Adicionar conhecimento
    await page.getByLabel('Novo conhecimento').fill('Conhecimento Teste E2E');
    await page.getByRole('button', { name: 'Adicionar conhecimento' }).click();

    // Verificar
    await expect(page.getByText('Conhecimento Teste E2E')).toBeVisible();
  });

  test('deve editar atividade', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    await page.getByTestId('atividades-card').click();

    // Passar mouse sobre atividade para mostrar botões
    await page.getByText('Atividade Teste E2E').hover();
    await page.getByRole('button', { name: 'Editar' }).click();

    // Editar
    await page.getByLabel('Editar atividade').fill('Atividade Editada E2E');
    await page.getByRole('button', { name: 'Salvar' }).click();

    // Verificar mudança
    await expect(page.getByText('Atividade Editada E2E')).toBeVisible();
  });

  test('deve remover atividade', async ({ page }) => {
    // Login como CHEFE
    await loginAsChefe(page);

    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();
    await page.getByTestId('atividades-card').click();

    // Remover
    await page.getByText('Atividade Editada E2E').hover();
    await page.getByRole('button', { name: 'Remover' }).click();

    // Confirmar
    await page.getByRole('button', { name: 'Confirmar' }).click();

    // Verificar remoção
    await expect(page.getByText('Atividade Editada E2E')).not.toBeVisible();
  });
});