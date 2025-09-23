import {test} from '@playwright/test';
import {TEXTOS, URLS} from "~/cdu/constantes-teste";

test.describe('Captura de Telas - Autenticação', () => {
    test('01 - Login Page', async ({page}) => {
        await page.goto(URLS.LOGIN);
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/01-login-page.png', fullPage: true});
    });

    test('02 - Login Page - Erro de Credenciais', async ({page}) => {
        await page.goto(URLS.LOGIN);
        await page.getByLabel('Título eleitoral').fill('0000000000');
        await page.getByLabel('Senha').fill('senha-invalida');
        await page.getByRole('button', {name: TEXTOS.ENTRAR}).click();
        await page.waitForSelector('.notification-error'); // Espera a notificação de erro
        await page.screenshot({path: 'screenshots/02-login-page-erro.png', fullPage: true});
    });
});
