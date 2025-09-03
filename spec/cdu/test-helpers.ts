import {expect, Page} from '@playwright/test';
import {LABELS, SELECTORS, TEXTS, URLS} from './test-constants';

// Common test helper functions for CDU tests

/**
 * Navigate to the first process in the table
 */
export async function navigateToProcess(page: Page): Promise<void> {
  const processoRow = page.locator('table tbody tr').first();
  await processoRow.click();
}

/**
 * Navigate to the first subprocess (unidade card)
 */
export async function navigateToSubprocess(page: Page): Promise<void> {
  const unidadeCard = page.locator(`[data-testid="${SELECTORS.UNIDADE_CARD}"]`).first();
  await unidadeCard.click();
}

/**
 * Click on a card by its text content
 */
export async function clickCard(page: Page, cardName: string): Promise<void> {
  await page.getByText(cardName).click();
}

/**
 * Fill a form field by label
 */
export async function fillFormField(page: Page, label: string, value: string): Promise<void> {
  await page.getByLabel(label).fill(value);
}

/**
 * Select an option from a select field by label
 */
export async function selectOption(page: Page, label: string, option: string): Promise<void> {
  await page.getByLabel(label).selectOption(option);
}

/**
 * Check the first checkbox on the page
 */
export async function checkFirstCheckbox(page: Page): Promise<void> {
  const firstCheckbox = page.locator('input[type="checkbox"]').first();
  await firstCheckbox.check();
}

/**
 * Click a button by its role name
 */
export async function clickButton(page: Page, buttonName: string): Promise<void> {
  await page.getByRole('button', { name: buttonName }).click();
}

/**
 * Confirm a modal dialog
 */
export async function confirmModal(page: Page): Promise<void> {
  await clickButton(page, TEXTS.CONFIRMAR);
}

/**
 * Cancel a modal dialog
 */
export async function cancelModal(page: Page): Promise<void> {
  await clickButton(page, TEXTS.CANCELAR);
}

/**
 * Expect a success message to be visible
 */
export async function expectSuccessMessage(page: Page, message: string): Promise<void> {
  await expect(page.getByText(message)).toBeVisible();
}

/**
 * Wait for redirect to panel
 */
export async function waitForPanelRedirect(page: Page): Promise<void> {
  await page.waitForURL('**/painel');
}

/**
 * Expect URL to match a pattern
 */
export async function expectUrl(page: Page, urlPattern: string): Promise<void> {
  if (urlPattern.includes('**')) {
    // Handle glob patterns
    const regex = new RegExp(urlPattern.replace(/\*\*/g, '.*'));
    await expect(page).toHaveURL(regex);
  } else {
    await expect(page).toHaveURL(urlPattern);
  }
}

/**
 * Expect an element to be visible by test ID
 */
export async function expectVisible(page: Page, testId: string): Promise<void> {
  await expect(page.getByTestId(testId)).toBeVisible();
}

/**
 * Expect an element to not be visible by test ID
 */
export async function expectNotVisible(page: Page, testId: string): Promise<void> {
  await expect(page.getByTestId(testId)).not.toBeVisible();
}

/**
 * Expect text to be visible on the page
 */
export async function expectTextVisible(page: Page, text: string): Promise<void> {
  await expect(page.getByText(text)).toBeVisible();
}

/**
 * Expect text to not be visible on the page
 */
export async function expectTextNotVisible(page: Page, text: string): Promise<void> {
  await expect(page.getByText(text)).not.toBeVisible();
}

/**
 * Navigate to process and then to subprocess
 */
export async function navigateToProcessAndSubprocess(page: Page): Promise<void> {
  await navigateToProcess(page);
  await navigateToSubprocess(page);
}

/**
 * Navigate to process, subprocess, and click on a specific card
 */
export async function navigateToProcessSubprocessAndCard(page: Page, cardName: string): Promise<void> {
  await navigateToProcess(page);
  await navigateToSubprocess(page);
  await clickCard(page, cardName);
}

/**
 * Fill a process creation form with basic data
 */
export async function fillProcessForm(page: Page, descricao: string, tipo: string, dataLimite?: string): Promise<void> {
  await fillFormField(page, LABELS.DESCRICAO, descricao);
  await selectOption(page, LABELS.TIPO, tipo);
  if (dataLimite) {
    await fillFormField(page, LABELS.DATA_LIMITE, dataLimite);
  }
  await checkFirstCheckbox(page);
}

/**
 * Perform login and navigate to panel
 */
export async function loginAndNavigateToPanel(page: Page, loginFunction: (page: Page) => Promise<void>): Promise<void> {
  await loginFunction(page);
  await expectUrl(page, `**${URLS.PAINEL}`);
}

/**
 * Click on a table row by text content
 */
export async function clickTableRowByText(page: Page, text: string): Promise<void> {
  const row = page.locator('table tbody tr').filter({ hasText: text }).first();
  await row.click();
}

/**
 * Hover over an element and click a button
 */
export async function hoverAndClick(page: Page, hoverText: string, buttonName: string): Promise<void> {
  await page.getByText(hoverText).hover();
  await clickButton(page, buttonName);
}

/**
 * Check if an element is visible before performing an action
 */
export async function ifVisibleThenClick(page: Page, locator: string, action: () => Promise<void>): Promise<void> {
  const element = page.locator(locator);
  if (await element.isVisible()) {
    await action();
  }
}

/**
 * Wait for a selector to be visible
 */
export async function waitForSelector(page: Page, selector: string): Promise<void> {
  await page.waitForSelector(selector);
}

/**
 * Get the current page URL
 */
export async function getCurrentUrl(page: Page): Promise<string> {
  return page.url();
}

/**
 * Go back in browser history
 */
export async function goBack(page: Page): Promise<void> {
  await page.goBack();
}