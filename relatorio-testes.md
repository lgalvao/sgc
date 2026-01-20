# Relatório de Testes Automatizados

**Data:** 20/01/2026, 20:45:29
**Sistema:** Linux 6.8.0

## Resumo Executivo

| Teste | Status | Duração (s) |
| :--- | :---: | :---: |
| Backend - Testes Unitários | ✅ Sucesso | 8.14s |
| Frontend - Testes Unitários | ✅ Sucesso | 64.66s |
| E2E - Playwright | ✅ Sucesso | 11.43s |

### Estatísticas Detalhadas

| Teste | Total | Passou | Falhou | Ignorado |
| :--- | :---: | :---: | :---: | :---: |
| Backend - Testes Unitários | 975 | 975 | 0 | 0 |
| Frontend - Testes Unitários | 1101 | 1101 | 0 | 0 |
| E2E - Playwright | 6 | 6 | 0 | 0 |

**Status Geral:** 🟢 APROVADO

## Detalhes da Execução

### Backend - Testes Unitários

- **Comando:** `./gradlew unitTest`
- **Diretório:** `backend`
- **Status:** ✅ Sucesso
- **Resultados:** 975 testes, 975 aprovados, 0 falhas

<details>
<summary>Ver Logs de Saída</summary>

```text
Starting a Gradle Daemon, 1 busy Daemon could not be reused, use --status for details
5 actionable tasks: 5 up-to-date
```

</details>

---

### Frontend - Testes Unitários

- **Comando:** `npm run test:unit`
- **Diretório:** `frontend`
- **Status:** ✅ Sucesso
- **Resultados:** 1101 testes, 1101 aprovados, 0 falhas

<details>
<summary>Ver Logs de Saída</summary>

```text
 RUN  v4.0.17 /app/frontend
 Test Files  98 passed (98)
      Tests  1101 passed (1101)
   Start at  20:44:14
   Duration  62.87s (transform 6.71s, setup 35.02s, import 29.45s, tests 22.04s, environment 80.13s)
```

</details>

---

### E2E - Playwright

- **Comando:** `npx playwright test e2e/cdu-01.spec.ts`
- **Diretório:** `.`
- **Status:** ✅ Sucesso
- **Resultados:** 6 testes, 6 aprovados, 0 falhas

<details>
<summary>Ver Logs de Saída</summary>

```text
Running 6 tests using 1 worker
ium] › e2e/cdu-01.spec.ts:9:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir erro com credenciais inválidas
ium] › e2e/cdu-01.spec.ts:14:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve realizar login com sucesso (Perfil Único)
ium] › e2e/cdu-01.spec.ts:22:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir seleção de perfil se houver múltiplos
ium] › e2e/cdu-01.spec.ts:34:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir barra de navegação após login
ium] › e2e/cdu-01.spec.ts:46:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir informações do usuário e controles
ium] › e2e/cdu-01.spec.ts:60:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir rodapé
[1A[2K  6 passed (7.5s)
```

</details>

---

