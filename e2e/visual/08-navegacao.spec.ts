import {test} from '@playwright/test';
import {loginComoAdmin} from "~/helpers";

test.describe('Captura de Telas - Navegação', () => {
    test('54 - Breadcrumbs - Processo > Unidade > Mapa', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/processo/1/SESEL/mapa');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/54-breadcrumbs-processo-unidade-mapa.png', fullPage: true});
    });

    test('55 - Breadcrumbs - Unidade', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/unidade/SESEL');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/55-breadcrumbs-unidade.png', fullPage: true});
    });
});
