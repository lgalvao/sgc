# Análise: try/catch e código defensivo no frontend

## Contexto: o que já existe e funciona bem

O frontend tem uma camada de interceptação centralizada em **`axios-setup.ts`** que:
- Normaliza todos os erros HTTP via `normalizarErro()`
- Redireciona para `/login` em caso de 401
- Cancela requisições durante transição de sessão
- Loga erros globais

Além disso, existe **`useAsyncAction`** — um composable criado exatamente para encapsular o padrão `try/catch/loading/erro`. Ele existe, mas **não é usado na maioria dos lugares**.

---

## Categorias de try/catch encontradas

### CATEGORIA A — try/catch legítimos e insubstituíveis ✅

Esses devem ser mantidos como estão:

| Arquivo | Função | Justificativa |
|---|---|---|
| `utils/date/parsing.ts:10` | `analisarStringData` | Isola falha de `parse()` de formato desconhecido — comportamento esperado |
| `utils/date/formatting.ts:27` | `formatarDataBR` | Isola exceção do `format()` de date-fns |
| `composables/useWebStorage.ts:16` | `lerValor` | `JSON.parse()` pode lançar com valor corrompido em storage |
| `stores/subprocesso/orquestrador.ts:29,55` | `garantirContexto*` | Erros são convertidos em `null` + estado de erro — padrão coerente |
| `views/FeedbacksAdminView.vue:274` | `formatarMetadados` | `JSON.parse()` de dados externos |
| `views/FeedbacksAdminView.vue:304` | `compactarRota` | `JSON.parse()` de query string externa |
| `views/NotificacoesAdminView.vue:251` | `carregarUrlLeitorEmailTestes` | Falha silenciosa intencional — URL opcional de dev |

---

### CATEGORIA B — try/catch duplicados: a view reinventa o que o composable já faz ⚠️

**`useAsyncAction`** já encapsula `loading/erro/try/catch` mas as views **não o usam** — e então reimplementam o mesmo padrão à mão.

**Exemplo repetitivo (aparece em ~15 funções):**
```ts
// Padrão recorrente — visto em RelatorioAndamentoView, HistoricoView, LimpezaProcessosView, etc.
async function carregar() {
  carregando.value = true;
  try {
    dados.value = await algumService.buscar();
  } catch (error) {
    notify("Erro ao carregar", "danger");
  } finally {
    carregando.value = false;
  }
}
```

**Locais concretos:**
- `RelatorioAndamentoView` — `carregarProcessos` (L81)
- `RelatorioMapasView` — 3 funções similares
- `HistoricoView` — `carregar` (L98)
- `LimpezaProcessosView` — `carregar` (L116)
- `AdministradoresView` — 2 funções (L210, L233)
- `AtribuicaoTemporariaView` — 3 funções (L313, L368, L401)
- `UnidadeView` — 2 funções (L167, L185)

**Proposta:** usar `useAsyncAction` ou um helper local limpo.

---

### CATEGORIA C — Padrão estranho: catch vazio + verificação de `lastError` depois ⚠️

Em `RelatorioAndamentoView`, há um padrão que faz `catch` silencioso e depois lê `lastError` da store:

```ts
try {
  await relatoriosStore.buscarRelatorioAndamento(codProcessoSelecionado.value);
} catch {
  // O erro já é normalizado na store; a view só precisa encerrar o estado de carregamento.
} finally {
  carregando.value = false;
}

if (relatoriosStore.lastError) {
  notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
}
```

**Problema:** a store já propagou o erro via `throw`; a view captura silenciosamente mas continua dependendo do estado da store para decidir o que fazer. São dois canais de comunicação de erro simultâneos (exception + estado reativo) — confuso e frágil. O `carregando.value` do relatório **também** deveria estar na store, não duplicado na view.

---

### CATEGORIA D — Defensividade excessiva no `formatarMetadados` (FeedbacksAdminView) ⚠️

Três funções auxiliares (`compactarRota`, `compactarAcesso`, `compactarResolucao`) fazem checagens `typeof === 'string'` em campos de um JSON que a própria view acabou de parsear. Se o backend enviou o JSON, os campos têm tipos conhecidos — a defensividade é de um JSON não tipado genérico sendo tratado como se pudesse ser qualquer coisa.

