# Plano de Política de Cache e Invalidação no Frontend

## Estado atual (atualizado em 2026-05-08)

### Concluído

| Item | O que foi feito |
| --- | --- |
| `mapas` store | contrato correto: `invalidar()` preserva snapshot, `resetar()` limpa tudo; testes |
| `processo` store | contrato correto (já estava próximo); testes |
| `subprocesso` store | contrato correto (já estava próximo); testes |
| `useCacheSync` | `org-cache-refreshed` invalida apenas org/unidade/painel; testes |
| `useInvalidacaoNavegacao` | testes |
| `useMapaOrquestracao` | parâmetro `limpar = false` explícito; testes |
| `useCadastroOrquestracao` | testes |
| `ProcessoDetalheView` | erro de recarga não apaga snapshot; testes de cache |
| `AtribuicaoTemporariaView` | overlap `onMounted` + `onActivated` cortado; testes |
| `UnidadeView` | overlap `watch(immediate)` + `onActivated` cortado; testes |
| `UnidadesView` | testes |
| `subprocessoCarregamento` | testes de keepAlive (não recarrega quando válido, recarrega quando stale) |

### Pendente

#### 1. Contrato uniforme dos stores

| Store | Problema | Correção |
| --- | --- | --- |
| `painel` | sem `resetar()` | adicionar `resetar()`: zera processos, alertas, carregadoEm, codigosMarcadosComoLidos |
| `historico` | sem `resetar()`; `garantirDados()` sem dedupe | adicionar `resetar()` + dedupe com promessa em andamento |
| `organizacao` | `invalidar()` apaga diagnostico (colapsada com reset); `$reset()` delega a `invalidar()` | `invalidar()` preserva diagnostico, só marca `carregado = false`; `resetar()` limpa tudo; remover `$reset()` |
| `unidade` | método `invalidarCache()` (nome inconsistente); sem `resetar()` | renomear para `invalidar()` em todos os pontos de chamada (`useCacheSync.ts`, `perfil.ts`, testes); adicionar `resetar()` |

Cada item exige atualização dos testes correspondentes. Sem aliases ou camadas de compatibilidade — renomear e atualizar todos os pontos de uma vez.

#### 2. Invalidação fora de escopo

| Arquivo | Linha | Problema | Correção |
| --- | --- | --- | --- |
| `stores/perfil.ts` | 54–61 | `invalidarDadosDaSessao()` chama `resetar()` em processo/subprocesso/mapas mas `invalidar()` em painel e organizacao; na troca de perfil os dados do painel pertencem ao perfil anterior e devem ser descartados por completo | trocar `painelStore.invalidar()` e `organizacaoStore.invalidar()` por `resetar()` nos dois |

#### 3. Overlap de carga

| Arquivo | Linha | Problema | Correção |
| --- | --- | --- | --- |
| `views/ProcessoCadastroView.vue` | 259–275 | `carregarProcessoParaEdicao` seta `tipo.value = processo.tipo`, o que dispara `watch(tipo)` que chama `buscarUnidadesParaProcesso` de novo; mas a própria função já chamou `buscarUnidadesParaProcesso` na linha anterior | suspender o watch durante a carga inicial com uma flag `inicializando` |
| `views/PainelView.vue` | 161 | flag nomeada `montadoUmaVez` diverge do padrão `carregamentoInicialConcluido` adotado nas demais views críticas | renomear para `carregamentoInicialConcluido` (ref reativo) para consistência |

## Objetivo

Tornar a política de cache, invalidação e carregamento de contexto do frontend consistente, previsível e uniforme,
reduzindo bugs em views `keepAlive`, recargas parciais, bootstrap após refresh completo da página e efeitos colaterais
de eventos assíncronos como SSE.

Este plano cobre:

- stores Pinia usados como cache de sessão;
- orquestrações de carregamento inicial;
- views reativadas por `KeepAlive`;
- invalidações disparadas por workflow;
- invalidações disparadas por eventos externos;
- regras de renderização quando o dado está `stale`;
- deduplicação e anti-overlap de recargas.

## Problema atual

Hoje coexistem estratégias diferentes e incompatíveis:

