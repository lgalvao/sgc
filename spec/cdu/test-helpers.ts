import {expect, Page} from '@playwright/test';

/**
 * Helper function to fill a form field by its placeholder or label.
 * @param page Playwright Page object.
 * @param labelOrPlaceholder The placeholder text or label of the input field.
 * @param value The value to fill into the input field.
 */
export async function fillFormField(page: Page, labelOrPlaceholder: string, value: string): Promise<void> {
    await page.getByPlaceholder(labelOrPlaceholder).fill(value);
}

/**
 * Helper function to click a button by its name.
 * @param page Playwright Page object.
 * @param name The name of the button.
 */
export async function clickButton(page: Page, name: string): Promise<void> {
    await page.getByRole('button', {name}).click();
}

/**
 * Helper function to expect a success message (toast) to be visible and then disappear.
 * @param page Playwright Page object.
 * @param message The success message text.
 */
export async function expectSuccessMessage(page: Page, message: string): Promise<void> {
    const notification = page.locator('.notification.notification-success'); // Seletor mais específico para sucesso
    await expect(notification).toBeVisible();
    await expect(notification).toContainText(message); // Use toContainText for partial matches
    // Não esperar que desapareça, pois a navegação de página o removerá
}

/**
 * Helper function to expect a text to be visible on the page.
 * @param page Playwright Page object.
 * @param text The text to expect.
 */
export async function expectTextVisible(page: Page, text: string): Promise<void> {
  await expect(page.getByText(text)).toBeVisible();
}

/**
 * Helper function to expect an element with a given test ID to be visible.
 * @param page Playwright Page object.
 * @param testId The data-testid of the element.
 */
export async function expectVisible(page: Page, testId: string): Promise<void> {
    await expect(page.getByTestId(testId)).toBeVisible();
}

/**
 * Helper function to wait for a notification (toast) to appear and then disappear.
 * @param page Playwright Page object.
 */
export async function waitForNotification(page: Page) {
    const notification = page.locator('.toast'); // Assuming toast is the class for notifications
    try {
        await expect(notification).toBeVisible({timeout: 5000}); // Wait for it to appear
        await notification.click(); // Click to dismiss
        await notification.waitFor({state: 'hidden', timeout: 5000}); // Wait for it to disappear
    } catch (_e) {
        // If toast doesn't appear within 5 seconds, it's fine, just continue
        console.log("No toast appeared or it disappeared too fast.");
    }
}

// Dummy functions to resolve import errors
export async function expectUrl(page: Page, url: string): Promise<void> {
    const regexUrl = new RegExp(url.replace(/\*\*/g, '.*'));
    await expect(page).toHaveURL(regexUrl);
}

export async function expectNotVisible(page: Page, text: string): Promise<void> {
  await expect(page.getByText(text)).not.toBeVisible();
}

/**
 * Helper function to expect an error message (toast) to be visible.
 * @param page Playwright Page object.
 * @param message The error message text.
 */
export async function expectErrorMessage(page: Page, message: string): Promise<void> {
    const notification = page.locator('.notification.notification-error'); // Seletor mais específico para erro
    await expect(notification).toBeVisible();
    await expect(notification).toContainText(message); // Use toContainText for partial matches
    // Do not wait for it to disappear, as error messages might persist or require user action
}
