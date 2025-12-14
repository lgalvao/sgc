## 2025-12-12 - [CRITICAL] Fix Insecure Authentication / Token Forgery
**Vulnerability:** The application used a "simulated" authentication where the JWT was just a Base64 encoded JSON string. Any user could forge a token by encoding a JSON with their desired role (e.g., ADMIN).
**Learning:** Even in "simulated" or "dev" environments, security mechanisms should be robust against trivial bypass. A "simulated" token should still be signed to prevent forgery if the environment is exposed.
**Prevention:** Implement HMAC signing for any token-based authentication, even if the "password check" is mocked. Used a random UUID generated at startup as the secret key to ensure tokens are invalidated on restart, avoiding hardcoded secrets.

## 2025-12-13 - [HIGH] Unsecured Actuator Endpoints
**Vulnerability:** Spring Boot Actuator endpoints (e.g., `/actuator/**`) were explicitly permitted for anonymous access in `ConfigSeguranca.java`. This could expose internal application state, configuration (environment variables), and heap dumps.
**Learning:** Explicit `permitAll()` on broad paths like `/actuator/**` overrides Spring Boot's default protections (which usually only expose `health`). Security configuration must explicitly protect administrative and observability endpoints.
**Prevention:** Added a specific security rule to require `ROLE_ADMIN` for all `/actuator/**` endpoints.

## 2025-12-13 - [INFO] Non-standard Spring Boot Packages
**Observation:** The project uses a custom or future version of Spring Boot (4.0.0) where `AutoConfigureMockMvc` is located in `org.springframework.boot.webmvc.test.autoconfigure` instead of the standard `org.springframework.boot.test.autoconfigure.web.servlet`.
**Action:** Used the repository-specific package in tests to avoid compilation errors.

## 2025-12-14 - [MEDIUM] Information Exposure in Validation Errors
**Vulnerability:** The `RestExceptionHandler` was reflecting `rejectedValue` from `MethodArgumentNotValidException` and `ConstraintViolationException` back to the client in JSON responses.
**Learning:** Even with generic error handlers, standard Spring validation exceptions often carry the raw invalid input. If this input is sensitive (e.g., a password that failed complexity checks), it gets logged or returned to the user, leading to info leakage (CWE-209).
**Prevention:** Explicitly nullify or mask `rejectedValue` in global exception handlers. Do not trust that validation errors only occur on non-sensitive fields.
