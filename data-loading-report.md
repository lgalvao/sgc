# Relatório: Carregamento e Cache no Frontend — Diagnóstico e Plano de Correção

## Contexto

O frontend do SGC usa Vue 3.5 + Pinia como gerenciador de estado. Existe uma estrutura de stores de sessão (`painelStore`, `processoStore`, `subprocessoStore`, `unidadeStore`, `organizacaoStore`) e composables de orquestração que encapsulam o carregamento inicial das views. Este relatório descreve as inconsistências e ineficiências encontradas e propõe correções específicas e priorizadas.

---

## Sumário Executivo

| # | Categoria | Severidade | Descrição curta |
|---|-----------|-----------|-----------------|
| P1 | Caching | Alta | `useMapas` com estado em nível de módulo (singleton não-store) |
| P2 | Caching | Alta | `useUnidadeAtual` com estado em nível de módulo |
| P3 | Loading | Alta | `ProcessoDetalheView` pisca spinner em toda re-ativação |
| P4 | Caching | Alta | `useCacheSync` (SSE) não invalida painel, processo, subprocesso |
| P5 | Loading | Média | Padrões de loading inconsistentes entre views |
| P6 | Caching | Média | `garantirArvoreElegibilidade` usa polling loop em vez de deduplicação via Promise |
| P7 | Caching | Média | `HistoricoView` sem cache + keepAlive sem `onActivated` |
| P8 | Código | Média | `WorkflowOptions.recarregarSubprocesso` é código morto |
| P9 | Código | Baixa | `painelService.ts` exporta funções paginadas que não são usadas |
| P10 | Caching | Baixa | `ProcessoCadastroView` duplica guarda de deduplicação que o store já oferece |
| P11 | Loading | Baixa | Estado duplo em `MapaView` (`mapasStore.mapaCompleto` + refs locais) |
| P12 | Caching | Baixa | Sem TTL: caches de sessão nunca expiram por tempo |

---

## Problemas Detalhados

---

### P1 — `useMapas`: estado em nível de módulo (singleton não-store) [ALTA]

**Arquivo:** `frontend/src/composables/useMapas.ts`

```ts
// Fora de qualquer função — estado de módulo global
const mapaCompleto = ref<MapaCompleto | null>(null);
const impactoMapa = ref<ImpactoMapa | null>(null);
```

**Problema:** As refs `mapaCompleto` e `impactoMapa` vivem fora da função `useMapas()`, tornando-as um singleton de módulo. Isso significa que:

1. Se o usuário navegar de `/processo/1/SIGLA/mapa` para `/processo/2/OUTRA/mapa`, o mapa antigo fica visível até que o novo seja carregado (flashback de dados stale).
2. Não existe ciclo de vida para limpeza — diferente das stores Pinia que são invalidadas no logout e troca de perfil.
3. `impactoMapa` e `carregando`/`erro` do `useAsyncAction` também são singletons, o que pode causar estados errados quando dois componentes usam o composable simultaneamente.

**Impacto observado em `MapaView.vue`:** O estado do mapa anterior pode piscar na tela durante a troca entre subprocessos diferentes no mesmo processo.

**Correção:** Migrar o estado de `useMapas` para uma Pinia store (`useMapasStore`), com invalidação explícita chamada nos mesmos pontos onde `useSubprocessoStore.invalidar()` é chamado (logout, troca de perfil, ações de workflow em `useInvalidacaoNavegacao`).

---

### P2 — `useUnidadeAtual`: estado em nível de módulo [ALTA]

**Arquivo:** `frontend/src/composables/useUnidadeAtual.ts`

```ts
const unidadeAtual = ref<Unidade | null>(null); // singleton de módulo
```

**Problema:** Mesmo padrão que P1. A unidade atual persiste entre navegações. Se o componente que deveria definir `unidadeAtual` não for montado (por keepAlive ou lazy-load), o valor anterior continua disponível para outros consumidores.

**Correção:** Promover para estado dentro de uma store Pinia existente (ex.: `usePerfilStore` já tem `unidadeSelecionada` e `unidadeSelecionadaSigla`) ou criar uma store dedicada, e limpar no logout/troca de perfil.

---

### P3 — `ProcessoDetalheView`: pisca spinner em toda re-ativação [ALTA]

**Arquivo:** `frontend/src/views/ProcessoDetalheView.vue`

```ts
async function carregarContextoCompleto() {
  clearError();
  processo.value = null;  // ← limpa dados antes da requisição
  // ...
}

onActivated(async () => {
  // Recarrega sempre ao ativar — sem cache check
  await carregarContextoCompleto();
});
```

