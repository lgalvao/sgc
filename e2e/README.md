# E2E Testing Documentation
Última atualização: 2025-12-04

This directory contains the End-to-End (E2E) tests for the SGC project, built with [Playwright](https://playwright.dev/).

## 🔔 Importante: Melhorias em Andamento

📄 Veja [melhorias-e2e.md](../melhorias-e2e.md) para análise detalhada de problemas de interferência de dados e propostas de padronização.

## 📂 Structure

```
e2e/
├── setup/              # Database setup and seeding for E2E tests
│   ├── schema.sql      # Database schema definition
│   ├── seed.sql        # Initial data for E2E tests (Source of Truth)
│   └── generate-seed.js # Helper script to generate seed data
├── helpers/            # UI interaction helpers
│   ├── auth.ts         # Login and authentication helpers
│   ├── processo-helpers.ts  # Process management helpers
│   └── atividade-helpers.ts # Activity management helpers
├── fixtures/           # 🆕 API-based data creation (PROPOSED)
│   ├── README.md       # Fixtures and hooks usage guide
│   └── processo-fixtures.ts # Process fixtures via API
├── hooks/              # 🆕 Test lifecycle management (PROPOSED)
│   └── cleanup-hooks.ts # Database reset and cleanup hooks
├── lifecycle.js        # Test runner lifecycle (starts Backend & Frontend)
├── *.spec.ts           # Playwright test files (e.g., cdu-01.spec.ts)
├── cdu-02-melhorado.spec.ts # 🆕 Example of improved test patterns
└── README.md           # This documentation
```

## 🗄️ Data Management

**Crucial Distinction:**

- **`e2e/setup/seed.sql`**: This is the **ONLY** data source for E2E tests. It is loaded into the test database before the tests run.
- **`backend/src/test/resources/data.sql`**: This file is **exclusively** for backend integration tests (JUnit/Spring Boot) and is **NOT** used by the E2E suite.

**Do not confuse the two.** When adding data for E2E scenarios, modify `e2e/setup/seed.sql`.

## 🚀 Running Tests

To run the E2E tests, use the standard Playwright command from the project root:

```bash
npx playwright test
```

Or run a specific test file:

```bash
npx playwright test e2e/cdu-01.spec.ts
```

## ⚙️ Lifecycle

The `e2e/lifecycle.js` script is configured in `playwright.config.ts` as the `webServer` command. It is responsible for:

1. Starting the Backend (Spring Boot) on port `10000` with the `e2e` profile.
2. Waiting for the Backend to be healthy.
3. Starting the Frontend (Vite) on port `5173`.
4. Cleaning up processes after tests complete.

## 🧪 Best Practices (Updated)

### Test Isolation

**Current Issue:** Many tests create data without cleanup, causing interference.

**Recommended Pattern:**

```typescript
import { resetDatabase, useProcessoCleanup } from './hooks/cleanup-hooks';

test.describe('My Test Suite', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;
    
    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
    });
    
    test.beforeEach(() => {
        cleanup = useProcessoCleanup();
    });
    
    test.afterEach(async ({ request }) => {
        await cleanup.limpar(request);
    });
    
    test('My test', async ({ page }) => {
        // Create process
        const processoId = /* ... */;
        cleanup.registrar(processoId); // Auto-cleanup
        // Test...
    });
});
```

### Test Documentation

Use `test.step()` for better readability:

```typescript
test('Complex workflow', async ({ page }) => {
    await test.step('Setup: Create process', async () => {
        // Setup code
    });
    
    await test.step('Action: Start process', async () => {
        // Action code
    });
    
    await test.step('Verify: Process is running', async () => {
        // Assertions
    });
});
```

See [cdu-02-melhorado.spec.ts](cdu-02-melhorado.spec.ts) for a complete example.

## 📚 Additional Resources

- [melhorias-e2e.md](../melhorias-e2e.md) - Complete analysis and improvement proposals
- [fixtures/README.md](fixtures/README.md) - Fixtures and hooks usage guide
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)

