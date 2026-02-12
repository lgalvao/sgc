# Implementation Plan - Standardizing Root Unit as ADMIN

This plan outlines the steps needed to adapt the implementation to the recent changes in requirements, where the root unit (ID 1) is now consistently referred to as `ADMIN` instead of `RAIZ` or `SEDOC`.

## Proposed Changes

### 1. Database Seed Data
- **File**: `backend/src/test/resources/data.sql`
- **Action**: Rename unit ID 1 from `RAIZ` to `ADMIN`.
- **Reason**: Align the physical data with the new terminology in requirements.

### 2. Backend Mappers (Data Presentation)
- **File**: `backend/src/main/java/sgc/subprocesso/mapper/MovimentacaoMapper.java`
- **Action**: Update `mapUnidadeSiglaParaUsuario` to return `"ADMIN"` for unit ID 1 instead of `"SEDOC"`.
- **File**: `backend/src/main/java/sgc/organizacao/mapper/UsuarioMapper.java`
- **Action**: Update `mapUnidadeSiglaParaUsuario` to return `"ADMIN"` for unit ID 1 instead of `"SEDOC"`.
- **Action**: Update `mapUnidadeNomeParaUsuario` to return `"Administração"` (or similar) for unit ID 1 instead of `"Secretaria de Documentação"`.

### 3. Backend Services and Facades
- **File**: `backend/src/main/java/sgc/alerta/AlertaFacade.java`
- **Action**: Rename `getSedoc()` to `getAdmin()` (via `@Getter` renaming or manual refactor).
- **Action**: Update `obterSiglaParaUsuario` to return `"ADMIN"` for ID 1.
- **Action**: Update descriptions in `criarAlertaReaberturaCadastro`, `criarAlertaReaberturaCadastroSuperior`, `criarAlertaReaberturaRevisao`, and `criarAlertaReaberturaRevisaoSuperior` to use `"ADMIN"` instead of `"SEDOC"`.
- **File**: `backend/src/main/java/sgc/subprocesso/service/notificacao/SubprocessoEmailService.java`
- **Action**: Update `obterSiglaParaUsuario` to return `"ADMIN"` for ID 1.

### 4. Tests
- **Action**: Update any tests that assert `"SEDOC"` for the root unit.
- **Relevant files**: `AlertaFacadeTest.java`, `MovimentacaoMapperTest.java`, `UsuarioMapperTest.java`, `E2eFixtureEndpointTest.java`, etc.

### 5. Frontend
- **Action**: Check if there are hardcoded references to `"SEDOC"` or `"RAIZ"` in the frontend that should be `"ADMIN"`.

## Verification Plan
1. Run backend unit and integration tests.
2. Run frontend tests.
3. Run E2E tests.
4. Manually verify the UI to ensure the root unit is displayed as `ADMIN`.
