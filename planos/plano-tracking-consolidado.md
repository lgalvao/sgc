# Plano e Tracking Consolidado — Refatoração + Qualidade

> Consolida: `planos/plano-refatoracao.md`, `planos/tracking-refatoracao.md`, `quality-report.md` e `quality-tracking.md`.
> Objetivo: manter um único documento operacional para direção arquitetural, estado atual e backlog priorizado.

---

## 1. Objetivo consolidado

Conduzir a simplificação estrutural do SGC sem quebrar contratos, requisitos, regras de acesso ou UX, atacando em paralelo:

1. hotspots arquiteturais e God Classes;
2. complexidade acidental no frontend, especialmente em subprocesso/cadastro/mapa;
3. dívida de qualidade remanescente da análise consolidada;
4. estabilidade de testes, com foco em tipagem, E2E semântico e regressões reais.

---

## 2. Estado atual consolidado

### 2.1 Direção arquitetural já validada

- O frontend de subprocesso agora usa `stores/subprocesso` como fonte única de verdade para contexto de edição/cadastro.
- `useSubprocessos` foi removido do runtime e dos testes remanescentes.
- `SubprocessoView`, `CadastroView`, `MapaView`, `useFluxoSubprocesso`, `useInvalidacaoNavegacao` e `SubprocessoCards` já foram alinhados ao store consolidado.
- A regra de frontend permanece: erros irrecuperáveis do backend devem falhar rápido, com tratamento centralizado, sem camadas de compatibilidade.

### 2.2 Qualidade já consolidada

- A maior parte dos itens críticos do `quality-report.md` já foi resolvida nas rodadas anteriores.
- O `quality-tracking.md` ficou internamente inconsistente no resumo final: a tabela aponta 4 pendências históricas, mas o cabeçalho diz 3.
- Após a rodada atual de consolidação do store e correção dos fluxos de cadastro/mapa, o item **8.5 (requisições duplicadas no frontend)** deve ser tratado como **resolvido**.

### 2.3 Situação validada nesta rodada

- `npm run test:unit`: 1286 testes verdes.
- `npx playwright test`: Todos os 217 cenários E2E (incluindo suíte completa, jornada e capturas) verdes.
- Estabilidade de URLs recuperada: helpers E2E agora suportam parâmetros de consulta (`codSubprocesso`).
- Correções de mock: stores agora expõem `invalidar` e `limparContextoAtual` consistentemente em ambiente de teste.

### 2.4 Diagnóstico de Monitoramento (Auditoria P0)

Após rodadas monitoradas (`SGC_MONITORAMENTO=sim`) e auditoria de código, as seguintes melhorias foram implementadas/identificadas:

1.  **Invalidação Conservadora**: O default em `useInvalidacaoNavegacao` foi alterado de `true` para `false` para `incluirPainel` e `incluirProcesso`. Isso evitou ~2 round-trips por navegação interna.
2.  **Double Reload pós-mutação**: Resolvido em `SubprocessoView.vue` com o uso de `dadosValidosEdicao` no `onActivated` e remoção de chamadas redundantes em `useFluxoSubprocesso`.
3.  **Toast Pendente e Redirecionamento**: Identificado que Toasts de sucesso se perdiam em redirecionamentos. Corrigido com `toastStore.setPending`.
4.  **Payload Bloat (Movimentações)**: O `SubprocessoConsultaService` agora limita o histórico enviado no contexto inicial a 15 itens, reduzindo drasticamente o JSON para processos antigos.
5.  **Seletor E2E (BOrchestrator)**: A classe `.orchestrator-container` foi reintroduzida no `App.vue` para estabilizar os seletores semânticos dos helpers Playwright.

---

## 3. Pendências reais remanescentes

### 3.1 Backlog herdado da análise de qualidade

