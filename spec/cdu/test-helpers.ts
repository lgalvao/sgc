import {expect, Page} from '@playwright/test';
import {SELECTORS, TEXTS, URLS} from './test-constants';

/**
 * Helper function to fill a form field by its placeholder or label.
 * @param page Playwright Page object.
 * @param labelOrPlaceholder The placeholder text or label of the input field.
 * @param value The value to fill into the input field.
 */
export async function fillFormField(page: Page, labelOrPlaceholder: string, value: string): Promise<void> {
    try {
        await page.getByPlaceholder(labelOrPlaceholder).fill(value);
    } catch {
        await page.getByLabel(labelOrPlaceholder).fill(value);
    }
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
 * Helper function to expect a success message (toast) to be visible.
 * @param page Playwright Page object.
 * @param message The success message text.
 */
export async function expectSuccessMessage(page: Page, message: string): Promise<void> {
    const notification = page.locator('.notification.notification-success');
    await expect(notification).toBeVisible();
    await expect(notification).toContainText(message);
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
    const notification = page.locator('.notification.notification-error');
    await expect(notification).toBeVisible();
    await expect(notification).toContainText(message);
}

/**
 * Helper function to expect common dashboard elements to be visible after login.
 * @param page Playwright Page object.
 */
export async function expectCommonDashboardElements(page: Page): Promise<void> {
    await expectVisible(page, SELECTORS.TITULO_PROCESSOS);
    await expectVisible(page, SELECTORS.TITULO_ALERTAS);
    await expectVisible(page, SELECTORS.TABELA_PROCESSOS);
    await expectVisible(page, SELECTORS.COLUNA_DESCRICAO);
    await expectVisible(page, SELECTORS.COLUNA_TIPO);
    await expectVisible(page, SELECTORS.COLUNA_UNIDADES);
    await expectVisible(page, SELECTORS.COLUNA_SITUACAO);
}

/**
 * Helper function to click on a table column header and verify the process table is visible.
 * @param page Playwright Page object.
 * @param columnTestId The data-testid of the column header to click.
 */
export async function clickAndVerifyProcessTableSort(page: Page, columnTestId: string): Promise<void> {
    await page.getByTestId(columnTestId).click();
    await expectVisible(page, SELECTORS.TABELA_PROCESSOS);
}

/**
 * Helper function to log in with a given role and click the first process in the table.
 * @param page Playwright Page object.
 * @param loginFunction The login function to use (e.g., loginAsServidor, loginAsGestor).
 */
export async function loginAndClickFirstProcess(page: Page, loginFunction: (page: Page) => Promise<void>): Promise<void> {
    await loginFunction(page);
    await page.locator('table tbody tr').first().click();
}

/**
 * Helper function to navigate to the process creation screen.
 * Assumes user is already logged in.
 * @param page Playwright Page object.
 */
export async function navigateToProcessCreation(page: Page): Promise<void> {
    await page.getByText(TEXTS.CRIAR_PROCESSO).click();
    await expectUrl(page, `**${URLS.PROCESSO_CADASTRO}`);
}

/**
 * Helper function to click on a process in the table and navigate to its details page.
 * @param page Playwright Page object.
 * @param processText The text content to filter the process row by (e.g., 'STIC/COINF').
 */
export async function navigateToProcessDetails(page: Page, processText: string): Promise<void> {
    const processoRow = page.locator('table tbody tr').filter({hasText: processText}).first();
    await processoRow.click();
    await expect(page).toHaveURL(/\/processo\/\d+/);
}

/**
 * Helper function to navigate to the activity registration page for a specific process.
 * Assumes user is already logged in.
 * @param page Playwright Page object.
 * @param processId The ID of the process.
 * @param unit The unit name (e.g., 'STIC').
 */
export async function navigateToActivityRegistration(page: Page, processId: number, unit: string): Promise<void> {
    await page.goto(`/processo/${processId}/${unit}/cadastro`);
    await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+\/cadastro/);
    await expectTextVisible(page, TEXTS.CADASTRO_ATIVIDADES_CONHECIMENTOS);
}

/**
 * Helper function to navigate to a 'Mapeamento' process and then to its activity registration card.
 * Assumes user is already logged in.
 * @param page Playwright Page object.
 */
export async function navigateToMapeamentoActivityRegistration(page: Page): Promise<void> {
    await page.goto('/painel');
    const processoMapeamento = page.locator('table tbody tr').filter({hasText: 'Mapeamento'}).first();
    await processoMapeamento.click();
    await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+/);
    await page.getByTestId('atividades-card').click();
}

// Common selectors
export const MODAL_SELECTOR = '.modal.show';

/**
 * Helper function to expect a confirmation modal to be visible with specific texts.
 * @param page Playwright Page object.
 * @param text1 The first text to expect in the modal.
 * @param text2 The second text to expect in the modal.
 */
export async function expectConfirmationModal(page: Page, text1: string, text2: string): Promise<void> {
    const modal = page.locator(MODAL_SELECTOR);
    await expect(modal).toBeVisible();
    await expectTextVisible(page, text1);
    await expectTextVisible(page, text2);
}

/**
 * Helper function to wait for a notification (toast) to appear.
 * @param page Playwright Page object.
 * @param message The message text to expect in the notification.
 * @param dataTestId The test ID attribute used to locate the notification element.
 */
export async function waitForNotification(page: Page, message: string, dataTestId: string): Promise<void> {
    const notificationLocator = page.getByTestId(dataTestId);
    await notificationLocator.waitFor({state: 'visible', timeout: 5000});
    await expect(notificationLocator).toContainText(message);
}

// Login data configuration
interface LoginData {
    idServidor: string;
    perfil: string;
    unidade: string;
}

const LOGIN_DATA: Record<string, LoginData> = {
    ADMIN: { idServidor: '6', perfil: 'ADMIN', unidade: 'SEDOC' },
    GESTOR: { idServidor: '1', perfil: 'GESTOR', unidade: 'SESEL' },
    CHEFE: { idServidor: '14', perfil: 'CHEFE', unidade: 'STIC' },
    SERVIDOR: { idServidor: '15', perfil: 'SERVIDOR', unidade: 'STIC' }
};

/**
 * Generic login function for different user roles.
 * @param page Playwright Page object.
 * @param role The user role to login as.
 */
async function loginAs(page: Page, role: keyof typeof LOGIN_DATA): Promise<void> {
    const userData = LOGIN_DATA[role];
    await page.context().addInitScript((data) => {
        localStorage.setItem('idServidor', data.idServidor);
        localStorage.setItem('perfilSelecionado', data.perfil);
        localStorage.setItem('unidadeSelecionada', data.unidade);
    }, userData);
    await page.goto('/painel');
    await expect(page).toHaveURL(/\/painel/);
    await expectCommonDashboardElements(page);
}

export const loginAsAdmin = (page: Page) => loginAs(page, 'ADMIN');
export const loginAsGestor = (page: Page) => loginAs(page, 'GESTOR');
export const loginAsChefe = (page: Page) => loginAs(page, 'CHEFE');
export const loginAsServidor = (page: Page) => loginAs(page, 'SERVIDOR');

