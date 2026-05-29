# Plano de Simplificação Estrutural do Frontend (SGC)

## Estado atual

Score arquitetural: **27** (medido em 2026-05-29).
`forcar→recarregar` concluído. `fachadaPura` zerado. Testes todos passando (1364 unitários).

Medir com: `node etc/scripts/sgc.js frontend arquitetura auditar`

---

## Métricas atuais

| Métrica                         | Atual | Meta     |
| ------------------------------- | ----- | -------- |
| Score total                     | 27    | < 25     |
| `superficieAmpla` (arquivos)    | 5     | < 6 ✅   |
| `palavraForcar` (ocorrências)   | 0     | 0 ✅     |
| `fachadasPuras`                 | 0     | 0 ✅     |
| `composablesMinusculos`         | 10    | < 4      |
| `familiasPulverizadas`          | 4     | < 2      |
| demais sinais                   | 0     | manter 0 |

---

## O que falta fazer

### Prioridade 1 — Bug do audit: `App.vue` score=8 sem sinal visível

`App.vue` é classificado como `outro` (fora de views/components/stores/composables).
`calcularScoreArquivo` adiciona +8 por `chamadasStore >= 8` para qualquer camada não-hub,
mas `obterSinaisAtivos` só exibe o sinal `acoplamentoStoreAlto` para `view/component`.
Resultado: 8 pontos invisíveis no score.

**Ação A** (semântica): adicionar `"frontend/src/App.vue"` à lista `HUBS_CENTRAIS` em
`arquitetura-lib.js` — App.vue É o hub raiz; chamar stores ali é correto por design.
**Ação B** (consistência): ajustar `obterSinaisAtivos` (linha 780) para exibir
`acoplamentoStoreAlto` para qualquer camada onde a penalidade é aplicada, não só `view/component`.

Impacto estimado: **-8 pontos** → score cai para ~19.

---

### Prioridade 2 — Composables minúsculos (10 arquivos, meta < 4)

Análise dos 10 composables < 30 linhas identificou dois grupos:

**Grupo A — padrão Pinia Colada (pequenos por design):**
`useProcessoQuery`, `useHistoricoQuery`, `usePainelQuery`, `useDiagnosticoOrganizacionalQuery`,
`useFeedbacksAdminQuery`, `useUnidadesQuery`.
Cada um exporta chave de query + hook de invalidação — é o padrão correto do Pinia Colada.
**Ação**: anotar cada um com `// @sgc-auditoria ignorar: arquivoMinusculo | padrão Pinia Colada`.
Impacto: **-6 pontos** (cada arquivo = 1 ponto).

**Grupo B — wrappers desnecessários:**
`useLocalStorage.ts` (5L) e `useSessionStorage.ts` (5L) são wrappers triviais de `useWebStorage`.
O único consumidor é `stores/perfil.ts`.
**Ação**: deletar os dois arquivos; em `perfil.ts` chamar `useWebStorage(localStorage, ...)` e
`useWebStorage(sessionStorage, ...)` diretamente.
Impacto: **-2 pontos**.

**Restante:**
`useFluxoSubprocesso.ts` (15L) — já tem annotation `ignorar: fachadaPura`, mas falta
`arquivoMinusculo`. Atualizar annotation.
Impacto: **-1 ponto**.

---

### Prioridade 3 — Famílias pulverizadas (4 famílias, meta < 2)

As 4 famílias (Fluxo, Mapa, Processo, Cadastro) têm 4–6 membros cada.
Consolidações agressivas foram descartadas nesta rodada: os satélites de Cadastro e Mapa
têm testes unitários dedicados que seriam perdidos no merge.

**Ação conservadora**: verificar se `familiasPulverizadas` cai naturalmente após remover
composables minúsculos do Grupo A e B acima (cada família perde membros minúsculos).
Reavaliar o sinal após as prioridades 1 e 2.

---

## Validação por mudança

```bash
npm run lint && npm run typecheck && npm run test:unit
node etc/scripts/sgc.js frontend arquitetura auditar
```
