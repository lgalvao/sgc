## 2024-05-22 - Memory Leak in Authentication Flow
**Vulnerability:** The `autenticacoesRecentes` map in `UsuarioService` stores successful authentication timestamps but only clears them upon successful login (`entrar`). If a user authenticates but never completes the login (or if an attacker spams the authentication endpoint), the map grows indefinitely, leading to a memory leak and potential DoS.
**Learning:** Stateful tracking of multi-step processes needs robust cleanup mechanisms (TTL/Expiration), not just cleanup on success.
**Prevention:** Use a caching library with expiration (like Caffeine or Guava) or implement a periodic cleanup task for map entries that exceed a certain age.
