## 2025-12-12 - [CRITICAL] Fix Insecure Authentication / Token Forgery
**Vulnerability:** The application used a "simulated" authentication where the JWT was just a Base64 encoded JSON string. Any user could forge a token by encoding a JSON with their desired role (e.g., ADMIN).
**Learning:** Even in "simulated" or "dev" environments, security mechanisms should be robust against trivial bypass. A "simulated" token should still be signed to prevent forgery if the environment is exposed.
**Prevention:** Implement HMAC signing for any token-based authentication, even if the "password check" is mocked. Used a random UUID generated at startup as the secret key to ensure tokens are invalidated on restart, avoiding hardcoded secrets.