- stores cujo `invalidar()` apenas marca o dado como inválido;
- stores cujo `invalidar()` apaga imediatamente o snapshot atual;
- bootstraps que recompõem o contexto pela rota;
- bootstraps que dependem demais de estado já residente em memória;
- views `keepAlive` com recarga explícita em `onActivated`;
- views `keepAlive` que dependem apenas de `onMounted`;
- telas com `onMounted` e `onActivated` sem guarda suficiente contra carga duplicada;
- watchers que podem competir com bootstrap ou reidratação;
- invalidação por domínio;
- invalidação ampla por conveniência.

Os sintomas esperados desse cenário são:

- contexto desaparecendo após um evento assíncrono;
- erro de "não encontrado" antes de uma nova busca real;
- mapa ou subprocesso sumindo ao voltar para uma view parada;
- chamadas repetidas ao mesmo endpoint no primeiro ciclo de vida da tela;
- recarga em cascata após ação de workflow;
- dependência excessiva de refresh manual da página para voltar ao estado coerente.

## Política alvo

### 1. Semântica uniforme dos stores

Cada store com papel de cache deve expor comportamento uniforme:

- `dadosValidos(...)`: informa se o snapshot atual pode ser reutilizado;
- `invalidar(...)`: preserva o último snapshot, marca o dado como `stale` e limpa dedupe em andamento quando fizer
  sentido;
- `resetar(...)`: limpa completamente snapshot, índices auxiliares e estados transitórios;
- `garantir...(...)`: reutiliza snapshot válido, recarrega snapshot inválido e deduplica requisições concorrentes.

### 2. Regra para eventos externos

Eventos externos nunca devem apagar contexto crítico ativo.

Exemplos:

- evento organizacional invalida caches organizacionais;
- não deve apagar mapa, subprocesso ou processo sem prova explícita de dependência.

### 3. Regra para views `keepAlive`

Toda view crítica reativada por `KeepAlive` deve seguir o mesmo padrão:

- `onMounted`: bootstrap inicial;
- `onActivated`: se o contexto estiver inválido, reidratar;
- durante reidratação, não interpretar ausência momentânea como "não encontrado".

### 4. Regra para carregamento

Toda tela crítica deve deixar explícitos quatro caminhos distintos:

- bootstrap inicial;
- reidratação por `keepAlive`;
- refresh completo da página reconstruindo pela rota;
- recarga forçada após ação de workflow.

Esses caminhos não devem competir entre si nem disparar chamadas redundantes ao backend.

### 5. Regra de renderização

Renderização deve tolerar o estado:

- snapshot anterior disponível;
- dado inválido;
- recarregamento em andamento.

Nessa situação, a UI deve:

- exibir loading incremental ou manter snapshot anterior de forma controlada;
- só mostrar "não encontrado" depois de falha real de busca.

## Frente de refatoração

Esta rodada deixa de tratar apenas "cache" e passa a tratar "política de contexto e carregamento".

Na prática, isso significa alinhar conjuntamente:

- semântica dos stores;
- bootstrap e reidratação das telas;
- dedupe de requisições;
- prevenção de overlap entre `onMounted`, `onActivated` e `watch`;
- invalidação por domínio;
- tratamento de erro sem apagar snapshot indevidamente.

## Classificação dos domínios

### Domínios críticos de trabalho

Devem preservar snapshot ao invalidar:

- `processo`
- `subprocesso`
- `mapas`

### Domínios administrativos e derivados

Podem ser mais agressivos, mas com decisão explícita:

- `organizacao`
- `unidade`
- `painel`
- `historico`

Mesmo nesses casos, a regra deve ser documentada. Não deve haver limpeza implícita por hábito.

## Matriz de eventos e invalidação

### Evento SSE `org-cache-refreshed`

Escopo esperado:

- `organizacao`
- `unidade`
- `painel` somente se houver dependência funcional comprovada

Não deve invalidar:

- `processo`
- `subprocesso`
- `mapas`

### Ações de workflow de processo

Escopo esperado:

- `processo`
- `subprocesso`
- `painel` quando a ação alterar listas ou alertas exibidos ali

### Ações de workflow de mapa

Escopo esperado:

- `mapas`
- `subprocesso` quando o backend devolver contexto alterado
- `processo` apenas se a ação alterar contexto agregado do processo

### Troca de perfil ou logout

Escopo esperado:

- `resetar()` amplo em todos os stores de sessão

## Inventário inicial

### Stores

