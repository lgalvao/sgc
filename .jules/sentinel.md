## 2024-05-22 - [JWT Secret Externalization]
**Vulnerability:** Hardcoded JWT secret key in `JwtProperties.java` and `application.yml`.
**Learning:** Default values in `@ConfigurationProperties` classes can be dangerous if they contain sensitive secrets, as they provide a fallback even if configuration files are missing. Also, test environment schema (`schema.sql`) was inconsistent with entity mappings (`usuario_codigo` vs `usuario_titulo`), masking issues until tests were run.
**Prevention:** Always use `${VAR_NAME}` injection for secrets and force a failure (no default) in production profiles. Maintain consistency between test schema and JPA mappings.

## 2025-05-18 - [DoS Protection via Input Validation]
**Vulnerability:** Missing length limits on authentication DTOs (`AutenticacaoReq`, `EntrarReq`) allowed potentially large payloads, posing a Denial of Service risk.
**Learning:** Even simulated or internal-facing endpoints should validate input size to prevent resource exhaustion or unexpected behavior in the persistence layer.
**Prevention:** Always apply `@Size` constraints to String fields in DTOs, especially those used in public endpoints.

## 2025-05-23 - [Insecure Default in Authentication]
**Vulnerability:** `SgrhService.autenticar` defaulted to returning `true` (bypass) if the AD Client bean was missing, assuming this meant a test environment. In production, a configuration error preventing bean creation would leave the system wide open.
**Learning:** Implicit "fail-open" logic based on bean presence is dangerous. Security controls must explicitly verify the environment before bypassing checks.
**Prevention:** Explicitly check configuration properties (e.g., `ambiente-testes`) and default to "Fail Closed" (deny access) if the environment is not explicitly safe.

## 2025-05-27 - [Authorization Bypass in List Endpoint]
**Vulnerability:** The `GET /api/subprocessos` endpoint allowed any authenticated user to list all subprocesses, exposing organizational workflow state (IDOR/Data Leakage).
**Learning:** `PreAuthorize("isAuthenticated()")` is insufficient for endpoints that return global collections. Default "list all" endpoints often lack context-aware filtering.
**Prevention:** Use `hasRole('ADMIN')` for global listings or implement filtering based on the authenticated user's scope (e.g., unit). Review all methods returning `List<Dto>` for authorization scope.
