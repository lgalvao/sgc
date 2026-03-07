import { test, expect } from '../fixtures/a11y';
import { login, USUARIOS } from '../helpers/helpers-auth';

test.describe('Accessibility Checks (WCAG)', () => {

    test('Login Page A11y', async ({ page, makeAxeBuilder }) => {
        await page.goto('/');
        const accessibilityScanResults = await makeAxeBuilder().analyze();
        expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('Dashboard Page A11y', async ({ page, makeAxeBuilder }) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.waitForURL('/painel');
        const accessibilityScanResults = await makeAxeBuilder().analyze();
        expect(accessibilityScanResults.violations).toEqual([]);
    });

});
