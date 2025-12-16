## 2024-05-22 - [JWT Secret Externalization]
**Vulnerability:** Hardcoded JWT secret key in `JwtProperties.java` and `application.yml`.
**Learning:** Default values in `@ConfigurationProperties` classes can be dangerous if they contain sensitive secrets, as they provide a fallback even if configuration files are missing. Also, test environment schema (`schema.sql`) was inconsistent with entity mappings (`usuario_codigo` vs `usuario_titulo`), masking issues until tests were run.
**Prevention:** Always use `${VAR_NAME}` injection for secrets and force a failure (no default) in production profiles. Maintain consistency between test schema and JPA mappings.
