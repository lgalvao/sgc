import {expect, Page} from '@playwright/test';

/**
 * Helper function to navigate to activity visualization page for a specific process and unit.
 * @param page Playwright Page object.
 * @param processId The process ID.
 * @param unit The unit name.
 */
export async function navigateToActivityVisualization(page: Page, processId: number, unit: string): Promise<void> {
  await page.goto(`/processo/${processId}`);
  await expect(page).toHaveURL(new RegExp(`/processo/${processId}`));
  
  await page.locator('table tbody tr').filter({hasText: unit}).first().click();
  await expect(page).toHaveURL(new RegExp(`/processo/${processId}/${unit}`));
  
  await page.waitForSelector('[data-testid="atividades-card"]');
  await page.getByTestId('atividades-card').click();
  await expect(page).toHaveURL(new RegExp(`/processo/${processId}/${unit}/vis-cadastro`));
}