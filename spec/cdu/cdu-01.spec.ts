import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsChefe, loginAsGestor, loginAsServidor} from '../utils/auth';
import {LABELS, SELECTORS, TEXTS, URLS} from './test-constants';
import {expectTextVisible, expectUrl, expectVisible} from './test-helpers';

test.describe('CDU-01: Realizar login e exibir estrutura das telas', () => {
   test('deve permitir login SERVIDOR e exibir estrutura da aplicação', async ({ page }) => {
     // Usar função de login específica para SERVIDOR
     await loginAsServidor(page);

     // Sistema exibe estrutura da aplicação
     await expectVisible(page, SELECTORS.TITULO_PROCESSOS);
     await expectVisible(page, SELECTORS.TITULO_ALERTAS);
   });

   test('deve permitir login GESTOR e exibir estrutura da aplicação', async ({ page }) => {
     // Usar função de login específica para GESTOR
     await loginAsGestor(page);

     // Sistema exibe estrutura da aplicação
     await expectVisible(page, SELECTORS.TITULO_PROCESSOS);
     await expectVisible(page, SELECTORS.TITULO_ALERTAS);
   });

   test('deve permitir login CHEFE e exibir estrutura da aplicação', async ({ page }) => {
     // Usar função de login específica para CHEFE
     await loginAsChefe(page);

     // Sistema exibe estrutura da aplicação
     await expectVisible(page, SELECTORS.TITULO_PROCESSOS);
     await expectVisible(page, SELECTORS.TITULO_ALERTAS);
   });

   test('deve permitir login ADMIN e exibir estrutura da aplicação', async ({ page }) => {
     // Usar função de login específica para ADMIN
     await loginAsAdmin(page);

     // Sistema exibe estrutura da aplicação
     await expectVisible(page, SELECTORS.TITULO_PROCESSOS);
     await expectVisible(page, SELECTORS.TITULO_ALERTAS);
   });

   test('deve carregar a página de login corretamente', async ({ page }) => {
     // 1. Usuário acessa o sistema
     await page.goto('/', );

     // 2. Sistema redireciona para login
     await expectUrl(page, `**${URLS.LOGIN}`);

     // 3. Sistema exibe tela de Login
     await expectTextVisible(page, TEXTS.SISTEMA_GESTAO_COMPETENCIAS);
     await expect(page.getByLabel(LABELS.TITULO_ELEITORAL)).toBeVisible();
     await expect(page.getByLabel(LABELS.SENHA)).toBeVisible();
     await expect(page.getByRole('button', { name: TEXTS.ENTRAR })).toBeVisible();
   });
});