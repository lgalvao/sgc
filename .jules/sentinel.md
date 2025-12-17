## 2024-05-22 - [JWT Secret Externalization]
**Vulnerability:** Hardcoded JWT secret key in `JwtProperties.java` and `application.yml`.
**Learning:** Default values in `@ConfigurationProperties` classes can be dangerous if they contain sensitive secrets, as they provide a fallback even if configuration files are missing. Also, test environment schema (`schema.sql`) was inconsistent with entity mappings (`usuario_codigo` vs `usuario_titulo`), masking issues until tests were run.
**Prevention:** Always use `${VAR_NAME}` injection for secrets and force a failure (no default) in production profiles. Maintain consistency between test schema and JPA mappings.

## 2025-05-18 - [DoS Protection via Input Validation]
**Vulnerability:** Missing length limits on authentication DTOs (`AutenticacaoReq`, `EntrarReq`) allowed potentially large payloads, posing a Denial of Service risk.
**Learning:** Even simulated or internal-facing endpoints should validate input size to prevent resource exhaustion or unexpected behavior in the persistence layer.
**Prevention:** Always apply `@Size` constraints to String fields in DTOs, especially those used in public endpoints.
