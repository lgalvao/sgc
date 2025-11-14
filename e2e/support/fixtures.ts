/**
 * Fixtures de teste com isolamento de banco de dados por teste.
 *
 * Este fixture garante que cada teste comece com um banco de dados H2 em memória
 * completamente isolado e populado apenas com dados de referência mínimos.
 * Elimina a necessidade de chamadas manuais de limpeza nos testes e permite
 * a execução paralela sem conflitos de estado.
 */
import { test as base } from '@playwright/test';
import { v4 as uuidv4 } from 'uuid'; // For generating unique test IDs

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:10000';
const debugLog = (...args: any[]) => { if (process.env.E2E_DEBUG === '1') console.log(...args); };

// Declare the types of fixtures you want to add.
type MyFixtures = {
    testId: string;
};

export const test = base.extend<MyFixtures>({
    // Override the default 'page' fixture to include database setup/teardown
    page: async ({ page }, use, testInfo) => {
        const testId = `test-${uuidv4()}`; // Generate a unique ID for this test run

        // 1. Create isolated database for this test
        try {
            const createDbResponse = await page.request.post(`${BACKEND_URL}/api/e2e/setup/create-isolated-db`, {
                data: { testId: testId }
            });
            if (!createDbResponse.ok()) {
                const errorBody = await createDbResponse.json();
                throw new Error(`Failed to create isolated DB for testId ${testId}: ${createDbResponse.status()} - ${JSON.stringify(errorBody)}`);
            }
            debugLog(`[${testInfo.title}] Created isolated DB: ${testId}`);
        } catch (error) {
            console.error(`[${testInfo.title}] Error during DB creation:`, error);
            throw error;
        }

        // 2. Set X-Test-ID header for all requests made by this page context
        await page.context().setExtraHTTPHeaders({ 'X-Test-ID': testId });

        // 3. Use the page (run the actual test)
        await use(page);

        // 4. Clean up the isolated database after the test
        try {
            const cleanupDbResponse = await page.request.post(`${BACKEND_URL}/api/e2e/setup/cleanup-db/${testId}`);
            if (!cleanupDbResponse.ok()) {
                const errorBody = await cleanupDbResponse.json();
                console.warn(`[${testInfo.title}] Warning: Failed to clean up isolated DB for testId ${testId}: ${cleanupDbResponse.status()} - ${JSON.stringify(errorBody)}`);
            } else {
                debugLog(`[${testInfo.title}] Cleaned up isolated DB: ${testId}`);
            }
        } catch (error) {
            console.warn(`[${testInfo.title}] Warning: Error during DB cleanup:`, error);
        }
    },
});

export { expect } from '@playwright/test';