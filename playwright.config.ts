// noinspection JSUnusedGlobalSymbols

import {defineConfig, devices} from '@playwright/test';

/**
 * ⚠️ IMPORTANTE: Idempotência dos Testes E2E
 *
 * Os testes E2E modificam o banco de dados (H2 em memória do backend).
 * Para executar a suíte completa múltiplas vezes, REINICIE O BACKEND entre execuções:
 *
 * 1. Pare o backend (Ctrl+C ou taskkill /F /IM java.exe)
 * 2. Inicie novamente: ./gradlew :backend:bootRun --args='--spring.profiles.active=e2e'
 * 3. Execute os testes: npm run test:e2e
 *
 * Alternativamente, execute apenas testes específicos que não foram modificados.
 */
export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    timeout: 10000,
    expect: {timeout: 5000},
    projects: [{name: 'chromium', use: {...devices['Desktop Chrome']}}],
    webServer: {
        command: 'cd frontend ; npm run dev',
        url: 'http://localhost:5173',
        timeout: 20000,
        reuseExistingServer: true
    },
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure'
    }
});