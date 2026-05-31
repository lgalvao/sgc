# SGC — Auditoria de Qualidade de Software

> Gerada em: 2026-05-30 | Agentes: 3 auditores paralelos (backend, frontend, testes/arquitetura)

---

## Sumário Executivo

O projeto está em **bom estado geral**: cobertura backend de 99,56%, zero TODO/FIXME/HACK, sem código `@Deprecated` ativo, convenções de exceção e REST não-padrão bem seguidas. Os problemas concentram-se em **violações de nomenclatura PT-BR** (persistentes no frontend), um **God Object crescente no backend** (`ProcessoService`), e **fragilidades pontuais nos testes E2E**.

| Severidade | Backend | Frontend | Testes/Arq |
|-----------|---------|----------|-----------|
| 🔴 HIGH | 3 | 8 | 0 |
| 🟡 MEDIUM | 7 | 8 | 8 |
| 🟢 LOW | 5 | 9 | 8 |

---

## 🔴 Problemas de Alta Severidade

### B-H1 — `ProcessoService` é um God Object
**Arquivo:** [ProcessoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/processo/ProcessoService.java)

- **1.480 linhas**, **17 dependências injetadas** via construtor
- Mistura responsabilidades: consulta, workflow, validação de elegibilidade em bloco, integração com notificação e relatório
- 8 métodos privados com **4 a 6 parâmetros** (violação direta da regra ≤ 3):

| Linha | Método | Params |
|-------|--------|--------|
| L691 | `podeAceitarCadastroEmBloco(Subprocesso, Usuario, boolean, Map)` | 4 |
| L707 | `podeAceitarMapaEmBloco(Subprocesso, Usuario, boolean, Map)` | 4 |
| L723 | `podeHomologarCadastroEmBloco(Subprocesso, Usuario, boolean, Map)` | 4 |
| L739 | `podeHomologarMapaEmBloco(Subprocesso, Usuario, boolean, Map)` | 4 |
| L755 | `podeDisponibilizarEmBloco(Subprocesso, Usuario, boolean, Map)` | 4 |
| L777 | `podeExecutarAcaoEmBloco(Subprocesso, Usuario, boolean, Map, AcaoPermissao, Predicate)` | **6** |
| L795 | `verificarPermissaoEscritaEmBloco(Usuario, Subprocesso, AcaoPermissao, boolean, Map)` | **5** |
| L815 | `avaliarElegibilidadeAcaoBloco(Subprocesso, Usuario, boolean, Map)` | 4 |

**Ação recomendada:** Extrair a família `*EmBloco` para um `ProcessoBlocoElegibilidadeService` ou similar, com um DTO de contexto (`ContextoBlocoDto`) carregando `usuario`, `carregarUnidades`, `mapaUnidades`.

---

### B-H2 — `ErroApi` e `ErroNegocioBase` com campos em inglês
**Arquivos:** [ErroApi.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/comum/erros/ErroApi.java) · [ErroNegocioBase.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/comum/erros/ErroNegocioBase.java)

Todos os campos públicos das classes de erro de infraestrutura estão em inglês — diretamente contrário à regra de idioma do projeto:

- `status`, `message`, `code`, `details`, `timestamp`, `traceId` → deveriam ser `situacao`/`mensagem`/`codigo`/`detalhes`/`instante`/`rastreioId`
- Getters correspondentes: `getCode()`, `getMessage()`, `getStatus()`, `getDetails()`

> **Ressalva:** Esses campos aparecem no payload JSON da API. Renomear exige compatibilidade com clientes. Se houver contratos externos, alinhar com o frontend antes.

---

### F-H1 — Funções `handle*` em inglês no código de produção
**Convenção violada:** identificadores devem ser em PT-BR.

