# Tracking da refatoração de validação e feedback

Este documento acompanha a execução incremental do plano definido em [`plano-validacao.md`](plano-validacao.md) e das diretrizes em [`ux.md`](ux.md).

## Estado geral

- **Fase atual:** Sprint 1 — contrato de erro e DTOs faltantes.
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

## Em aberto

- Migrar contrato legado `subErrors`/`field`/`message` para `erros`/`campo`/`mensagem`.
- Atualizar `RestExceptionHandler`, `normalizeError` e `useFormErrors` no mesmo corte.
- Remover aliases e traduções silenciosas remanescentes após a migração.
- Criar composable padrão de validação de formulário.
- Padronizar foco/rolagem para o primeiro erro relevante.
- Revisar fluxos ainda fora da primeira fatia, especialmente login, relatórios, limpeza e modais de visualização.

## Validações executadas

- `npm run typecheck`
- `./gradlew :backend:compileTestJava`
- `./gradlew :backend:test --tests ...` nos testes backend focados: 108 testes passaram.
- `npm run test:unit -- ...` nos testes frontend focados: 98 testes passaram.
- `npm run test:unit -- --run src/views/__tests__/MapaViewCoverage.spec.ts`: 8 testes passaram.

## Próximo passo recomendado

Migrar o contrato estruturado de erros para português em um único corte controlado:

1. Backend: `ErroApi`, `ErroSubApi` e `RestExceptionHandler`.
2. Frontend: `apiError.ts`, `useFormErrors` e tipos relacionados.
3. Testes: cobrir erro por campo, erro global e rejeição de aliases antigos.
