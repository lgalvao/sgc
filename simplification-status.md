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
| Backend – Erros | Hierarquia total | **8 classes/interfaces** (era 18+) |
| Backend – `processo/service` | Classes de serviço | **6 classes** (era 8) |
| Backend – DTOs do módulo `subprocesso` | Arquivos | **27** (era 28) |
| Frontend – Stores | Total | **13 stores** Pinia |
| Frontend – Composables de async | `useAsyncAction` | ✅ em uso em `mapas.ts` e `analises.ts` |
| Frontend – Componentes Vue | Total | **48** (era 49) |

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
- [x] Remoção de `ErroProcesso` no módulo `processo`, substituindo por `ErroValidacao` e
  ajustando os testes afetados.
- [x] **Consolidação de serviços do módulo `processo`** de 8 → 6 classes:
  - `ProcessoFinalizador` + `ProcessoInicializador` → `ProcessoWorkflowService`.
  - `ProcessoValidador` + `ProcessoAcessoService` → `ProcessoValidacaoService`.
- [x] **Correção de violação de arquitetura**: acesso cross-module a `UnidadeRepo`/`UnidadeMapaRepo`
  agora roteado via `UnidadeService`; adicionado método `buscarMapasPorUnidades`.
- [x] **Remoção de 5 métodos mortos** de `UsuarioFacade` sem chamadores em produção:
  `buscarUsuariosPorUnidade`, `buscarUsuariosAtivos`, `buscarResponsaveisUnidades`,
  `buscarUnidadesOndeEhResponsavel`, `isAdministrador`.
- [x] **Consolidação de 3 métodos idênticos** `iniciarProcessoMapeamento` + `iniciarProcessoRevisao`
  + `iniciarProcessoDiagnostico` → `iniciarProcesso`; simplificação do dispatch no
  `ProcessoController` (eliminado o mapa de funções).
- [x] **Frontend**: `analises.ts` migrado de try/catch manual para `useAsyncAction`.
- [x] **Backend**: `ErroValidacaoDto` embutido como record aninhado `ValidacaoCadastroDto.Erro`;
  arquivo `ErroValidacaoDto.java` removido (DTOs de 28 → 27).
- [x] **Backend**: `AlertaFacade` passa a injetar `UsuarioService` diretamente em vez de `UsuarioFacade`
  (remove dependência facade→facade); método `buscarPorTitulo` removido de `UsuarioFacade`.
- [x] **Backend**: `SubprocessoNotificacaoService` passa a injetar `UsuarioService` diretamente em vez
  de `UsuarioFacade` (remove dependência cross-module desnecessária no facade).
- [x] **Frontend**: Componente `CampoTexto.vue` removido (sem uso em produção); story e teste
  associados também removidos (componentes Vue de 49 → 48).

### Em andamento / Próximos passos

- [ ] **Migrar stores de leitura simples** para composables com estado local na view:
  - Candidatos: `configuracoes` (1 consumidor), `unidades`, `usuarios`.
- [ ] **Consolidar DTOs de `subprocesso`** de 27 → ~14, fundindo DTOs minúsculos de uso único.
- [ ] **Inlinear componentes Vue single-use** para reduzir a contagem de ~48 componentes.
- [ ] **Avaliar `UsuarioFacade`**: método `buscarUsuarioPorTitulo` ainda usado apenas por
  `UsuarioController`; avaliar inlining direto no controller.

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
5. Unificar exceções de negócio no `ErroValidacao` reduziu a hierarquia de erros e deixou o contrato HTTP
   mais consistente (422) para validações no módulo `processo`.
6. Serviços com nomes terminados em `Service` são sujeitos ao teste de arquitetura ArchUnit que proíbe
   acesso cross-module a repositórios. Ao renomear `ProcessoInicializador` → `ProcessoWorkflowService`,
   foi necessário mover o acesso a `UnidadeRepo`/`UnidadeMapaRepo` para dentro do `UnidadeService`.
7. Métodos mortos (sem chamadores em produção) podem acumular nos facades ao longo do tempo. É vale
   verificar regularmente com grep/análise estática.
8. Múltiplos métodos de facade idênticos (como os 3 `iniciarProcesso*`) surgem quando o dispatcher
   foi movido para o service subjacente mas os métodos de fachada não foram consolidados.
9. Dependências facade→facade (ex.: `AlertaFacade` injetando `UsuarioFacade`) são ruído arquitetural —
   quando a operação é simples (buscar entidade por id), injetar o `Service` diretamente é mais limpo.
10. Componentes Vue sem nenhum consumidor em produção (ex.: `CampoTexto`) devem ser removidos junto com
    seus testes e stories para evitar acúmulo de código morto.
