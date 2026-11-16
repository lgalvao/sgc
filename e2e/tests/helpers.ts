import {Page} from '@playwright/test';

export async function login(page: Page, usuario: string, senha: string) {
    await page.goto('/logout');
    await page.goto('/login');

    if (await page.locator('input[name="username"]').isVisible()) {
        await page.fill('input[name="username"]', usuario);
        await page.fill('input[name="password"]', senha);
        await page.click('button[type="submit"]');
    }
}
