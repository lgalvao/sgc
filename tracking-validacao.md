# Tracking da refatoração de validação e feedback

Este documento acompanha a execução incremental do plano definido em [`plano-validacao.md`](plano-validacao.md) e das diretrizes em [`ux.md`](ux.md).

## Estado geral

- **Fase atual:** Sprint 1 — contrato de erro e DTOs faltantes (concluída); iniciando Sprint 2.
- **Última atualização:** 2026-04-24.
- **Diretriz vigente:** validação não usa toast; erros corrigíveis aparecem inline ou em `BAlert` contextual.

## Concluído

- Criado [`ux.md`](ux.md) com diretrizes de validação, feedback e disponibilidade de ações.
- Renomeado `validation-report.md` para [`plano-validacao.md`](plano-validacao.md).
- Adicionado `AdicionarAdministradorRequest` para `POST /api/usuarios/administradores`.
- Fortalecido `CriarAtribuicaoRequest` com obrigatoriedade de datas e justificativa.
- Separados `CriarCompetenciaRequest` e `AtualizarCompetenciaRequest`.
- Migrado o contrato de competência no frontend de `atividadesIds` para `atividadesCodigos`.
- Removidas notificações de validação nos fluxos tocados:
  - adicionar/remover administrador;
  - criar atribuição temporária;
  - reabrir cadastro/revisão.
- Adicionados feedbacks inline/contextuais nos pontos ajustados.
- Migrado contrato estruturado de erros de `subErrors`/`field`/`message` para `erros`/`campo`/`mensagem`.
- Atualizados `RestExceptionHandler`, `normalizeError`, `useFormErrors` e testes relacionados.
- Criado `useValidacaoFormulario` e aplicado primeiro em `AtribuicaoTemporariaView`.
- Adicionado `focarPrimeiroErroInvalido()` em `useValidacaoFormulario`: após validação falhar, foca
  automaticamente o primeiro campo com classe `.is-invalid`, usando `nextTick` para aguardar o DOM.
  Aplicado em `AtribuicaoTemporariaView`. Consistente com o padrão já existente em `ProcessoCadastroView`
  para erros de backend.

## Em aberto

- Revisar fluxos ainda fora da primeira fatia: login, relatórios, limpeza e modais de visualização.
- **Sprint 2 (frontend base):** criar composable padrão de validação reutilizável (em andamento via
  `useValidacaoFormulario`); expandir uso de `focarPrimeiroErroInvalido` para outros formulários.

## Validações executadas

- `npm run typecheck`
- `./gradlew :backend:compileTestJava`
- `./gradlew :backend:test --tests ...` nos testes backend focados: 108 testes passaram.
- `npm run test:unit -- ...` nos testes frontend focados: 98 testes passaram.
- `npm run test:unit -- --run src/views/__tests__/MapaViewCoverage.spec.ts`: 8 testes passaram.
- `./gradlew :backend:test --tests ...` para contrato de erro: 24 testes passaram.
- `npm run test:unit -- ...` para contrato de erro no frontend: 79 testes passaram.
- `npm run test:unit -- --run src/utils/__tests__/apiError.spec.ts src/composables/__tests__/useFormErrors.spec.ts`: 24 testes passaram.
- `npm run typecheck` após criação de `useValidacaoFormulario`.
- `npx vitest run useValidacaoFormulario.spec.ts AtribuicaoTemporariaView.spec.ts CadAtribuicao.spec.ts`: 25 testes passaram.
- `npm run typecheck` após adição de `focarPrimeiroErroInvalido`: sem erros.

## Próximo passo recomendado

Iniciar Sprint 2: expandir `focarPrimeiroErroInvalido` para `ParametrosView` e `LoginView` (formulários
simples), e revisar se o `ProcessoCadastroView` pode delegar o foco para o composable em vez de usar
`document.querySelector('.is-invalid')` diretamente.
