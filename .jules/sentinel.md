## 2025-02-22 - Authorization Bypass in Login Flow
**Vulnerability:** The `/autorizar` endpoint (step 2 of login) allowed retrieving user permissions without verifying that `/autenticar` (step 1) was completed.
**Learning:** Multi-step workflows need state validation at EACH step. Don't assume the client will follow the sequence.
**Prevention:** Use a short-lived state cache (like `autenticacoesRecentes`) to enforce sequence, or issue a temporary "pre-auth" token.
