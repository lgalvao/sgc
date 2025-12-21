# Sentinel Journal

## 2024-05-22 - Critical Auth Bypass in Stateless Login Flow
**Vulnerability:** The login flow was split into three stateless REST steps (`/autenticar` -> `/autorizar` -> `/entrar`). The final step `/entrar` (which issues the JWT) did not verify that the user had successfully completed the password verification in `/autenticar`. It only checked if the user had authorization for the requested unit/profile. This allowed anyone to bypass authentication by calling `/entrar` directly with a valid user ID.
**Learning:** Stateless APIs that implement multi-step workflows (like 2FA or Profile Selection) must carry a "proof of progress" token (state) between steps. Relying on client-side sequencing is a critical flaw.
**Prevention:**
1. Use a temporary signed token (Pre-Auth Token) returned by the first step, required by subsequent steps.
2. Or use a server-side short-lived cache to track recent successful authentications (implemented here as a non-breaking fix).
3. Or merge the steps into a single request if possible.
