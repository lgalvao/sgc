import {expect, test} from './fixtures/base.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';

test.describe('UI Consistency & Accessibility', () => {

    test.beforeEach(async ({ page }) => {
        // Navigate to login
        // Login as Admin
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
    });

    test('All views have consistent page headers', async ({page}) => {
        const views = [
            '/painel',
            '/configuracoes',
            '/relatorios',
            '/historico'
        ];

        for (const view of views) {
            await page.goto(view);

            // Verify h2 presence (title)
            const headings = page.locator('h2');
            await expect(headings.first()).toBeVisible();

            // Verify PageHeader container classes for all h2
            const count = await headings.count();
            for (let i = 0; i < count; ++i) {
                const heading = headings.nth(i);
                // Check parent structure: h2 -> div -> div.d-flex...mb-3...
                const container = heading.locator('..').locator('..');
                await expect(container).toHaveClass(/d-flex.*mb-3/);
            }
        }
    });

    test('Interactive elements are keyboard accessible (Tab navigation)', async ({page}) => {
        await page.goto('/painel');

        // Ensure body is focused
        await page.locator('body').focus();

        // Press Tab and verify focus moves to an interactive element
        await page.keyboard.press('Tab');

        // Check if the focused element is valid
        const focusedTagName = await page.evaluate(() => document.activeElement?.tagName);
        expect(focusedTagName).toMatch(/^(A|BUTTON|INPUT|SELECT|TEXTAREA)$/);
    });

});