| Store | Estado atual de `invalidar()` | Snapshot preservado? | `resetar()` explícito? | Observação |
| --- | --- | --- | --- | --- |
| `processo` | marca `contextoInvalido = true` e limpa dedupe | sim | sim | comportamento já próximo da política alvo |
| `subprocesso` | marca contexto de edição/cadastro como inválido e limpa dedupe | sim | sim | comportamento já próximo da política alvo |
| `mapas` | marca o mapa como `stale`, preserva snapshot principal e invalida impacto derivado | sim, para o mapa | sim | já alinhado para o mapa; impacto segue política própria |
| `painel` | marca como não carregado e preserva listas/alertas | sim | não | comportamento intermediário; falta separar melhor invalidação de reset |
| `historico` | marca como não carregado e preserva lista | sim | não | comportamento intermediário; simples e previsível |
| `unidade` | limpa todos os caches imediatamente | não | não | aceitável só se o domínio for tratado como puramente administrativo |
| `organizacao` | limpa diagnóstico e estado de carregamento imediatamente | não | usa `invalidar()` como reset | semântica colapsada entre invalidação e limpeza total |

### Leitura do inventário

- `processo` e `subprocesso` já seguem a direção correta: manter snapshot e marcar `stale`.
- `mapas` é hoje o store mais inconsistente, porque o domínio é crítico e a invalidação apaga o contexto ativo.
- dentro de `mapas`, o snapshot principal e o impacto do mapa não precisam da mesma política: mapa é contexto crítico; impacto é dado derivado e deve ser recalculado sob demanda, não cacheado como contexto.
- `painel` e `historico` preservam snapshot, mas ainda não distinguem formalmente "invalidar" de "resetar".
- `unidade` e `organizacao` estão mais próximos de caches administrativos simples, mas ainda sem semântica uniforme.

### Views `keepAlive` críticas

| View | `onMounted` | `onActivated` | Situação atual |
| --- | --- | --- | --- |
| `ProcessoDetalheView` | sim | sim | alinhada com a política alvo |
| `SubprocessoView` | indireto via `subprocessoCarregamento` | indireto via `subprocessoCarregamento` | alinhada em parte; depende da robustez do store |
| `PainelView` | sim | sim | alinhada com cache simples por TTL |
| `HistoricoView` | sim | sim | alinhada com cache simples |
| `UnidadesView` | sim | sim | precisa só revisão de coerência com os stores organizacionais |
| `UnidadeView` | parcial | sim | precisa revisão fina de coerência com invalidação organizacional |
| `MapaView` | sim | não | a rota atual não usa `keepAlive`; o risco principal aqui é invalidação destrutiva do store, não reativação |

### Conclusões do inventário

1. O problema estrutural mais claro está na combinação `mapas` + `MapaView`.
2. O store `mapas` já preserva snapshot crítico ao invalidar; o foco agora é consolidar essa semântica em testes de contrato.
3. A `MapaView` não está em `keepAlive`, então o sintoma de "mapa sumiu" aponta mais para invalidação destrutiva do store do que para ausência de `onActivated`.
4. `processo` e `subprocesso` servem como referência local de contrato mais seguro.
5. `ProcessoDetalheView` tinha um desvio sutil de política: em erro de recarga ela limpava o snapshot mesmo durante refresh em background. Esse tipo de divergência precisa ser coberto por teste de view.
6. `CadastroView` e `MapaView` dependem mais do bootstrap por rota do que do `keepAlive`. Para essas telas, a robustez contra refresh completo da página precisa ser validada nas orquestrações (`useCadastroOrquestracao` e `useMapaOrquestracao`).
7. `AtribuicaoTemporariaView` tinha `onMounted` e `onActivated` sem guarda suficiente. Em rota `keepAlive`, isso abre espaço para recarga duplicada logo no primeiro ciclo de vida.

## Hotspots de overlap e duplicidade

### Hotspots já confirmados

- `AtribuicaoTemporariaView`
  - havia `onMounted(carregarDados)` e `onActivated(carregarDados)` sem guarda do primeiro carregamento;
- `ProcessoDetalheView`
  - havia divergência entre comentário e comportamento real em erro de recarga;
- `useCacheSync`
  - já invalidava domínios demais antes do corte do SSE organizacional.
- `useFluxoSubprocesso`
  - o caminho de homologação sem retorno ao painel ainda passava `invalidarCaches: {}`, o que acionava invalidação implícita de `subprocesso` por efeito colateral do helper.
- `useImpactoMapaModal`
  - permitia cliques repetidos dispararem buscas concorrentes do mesmo impacto enquanto o carregamento anterior ainda estava em andamento.

