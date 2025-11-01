import { Page } from '@playwright/test';

const BASE_URL = 'http://localhost:10000/api';

/**
 * Invokes a backend scenario setup endpoint.
 * This requires the user to be authenticated in the browser context.
 *
 * @param page The Playwright page object with an authenticated session.
 * @param scenario The name of the scenario to invoke on the backend.
 * @param params An object containing parameters for the scenario.
 * @returns The response data from the backend, typically containing IDs of created entities.
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
