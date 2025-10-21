import {test, expect} from '@playwright/test';

test.describe('CDU-00: Baseline Test', () => {
    test('should load the login page correctly', async ({page}) => {
        await page.goto('http://localhost:5173/login');
        await expect(page.getByLabel('Título eleitoral')).toBeVisible();
        await expect(page.getByLabel('Senha')).toBeVisible();
    });
});
