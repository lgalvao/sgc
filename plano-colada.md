# Plano de Simplificação Estrutural do Frontend (SGC)

## Estado atual

Score arquitetural: **11** (bom — abaixo da meta de 25 ✅).
`forcar→recarregar` concluído. `fachadaPura` zerado. Testes todos passando (1364 unitários).

Medir com: `node etc/scripts/sgc.js frontend arquitetura auditar`

---

## Métricas atuais

| Métrica                         | Atual | Meta     |
| ------------------------------- | ----- | -------- |
| Score total                     | 11    | < 25 ✅  |
| `superficieAmpla` (arquivos)    | 5     | < 6 ✅   |
| `palavraForcar` (ocorrências)   | 0     | 0 ✅     |
| `fachadasPuras`                 | 0     | 0 ✅     |
| `composablesMinusculos`         | 2     | < 4 ✅   |
| `familiasPulverizadas`          | 4     | < 2      |
| demais sinais                   | 0     | manter 0 |

---

## O que resta

### `familiasPulverizadas` (4 famílias, meta < 2) — baixa prioridade

As 4 famílias (Fluxo, Mapa, Processo, Cadastro) têm 4–6 membros cada.
O score já está bem abaixo da meta — esta métrica **não está pressionando o score total**.
As famílias têm responsabilidades bem separadas e testes unitários dedicados por membro.
Consolidação agressiva quebraria seams de teste sem ganho de legibilidade real.

**Ação recomendada para próxima rodada:**
Avaliar apenas se `useUnidadeAtual` (15L, 2 consumidores) e `useRelatorioAndamentoTela` (17L)
merecem anotação `@sgc-auditoria ignorar: arquivoMinusculo` ou absorção no consumidor.
Impacto máximo: **-2 pts** (score iria para 9).

### `superficieAmpla` em `stores/subprocesso/index.ts` (score 9)

A store exporta símbolos demais. Vale investigar se há exports não consumidos externamente
que podem ser tornados privados. Essa é a mudança de maior impacto restante.

---

## Validação por mudança

```bash
npm run lint && npm run typecheck && npm run test:unit
node etc/scripts/sgc.js frontend arquitetura auditar
```
