# Plano de Migração do Modelo de Dados

Objetivo: alinhar o protótipo ao modelo relacional descrito em [`modelo-dados.md`](modelo-dados.md:1).

Visão resumida

- Estratégia atual: padronizar o formato dos mocks para o formato do frontend (camelCase, id, datas ISO/local quando apropriado) e centralizar regras (datas, situações, snapshots). Onde conveniente, usar mappers/adapters para isolamento.
- Migrar de forma incremental, validando com testes automatizados a cada passo.

Mudanças já aplicadas (estado atual)

- Inventário dos mocks gerado com o script [`scripts/inventario-mocks.js`](scripts/inventario-mocks.js:1). Relatório salvo em `scripts/relatorios/inventario-mocks.json`.
- Script de transformação (dry-run) criado: [`scripts/transformar-mocks-para-formato-frontend.js`](scripts/transformar-mocks-para-formato-frontend.js:1). O dry-run indicou que os mocks já estão compatíveis (sem mudanças necessárias).
- Endurecimento do parsing de datas:
  - Atualizado: [`src/utils/index.ts`](src/utils/index.ts:1) — `parseDate` trata strings "YYYY-MM-DD" como data local; `formatDateTimeBR` agora retorna "Não informado" para entradas nulas/undefined.
  - Testes adicionados: [`src/utils/__tests__/parseDate.spec.ts`](src/utils/__tests__/parseDate.spec.ts:1).
  - Resultado: testes unitários executados; as falhas iniciais foram resolvidas.
- Centralização parcial de situações:
  - Constantes em: [`src/constants/situacoes.ts`](src/constants/situacoes.ts:1).
  - Substituições aplicadas em: [`src/stores/subprocessos.ts`](src/stores/subprocessos.ts:1).
  - Substituições parciais em: [`src/stores/processos.ts`](src/stores/processos.ts:1) — restante precisa ser completado.
- Plano salvo e atualizado neste arquivo: [`plano-migracao-modelo.md`](plano-migracao-modelo.md:1).

Relatórios e artefatos gerados

- `scripts/relatorios/inventario-mocks.json` — inventário dos arquivos em `src/mocks/`.
- `scripts/relatorios/transformacao-mocks.json` — resultado do dry-run do transformador de mocks.
- Testes unitários adicionados/atualizados em `src/utils/__tests__/`.

Situação atual — resumo rápido

- Inventário: concluído.
- Transformação de mocks (dry-run): nenhum ajuste necessário.
- parseDate / formatDateTimeBR: corrigidos e testados.
- Substituição de literais por constantes: iniciada (subprocessos feita; processos parcialmente).
- Lint/typecheck: sem erros críticos; aparecem warnings em scripts auxiliares (não críticos).
- Testes unitários: passaram após correções (runner local).

Próximos passos (execução automática feita por este agente)

1) Finalizar substituição de literais por constantes
   - Arquivo alvo primário: [`src/stores/processos.ts`](src/stores/processos.ts:1)
   - Ação: substituir todas as comparações com strings de situação por referências a `SITUACOES_SUBPROCESSO.*` e `SITUACOES_MAPA.*`.
   - Verificações: `npm run lint && npm run typecheck` e `npm run test:unit` executados após alteração.

2) Refatorar stores restantes para consumir mappers ou mocks padronizados
   - Stores: `src/stores/servidores.ts`, `src/stores/unidades.ts`, `src/stores/mapas.ts`, `src/stores/atividades.ts`, `src/stores/analises.ts`, `src/stores/alertas.ts`.
   - Ação: optar por usar mappers (em `src/mappers/`) ou consumir mocks já padronizados. Neste protótipo, a preferência é consumir mocks diretos quando já estiverem corretos.

3) Criar validação de contrato para mocks
   - Script: `scripts/validar-mocks.js` (ou TypeScript com Zod)
   - Função: validar presença de chaves essenciais e formatos de data; falhar em CI se inválido.

4) Documentar convenções
   - Atualizar `guias/guia-refatoracao.md` com convenções de `id` vs `codigo`, formatos de data, lista de situações (fonte: `reqs/situacoes.md`).

5) Rodar suíte completa de testes E2E e corrigir regressões
   - Comando: `npx playwright test`
   - Se E2E apresentar flakiness, priorizar estabilidade dos seletores (`data-testid`) e dos helpers semânticos.

Comandos de trabalho (para cada passo)

- Lint + typecheck: `npm run lint && npm run typecheck`
- Testes unitários: `npm run test:unit`
- Testes E2E: `npx playwright test`
- Inventário de mocks: `node scripts/inventario-mocks.js`
- Dry-run transformador: `node scripts/transformar-mocks-para-formato-frontend.js`
- Aplicar transformação (se desejado): `node scripts/transformar-mocks-para-formato-frontend.js --apply`

Critérios de aceitação (alinhamento com `modelo-dados.md`)

- Stores consomem dados com as chaves esperadas pelos tipos em [`src/types/tipos.ts`](src/types/tipos.ts:1).
- Todas as datas carregadas dos mocks são convertidas para objetos Date onde necessário e são testadas.
- Todas as situações são referenciadas via constantes em [`src/constants/situacoes.ts`](src/constants/situacoes.ts:1).
- Scripts de validação detectam divergências de mocks e são executáveis localmente.
- Nenhuma alteração de comportamento visível na UI (testes E2E verdes ou com falhas conhecidas documentadas).

Atualização do checklist (situação atual)

- [x] Criar inventário dos mocks (`scripts/inventario-mocks.js`)
- [x] Criar transformador dry-run (`scripts/transformar-mocks-para-formato-frontend.js`) e executar dry-run
- [x] Harden `parseDate` e `formatDateTimeBR` (`src/utils/index.ts`)
- [x] Adicionar testes para parseDate (`src/utils/__tests__/parseDate.spec.ts`)
- [x] Executar testes unitários e resolver falhas iniciais
- [x] Iniciar centralização de situações (`src/constants/situacoes.ts`) e aplicar em `src/stores/subprocessos.ts`
- [ ] Completar substituições em `src/stores/processos.ts`
- [ ] Refatorar demais stores para usar mappers ou mocks padronizados
- [ ] Criar `scripts/validar-mocks.js` (Zod/JSON Schema)
- [ ] Atualizar documentação (`guias/`)
- [ ] Executar suíte E2E e resolver regressões