### Hotspots que merecem revisão imediata

- `UnidadeView`
  - combina `watch(..., {immediate: true})` com `onActivated`, o que pode gerar recarga redundante quando a view volta do cache;
- `UnidadesView`
  - ainda recarrega diretamente no `onMounted` e no `onActivated`, sem store dedicado para dedupe;
- `CadastroView`
  - depende de bootstrap por rota e vários `watch`, então merece revisão para garantir que mutações locais não disparem recargas fora de hora;
- `MapaView`
  - depende de bootstrap por rota e de mutações locais no mesmo contexto, então precisa revisão focada em overlap de refresh pós-workflow.
- `useMapaOrquestracao`
  - o bootstrap por `processo + unidade` não explicitava o parâmetro `limpar = false`, abrindo espaço para semântica divergente em relação aos outros resolvedores de contexto.

## Fases de execução

## Fase 1: Inventário

Mapear store por store:

- chave de cache;
- snapshot atual;
- validade;
- semântica de `invalidar()`;
- existência de `resetar()`;
- dependências de views;
- gatilhos de invalidação.

Stores alvo:

- `painel`
- `processo`
- `subprocesso`
- `mapas`
- `unidade`
- `organizacao`
- `historico`

Saída esperada:

- tabela "estado atual" x "estado desejado".

## Fase 2: Contrato dos stores

Padronizar primeiro os stores críticos:

1. `processo`
2. `subprocesso`
3. `mapas`

Objetivos:

- `invalidar()` não apagar snapshot crítico;
- `resetar()` assumir limpeza real;
- `dadosValidos(...)` ter semântica equivalente entre os três.

## Fase 3: Reidratação e bootstrap das views

Padronizar:

1. `SubprocessoView`
2. `MapaView`
3. `ProcessoView`

Depois expandir para:

4. `PainelView`
5. `HistoricoView`
6. `UnidadeView`, se aplicável

Objetivos:

- toda view crítica reidrata em `onActivated`;
- nenhuma view depende apenas de `onMounted`;
- refresh completo da página recompõe o contexto pela rota sem depender do estado anterior da SPA;
- "não encontrado" só aparece após tentativa real de recarga.

## Fase 4: Invalidação por domínio

Revisar:

- `useCacheSync`
- `useInvalidacaoNavegacao`
- helpers de workflow
- composables que hoje invalidam mais do que precisam

Objetivos:

- remover invalidação transversal desnecessária;
- manter escopo mínimo por evento.

## Fase 5: Anti-overlap de carregamento

Revisar:

- telas com `onMounted` + `onActivated`;
- telas com `watch(..., {immediate: true})` somado a reidratação;
- fluxos que recarregam store e tela ao mesmo tempo após workflow.

Objetivos:

- eliminar carga duplicada no primeiro ciclo de vida;
- deduplicar recargas concorrentes da mesma tela;
- evitar refresh em cascata quando o store já foi reidratado pela ação anterior.

## Ordem recomendada

1. inventariar stores e views;
2. fixar contrato em `processo`, `subprocesso` e `mapas`;
3. corrigir reidratação e bootstrap de `SubprocessoView`, `CadastroView` e `MapaView`;
4. revisar SSE e helpers de invalidação;
5. cortar hotspots de overlap e duplicidade;
6. alinhar `painel`, `historico`, `unidade` e `organizacao`.

## Trilhas iniciais de implementação

### Trilha 1: estabilizar `mapas` e `MapaView`

Escopo:

- `frontend/src/stores/mapas.ts`
- `frontend/src/composables/useMapaOrquestracao.ts`
- `frontend/src/views/MapaView.vue`

Objetivos:

- fazer `invalidar()` preservar o último snapshot do mapa ativo;
- explicitar diferença entre invalidação e limpeza total;
- impedir que a UI perca o mapa ativo por invalidação destrutiva;
- revisar apenas o necessário na `MapaView` para refletir o novo contrato do store, sem presumir reativação por `keepAlive`.

Justificativa:

- é o ponto com sintoma mais grave relatado;
- concentra a combinação mais perigosa entre store e view;
- a rota do mapa não usa `keepAlive`, então esta trilha permite isolar o problema real antes de abrir uma frente de reidratação que talvez nem seja necessária.

### Trilha 2: consolidar `subprocesso` como referência

Escopo:

