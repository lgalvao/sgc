# Relatório de Segurança - SGC (Pendências Atuais)

Este arquivo foi filtrado após a rodada massiva de segurança no backend concluída recentemente. Todas as vulnerabilidades críticas e arquiteturais do servidor Java (IDOR, Broken Access Control, E2E leaking, PII leakage, X-Forwarded-For Bypass, Default JWT Secrets) **foram mitigadas** e validadas por mais de 950 testes unitários.

Abaixo constam **apenas as 2 vulnerabilidades restantes** que deverão ser tratadas na próxima fase técnica (com foco exclusivo no Frontend):

---

### 1. JWT armazenado em `localStorage` no frontend

- **Severidade:** Média/Alta
- **Local apontado na auditoria original:**
  - `frontend/src/stores/perfil.ts:9-10,127-138`
  - `frontend/src/axios-setup.ts:43-49`

**Evidência**

- O token JWT é gravado em `localStorage` com a chave `jwtToken` nativa.
- O `axios` lê o token direto da storage ou da Pinia Store e injeta em cabeçalhos `Authorization: Bearer`.

**Impacto**

Esse padrão eleva em muitas vezes o impacto de qualquer vulnerabilidade eventual de Injeção (XSS), pois scripts maliciosos injetados pelo atacante no navegador da vítima conseguem ler facilmente `window.localStorage` e enviar o token permanente para o servidor do hacker, burlando qualquer proteção do servidor Backend.

**Sugestões (A implementar)**

- Mover a persistência do JWT no login para um cookie do tipo HttpOnly (para impedir a extração via JavaScript).
- Refatorar inteiramente a lógica de interceptor do `axios-setup.ts` para usar o fluxo `withCredentials`.
- Endurecer Content Security Policy (CSP).

---

### 2. Vulnerabilidades Críticas em bibliotecas de desenvolvimento do Frontend

- **Severidade:** Alta
- **Ferramenta de Auditoria:** `npm audit` (na pasta frontend)

**Evidência e Resultado**

- Na auditoria atual, acusam **8 vulnerabilidades de nível Alto**, profundamente embasadas na biblioteca transitiva `flatted` (associada ao framework de testes unitários `@vitest`).

**Impacto**

Apesar de as bibliotecas afetadas pertencerem apenas as devDependencies (e não influenciarem diretamente o frontend empacotado que o usuário final utiliza), vulnerabilidades aqui são foco principal em ataques de "Envenenamento de Cadeia de Suprimentos" (Supply Chain attacks) e comprometimento local das máquinas de desenvolvimento ou runners de CI/CD.

**Sugestões (A implementar)**

- Executar atualização controlada e coordenada do stack `vitest`/`flatted` na package.json.
- Realizar validação formal (executar todos os testes de novo), visto que as correções quebram compatibilidade retroativa (`breaking changes`) da lib `@vitest/coverage-v8`.
