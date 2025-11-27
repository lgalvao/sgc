/**
 * Fixtures de teste com isolamento de banco de dados por teste.
 *
 * Este fixture garante que cada teste comece com um banco de dados H2 em memória
 * completamente isolado e populado apenas com dados de referência mínimos.
 * Elimina a necessidade de chamadas manuais de limpeza nos testes e permite
 * a execução paralela sem conflitos de estado.
 */
import { test as base } from '@playwright/test';
import { debug as debugLog, logger } from '../helpers/utils/logger';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:10000';

// Declare the types of fixtures you want to add.
type MyFixtures = {
    testId: string;
};

export const test = base.extend<MyFixtures>({
    // Override the default 'page' fixture to include database setup
    page: async ({ page }, use, testInfo) => {
        // 1. Reset database state before each test
        try {
            const resetResponse = await page.request.post(`${BACKEND_URL}/api/e2e/dados-teste/recarregar`);
            if (!resetResponse.ok()) {
                const errorBody = await resetResponse.json().catch(() => ({}));
                throw new Error(`Failed to reset DB for test ${testInfo.title}: ${resetResponse.status()} - ${JSON.stringify(errorBody)}`);
            }
            debugLog(`[${testInfo.title}] Database reset successful`);
        } catch (error) {
            logger.error(`[${testInfo.title}] Error during DB reset:`, error);
            throw error;
        }

        // 2. Use the page (run the actual test)
        await use(page);
    },
});

export { expect } from '@playwright/test';