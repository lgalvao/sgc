# Status de Simplificação — SGC

> **Contexto:** aplicação de intranet com 5-10 usuários simultâneos. Sobreengenharia deve ser removida
> progressivamente, priorizando impacto alto e risco baixo.

---

## Estado Atual (01/03/2026)

| Camada | Indicador | Valor atual |
|---|---|---|
| Backend – `SubprocessoService` | Linhas | **779** (era 1 712) |
| Backend – `SubprocessoTransicaoService` | Linhas | **704** (extraído do service original) |
| Backend – `SubprocessoController` | Linhas | **524** (era 549) |
| Backend – Facades | Total | **7** (`UsuarioFacade` + 6 com lógica real) |
| Backend – Erros | Hierarquia total | **9 classes/interfaces** (era 18+) |
| Backend – `processo/service` | Classes de serviço | **8 classes** |
| Backend – DTOs do módulo `subprocesso` | Arquivos | **28** |
| Frontend – Stores | Total | **13 stores** Pinia |
| Frontend – Composables de async | `useAsyncAction` | ✅ criado e em uso em `mapas.ts` |

---

## Progresso

### Concluído

- [x] Investigação inicial e identificação dos pontos de sobreengenharia.
- [x] Remoção de `OrganizacaoFacade` (151 linhas / 28 métodos 100% pass-through).
- [x] Redução de `SubprocessoService` de 1 712 → 779 linhas via extração de
  `SubprocessoValidacaoService` e `SubprocessoTransicaoService`.
- [x] Centralização da validação de atividades no `SubprocessoService` (removida do controller).
- [x] Criação do composable `useAsyncAction()` e uso em `mapas.ts`.
- [x] Redução de classes de erro de 18+ → 9 (eliminadas subclasses de `subprocesso/erros` e
  a maioria das de `processo/erros`).
- [x] Movimentação de `enviarLembrete` do `ProcessoFacade` para `ProcessoNotificacaoService`,
  removendo 3 dependências desnecessárias da facade (`AlertaFacade`, `EmailService`,
  `EmailModelosService`).

### Em andamento / Próximos passos

- [ ] **Remover `ErroProcesso`** — a única subclasse remanescente em `processo/erros` (14 linhas).
  Substituir pelo `ErroValidacao` comum e ajustar os 5 pontos de uso + testes.
- [ ] **Consolidar serviços do módulo `processo`** de 8 → 4-5 classes:
  - `ProcessoFinalizador` (59 linhas) + `ProcessoInicializador` (150 linhas) → `ProcessoWorkflowService`.
  - `ProcessoValidador` (93 linhas) + `ProcessoAcessoService` (98 linhas) → unir em um único validador.
- [ ] **Migrar stores de leitura simples** para composables com estado local na view:
  - Candidatos: `configuracoes`, `unidades`, `usuarios` (dados pouco voláteis, sem escrita complexa).
- [ ] **Avaliar `UsuarioFacade`** (~50% pass-through): separar métodos de autenticação/contexto
  (lógica real) dos métodos que apenas delegam a `UsuarioService`.
- [ ] **Consolidar DTOs de `subprocesso`** de 28 → ~15, fundindo DTOs minúsculos de uso único.
- [ ] **Inlinear componentes Vue single-use** para reduzir a contagem de ~146 componentes.

---

## Aprendizados

1. O principal motor de complexidade é a duplicação de lógica entre camadas (Controller ↔ Service,
   Facade ↔ Service).
2. Refatorações orientadas por fluxo (ex.: mover `enviarLembrete` para o serviço de notificação) são
   seguras e revelam dependências desnecessárias na camada superior.
3. Simplificações de uma camada (ex.: remover 3 deps da Facade) eliminam testabilidade forçada —
   os testes da Facade voltam a testar apenas delegação.
4. O padrão `useAsyncAction()` no frontend elimina boilerplate de try/catch/loading sem alterar a API
   das stores.
