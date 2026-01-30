## 2026-01-28 - Stateful Authentication Bypass
**Vulnerability:** The authentication flow relied on an in-memory `ConcurrentHashMap` (`autenticacoesRecentes`) to track "authenticated" users by username only. This allowed a race condition where an attacker could hijack a session by polling the authorization endpoint while a legitimate user was logging in.
**Learning:** Storing authentication state in application memory based solely on user identity (without a unique session token bound to the client) is insecure and prone to race conditions, especially in a REST API which should be stateless.
**Prevention:** Always use cryptographically signed tokens (like JWTs) or secure random session IDs stored in HttpOnly cookies to bind the authentication state to the client. Ensure that every step of a multi-step login process verifies this token.
## 2025-02-18 - [IDOR in ProcessoController]
**Vulnerability:** Found  endpoint in  was accessible to any authenticated user, exposing sensitive subprocess details (IDOR).
**Learning:** Existing controller tests () often do not check  annotations unless  is explicitly added to the test class. This leads to a false sense of security where logic is tested but access control is not.
**Prevention:** Always verify security fixes with a dedicated test class that includes . When using SpEL in annotations within tests involving mocks, ensure  is used to allow name resolution.
## 2025-02-18 - [IDOR in ProcessoController]
**Vulnerability:** Found `listarSubprocessos` endpoint in `ProcessoController` was accessible to any authenticated user, exposing sensitive subprocess details (IDOR).
**Learning:** Existing controller tests (`@WebMvcTest`) often do not check `@PreAuthorize` annotations unless `@EnableMethodSecurity` is explicitly added to the test class. This leads to a false sense of security where logic is tested but access control is not.
**Prevention:** Always verify security fixes with a dedicated test class that includes `@EnableMethodSecurity`. When using SpEL in annotations within tests involving mocks, ensure `@MockitoBean(name="...")` is used to allow name resolution.
