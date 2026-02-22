# Implementation Plan - Fixing Email Notifications and Enhancing Integration Tests

This plan addresses the bugs found in the email notification system and ensures that all integration tests correctly verify real email delivery using GreenMail.

## User Review Required

> [!IMPORTANT]
> - Critical: `SubprocessoEmailService` was attempting to send emails to unit acronyms (e.g., "GESTOR") instead of person emails, causing all transition notifications to fail silently in logs.
> - Subject/Template mismatch: Some subjects and variables in `SubprocessoEmailService` do not match the requirements or the templates' expectations.

- [ ] Confirm if we should always notify both the titular and the substitute of a unit.
- [ ] Confirm if the URL `https://sgc.tre-pe.jus.br` should be hardcoded in templates or passed as a variable (currently it's hardcoded in some, but others expect a variable).

## Proposed Changes

### 1. Notification System Fixes

#### `SubprocessoEmailService.java`
- Fix `enviarEmailTransicaoDireta` to:
    - Resolve the destination unit's titular and substitute email addresses.
    - Use `enviarEmailHtml` instead of `enviarEmail`.
    - Correctly populate all variables required by templates (e.g., `siglaUnidadeOrigem`, `siglaUnidadeDestino`, `nomeProcesso`).
- Update `criarAssunto` to strictly follow `cdu-14.md` and other requirements.

#### `TipoTransicao.java`
- Add `email/` prefix to all template names to ensure Thymeleaf finds them (e.g., `"email/aceite-revisao-cadastro"`).
- Verify all template names against the actual files in `src/main/resources/templates/email/`.

### 2. Integration Test Infrastructure

#### `BaseIntegrationTest.java`
- Already updated to include GreenMail and `algumEmailContem`.
- Add a helper to wait for a specific number of emails and return the last one or search through them.

#### `CDU14IntegrationTest.java`
- Update assertions to match the REAL requirements (e.g., checking for "submetida para análise").
- Clean up redundant `@ActiveProfiles` and `@Import`.

### 3. Cleanup of other Integration Tests
- Periodically update other `CDUxxIntegrationTest.java` files to remove `TestThymeleafConfig` and rely on the real `NotificacaoModelosService` and `GreenMail`.

## Verification Plan

### Automated Tests
- Run `ProcessoEmailIntegrationTest` to verify basic HTML email sending.
- Run `CDU14IntegrationTest` to verify the complete workflow including notifications.
- Run `./gradlew :backend:test` for all integration tests to ensure no regressions.

### Manual Verification (Logs)
- Check terminal output for `E-mail para ... enviado`.
- Ensure no `Endereço de e-mail inválido` errors appear for valid transitions.
