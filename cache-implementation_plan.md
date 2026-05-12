# Consolidação da Estratégia de Cache — Frontend SGC

## Problema

A auditoria identificou que o sistema de cache é **funcionalmente correto** após as correções anteriores, mas tem **3 problemas estruturais** que tornam a estratégia imprecisa e inconsistente:

---

## Problemas estruturais

### 🔴 Problema 1 — Regressão: carga inicial invalida o painel desnecessariamente

**Causa:** A correção do Bug 1 (sessão anterior) adicionou `painelStore.invalidar()` dentro de `processarRespostaLocal`. Porém, `sincronizarEstadoInicialContexto` — que é chamada na **carga inicial** — reutiliza `processarRespostaLocal` internamente.

**Consequência:** Toda vez que o usuário abre `CadastroView` pela primeira vez (mount), o `painelStore` é invalidado sem nenhuma mutação ter ocorrido. Ao voltar ao painel, os dados são recarregados desnecessariamente do backend.

**Fluxo atual (incorreto):**
```
onMounted → carregarContextoInicial()
  → sincronizarEstadoInicialContexto()
    → processarRespostaLocal()      ← chamada de LEITURA
      → painelStore.invalidar()     ← invalida sem motivo
```

**Fluxo correto:**
```
onMounted → carregarContextoInicial()
  → sincronizarEstadoInicialContexto()
    → aplicarEstadoContexto()       ← lógica comum sem side-effects de cache

handleImportAtividades() → processarRespostaLocal()
  → aplicarEstadoContexto()         ← mesma lógica
  → painelStore.invalidar()         ← só em mutações
```

---

### 🟡 Problema 2 — Duas rotas de invalidação paralelas (bypass do ponto central)

`useInvalidacaoNavegacao` existe como ponto central de invalidação documentado, mas os composables de cadastro contornam-no diretamente:

| Ponto de invalidação | Via |
|---|---|
| `useFluxoSubprocesso` | `useInvalidacaoNavegacao` ✅ |
| `ProcessoCadastroView` | `useInvalidacaoNavegacao` ✅ |
| `processoDetalheAcoes` | `useInvalidacaoNavegacao` ✅ |
| `MapaView` | `useInvalidacaoNavegacao` ✅ |
| `useCadastroOrquestracao` | **direto** `painelStore.invalidar()` ❌ |
| `subprocessoAcoesAdministrativas` | **injetado** como `invalidarPainel` ❌ |

Os dois últimos foram adicionados nas correções desta sessão e burlaram o padrão estabelecido. Isso torna a estratégia inconsistente e dificulta rastrear quem invalida o quê.

---

### 🟡 Problema 3 — Parity gap: `useMapaOrquestracao` ≠ `useCadastroOrquestracao`

`useCadastroOrquestracao.sincronizarEstadoInicialContexto` delega para `processarRespostaLocal` (padrão complexo). `useMapaOrquestracao.sincronizarEstadoInicialContexto` chama `mapasStore.definirMapaCompleto` diretamente (padrão simples). As duas orquestrações de carga inicial usam abordagens diferentes sem justificativa documentada.

---

## Solução proposta

### Mudança 1 — Extrair `aplicarEstadoContexto` em `useCadastroOrquestracao.ts`

Separar a **sincronização de estado local** (que pode acontecer em leitura ou mutação) da **invalidação de cache** (que só deve acontecer em mutações).

```ts
// Privado: aplica o payload sem side-effects de cache
function aplicarEstadoContexto(response: RespostaLocalCadastro) {
    atividades.value = response.atividadesAtualizadas;
    subprocessoStore.atualizarStatusLocal({
        ...response.subprocesso,
        permissoes: response.permissoes
    });
    mapasStore.invalidar(response.subprocesso.codigo);
    subprocessoStore.invalidarContextoEdicao(response.subprocesso.codigo);
}

// Público: usado após mutações — aplica estado E invalida caches de navegação
function processarRespostaLocal(response: RespostaLocalCadastro) {
    aplicarEstadoContexto(response);
    invalidarCachesSubprocesso({ incluirPainel: true });
}

// Usado na carga inicial — SEM invalidar painel
function sincronizarEstadoInicialContexto(data: ContextoCadastroAtividadesSubprocesso) {
    aplicarEstadoContexto({ ... });
    atividadesSnapshotInicial.value = ...;
    unidade.value = data.unidade;
    codMapa.value = data.mapa.codigo;
}
```