**Problema:** A view usa `keepAlive` (rota `Processo` em `processo.routes.ts`). Ao ativar, sempre chama `carregarContextoCompleto()`, que começa por `processo.value = null`. Isso faz o template renderizar o bloco de spinner (`<div v-else class="text-center py-5">`) por toda a duração da requisição, mesmo que os dados sejam quase os mesmos. O resultado é um flash visual desnecessário toda vez que o usuário volta à tela de detalhes do processo.

A política de "sempre recarregar" tem justificativa: o contexto muda ao longo do workflow. Mas o flash é evitável — é possível manter os dados antigos visíveis enquanto a nova requisição é feita em background.

**Correção:** Remover `processo.value = null` do início de `carregarContextoCompleto()`. Dados antigos continuam visíveis durante o recarregamento. Adicionar um indicador visual sutil de "atualizando" (ex.: spinner pequeno no PageHeader ou um atributo `aria-busy`) em vez de limpar o conteúdo. Colocar o `null` somente em caso de erro.

---

### P4 — `useCacheSync` (SSE) não invalida stores de processo/subprocesso/painel [ALTA]

**Arquivo:** `frontend/src/composables/useCacheSync.ts`

```ts
source.addEventListener(EVENTO_CACHE_ATUALIZADO, () => {
    unidadeStore.invalidarCache();
    organizacaoStore.$reset();
    // painel, processo, subprocesso — não invalidados
});
```

**Problema:** O evento SSE `org-cache-refreshed` é disparado pelo backend quando dados organizacionais mudam. A invalidação atual cobre apenas `unidadeStore` e `organizacaoStore`. Quando uma ação de workflow executada por outro usuário muda o estado de um processo (ex.: um gestor aceita um cadastro em bloco), os seguintes stores não são invalidados:

- `painelStore`: a lista de processos/alertas do painel pode ficar stale
- `processoStore`: o contexto completo do processo pode refletir situação antiga
- `subprocessoStore`: contexto de edição/cadastro do subprocesso pode estar desatualizado

Isso significa que um usuário que está com a tela aberta pode ver dados inconsistentes até fazer um refresh manual.

**Correção:**
1. Avaliar se o evento `org-cache-refreshed` é o ponto correto para ampliar a invalidação, ou se é necessário um evento separado (ex.: `processo-cache-atualizado`) que o backend emita após ações de workflow.
2. Se a semântica do evento for ampliada, adicionar `painelStore.invalidar()`, `processoStore.invalidar()` e `subprocessoStore.invalidar()` no handler.
3. Garantir que a invalidação force recarregamento somente na próxima ativação da view (via `onActivated`), não imediatamente — para não interromper o usuário.

---

### P5 — Padrões de loading inconsistentes entre views [MÉDIA]

**Arquivos afetados:** todas as views

O projeto tem um componente `<CarregamentoPagina>` em `frontend/src/components/comum/CarregamentoPagina.vue` e o `TEXTOS.comum.CARREGANDO_DADOS` centralizado, mas seu uso é inconsistente:

| View | Padrão de loading |
|------|------------------|
| `CadastroView` | `<CarregamentoPagina v-if="carregandoInicial" />` ✅ |
| `MapaView` | `<CarregamentoPagina v-if="carregandoInicial" />` ✅ |
| `PainelView` | `v-if="carregandoPainel"` → `<BSpinner>` inline + `<p>` manual |
| `ProcessoDetalheView` | `v-else` em `<div v-if="processo">` → `<BSpinner>` inline |
| `SubprocessoView` | `<div v-else class="loading-container py-5">` com CSS customizado |
| `HistoricoView` | `v-if="loading"` → `<BSpinner label="Carregando...">` hardcoded |

**Consequências:**
- Mensagens de loading diferentes ("Carregando dados...", "Carregando...", "Carregando detalhes do processo...")
- Animações diferentes (CSS fadeIn em `SubprocessoView`, sem animação nos outros)
- Textos hardcoded em vez de usar `TEXTOS.comum.CARREGANDO_DADOS`

**Correção:** Padronizar todas as views para usar `<CarregamentoPagina v-if="carregandoInicial" />` com o prop `mensagem` opcional para contexto específico.

---

### P6 — `garantirArvoreElegibilidade` usa polling loop [MÉDIA]

**Arquivo:** `frontend/src/stores/unidade.ts`

```ts
if (carregando.value.has(key)) {
    while (carregando.value.has(key)) {
        await new Promise(resolve => setTimeout(resolve, 50)); // polling a cada 50ms
    }
    return cacheArvoreElegibilidade.value.get(key) || [];
}
```

