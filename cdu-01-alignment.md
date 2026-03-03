# CDU-01 - Realizar login e exibir estrutura das telas - Alignment

## Current Status
- The test verifies invalid credentials.
- It verifies successful login for a single-profile user (GESTOR).
- It verifies the profile selection screen for a multi-profile user (ADMIN + CHEFE).
- It verifies the presence of navbar elements for an ADMIN (Painel, Unidades, Relatórios, Histórico, Configurações, Logout, User Info).
- It verifies the footer text.

## Gaps & Missing Coverage
1. **Navbar items per profile:** Step 9.1.1 states that GESTOR, CHEFE, and SERVIDOR should see `Minha unidade` instead of `Unidades`, and they should not see the gear icon (Configurações). The test only checks the navbar as an ADMIN. It does not verify the differing navbar state for non-admins.

## Recommended Changes
- Add a scenario where a non-ADMIN user (e.g., CHEFE or GESTOR) logs in, and assert that they see the `Minha unidade` link instead of `Unidades`, and that the `Configurações` (gear icon) is explicitly *not* visible.