### Mudança 2 — Canalizar via `useInvalidacaoNavegacao`

Substituir o acesso direto ao `painelStore` nos composables pela função centralizada, restaurando o ponto único de controle.

- `useCadastroOrquestracao`: usar `invalidarCachesSubprocesso({ incluirPainel: true })` (via `useInvalidacaoNavegacao`)
- `subprocessoAcoesAdministrativas`: remover `invalidarPainel` como parâmetro injetado; usar `useInvalidacaoNavegacao` internamente, consistente com os demais composables que não aceitam a store como dependência

### Mudança 3 — Documentar a estratégia em `useInvalidacaoNavegacao.ts`

Expandir o comentário existente para documentar:
- Quando usar `invalidarCachesProcesso` vs `invalidarCachesSubprocesso`
- Que `incluirPainel` deve ser `true` sempre que a mutação afeta dados exibidos em `ProcessoResumo`
- Que mutações de estado local (atividades, competências) sempre devem invalidar o painel

---

## Arquivos modificados

### [MODIFY] [useCadastroOrquestracao.ts](file:///c:/sgc/frontend/src/composables/useCadastroOrquestracao.ts)
- Remover import direto de `usePainelStore`
- Adicionar `useInvalidacaoNavegacao`
- Extrair `aplicarEstadoContexto` (função interna sem side-effects de cache)
- `processarRespostaLocal` passa a chamar `aplicarEstadoContexto` + `invalidarCachesSubprocesso({ incluirPainel: true })`
- `sincronizarEstadoInicialContexto` passa a chamar `aplicarEstadoContexto` diretamente (sem invalidar painel)

### [MODIFY] [subprocessoAcoesAdministrativas.ts](file:///c:/sgc/frontend/src/views/subprocessoAcoesAdministrativas.ts)
- Remover `invalidarPainel` do type `DependenciasSubprocessoAcoesAdministrativas`
- Adicionar `useInvalidacaoNavegacao` internamente
- `confirmarAlteracaoDataLimite` chama `invalidarCachesSubprocesso({ incluirPainel: true })` diretamente

### [MODIFY] [SubprocessoView.vue](file:///c:/sgc/frontend/src/views/SubprocessoView.vue)
- Remover `usePainelStore` import e instância
- Remover `invalidarPainel: () => painelStore.invalidar()` da chamada de `useSubprocessoAcoesAdministrativas`

### [MODIFY] [useInvalidacaoNavegacao.ts](file:///c:/sgc/frontend/src/composables/useInvalidacaoNavegacao.ts)
- Expandir documentação do JSDoc para cobrir os casos de uso de cada função

### [MODIFY] [useCadastroOrquestracao.spec.ts](file:///c:/sgc/frontend/src/composables/__tests__/useCadastroOrquestracao.spec.ts)
- Atualizar mock: trocar `usePainelStore` por `useInvalidacaoNavegacao`
- Verificar que `processarRespostaLocal` chama `invalidarCachesSubprocesso({ incluirPainel: true })`
- Verificar que `sincronizarEstadoInicialContexto` **não** invalida o painel

---

## Verificação

```
npm run test:unit   → 1284/1284 devem continuar passando
npm run typecheck   → sem erros TypeScript
```

> [!IMPORTANT]
> O Problema 1 (regressão) introduz uma recarga desnecessária do painel toda vez que o usuário abre o cadastro. Dependendo da frequência de uso, isso é ruído constante de rede. Recomendo aprovação imediata desta correção.
