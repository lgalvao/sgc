import {test} from '@playwright/test';
import {loginComoAdmin} from "../cdu/helpers";

test.describe('Captura de Telas - Unidades', () => {
    test('50 - Detalhes da Unidade (STIC - ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/unidade/STIC');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/50-detalhes-unidade-stic.png', fullPage: true});
    });

    test('51 - Detalhes da Unidade (SESEL - ADMIN, sem subordinadas)', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/unidade/SESEL');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/51-detalhes-unidade-sesel.png', fullPage: true});
    });

    test('52 - Detalhes da Unidade (Inexistente - ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/unidade/UNIDADE_INEXISTENTE');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/52-detalhes-unidade-inexistente.png', fullPage: true});
    });

    test('53 - Cadastro de Atribuição Temporária (ADMIN)', async ({page}) => {
        await loginComoAdmin(page);
        await page.goto('/unidade/STIC/atribuicao');
        await page.waitForLoadState('networkidle');
        await page.screenshot({path: 'screenshots/53-cadastro-atribuicao-temporaria.png', fullPage: true});
    });
});