**Problema:** Enquanto uma requisição está em andamento para uma chave, chamadas concorrentes entram em um loop de polling com `setTimeout(50ms)`. Isso é diferente do padrão de deduplicação via Promise usado em `processoStore` e `subprocessoStore`, que simplesmente retornam a Promise já em andamento:

```ts
// Padrão correto (processoStore):
const carregamentoExistente = carregamentosEmAndamento.get(codProcesso);
if (carregamentoExistente) return carregamentoExistente;
```

**Consequências:**
- Potencial race condition: se `carregando` for removido do Set antes de todas as instâncias do while-loop acordarem, a segunda chamada retorna `[]` em vez do resultado.
- Micro-delays desnecessários: cada chamada concorrente acorda a cada 50ms mesmo que a requisição demore mais.
- Inconsistência arquitetural: o resto do projeto usa deduplicação elegante via Map de Promises.

**Correção:** Substituir o Set `carregando` + while-loop por um `Map<string, Promise<Unidade[]>>` usando o mesmo padrão de `processoStore`.

---

### P7 — `HistoricoView` sem cache, com `keepAlive` mas sem `onActivated` [MÉDIA]

**Arquivos:** `frontend/src/views/HistoricoView.vue`, `frontend/src/router/main.routes.ts`

```ts
// main.routes.ts
{ path: "/historico", meta: { keepAlive: true } }

// HistoricoView.vue — sem onActivated
onMounted(() => {
    carregarHistorico(); // sem cache, sem deduplicação
});
```

**Problemas:**
1. A rota tem `keepAlive: true`, mas a view não tem `onActivated`. Isso significa que o histórico é carregado uma única vez no primeiro mount e nunca mais atualizado — mesmo que novos processos sejam finalizados.
2. Não há store/cache para `processosFinalizados` — os dados não sobrevivem ao unmount (se keepAlive for removido).
3. O mesmo padrão afeta `RelatoriosView` (keepAlive sem conteúdo dinâmico — menos crítico pois é apenas navegação).

**Correção:**
- Criar um `historicoStore` simples (painel análogo ao `painelStore`) que cache os processos finalizados.
- Adicionar `onActivated` que verifique a flag `carregado` antes de recarregar.
- Invalidar o store quando um processo for finalizado (em `ProcessoDetalheView.confirmarFinalizacao` já chama `invalidarCachesProcesso()` — estender para incluir `historicoStore.invalidar()`).

---

### P8 — `WorkflowOptions.recarregarSubprocesso` é código morto [MÉDIA]

**Arquivo:** `frontend/src/composables/useFluxoSubprocesso.ts`

```ts
interface WorkflowOptions {
    mensagemSucesso?: string;
    redirecionarParaPainel?: boolean;
    recarregarSubprocesso?: boolean;  // ← declarado mas nunca lido
    invalidarCaches?: { incluirPainel?: boolean; };
    redirecionarPara?: RouteLocationRaw;
}
```

A opção `recarregarSubprocesso` é passada em `reabrirCadastro()`:

```ts
async function reabrirCadastro(...) {
    return executarAcaoWorkflow(..., { recarregarSubprocesso: true }); // ← nunca executado
}
```

Mas `executarAcaoWorkflow` nunca verifica essa flag — o recarregamento é feito manualmente em `SubprocessoView.confirmarReabertura()`.

**Consequência:** O código cria a falsa impressão de que o recarregamento é automático via a opção. Futuros desenvolvedores podem adicionar outros callers de `reabrirCadastro()` esperando que o recarregamento aconteça, e ficarão surpresos quando não acontecer.

**Correção:** Remover `recarregarSubprocesso` da interface `WorkflowOptions` e do call em `reabrirCadastro`, deixando o recarregamento explicitamente documentado no comment de `SubprocessoView.confirmarReabertura()`.

---

### P9 — `painelService.ts` exporta funções paginadas não utilizadas [BAIXA]

**Arquivo:** `frontend/src/services/painelService.ts`

As funções `listarProcessos()` e `listarAlertas()` (com suporte a paginação) são exportadas mas não são usadas em nenhum lugar do frontend — o painel usa exclusivamente `obterBootstrap()`.

**Consequência:** Superfície de API morta que pode confundir quem for implementar paginação no futuro, induzindo-os a usar essas funções que podem não ser compatíveis com o estado atual do backend.

**Correção:** Remover `listarProcessos()` e `listarAlertas()` de `painelService.ts`, ou manter comentadas com `// Para uso futuro: paginação de processos no painel`.

---

### P10 — `ProcessoCadastroView` duplica guarda de deduplicação do store [BAIXA]

