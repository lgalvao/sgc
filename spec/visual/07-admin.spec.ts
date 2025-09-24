import {test} from '@playwright/test';
import {loginComoAdmin} from "~/cdu/auxiliares-verificacoes";

test.describe('Captura de Telas - Admin', () => {
    test('44 - Página de Configurações (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/configuracoes');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/44-configuracoes-page.png', fullPage: true});
    });

    test('46 - Página de Relatórios (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/relatorios');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/46-relatorios-page.png', fullPage: true});
    });
});
