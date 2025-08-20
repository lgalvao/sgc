import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './spec',
  fullyParallel: true,

  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173/',
    reuseExistingServer: true,
    timeout: 60000, // Aumentado para 60 segundos
  },

  use: {
    baseURL: 'http://localhost:5173/',
    trace: 'on-first-retry',
    actionTimeout: 10000, // Aumentado para 10 segundos para ações
    navigationTimeout: 30000, // Aumentado para 30 segundos para navegações
  },

  projects: [{
    name: 'chromium',
    use: { ...devices['Desktop Chrome'] },
  }],
});
