import {expect, test} from '@playwright/test';
import {login} from './utils/auth';

test.describe('Impacto no Mapa', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/processo/1/STIC/cadastro');
    await page.waitForLoadState('domcontentloaded');
  });

  test('deve exibir a adição de atividade na página de impacto', async ({ page }) => {
    const novaAtividadeDesc = 'Nova Atividade Teste Impacto';
    await page.getByTestId('input-nova-atividade').fill(novaAtividadeDesc);
    await page.getByTestId('btn-adicionar-atividade').click();

    await page.click('text="Impacto no mapa"');
    await expect(page).toHaveURL(/.*impacto-mapa/);
    await expect(page.locator('li').filter({ hasText: 'Atividade adicionada: ' + novaAtividadeDesc })).toBeVisible();
  });

  test('deve exibir a remoção de atividade na página de impacto', async ({ page }) => {
    const atividadeDesc = 'Atividade a ser removida';
    await page.getByTestId('input-nova-atividade').fill(atividadeDesc);
    await page.getByTestId('btn-adicionar-atividade').click();

    const atividadeCard = page.locator('.atividade-card').filter({ hasText: atividadeDesc });
    await atividadeCard.hover();
    await atividadeCard.getByTestId('btn-remover-atividade').waitFor({state: 'visible'});
    await atividadeCard.getByTestId('btn-remover-atividade').click({force: true});

    await page.click('text="Impacto no mapa"');
    await expect(page).toHaveURL(/.*impacto-mapa/);
    await expect(page.locator('li').filter({ hasText: 'Atividade removida: ' + atividadeDesc })).toBeVisible();
  });

  test('deve exibir a alteração de atividade na página de impacto', async ({ page }) => {
    const atividadeOriginalDesc = 'Atividade Original';
    const atividadeAlteradaDesc = 'Atividade Alterada';
    await page.getByTestId('input-nova-atividade').fill(atividadeOriginalDesc);
    await page.getByTestId('btn-adicionar-atividade').click();

    const atividadeCard = page.locator('.atividade-card').filter({ hasText: atividadeOriginalDesc });
    await atividadeCard.hover();
    await atividadeCard.getByTestId('btn-editar-atividade').waitFor({state: 'visible'});
    await atividadeCard.getByTestId('btn-editar-atividade').click({force: true});
    await page.getByTestId('input-editar-atividade').fill(atividadeAlteradaDesc);
    await page.getByTestId('btn-salvar-edicao-atividade').click();

    await page.click('text="Impacto no mapa"');
    await expect(page).toHaveURL(/.*impacto-mapa/);
    await expect(page.locator('li').filter({ hasText: `Atividade alterada: ${atividadeAlteradaDesc} (de "${atividadeOriginalDesc}" para "${atividadeAlteradaDesc}")` })).toBeVisible();
  });

  test('deve exibir a adição de conhecimento na página de impacto', async ({ page }) => {
    const atividadeDesc = 'Atividade para adicionar conhecimento';
    const conhecimentoDesc = 'Novo Conhecimento';
    await page.getByTestId('input-nova-atividade').fill(atividadeDesc);
    await page.getByTestId('btn-adicionar-atividade').click();

    const atividadeCard = page.locator('.atividade-card').filter({ hasText: atividadeDesc });
    await atividadeCard.getByTestId('input-novo-conhecimento').fill(conhecimentoDesc);
    await atividadeCard.getByTestId('btn-adicionar-conhecimento').click();

    await page.click('text="Impacto no mapa"');
    await expect(page).toHaveURL(/.*impacto-mapa/);
    await expect(page.locator('li').filter({ hasText: `Conhecimento adicionado à atividade "${atividadeDesc}": ${conhecimentoDesc}` })).toBeVisible();
  });

  test('deve exibir a remoção de conhecimento na página de impacto', async ({ page }) => {
    const atividadeDesc = 'Atividade para remover conhecimento';
    const conhecimentoDesc = 'Conhecimento a ser removido';
    await page.getByTestId('input-nova-atividade').fill(atividadeDesc);
    await page.getByTestId('btn-adicionar-atividade').click();

    const atividadeCard = page.locator('.atividade-card').filter({ hasText: atividadeDesc });
    await atividadeCard.getByTestId('input-novo-conhecimento').fill(conhecimentoDesc);
    await atividadeCard.getByTestId('btn-adicionar-conhecimento').click();

    const conhecimentoRow = atividadeCard.locator('.group-conhecimento').filter({ hasText: conhecimentoDesc });
    await conhecimentoRow.hover();
    await conhecimentoRow.getByTestId('btn-remover-conhecimento').waitFor({state: 'visible'});
    await conhecimentoRow.getByTestId('btn-remover-conhecimento').click({force: true});

    await page.click('text="Impacto no mapa"');
    await expect(page).toHaveURL(/.*impacto-mapa/);
    await expect(page.locator('li').filter({ hasText: `Conhecimento removido da atividade "${atividadeDesc}": ${conhecimentoDesc}` })).toBeVisible();
  });

  test('deve exibir a alteração de conhecimento na página de impacto', async ({ page }) => {
    const atividadeDesc = 'Atividade para alterar conhecimento';
    const conhecimentoOriginalDesc = 'Conhecimento Original';
    const conhecimentoAlteradoDesc = 'Conhecimento Alterado';
    await page.getByTestId('input-nova-atividade').fill(atividadeDesc);
    await page.getByTestId('btn-adicionar-atividade').click();

    const atividadeCard = page.locator('.atividade-card').filter({ hasText: atividadeDesc });
    await atividadeCard.getByTestId('input-novo-conhecimento').fill(conhecimentoOriginalDesc);
    await atividadeCard.getByTestId('btn-adicionar-conhecimento').click();

    const conhecimentoRow = atividadeCard.locator('.group-conhecimento').filter({ hasText: conhecimentoOriginalDesc });
    await conhecimentoRow.hover();
    await conhecimentoRow.getByTestId('btn-editar-conhecimento').waitFor({state: 'visible'});
    await conhecimentoRow.getByTestId('btn-editar-conhecimento').click({force: true});
    await page.getByTestId('input-editar-conhecimento').fill(conhecimentoAlteradoDesc);
    await page.getByTestId('btn-salvar-edicao-conhecimento').click();

    await page.click('text="Impacto no mapa"');
    await expect(page).toHaveURL(/.*impacto-mapa/);
    await expect(page.locator('li').filter({ hasText: `Conhecimento alterado na atividade "${atividadeDesc}": ${conhecimentoAlteradoDesc} (de "${conhecimentoOriginalDesc}" para "${conhecimentoAlteradoDesc}")` })).toBeVisible();
  });
});
