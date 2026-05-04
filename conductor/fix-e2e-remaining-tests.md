# Plan: Fix Remaining E2E Tests

## Objective

Fix the remaining e2e test failures caused by structural and visual simplifications in the frontend screens (removed
headings, changed selects, and new disabled button states).

## Key Files & Context

- `e2e/captura.spec.ts`, `e2e/cdu-21.spec.ts`, `e2e/cdu-22.spec.ts`, `e2e/cdu-23.spec.ts`, `e2e/cdu-24.spec.ts`,
  `e2e/cdu-25.spec.ts`, `e2e/cdu-26.spec.ts`, `e2e/cdu-34.spec.ts`: Old `Unidades participantes` heading validations.
- `e2e/cdu-35.spec.ts` (and `captura.spec.ts`): Relatório Andamento test.
- `e2e/cdu-36.spec.ts` (and `captura.spec.ts`): Relatório Mapas test.
- `e2e/cdu-28.spec.ts`: Unidades test.

## Implementation Steps

1. **Heading Validation**: Replace `page.getByRole('heading', {name: /Unidades participantes/i})` with
   `page.getByTestId('processo-info')` across all test specs.
2. **Relatório Andamento**: In `cdu-35.spec.ts` and `captura.spec.ts`, change the test to verify
   `expect(botaoGerar).toBeDisabled()` instead of clicking it and waiting for an error message.
3. **Relatório Mapas**: In `cdu-36.spec.ts` and `captura.spec.ts`, remove the usage of `select-processo-mapas` and
   `select-unidade-mapas`. Adapt the tests to directly use the new unit tree (`container-arvore-unidades-mapas`) and the
   export PDF button (`btn-gerar-mapas`).
4. **Unidades View**: Update `btn-expandir-todas` to `btn-unidades-expandir-todas` in `cdu-28.spec.ts`.

## Verification & Testing

1. Run `npx playwright test` to ensure all tests pass.
2. Ensure no implicit timeouts or remaining unresolved locators affect the suite.