## 2026-01-28 - Stateful Authentication Bypass
**Vulnerability:** The authentication flow relied on an in-memory `ConcurrentHashMap` (`autenticacoesRecentes`) to track "authenticated" users by username only. This allowed a race condition where an attacker could hijack a session by polling the authorization endpoint while a legitimate user was logging in.
**Learning:** Storing authentication state in application memory based solely on user identity (without a unique session token bound to the client) is insecure and prone to race conditions, especially in a REST API which should be stateless.
**Prevention:** Always use cryptographically signed tokens (like JWTs) or secure random session IDs stored in HttpOnly cookies to bind the authentication state to the client. Ensure that every step of a multi-step login process verifies this token.

## 2026-01-29 - Log Injection via X-Forwarded-For
**Vulnerability:** The `LoginController` extracted the client IP address from the `X-Forwarded-For` header and logged it directly without sanitization. This allowed attackers to inject fake log entries (Log Forging/CWE-117) by sending requests with newlines in the header.
**Learning:** Even "system" headers like `X-Forwarded-For` are untrusted user input when the request comes from the internet. Standard Java loggers often do not automatically sanitize newlines.
**Prevention:** Always sanitize untrusted input before logging. Replacing `\n` and `\r` with `_` is a simple and effective mitigation for log injection.
