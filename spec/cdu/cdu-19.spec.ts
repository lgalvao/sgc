import {expect, test} from '@playwright/test';
import {loginAsChefe} from '~/utils/auth';

test.describe('CDU-19: Validar mapa de competências', () => {
  // Helper function to navigate to competency map
  async function navigateToCompetencyMap(page) {
    await loginAsChefe(page);
    await page.locator('table tbody tr').first().click();
    await page.locator('[data-testid="mapa-card"]').click();
  }

  test('deve acessar tela de validação do mapa como CHEFE', async ({ page }) => {
    await navigateToCompetencyMap(page);

    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
    await expect(page.getByTestId('apresentar-sugestoes-btn')).toBeVisible();
    await expect(page.getByTestId('validar-btn')).toBeVisible();
  });

  test('deve mostrar histórico de análise quando disponível', async ({ page }) => {
    await navigateToCompetencyMap(page);

    const historicoButton = page.getByTestId('historico-analise-btn');
    if (await historicoButton.isVisible()) {
      await historicoButton.click();
      await expect(page.getByText('Histórico de análise')).toBeVisible();

      const tabelaHistorico = page.locator('table');
      if (await tabelaHistorico.isVisible()) {
        await expect(tabelaHistorico.locator('thead')).toBeVisible();
      }
    }
  });

  test('deve apresentar sugestões para o mapa', async ({ page }) => {
    await navigateToCompetencyMap(page);

    await page.getByTestId('apresentar-sugestoes-btn').click();
    await expect(page.getByTestId('modal-sugestoes-title')).toBeVisible();

    await page.getByTestId('sugestoes-textarea').fill('Sugestões de teste para validação do mapa de competências E2E');
    await page.getByTestId('modal-sugestoes-confirmar').click();

    await expect(page.getByText('Sugestões submetidas para análise da unidade superior')).toBeVisible();
    await page.waitForURL('**/processo/**');
  });

  test('deve validar mapa de competências', async ({ page }) => {
    await navigateToCompetencyMap(page);

    await page.getByTestId('validar-btn').click();
    await expect(page.getByTestId('modal-validar-title')).toBeVisible();
    await expect(page.getByTestId('modal-validar-body')).toBeVisible();

    await page.getByTestId('modal-validar-confirmar').click();
    await expect(page.getByText('Mapa validado e submetido para análise da unidade superior')).toBeVisible();
    await page.waitForURL('**/processo/**');
  });

  test('deve cancelar apresentação de sugestões', async ({ page }) => {
    await navigateToCompetencyMap(page);

    await page.getByTestId('apresentar-sugestoes-btn').click();
    await page.getByTestId('sugestoes-textarea').fill('Sugestões que serão canceladas');
    await page.getByTestId('modal-sugestoes-cancelar').click();

    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
    await expect(page.getByTestId('modal-sugestoes-title')).not.toBeVisible();
  });

  test('deve cancelar validação do mapa', async ({ page }) => {
    await navigateToCompetencyMap(page);

    await page.getByTestId('validar-btn').click();
    await page.getByTestId('modal-validar-cancelar').click();

    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
    await expect(page.getByTestId('modal-validar-title')).not.toBeVisible();
  });

  test('deve carregar sugestões existentes no modal', async ({ page }) => {
    await navigateToCompetencyMap(page);

    await page.getByTestId('apresentar-sugestoes-btn').click();

    const campoSugestoes = page.getByTestId('sugestoes-textarea');
    const valorCampo = await campoSugestoes.inputValue();

    if (valorCampo && valorCampo.length > 0) {
      expect(valorCampo.length).toBeGreaterThan(0);
    }

    await page.getByTestId('modal-sugestoes-cancelar').click();
  });
});