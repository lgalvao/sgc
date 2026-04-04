# Fechamento dos issues — CDU-32 e CDU-33 (alertas para unidade superior)

## Issue 1 — CDU-32: alerta para unidade superior na reabertura de cadastro

### Status
✅ **Resolvido**

### Evidências técnicas
- **Correção de backend (mensagem esperada na UI):**
  - `AlertaFacade.criarAlertaReaberturaCadastroSuperior` agora gera `"Cadastro da unidade %s reaberto"`.
- **Integração backend:**
  - `CDU32IntegrationTest` valida que o alerta é gerado para:
    1) unidade alvo do subprocesso; e
    2) unidade superior imediata (`codigoUnidadeSuperior` não nulo).
- **E2E (comportamento visível ao usuário):**
  - `e2e/cdu-32.spec.ts` inclui o cenário:
    - `"Cenário complementar: unidade superior visualiza alerta de reabertura no painel"`,
    - com login `GESTOR_COORD_22`,
    - validando descrição, processo e data.

### Comandos e resultados
- `./gradlew :backend:test --tests 'sgc.integracao.CDU32IntegrationTest' --tests 'sgc.integracao.CDU33IntegrationTest'`
  - Resultado: **8 testes executados, 8 aprovados**.
- `npx playwright test e2e/cdu-32.spec.ts e2e/cdu-33.spec.ts --workers=1`
  - Resultado: **8 cenários executados, 8 aprovados**.

### Comentário sugerido para fechamento do issue
> Issue validada e encerrada.  
> Foi corrigida a mensagem do alerta de reabertura para unidade superior e adicionadas provas automatizadas em dois níveis:  
> 1) integração backend (`CDU32IntegrationTest`), garantindo criação do alerta para a cadeia hierárquica superior;  
> 2) E2E (`e2e/cdu-32.spec.ts`), garantindo visualização do alerta por `GESTOR_COORD_22` no painel, com validação de descrição, processo e data.

---

## Issue 2 — CDU-33: alerta para unidade superior na reabertura de revisão

### Status
✅ **Resolvido**

### Evidências técnicas
- **Integração backend:**
  - `CDU33IntegrationTest` valida que o alerta é gerado para:
    1) unidade solicitante; e
    2) unidade superior imediata (`codigoUnidadeSuperior` não nulo),
    incluindo verificação textual da descrição.
- **E2E (comportamento visível ao usuário):**
  - `e2e/cdu-33.spec.ts` inclui o cenário:
    - `"Cenário complementar: unidade superior visualiza alerta de reabertura de revisão no painel"`,
    - com login `GESTOR_COORD_21`,
    - validando descrição, processo e data.

### Comandos e resultados
- `./gradlew :backend:test --tests 'sgc.integracao.CDU32IntegrationTest' --tests 'sgc.integracao.CDU33IntegrationTest'`
  - Resultado: **8 testes executados, 8 aprovados**.
- `npx playwright test e2e/cdu-32.spec.ts e2e/cdu-33.spec.ts --workers=1`
  - Resultado: **8 cenários executados, 8 aprovados**.

### Comentário sugerido para fechamento do issue
> Issue validada e encerrada.  
> O fluxo de reabertura de revisão passou a ter comprovação automatizada para alerta da unidade superior em backend e UI:  
> 1) integração (`CDU33IntegrationTest`) garante criação correta do alerta para superior;  
> 2) E2E (`e2e/cdu-33.spec.ts`) comprova que `GESTOR_COORD_21` visualiza no painel a mensagem esperada com processo e data/hora.
