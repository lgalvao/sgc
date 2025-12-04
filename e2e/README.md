# E2E Testing Documentation
Última atualização: 2025-12-04 14:18:38Z

This directory contains the End-to-End (E2E) tests for the SGC project, built with [Playwright](https://playwright.dev/).

## 📂 Structure

```
e2e/
├── setup/              # Database setup and seeding for E2E tests
│   ├── schema.sql      # Database schema definition
│   ├── seed.sql        # Initial data for E2E tests (Source of Truth)
│   └── generate-seed.js # Helper script to generate seed data
├── lifecycle.js        # Test runner lifecycle (starts Backend & Frontend)
├── *.spec.ts           # Playwright test files (e.g., cdu-01.spec.ts)
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


## Detalhamento técnico (gerado em 2025-12-04T14:22:48Z)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