**Arquivo:** `frontend/src/views/ProcessoCadastroView.vue`

```ts
const ultimaBuscaUnidades = ref<{tipoProcesso: TipoProcesso; codProcesso?: number} | null>(null);

async function buscarUnidadesParaProcesso(tipoProcesso, codProcesso?) {
    if (ultimaBuscaUnidades.value?.tipoProcesso === tipoProcesso
        && ultimaBuscaUnidades.value?.codProcesso === codProcesso) {
        return; // guarda manual de deduplicação
    }
    ultimaBuscaUnidades.value = {tipoProcesso, codProcesso};
    // ...
    await unidadeStore.garantirArvoreElegibilidade(tipoProcesso, codProcesso);
}
```

O `unidadeStore.garantirArvoreElegibilidade()` já oferece cache por chave `${tipoProcesso}_${codProcesso ?? 'novo'}`. A guarda local `ultimaBuscaUnidades` é redundante e adiciona complexidade desnecessária à view.

**Correção:** Remover `ultimaBuscaUnidades` e a guarda manual. Deixar o store ser a única fonte de verdade sobre cache/deduplicação.

---

### P11 — Estado duplo em `MapaView`: store + refs locais [BAIXA]

**Arquivo:** `frontend/src/views/MapaView.vue`

```ts
// Dados em dois lugares ao mesmo tempo:
const atividades = ref<Atividade[]>([]);       // ref local
const competencias = ref<Competencia[]>([]);   // ref local
// ...e no store:
mapasStore.mapaCompleto.value = data.mapa;     // store

// Sincronizados via watch bidirecional:
watch(() => mapasStore.mapaCompleto.value, (novoMapa) => {
    atividades.value = novoMapa.atividades;
    competencias.value = novoMapa.competencias;
}, { deep: true, flush: 'sync' });
```

**Problema:** `atividades` e `competencias` são derivados de `mapasStore.mapaCompleto`, mas mantidos como refs locais separadas. Mutações nas refs locais não atualizam o store (a sincronização é unidirecional — do store para as refs). Mutações no store (via `sincronizarMapa`) atualizam as refs via watch. Isso é redundante e pode levar a estados temporariamente inconsistentes entre o watch e as mutações diretas.

**Correção:** Substituir as refs locais por `computed` derivados de `mapasStore.mapaCompleto` (somente leitura). Para operações de edição, usar o store diretamente. Isso elimina a necessidade do watch.

---

### P12 — Caches de sessão sem TTL [BAIXA]

**Afeta:** `painelStore`, `processoStore`, `subprocessoStore`, `unidadeStore`

Todos os caches dependem exclusivamente de invalidação explícita. Não há mecanismo de expiração por tempo. Um usuário que deixa a aba aberta por horas e retorna verá dados do último carregamento. Isso é aceitável para dados de workflow (onde a invalidação é precisa), mas mais crítico para o painel (processos e alertas que podem ter mudado por ações de outros usuários).

**Correção (incremental):**
- Adicionar `carregadoEm: number | null` no `painelStore`.
- Em `PainelView.onActivated`, se `dadosValidos()` mas `Date.now() - carregadoEm > 5 * 60 * 1000` (5 minutos), recarregar em background (sem limpar dados, resolvendo também P3 para o painel).
- Não aplicar TTL a `processoStore` e `subprocessoStore` — esses dados têm lógica de invalidação precisa via ações de workflow.

---

## Mapa de Chamadas Relevantes (Backend)

Para referência das correções de P4 (SSE e invalidação):

| Frontend call | Endpoint backend | Invalida |
|--------------|-----------------|---------|
| `painelService.obterBootstrap()` | `GET /painel/bootstrap` | — |
| `processoService.buscarContextoCompleto()` | `GET /processos/{id}/contexto-completo` | — |
| `subprocessoService.buscarContextoEdicao()` | endpoint de contexto de edição | — |
| `processoService.finalizarProcesso()` | `POST /processos/{id}/finalizar` | invalida processo + painel |
| `processoService.executarAcaoEmBloco()` | `POST /processos/{id}/acao-em-bloco` | invalida processo + subprocesso + painel (condicionalmente) |
| `cadastroService.*` (disponibilizar, aceitar, homologar) | vários `POST /subprocessos/{id}/...` | invalida subprocesso + painel |
| `subprocessoService.*` (adicionar/remover competência) | vários `POST /subprocessos/{id}/...` | invalida store mapa (P1) |

