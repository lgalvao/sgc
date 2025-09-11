import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsAdmin, loginAsChefe, loginAsGestor, loginAsServidor} from '~/utils/auth';
import {LABELS, TEXTS, URLS} from './test-constants';
import {expectCommonDashboardElements, expectTextVisible, expectUrl} from './test-helpers';

test.describe('CDU-01: Realizar login e exibir estrutura das telas', () => {
  test('deve permitir login SERVIDOR e exibir estrutura da aplicação', async ({ page }) => {
    await loginAsServidor(page);
    await expectCommonDashboardElements(page);
  });

  test('deve permitir login GESTOR e exibir estrutura da aplicação', async ({ page }) => {
    await loginAsGestor(page);
    await expectCommonDashboardElements(page);
  });

  test('deve permitir login CHEFE e exibir estrutura da aplicação', async ({ page }) => {
    await loginAsChefe(page);
    await expectCommonDashboardElements(page);
  });

  test('deve permitir login ADMIN e exibir estrutura da aplicação', async ({ page }) => {
    await loginAsAdmin(page);
    await expectCommonDashboardElements(page);
  });

   test('deve carregar a página de login corretamente', async ({ page }) => {
    await page.goto('/');
    await expectUrl(page, `**${URLS.LOGIN}`);
    await expectTextVisible(page, TEXTS.SISTEMA_GESTAO_COMPETENCIAS);
    await expect(page.getByLabel(LABELS.TITULO_ELEITORAL)).toBeVisible();
    await expect(page.getByLabel(LABELS.SENHA)).toBeVisible();
    await expect(page.getByRole('button', { name: TEXTS.ENTRAR })).toBeVisible();
  });
});