- `frontend/src/stores/subprocesso/index.ts`
- `frontend/src/views/subprocessoCarregamento.ts`
- `frontend/src/views/SubprocessoView.vue`

Objetivos:

- revisar se o contrato atual cobre todos os estados `stale + keepAlive`;
- reforçar testes para usar `subprocesso` como baseline de comportamento esperado;
- só mexer no código se aparecer inconsistência real depois da trilha 1.

### Trilha 3: cortar overlap de carregamento

Escopo inicial:

- `frontend/src/views/AtribuicaoTemporariaView.vue`
- `frontend/src/views/UnidadeView.vue`
- `frontend/src/views/UnidadesView.vue`

Objetivos:

- remover duplicidade entre `onMounted`, `onActivated` e `watch`;
- deixar claro quando a tela usa cache local e quando força recarga;
- evitar spinner e round-trip redundantes na volta para a tela.

Arquivos prioritários para a primeira rodada:

- `frontend/src/composables/useCacheSync.ts`
- `frontend/src/composables/useInvalidacaoNavegacao.ts`
- `frontend/src/stores/processo.ts`
- `frontend/src/stores/subprocesso/index.ts`
- `frontend/src/stores/mapas.ts`
- `frontend/src/views/subprocessoCarregamento.ts`
- `frontend/src/composables/useMapaOrquestracao.ts`
- `frontend/src/views/SubprocessoView.vue`
- `frontend/src/views/MapaView.vue`

## Critérios de aceite

Considerar a política consistente quando:

- `invalidar()` e `resetar()` tiverem semântica uniforme;
- nenhuma view crítica `keepAlive` depender apenas de `onMounted`;
- nenhuma tela crítica disparar a mesma carga duas vezes no primeiro ciclo de vida;
- eventos organizacionais não apagarem contexto crítico;
- contexto crítico inválido acionar reidratação em vez de desaparecimento abrupto;
- "não encontrado" só surgir após busca real malsucedida.

## Cobertura mínima esperada

### Stores

- `invalidar()` preserva snapshot quando o domínio for crítico;
- `resetar()` limpa tudo;
- `dadosValidos(...)` muda de forma previsível.

### Composables e views

- reativação de view `keepAlive` com dado inválido dispara recarga;
- bootstrap por rota recompõe o contexto após refresh completo da página;
- `onMounted`, `onActivated` e `watch` não geram overlap indevido;
- a UI não cai para "não encontrado" enquanto reidrata.

### SSE

- `org-cache-refreshed` invalida apenas domínios previstos.

## Estratégia de testes

Para este tema, a cobertura precisa ser contratual e em camadas:

### 1. Disparador de invalidação

Testes de SSE e helpers de navegação devem provar:

- qual evento foi recebido;
- quais stores foram invalidados;
- quais stores não foram tocados.

Esses testes não devem parar em `spy`; quando possível, devem também verificar o estado resultante do store.

### 2. Contrato do store

Cada store crítico deve ter testes explícitos para:

- `invalidar()` preserva ou não o snapshot, conforme o domínio;
- `dadosValidos(...)` muda para `false` quando esperado;
- `garantir...(...)` volta a buscar depois da invalidação;
- `resetar()` limpa o estado completo.

### 3. Reidratação da view

Views `keepAlive` críticas devem provar:

- não recarregam quando o store continua válido;
- recarregam quando o store está `stale`;
- não caem em estado de "não encontrado" antes de uma falha real.

### 4. Anti-overlap

Telas com múltiplos gatilhos de carga devem provar:

- nenhuma recarga duplicada no primeiro ciclo de vida;
- retorno à tela com dados válidos não dispara round-trip desnecessário;
- retorno à tela com dados inválidos dispara exatamente uma recarga.

### Priorização atual da suíte

1. `useCacheSync`
2. stores `processo`, `subprocesso`, `mapas`, `painel`
3. `subprocessoCarregamento`
4. `ProcessoDetalheView` e `SubprocessoView`
5. orquestrações `useCadastroOrquestracao` e `useMapaOrquestracao`
6. hotspots de overlap (`AtribuicaoTemporariaView`, `UnidadeView`, `UnidadesView`)

## Observação final

O objetivo não é remover cache do frontend.

O objetivo é fazer com que cache, invalidação e reidratação obedeçam a uma política única, pequena e auditável. A
causa dos bugs atuais parece ser menos "existência de cache" e mais "ausência de contrato uniforme".