| Arquivo | Linha | Identificador | Sugestão |
|---------|-------|---------------|----------|
| [useCadastroTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useCadastroTela.ts) | L220 | `handleImportAtividades` | `aoImportarAtividades` |
| [useCadastroTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useCadastroTela.ts) | L362 | `handleAdicionarAtividade` | `adicionarAtividade` |
| [mapaAnaliseFluxo.ts](file:///Users/leonardo/sgc/frontend/src/views/mapaAnaliseFluxo.ts) | L114 | `handleConfirmarDevolucao` | `confirmarDevolucao` |
| [MainNavbar.vue](file:///Users/leonardo/sgc/frontend/src/components/layout/MainNavbar.vue) | L188 | `handleLogout` | `sair` |

---

### F-H2 — Props/variáveis `isXxx` / `isLoading` em inglês espalhadas no frontend

| Arquivo | Identificador | Sugestão |
|---------|---------------|----------|
| [usePerfil.ts](file:///Users/leonardo/sgc/frontend/src/composables/usePerfil.ts) L11 | `isAdmin` | `ehAdmin` |
| [ProcessoBasicFields.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoBasicFields.vue) L58 | prop `isEdit` | `modoEdicao` |
| [ProcessoFormFields.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoFormFields.vue) L59 | prop `isEdit` | `modoEdicao` |
| [LoginCredenciaisCampos.vue](file:///Users/leonardo/sgc/frontend/src/components/login/LoginCredenciaisCampos.vue) L10 | prop `isLoading` | `carregando` |
| [ProcessoCadastroModais.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoCadastroModais.vue) L51-52 | `isLoadingConfirmacao`, `isLoadingRemocao` | `carregandoConfirmacao`, `carregandoRemocao` |
| [ProcessoFormFields.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoFormFields.vue) L58 | `isLoadingUnidades` | `carregandoUnidades` |
| [ProcessoUnidadesField.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoUnidadesField.vue) L43 | `isLoading` | `carregando` |
| [useAtribuicaoTemporariaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useAtribuicaoTemporariaTela.ts) L328 | `isLoading` | `carregando` |
| [useAtribuicaoTemporariaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useAtribuicaoTemporariaTela.ts) L109 | `active` | `vigente` / `ativa` |
| [InlineEditor.vue](file:///Users/leonardo/sgc/frontend/src/components/comum/InlineEditor.vue) L107 | `isEditing` | `editando` |

---

### F-H3 — `fieldErrors` em interfaces/props públicas e `PendingToast.body`

| Arquivo | Identificador | Sugestão |
|---------|---------------|----------|
| [useMapaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useMapaTela.ts) L391 | `fieldErrors` (return) | `errosCampos` |
| [CompetenciaEdicaoModal.vue](file:///Users/leonardo/sgc/frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue) L14 | prop `fieldErrors` | `errosCampos` |
| [MapaDisponibilizacaoModal.vue](file:///Users/leonardo/sgc/frontend/src/components/mapa/modais/MapaDisponibilizacaoModal.vue) L95 | prop `fieldErrors` | `errosCampos` |
| [MapaModaisRoot.vue](file:///Users/leonardo/sgc/frontend/src/components/mapa/modais/MapaModaisRoot.vue) L23 | `fieldErrors` | `errosCampos` |
| [ProcessoFormFields.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoFormFields.vue) L56 | prop `fieldErrors` | `errosCampos` |
| [toast.ts](file:///Users/leonardo/sgc/frontend/src/stores/toast.ts) L6 | interface `PendingToast { body }` | `ToastPendente { mensagem }` |

---

### F-H4 — `isEmAndamento` em tipos compartilhados
**Arquivo:** [subprocesso-contexto.ts](file:///Users/leonardo/sgc/frontend/src/types/subprocesso-contexto.ts) L59, L79

- `isEmAndamento: boolean` → `emAndamento: boolean`

Esse tipo é consumido transversalmente. A renomeação exige busca global e atualização em [subprocessoServiceBase.ts](file:///Users/leonardo/sgc/frontend/src/services/subprocessoServiceBase.ts) L48 também.

---

## 🟡 Problemas de Severidade Média

### B-M1 — Outros métodos com > 3 parâmetros

| Arquivo | Linha | Método | Params |
|---------|-------|--------|--------|
| [SubprocessoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoService.java) | L216 | `criarMovimentacaoInicial(Subprocesso, Unidade, Usuario, String)` | 4 |
| [SubprocessoNotificacaoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoNotificacaoService.java) | L201 | `criarNotificacaoComChave(NotificacaoCommand, EmailGerado, TipoNotificacao, String)` | 4 |
| [ResponsavelUnidadeService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/organizacao/ResponsavelUnidadeService.java) | L151 | `montarAtribuicaoTemporaria(AtribuicaoTemporaria, Unidade, Usuario, CriarAtribuicaoRequest)` | 4 |
| [ResponsavelUnidadeService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/organizacao/ResponsavelUnidadeService.java) | L175 | `validarSobreposicaoPeriodo(Long, LocalDateTime, LocalDateTime, Long)` | 4 |

---

### B-M2 — Parâmetros explicitamente `ignorado` (dead-weight)

Três métodos recebem um parâmetro que nunca usam, nomeado `ignorado` para satisfazer assinatura de interface funcional:

| Arquivo | Linha | Método |
|---------|-------|--------|
| [ProcessoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/processo/ProcessoService.java) | L1426 | `obterDataLimiteObrigatoria(Processo, Long ignorado)` |
| [SubprocessoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoService.java) | L212 | `obterCodigoMapaVigenteObrigatorio(UnidadeMapa, Unidade ignorado)` |
| [SubprocessoTransicaoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoTransicaoService.java) | L628 | `obterSituacaoObrigatoria(Map, Subprocesso, String ignorado)` |

**Ação:** Avaliar se a interface funcional pode ser trocada por uma mais estreita, ou documentar explicitamente o motivo com `@SuppressWarnings("unused")`.

---

### B-M3 — Colunas do banco com nomes em inglês em `FeedbackRegistro`
**Arquivo:** [FeedbackRegistro.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/feedback/FeedbackRegistro.java)

- L25: `@Column(name = "id")` — campo Java `codigo` correto, mas coluna `"id"` viola o padrão
- L42: `@Column(name = "usuario_id")` — campo Java `usuarioCodigo` correto, coluna deveria ser `"usuario_codigo"`
- L55: `@Column(name = "status")` — deveria ser `"situacao"` para manter consistência com o restante do schema

---

### B-M4 — H2 como `implementation` em vez de `runtimeOnly`
**Arquivo:** [build.gradle.kts](file:///Users/leonardo/sgc/backend/build.gradle.kts) L53

```diff
-implementation(libs.h2)
+runtimeOnly(libs.h2)
```

O H2 está no classpath de produção. Embora o Spring só o ative sob os perfis `e2e`/`test`, nada impede que seja instanciado acidentalmente em produção.

---

### B-M5 — `Rhino` (motor JS) como dependência de produção sem contexto óbvio
**Arquivo:** [build.gradle.kts](file:///Users/leonardo/sgc/backend/build.gradle.kts) L51

`implementation(libs.rhino)` — Mozilla Rhino (interpretador JavaScript) está como dependência de produção. Verificar se está em uso real e se poderia ser `runtimeOnly` ou removido.

---

### B-M6 — 3 endpoints sem `@PreAuthorize` de perfil em `ProcessoController`
**Arquivo:** [ProcessoController.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/processo/ProcessoController.java)

- L75: `GET /api/processos/finalizados` — apenas `isAuthenticated()` herdado da classe
- L115: `GET /api/processos/ativos` — idem
- L158: `GET /api/processos/unidades-bloqueadas` — idem

Qualquer usuário autenticado (até `SERVIDOR`) pode listar todos os processos finalizados/ativos. Verificar se o controle de acesso está nas hierarquias de query ou se é necessário `@PreAuthorize("hasRole('GESTOR') or hasRole('CHEFE') or hasRole('ADMIN')")`.

---

### F-M1 — Nomes de componentes Vue em inglês

| Arquivo atual | Nome sugerido |
|---------------|--------------|
| [ProcessoBasicFields.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoBasicFields.vue) | `ProcessoCamposBasicos.vue` |
| [ProcessoDeadlineField.vue](file:///Users/leonardo/sgc/frontend/src/components/processo/ProcessoDeadlineField.vue) | `ProcessoCampoDataLimite.vue` |
| [MainNavbar.vue](file:///Users/leonardo/sgc/frontend/src/components/layout/MainNavbar.vue) | `BarraNavegacaoPrincipal.vue` |
| [PageHeader.vue](file:///Users/leonardo/sgc/frontend/src/components/layout/PageHeader.vue) | `CabecalhoPagina.vue` |
| [EmptyState.vue](file:///Users/leonardo/sgc/frontend/src/components/comum/EmptyState.vue) | `EstadoVazio.vue` |

---

### F-M2 — Composables grandes (acima de 400 linhas)

| Arquivo | Linhas |
|---------|--------|
| [useCadastroTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useCadastroTela.ts) | 494 |
| [useMapaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useMapaTela.ts) | 431 |
| [useAtribuicaoTemporariaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useAtribuicaoTemporariaTela.ts) | 413 |

Os dois primeiros exportam ~55 itens cada no `return`. Já delegam para sub-composables, mas ainda concentram muita orquestração. Candidatos à extração adicional.

---

### F-M3 — Inconsistência na organização da camada de services

- Maioria: arquivo único (`atividadeService.ts`, `cadastroService.ts`)
- `subprocessoService.ts`: split em `Base` + `Contexto` + `Mapa` com re-export
- `processo/`: subdiretório com `acoes.ts`, `leituras.ts`, `mapeadores.ts`, `tipos.ts`, `index.ts`

Não há um padrão único. Novos serviços ficam sem referência clara de qual organização seguir.

---

### F-M4 — `services/processo/types.ts` em inglês
**Arquivo:** [types.ts](file:///Users/leonardo/sgc/frontend/src/services/processo/types.ts)

Único arquivo em inglês num diretório onde todos os outros são PT-BR (`acoes.ts`, `leituras.ts`, `mapeadores.ts`). Renomear para `tipos.ts`.

---

### E-M1 — `compose.monitoring.yaml` referenciado no README mas inexistente
**Arquivo:** [README.md](file:///Users/leonardo/sgc/README.md) L40, L244

O README lista o arquivo na estrutura do projeto e descreve sua finalidade, mas ele não existe no repositório. Documento ou arquivo devem ser sincronizados.

---

### E-M2 — `JWT_SECRET` ausente no `.env.e2e`
**Arquivo:** [.env.e2e](file:///Users/leonardo/sgc/.env.e2e)

Presente em `.env.test`, ausente em `.env.e2e`. O backend provavelmente usa um default do `application.yml` para o perfil E2E. Verificar se esse default é seguro (chave fraca hard-coded = risco).

---

### E-M3 — Seletores por índice `.nth()` em colunas de tabela (fragilidade E2E)

| Arquivo | Linha | Problema |
|---------|-------|---------|
| [cdu-20.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-20.spec.ts) | L264-267 | `.nth(0)`, `.nth(1)`, `.nth(2)` em `td` sem semântica |
| [cdu-07.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-07.spec.ts) | L89 | Iteração por índice em linhas para extrair data/hora |
| [helpers-processos.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-processos.ts) | L247 | `.nth(1)` para pegar sigla de coluna |

Se a ordem das colunas mudar, esses testes falham silenciosamente (sem mensagem de erro clara). Preferir `getByRole('cell', { name: ... })` ou `data-testid` nas células.

---

### E-M4 — Seletor `.modal.show` vs `getByRole('dialog')` (inconsistência)

**Arquivos que usam `.modal.show`:** [helpers-analise.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts) L143, L177, L196 · [cdu-20.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-20.spec.ts) L76, L225

O padrão do projeto (e Playwright best practices) é `page.getByRole('dialog')`. Os arquivos listados usam seletor CSS de implementação interna do Bootstrap. Padronizar.

---

### E-M5 — `locator('main table')` sem testid em `cdu-30.spec.ts`
**Arquivo:** [cdu-30.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-30.spec.ts) — 6 ocorrências (L30, L65, L77, L114, L196, L229)

Seletor por tag HTML sem `data-testid`. Se uma segunda tabela for adicionada à `<main>`, o seletor ficará ambíguo. O projeto já usa `getByTestId('tbl-processos')` em outros specs.

---

### E-M6 — Lógica condicional defensiva em testes seriais (cdu-10)
**Arquivo:** [cdu-10.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-10.spec.ts) L176-184, L309-322

```ts
if (await botaoDisponibilizar.isDisabled()) {
    const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
    if (await checkboxSemMudancas.count() > 0) { ... }
}
```

Lógica defensiva `if (isDisabled())` sugere que o setup não garante estado determinístico antes do teste. Numa suíte `.serial`, cada teste deveria poder contar com o estado deixado pelo anterior. O uso de `if` mascara uma condição de corrida ou de estado residual.

---

### E-M7 — Funções `criarProcesso*PorFixture` duplicadas em `captura.spec.ts`
**Arquivo:** [captura.spec.ts](file:///Users/leonardo/sgc/e2e/captura.spec.ts)

8 funções locais `criarProcessoXxxPorFixture` que duplicam funcionalidade das fixtures centrais em [e2e/fixtures/index.ts](file:///Users/leonardo/sgc/e2e/fixtures/index.ts). Aumenta a superfície de manutenção e divergirá das fixtures ao longo do tempo.

---

### E-M8 — `useMapaQuery.ts` com 44,9% de cobertura de linhas (maior gap do frontend)

O composable mais complexo do frontend tem a menor cobertura. Prioridade P2 conforme `frontend-coverage-auditoria.md`. Candidato imediato a novos testes unitários.

---

## 🟢 Problemas de Baixa Severidade

### B-L1 — `DATE_FORMATTER` em inglês como constante
**Arquivos:** [SubprocessoNotificacaoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoNotificacaoService.java) L26 · [SubprocessoTransicaoService.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoTransicaoService.java) L33

Constantes em inglês. Sugestão: `FORMATO_DATA`.

---

### B-L2 — `"VALIDATION_ERROR"` como código de erro em inglês
**Arquivo:** [FeedbackController.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/feedback/FeedbackController.java) L50, L79

Os demais controllers usam `"ERRO_..."`. Padronizar para `"ERRO_VALIDACAO"`.

---

### B-L3 — `String error` / `String message` como variáveis locais em inglês
**Arquivo:** [RestExceptionHandler.java](file:///Users/leonardo/sgc/backend/src/main/java/sgc/configuracoes/RestExceptionHandler.java) L114, L159, L223

Variáveis locais (`error`, `message`) em inglês. Sugestão: `erro`, `mensagem`.

---

### B-L4 — `showSlowTests = false` hardcoded no buildscript
**Arquivo:** [build.gradle.kts](file:///Users/leonardo/sgc/backend/build.gradle.kts) L178

Flag de debug hardcoded. Externalizar para `gradle.properties` ou remover.

---

### B-L5 — `val parts` em inglês no buildscript Kotlin
**Arquivo:** [build.gradle.kts](file:///Users/leonardo/sgc/backend/build.gradle.kts) L132

Variável `parts` em inglês no script Gradle. Baixo impacto, mas inconsistente.

---

### F-L1 — BOM character desnecessário em 4 arquivos Vue

| Arquivo |
|---------|
| [ModalAceiteCadastro.vue](file:///Users/leonardo/sgc/frontend/src/components/cadastro/ModalAceiteCadastro.vue) |
| [ModalDevolucaoCadastro.vue](file:///Users/leonardo/sgc/frontend/src/components/cadastro/ModalDevolucaoCadastro.vue) |
| [CompetenciaEdicaoModal.vue](file:///Users/leonardo/sgc/frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue) |
| [CompetenciaExclusaoModal.vue](file:///Users/leonardo/sgc/frontend/src/components/mapa/modais/CompetenciaExclusaoModal.vue) |

Remover via `sed -i '' $'s/\xef\xbb\xbf//' arquivo.vue` ou configurar editor para não emitir BOM em UTF-8.

---

### F-L2 — Strings de validação hardcoded fora de constantes `TEXTOS`

| Arquivo | Linha | String |
|---------|-------|--------|
| [useAtribuicaoTemporariaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useAtribuicaoTemporariaTela.ts) | L77 | `"Informe a data de início."` |
| [useAtribuicaoTemporariaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useAtribuicaoTemporariaTela.ts) | L81 | `"Informe a data de término."` |
| [useAtribuicaoTemporariaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useAtribuicaoTemporariaTela.ts) | L85 | `"Informe a justificativa."` |
| [useMapaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useMapaTela.ts) | L153 | `"A justificativa é obrigatória para a devolução."` |
| [useMapaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useMapaTela.ts) | L156 | `"As sugestões são obrigatórias."` |
| [useMapaTela.ts](file:///Users/leonardo/sgc/frontend/src/composables/useMapaTela.ts) | L330 | `'Falha grave ao resolver subprocesso para o mapa...'` |
| [CompetenciaEdicaoModal.vue](file:///Users/leonardo/sgc/frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue) | L34, L38 | Mensagens de validação |

---

### F-L3 — Variáveis em inglês em `MainNavbar.vue`
**Arquivo:** [MainNavbar.vue](file:///Users/leonardo/sgc/frontend/src/components/layout/MainNavbar.vue) L171-177

`windowWidth` → `larguraJanela`, `isMobile` → `ehMobile`, `updateWidth` → `atualizarLargura`.

---

### F-L4 — `subprocessoCarregamento.ts` — possível dead code
**Arquivo:** [subprocessoCarregamento.ts](file:///Users/leonardo/sgc/frontend/src/views/subprocessoCarregamento.ts)

Não segue o padrão de nenhuma camada (`composable`, `view`, `service`). Verificar se está em uso ou se pode ser removido.

---

### F-L5 — `acoesBloco?: unknown[]` sem tipo preciso
**Arquivo:** [services/processo/types.ts](file:///Users/leonardo/sgc/frontend/src/services/processo/types.ts) L42

Campo tipado como `unknown[]` onde deveria ter um tipo específico.

---

### F-L6 — Rotas de administração sem arquivo dedicado
**Arquivo:** [main.routes.ts](file:///Users/leonardo/sgc/frontend/src/router/main.routes.ts)

Rotas de `/administradores`, `/notificacoes`, `/feedbacks`, `/limpeza` estão misturadas com rotas principais. Candidato a `administracao.routes.ts`.

---

### E-L1 — `waitForTimeout` sem espera semântica no crawler A11Y
**Arquivo:** [e2e/a11y/crawler.spec.ts](file:///Users/leonardo/sgc/e2e/a11y/crawler.spec.ts) L43

```ts
await page.waitForTimeout(1000); // "Wait for some content to be visible"
```
Substituir por `await expect(page.locator('[data-v-app]')).toBeVisible()` ou `waitForLoadState('networkidle')`.

---

### E-L2 — `setInterval` vazio para manter processo vivo (sem comentário)
**Arquivo:** [e2e/lifecycle.js](file:///Users/leonardo/sgc/e2e/lifecycle.js) L569-571

```js
setInterval(() => {}, 1000); // mantém o processo Node.js vivo
```
Técnica válida mas estranha. Substituir por `process.stdin.resume()` ou adicionar comentário explicativo.

---

### E-L3 — Bloco `else` inalcançável no lifecycle
**Arquivo:** [e2e/lifecycle.js](file:///Users/leonardo/sgc/e2e/lifecycle.js) L552-558

O `else` após `if (modoE2e() || modoHomologacao())` nunca executa porque `validarPerfilLifecycle()` acima garante que só um dos dois perfis é válido. O `warn` no `else` é dead code — provavelmente artefato de refatoração anterior.

---

### E-L4 — `plano-100.md` desatualizado
**Arquivo:** [plano-100.md](file:///Users/leonardo/sgc/plano-100.md)

Documenta 4 classes com lacunas de cobertura e 99,43% global. Estado atual: 1 classe (`RelatorioFacade`), 99,56% global. O arquivo pode ser arquivado ou atualizado para refletir apenas o `RelatorioFacade` pendente.

---

### E-L5 — Ausência de helper centralizado para verificação de movimentações

A tabela `tbl-movimentacoes` é verificada inline em vários specs com padrões diferentes (`.toContainText`, `.toHaveText`, `.nth(i)`). Um `helpers-movimentacoes.ts` reduziria duplicação e fragilidade.

---

### E-L6 — Axios fixado sem `^` no `package.json` do frontend
**Arquivo:** [frontend/package.json](file:///Users/leonardo/sgc/frontend/package.json) L36

`"axios": "1.16.1"` — sem `^`. Pode ser intencional ou esquecimento. Alinhar com política de versionamento do restante das dependências.

---

## O Que Está Bem (Não Tocar)

| Área | Status |
|------|--------|
| Cobertura backend | ✅ 99,56% instruções — excepcional |
| Convenções de exceção (`Erro*`) | ✅ 100% conformidade |
| REST não-padrão (sem PUT/PATCH/DELETE) | ✅ Totalmente aderente |
| Stores (Setup stores pattern) | ✅ Todas as stores corretas |
| Script `<script setup lang="ts">` | ✅ Todos os componentes |
| Options API | ✅ Zero ocorrências |
| `test.describe.serial()` onde necessário | ✅ Corretamente aplicado |
| `data-testid` como padrão dominante | ✅ Boa prática mantida |
| READMEs alinhados com o código | ✅ Arquitetura documentada corretamente |
| lifecycle.js | ✅ Robusto, cleanup, cross-platform |
| Zero TODO/FIXME/HACK | ✅ Backend e frontend |
| Zero `@Deprecated` ativo | ✅ Backend |
| Type safety (`any`) | ✅ Zero em produção |

---

## Plano de Ação Sugerido

### Sprint 1 — Renomeações de nomenclatura (frontend)
Renomear identificadores em inglês para PT-BR. São mudanças mecânicas de baixo risco — escopo bem definido. Aplicar skill `simplificacao-codigo`.

### Sprint 2 — Refatoração `ProcessoService`
Extrair família `*EmBloco` com DTO de contexto. Aproveitar o schema de testes (99,56%) como rede de segurança.

### Sprint 3 — Métodos com > 3 parâmetros
Criar DTOs command para os 4 outros métodos violadores (`criarMovimentacaoInicial`, `criarNotificacaoComChave`, `montarAtribuicaoTemporaria`, `validarSobreposicaoPeriodo`).

### Sprint 4 — Hardening E2E
Substituir `.nth()` por `getByRole('cell')`, padronizar `getByRole('dialog')`, adicionar `data-testid` nas tabelas de cdu-30.

### Sprint 5 — Cobertura `useMapaQuery.ts`
Adicionar testes unitários para o composable mais complexo do frontend (44,9% → meta 80%+).
