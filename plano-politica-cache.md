# Plano de Política de Cache e Invalidação no Frontend

## Objetivo

Tornar a política de cache e invalidação do frontend consistente, previsível e uniforme, reduzindo bugs em views
`keepAlive`, recargas parciais e efeitos colaterais de eventos assíncronos como SSE.

Este plano cobre:

- stores Pinia usados como cache de sessão;
- views reativadas por `KeepAlive`;
- invalidações disparadas por workflow;
- invalidações disparadas por eventos externos;
- regras de renderização quando o dado está `stale`.

## Problema atual

Hoje coexistem estratégias diferentes e incompatíveis:

- stores cujo `invalidar()` apenas marca o dado como inválido;
- stores cujo `invalidar()` apaga imediatamente o snapshot atual;
- views `keepAlive` com recarga explícita em `onActivated`;
- views `keepAlive` que dependem apenas de `onMounted`;
- invalidação por domínio;
- invalidação ampla por conveniência.

Os sintomas esperados desse cenário são:

- contexto desaparecendo após um evento assíncrono;
- erro de "não encontrado" antes de uma nova busca real;
- mapa ou subprocesso sumindo ao voltar para uma view parada;
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

### 4. Regra de renderização

Renderização deve tolerar o estado:

- snapshot anterior disponível;
- dado inválido;
- recarregamento em andamento.

Nessa situação, a UI deve:

- exibir loading incremental ou manter snapshot anterior de forma controlada;
- só mostrar "não encontrado" depois de falha real de busca.

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

## Fase 3: Reidratação das views `keepAlive`

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

## Ordem recomendada

1. inventariar stores e views;
2. fixar contrato em `processo`, `subprocesso` e `mapas`;
3. corrigir reidratação de `SubprocessoView` e `MapaView`;
4. revisar SSE e helpers de invalidação;
5. alinhar `painel`, `historico`, `unidade` e `organizacao`.

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

### Priorização atual da suíte

1. `useCacheSync`
2. stores `processo`, `subprocesso`, `mapas`, `painel`
3. `subprocessoCarregamento`
4. `ProcessoDetalheView` e `SubprocessoView`

## Observação final

O objetivo não é remover cache do frontend.

O objetivo é fazer com que cache, invalidação e reidratação obedeçam a uma política única, pequena e auditável. A
causa dos bugs atuais parece ser menos "existência de cache" e mais "ausência de contrato uniforme".