**Impacto:** baixo (não é código quente), mas contribui para verbosidade desnecessária.

---

### CATEGORIA E — dois composables duplicados para o mesmo fim ⚠️

Existem **dois** composables de tratamento de erro:

- **`useAsyncAction`** — gerencia `loading + erro + execução`
- **`useErrorHandler`** — gerencia `lastError + execução com callback`

`useErrorHandler` é mais complexo (requer importar `ErroNormalizado`), menos usado e sobrepõe funcionalidade com `useAsyncAction`. Verificar se tem consumidores reais além de testes.

---

### CATEGORIA F — Catch genérico que suprime rastreabilidade ⚠️

Em `cadastroAnaliseFluxo.ts`, `cadastroDisponibilizacao.ts` e similares, os catches fazem `notify(mensagemErro)` sem `logger.error()`. Se o backend devolver um erro inesperado (500, timeout, etc.), o desenvolvedor não terá nenhum registro.

Padrão ruim:
```ts
} catch (error) {
  notify("Erro ao salvar", "danger"); // sem logger
}
```

O interceptor do Axios já loga erros globais — então isso é redundante para erros HTTP. Mas para erros locais (ex: `new Error("Tipo não definido")`), o logger é necessário.

---

## Mapa de prioridade para simplificação

| Prioridade | Alvo | Ação |
|---|---|---|
| 🔴 Alta | **Categoria C** — catch silencioso + `lastError` | Escolher um canal: ou propagação via exception, ou estado reativo. Remover duplicação. |
| 🟠 Média | **Categoria B** — padrão loading/catch duplicado | Extrair helper local por view ou usar `useAsyncAction` |
| 🟠 Média | **Categoria E** — `useErrorHandler` duplicado | Auditar consumidores; eliminar se órfão |
| 🟡 Baixa | **Categoria F** — catch sem logger | Adicionar `logger.error` nos catches que já não logam |
| 🟢 Manter | **Categoria A** | Sem alteração |

---

## Cortes concretos propostos (por ordem de segurança)

### Corte 1 — RelatorioAndamentoView: eliminar duplo canal de erro

**Antes:**
```ts
try {
  carregando.value = true;
  await relatoriosStore.buscarRelatorioAndamento(id);
} catch {
  // silencioso
} finally {
  carregando.value = false;
}
if (relatoriosStore.lastError) {
  notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
}
```

**Depois:**
```ts
carregando.value = true;
await relatoriosStore.buscarRelatorioAndamento(id)
  .catch(() => notify(TEXTOS.relatorios.ERRO_BUSCA, "danger"))
  .finally(() => { carregando.value = false; });
```
Ou, se `lastError` for necessário para a store, mantê-lo lá e usar um `watch` na view — não misturar os dois canais.

---

### Corte 2 — Auditar e remover `useErrorHandler` se órfão

Verificar se `useErrorHandler` tem consumidor real em produção. Se não, apagar.

---

### Corte 3 — Padronizar `carregar()` com helper por view

Para views admin que só fazem "lista + loading + erro inline" (`FeedbacksAdminView`, `NotificacoesAdminView`, etc.), um helper local simples elimina o padrão repetido:

```ts
// helper local (não precisa ser composable global)
async function carregarComErro<T>(fn: () => Promise<T>, aoErrar: (e: unknown) => void): Promise<T | undefined> {
  carregando.value = true;
  try { return await fn(); }
  catch (e) { aoErrar(e); }
  finally { carregando.value = false; }
}
```

---

## O que NÃO simplificar

- `analisarData` e `analisarStringData` — defensividade necessária para tipo de entrada genuinamente variável
- `useWebStorage` — `JSON.parse` pode lançar com dados corrompidos em produção
- `orquestrador.ts` — converte exceções em `null` com semântica clara
- Catches que fazem redirect pós-401 (já no Axios, não duplicar)
- `ProcessoCadastroView.handleApiErrors` — lógica de mapeamento de erros de campo é necessária e específica

---

## Próximo passo natural

**Corte 1** (RelatorioAndamentoView) é o mais seguro e demonstra o princípio: um único canal de erro por operação. Validar com `npm run typecheck` + `npm run test:unit`.