Nota: `processoService` define três endpoints para buscar dados de processo:
- `obterProcessoPorCodigo()` → `GET /processos/{id}` — não usado em views principais
- `obterDetalhesProcesso()` → `GET /processos/{id}/detalhes` — usado apenas em `ProcessoCadastroView` para edição
- `buscarContextoCompleto()` → `GET /processos/{id}/contexto-completo` — usado pelo `processoStore`

O endpoint `/detalhes` retorna dados diferentes de `/contexto-completo` (sem ações de bloco, sem situação de subprocessos). A duplicidade não é um problema de caching, mas deve ser documentada para evitar confusão.

---

## Plano de Correção Priorizado

### Fase 1 — Impacto imediato (P1, P2, P3, P4)

1. **[P1] Migrar `useMapas` para Pinia store**
   - Criar `useMapasStore` em `frontend/src/stores/mapas.ts`
   - Mover `mapaCompleto`, `impactoMapa` para a store
   - Adicionar `invalidar()` e chamar em `useInvalidacaoNavegacao` e no logout
   - Atualizar `MapaView`, `CadastroView` e qualquer outro consumidor

2. **[P2] Migrar `useUnidadeAtual` para estado em store**
   - Adicionar `unidadeAtualDetalhes` ao `usePerfilStore` ou criar store separada
   - Atualizar consumidores

3. **[P3] Remover flash de spinner em `ProcessoDetalheView`**
   - Remover `processo.value = null` do início de `carregarContextoCompleto()`
   - Mover o `null` para o bloco `catch`
   - Adicionar indicador visual sutil de "atualizando" (atributo `aria-busy` ou spinner de atualização no header)

4. **[P4] Ampliar invalidação no `useCacheSync`**
   - Avaliar se evento SSE existente tem semântica suficiente, ou criar evento específico de workflow no backend
   - Adicionar invalidação de `painelStore` e `processoStore` no handler
   - Garantir que a invalidação não force recarregamento imediato — apenas marca como stale

### Fase 2 — Qualidade e consistência (P5, P6, P7, P8)

5. **[P5] Padronizar componente de loading**
   - Substituir spinners inline em `PainelView`, `ProcessoDetalheView`, `SubprocessoView`, `HistoricoView` por `<CarregamentoPagina>`
   - Usar `TEXTOS.comum.CARREGANDO_DADOS` em todos os casos

6. **[P6] Corrigir deduplicação em `garantirArvoreElegibilidade`**
   - Substituir `Set<string> carregando` + while-loop por `Map<string, Promise<Unidade[]>>` seguindo o padrão de `processoStore`

7. **[P7] Adicionar cache e `onActivated` em `HistoricoView`**
   - Criar `historicoStore` simples com `processos`, `carregado`, `invalidar()`
   - Adicionar `onActivated` com verificação de cache
   - Chamar `historicoStore.invalidar()` em `ProcessoDetalheView.confirmarFinalizacao()`

8. **[P8] Remover código morto `recarregarSubprocesso`**
   - Remover da interface `WorkflowOptions` e do call em `reabrirCadastro()`

### Fase 3 — Limpeza e refinamento (P9, P10, P11, P12)

9. **[P9] Remover ou documentar funções não usadas em `painelService.ts`**
   - Remover `listarProcessos()` e `listarAlertas()` se não houver roadmap para paginação
   - Ou adicionar comentário explícito de "para uso futuro"

10. **[P10] Remover guarda redundante em `ProcessoCadastroView`**
    - Remover `ultimaBuscaUnidades` e a guarda manual de deduplicação

11. **[P11] Eliminar estado duplo em `MapaView`**
    - Converter `atividades` e `competencias` de refs locais para `computed` derivados do store de mapas (após P1)

12. **[P12] Adicionar TTL simples ao `painelStore`**
    - Adicionar `carregadoEm: number | null`
    - Recarregar em background se `> 5 min` em `onActivated` do `PainelView`

---

## Notas Adicionais

- O mecanismo de deduplicação de requisições concorrentes via `Map<key, Promise>` (usado em `processoStore` e `subprocessoStore`) é o padrão correto e deve ser o modelo para todos os stores que fazem fetch. O `unidadeStore` (P6) é o único desvio.
- A arquitetura de invalidação explícita via `useInvalidacaoNavegacao` é sólida. O risco está nos pontos onde a invalidação é esquecida (P4 — ações remotas) ou é feita de forma incompleta (sub-stores não invalidados após ações específicas).
- O sistema de SSE em `useCacheSync` é um bom mecanismo para invalidação cross-user. O problema é seu escopo limitado atual (P4). Considerar um protocolo de evento mais expressivo (ex.: payload com `{ tipo: "processo" | "org", codProcesso?: number }`) para invalidação granular.