| Item | Severidade | Situação consolidada | Próxima ação |
|---|---|---|---|
| 1.1 `ProcessoService` como God Service | Média | Pendente | separar workflow, consulta e validação |
| 1.2 `CadastroFluxoService` métodos duplicados | Média | **Concluído** | Consolidado mapeamento e revisão |
| 1.3 `PermissoesSubprocessoDto` com 34 booleanos | Média | Pendente | redesenhar contrato interno/externo com cuidado |
| 1.4 `CadastroView.vue` e `MapaView.vue` grandes | Média | Pendente | extrair bootstrap, ações e modais |

### 3.2 Backlog estrutural/remanescente da refatoração

| Prioridade | Frente | Item | Situação |
|---|---|---|---|
| P0 | Frontend | Auditar recargas redundantes em `SubprocessoView`, `CadastroView` e `MapaView` após mutações | **Concluído** |
| P0 | Frontend | Reduzir invalidação ampla em `useInvalidacaoNavegacao` | **Concluído** |
| P0 | Frontend | Consolidar cancelamento escopado por rota/subprocesso com `AbortController` | Pendente |
| P0 | Qualidade | Rodar a suíte E2E completa como gate de saída | **Concluído** |
| P0 | Qualidade | Revalidar estabilidade de alertas/notificações nos casos CDU-32/33 | Pendente |
| P1 | Backend | Consolidar a fachada remanescente de `SubprocessoConsultaService` | Pendente |
| P1 | Backend | Isolar responsabilidades de `ProcessoService` | Pendente |
| P1 | Backend | Reduzir acoplamento restante em `SubprocessoTransicaoService` | Parcial |
| P1 | Backend | Consolidar métodos idênticos em `CadastroFluxoService` | **Concluído** |
| P1 | Frontend | Extrair bootstrap de contexto e orquestração local de `CadastroView` | Pendente |
| P1 | Frontend | Extrair bootstrap de contexto e orquestração local de `MapaView` | Pendente |
| P2 | Backend | Reduzir nulabilidade estrutural em DTOs estáveis | Pendente |
| P2 | Frontend | Limpar `any` remanescente em helpers compartilhados de teste/E2E | Pendente |
| P2 | UX/QA | Validar responsividade móvel e recuperar status verde no dashboard de QA | Pendente |

---

## 4. Estratégia de execução

### P0 — Estabilização operacional

1. concluir a auditoria de recarregamento e invalidação em subprocesso/cadastro/mapa;
2. executar suíte E2E completa e tratar regressões por causa raiz;
3. fechar as pendências de alertas/notificações ainda sensíveis.

### P1 — Redução de acoplamento estrutural

1. quebrar `ProcessoService` por responsabilidade;
2. concluir a redução de superfície em `SubprocessoConsultaService` e `SubprocessoTransicaoService`;
3. extrair da view para composables apenas o que for contrato real, sem criar wrappers finos.

### P2 — Dívida técnica sustentável

1. atacar DTOs e nulabilidade onde o contrato já está estável;
2. reduzir complexidade dos testes/helpers compartilhados;
3. consolidar o dashboard de QA como evidência de saída, não como fonte paralela de verdade.

---

## 5. Guardrails mantidos

- Nada de resiliência artificial em E2E.
- Nada de compatibilidade legada, aliases ou fachadas finas só para preservar chamadas antigas.
- Nada de tratamento local para erro irrecuperável de backend no frontend.
- Simplificação sempre por menor fronteira segura, com contrato explícito.
- `CadastroView` e `MapaView` devem perder orquestração acidental, não regra de negócio.

---

## 6. Próximo corte natural

O próximo alvo com melhor relação risco/retorno é:

1. **otimização de payload e estreitamento de interfaces (P1.1)**, focando em remover duplicação de dados entre o `mapa` e `atividadesDisponiveis` (P1.1 finalizado no histórico de movimentações);
2. em seguida, **extração do bootstrap/orquestração de `CadastroView` e `MapaView`** (Iniciando P1.3);
3. depois, **simplificação de `useFluxoSubprocesso`** para consolidar feedback e redirecionamento;
4. por fim, **quebra de `ProcessoService`** como principal God Service remanescente.
