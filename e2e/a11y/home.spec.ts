import {expect, test} from '../fixtures/a11y.js';
import {login, USUARIOS} from '../helpers/helpers-auth.js';

test.describe('Accessibility checks (WCAG)', () => {

    test('Login page A11y', async ({ page, makeAxeBuilder }: { page: any, makeAxeBuilder: any }) => {
        await page.goto('/');
        const accessibilityScanResults = await makeAxeBuilder().analyze();
        expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('Dashboard page A11y', async ({ page, makeAxeBuilder }: { page: any, makeAxeBuilder: any }) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.waitForURL('/painel');
        const accessibilityScanResults = await makeAxeBuilder().analyze();
        expect(accessibilityScanResults.violations).toEqual([]);
    });

});
