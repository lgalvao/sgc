# Tracking da refatoração de validação e feedback

Este documento acompanha a execução incremental do plano definido em [`plano-validacao.md`](plano-validacao.md) e das diretrizes em [`ux.md`](ux.md).

## Estado geral

- **Fase atual:** convergência fina dos fluxos restantes; base compartilhada, modais críticos, administração, limpeza e relatórios já estão alinhados ao padrão.
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
- Migrado `ParametrosView` para `useValidacaoFormulario`: eliminou `validacaoSubmetida` local (ref próprio),
  extraiu computed `formularioValido` e aplica `focarPrimeiroErroInvalido` no submit inválido.
- Consolidado `ProcessoCadastroView`: substituiu `document.querySelector('.is-invalid')` + `nextTick` inline
  por `focarPrimeiroErroInvalido()` do composable — foco no backend agora reutiliza o mesmo utilitário.
- `LoginView` mantida sem alteração: exceção saudável documentada no `ux.md` (segurança/autenticação).
- **Revisão da exceção do Login (por decisão do usuário):** adicionado feedback inline por campo para
  título e senha vazios (`BFormInvalidFeedback` com `ERRO_CAMPO_TITULO` e `ERRO_CAMPO_SENHA`), usando
  `useValidacaoFormulario`. Erros de credenciais, rede e autorização permanecem como alerta global
  (exceção de segurança preservada). Removidas constantes `ERRO_PREENCHIMENTO` (código morto).

- Implementado erro inline no dropdown de perfil (passo 2 do Login) em substituição ao toast.

### Sprint 2: Modais com Campos Obrigatórios (Concluída)
- `DisponibilizarMapaModal`: removido o bloqueio silencioso do botão (`acao-desabilitada`), implementado `useValidacaoFormulario` para exibir a obrigatoriedade da data apenas no submit, com foco na origem do erro.
- `CriarCompetenciaModal`: removido `salvamentoDesabilitado` silencioso, convertidas validações textuais para computadas via `deveExibirErro` do composable, adicionado `.is-invalid` na caixa de listagem de atividades e focado o primeiro erro no clique do botão Salvar.
- `SubprocessoView` (Modal de Reabertura): convertido aviso genérico de "Informe a justificativa" que era renderizado indiscriminadamente para `BFormInvalidFeedback` reativo a submit, integrado com validação global usando `useValidacaoFormulario`.

### Sprint 4: Administração, Limpeza e Relatórios (Concluída)
- `AdministradoresView`: modal de adição migrado para `useValidacaoFormulario`, com obrigatoriedade inline do título, foco no primeiro erro e sem validação solta fora do contexto do campo.
- `LimpezaProcessosView`: código do processo passou a validar no clique da ação, com `BFormInvalidFeedback` inline e foco antes da abertura da confirmação.
- `RelatorioAndamentoView`: seleção obrigatória do processo agora usa validação inline no submit, com foco automático no primeiro erro.
- `RelatorioMapasView`: seleção obrigatória do processo agora usa validação inline no submit, preservando bloqueio apenas para carregamento.

### Cadastro de atividades (Concluída)
- `CadAtividadeForm`: migrado para `useValidacaoFormulario`, removendo o pré-bloqueio do botão "adicionar atividade" por campo vazio. A obrigatoriedade agora valida no submit com erro inline e limpeza após correção.
- `CadastroView`: limpeza de erros estruturados (globais e por item) centralizada em helpers privados. Quando o cadastro muda após uma validação inválida, o feedback anterior é descartado para evitar erro stale durante a correção.
- `CadastroView`: fluxo de disponibilização simplificado com helpers privados para pré-validação e aplicação do resultado de `validarCadastro`, preservando erro global, erro por item, scroll e abertura do modal de confirmação.
- `AtividadeItem`: formulário de novo conhecimento alinhado ao padrão de validação inline no submit. O componente deixou de falhar silenciosamente em campo vazio e passou a limpar o erro local após a correção.
- `InlineEditor` + `AtividadeItem`: edição inline de atividade e conhecimento agora mostra erro local quando o save é tentado com texto vazio, mantendo a edição aberta até a correção. A falha silenciosa do save vazio foi removida sem mudar os eventos externos.

## Em aberto

- Hardening final da refatoração: consolidar a atualização do plano macro, revisar microcopy residual e ampliar a leitura de cobertura/regressão dos fluxos já convergidos.

### Sprint 3: Acessibilidade e Clareza (Concluída)
- [x] Indicação visual de campos obrigatórios (asterisco) em todos os formulários e modais.
- [x] Consolidação de formulários remanescentes com validações customizadas para o padrão `useValidacaoFormulario`:
    - `CadastroVisualizacaoView`: modal de devolução.
    - `SubprocessoModal`: modal de alteração de data limite (migrado para `ModalPadrao`).
    - `MapaView`: modal de devolução.
    - `MapaVisualizacaoView`: modais de sugestões e devolução.
    - `ModalAcaoBloco`: modal de ações em bloco (migrado para `ModalPadrao`).
    - `ImportarAtividadesModal`: modal de importação (migrado para `ModalPadrao`).
- [x] Remoção de bloqueios preventivos (`ok-disabled`, `acao-desabilitada`) permitindo validação no clique em todos os modais refatorados.

## Validações executadas
...
- `npm run typecheck` e `npm run lint` após refatoração da Sprint 3: aprovados.

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
- `npx vitest run ParametrosView.spec.ts CadProcesso.spec.ts CadProcessoCoverage.spec.ts useValidacaoFormulario.spec.ts`: 49 testes passaram.
- `npm run typecheck` após migração de ParametrosView e ProcessoCadastroView: sem erros.

- `npx vitest run DisponibilizarMapaModal.spec.ts CriarCompetenciaModal.spec.ts SubprocessoView.spec.ts`: Testes atualizados e passando sem regressões.
- `npm run test:unit --prefix frontend -- src/components/__tests__/AtividadeItem.spec.ts src/components/atividades/__tests__/CadAtividadeForm.spec.ts src/views/__tests__/AtividadesCadastroView.spec.ts`: 39 testes passaram após a convergência do cadastro de atividades.
- `npm run typecheck`: aprovado após a convergência do cadastro de atividades.

## Próximo passo recomendado

Sprint 5 — hardening: atualizar o plano macro para refletir a convergência do cadastro de atividades e seguir com a auditoria final de regressão de UX e acessibilidade.
