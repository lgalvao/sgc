import { Page } from '@playwright/test';

const BASE_URL = 'http://localhost:10000/api';

/**
 * Executa um cenário de setup no backend.
 * @param page A instância da página do Playwright.
 * @param scenario O nome do cenário a ser executado.
 * @param params Os parâmetros para o cenário.
 * @returns A resposta da API.
 */
export async function setupScenario(page: Page, scenario: string, params: Record<string, any>): Promise<any> {
    const response = await page.request.post(`${BASE_URL}/test-setup/${scenario}`, {
        data: params,
        headers: {
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok()) {
        const errorText = await response.text();
        throw new Error(`Failed to set up scenario '${scenario}'. Status: ${response.status()}. Body: ${errorText}`);
    }

    return response.json();
}
