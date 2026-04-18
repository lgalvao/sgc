# Plano de racionalização de desempenho

## Papel deste documento

Este arquivo nao deve competir com o [backlog-racionalizacao.md](/Users/leonardo/sgc/backlog-racionalizacao.md).

O backlog e a lista executavel de itens.
Este plano passa a registrar apenas:

- o racional da iniciativa
- o que ja foi suficientemente esclarecido
- o que ainda falta atacar
- os criterios para priorizacao das proximas rodadas

## Objetivo

Racionalizar o sistema para entregar a mesma experiencia funcional com menos trabalho total, reduzindo:

- chamadas redundantes no frontend
- round-trips desnecessarios entre frontend e backend
- recomposicao de contexto sem ganho funcional
- custo interno de montagem de telas e transicoes

O alvo principal nao e latencia isolada de um endpoint. O alvo principal e diminuir trabalho repetido ao longo da jornada real.

## Status consolidado

O ciclo inicial de investigacao ja produziu evidencias suficientes para encerrar a fase de descoberta generica.

### O que ja ficou claro

- o painel era a principal fonte de repeticao e ja foi tratado no backlog como frente encerrada
- a sequencia de leitura de subprocesso continua reaparecendo nas jornadas mais representativas, mas a montagem de contexto começou a ser separada por intenção na rodada 10
- a rodada 11 iniciou a quebra física de `SubprocessoConsultaService` para reduzir concentração de responsabilidades
- `processos/{codigo}/contexto-completo` ainda merece revisao de escopo e consumo
- a tela `MapaVisualizacaoView` reduziu duplicação de fluxo (sugestoes/historico), porém ainda concentra decisões condicionais de ação
- a abertura de modal na visualização de mapa nao deve depender de round-trip síncrono para preservar fluidez de interação
- a cadeia de workflow ainda concentra custo sincronico em validacoes, notificacoes e envio de e-mail
- `diagnostico-organizacional` e `arvore-com-elegibilidade` deixaram de ser suspeitas abertas amplas e agora sao itens localizados, com chamadores conhecidos
- o monitoramento com `SGC_MONITORAMENTO=on` ja e confiavel o bastante para comparacoes dirigidas

### O que nao precisa permanecer neste plano

- ranking extensivo de endpoints
- inventario detalhado de chamadas por tela
- lista de tarefas por item
- sequencia operacional por rodada

Esses pontos pertencem ao [backlog-racionalizacao.md](/Users/leonardo/sgc/backlog-racionalizacao.md).

## Frentes que ainda faltam

## Frente 1: processo

### Pergunta central

`contexto-completo` e `detalhes` ainda carregam mais do que a navegacao realmente precisa?

### O que falta esclarecer

- quais telas realmente precisam de `contexto-completo`
- quanto de sobreposicao existe entre `contexto-completo` e `detalhes`
- quais blocos poderiam ser carregados sob demanda ou reaproveitados localmente

### Sinal de conclusao

Essa frente estara madura quando houver uma sequencia canônica de leitura de processo com menos recomposicao e contrato mais previsivel.

## Frente 2: subprocesso

### Pergunta central

Por que a mesma jornada continua reabrindo `subprocessos/buscar` e `subprocessos/{codigo}/contexto-edicao` dentro do mesmo fluxo?

### O que falta esclarecer

- onde a navegacao reentra no mesmo contexto
- qual parte da repeticao e frontend e qual parte e contrato fragmentado
- se o ganho vem mais de cache, reuso de estado ou consolidacao de endpoint

### Sinal de conclusao

Essa frente estara madura quando a abertura e a retomada de subprocesso deixarem de recompor o mesmo contexto varias vezes na mesma jornada.

## Frente 3: workflows lentos

### Pergunta central

Quais custos ainda estao no caminho critico das transicoes e deveriam sair do fluxo sincronico?

### O que falta esclarecer

- peso relativo de validacao, persistencia, notificacao e envio de e-mail
- quais etapas sao obrigatorias no request principal
- quais efeitos colaterais podem ser desacoplados sem quebrar regra de negocio nem observabilidade

### Sinal de conclusao

Essa frente estara madura quando os outliers de workflow deixarem de depender de notificacao ou processamento secundario dentro da mesma requisicao.

## Atualizacao da rodada 10

A rodada 10 confirmou o inicio da Onda 2 prevista no plano:

- backend: montagem de contexto de consulta de subprocesso com vínculos de unidade isolados em bloco dedicado;
- frontend: desduplicação do fluxo de sugestões/histórico em `MapaVisualizacaoView`.

Com isso, o foco sai de "descobrir onde está a duplicação" e volta para "eliminar o restante da recomposição e da decisão espalhada".

## Atualizacao da rodada 11

A rodada 11 consolidou dois ajustes de direção para a Onda 2:

- frontend: modal de sugestões passa a abrir de forma imediata, com carregamento assíncrono em segundo plano;
- frontend: leitura de sugestões deixa de usar fallback para não mascarar erro de integração;
- backend: criação de serviço dedicado de contexto para reduzir acoplamento interno de `SubprocessoConsultaService`.

Com isso, fica explícito que simplificação deve reduzir tamanho e responsabilidade de hotspots, sem aliases desnecessários nem camadas de compatibilidade.

## Como priorizar daqui para frente

As proximas rodadas devem seguir esta ordem de decisao:

1. atacar fluxos ainda abertos no backlog, nao reabrir frentes ja resolvidas
2. priorizar jornada real antes de suite ampla
3. preferir eliminar round-trip ou recomposicao antes de micro-otimizar query
4. consolidar contrato apenas quando a tela realmente depender de chamadas estaveis em conjunto
5. medir antes e depois com o mesmo cenario monitorado

## Relacao com o backlog

O [backlog-racionalizacao.md](/Users/leonardo/sgc/backlog-racionalizacao.md) e a fonte de verdade para execucao.

Este plano deve ser atualizado apenas quando mudar pelo menos um destes pontos:

- o mapa das frentes ainda abertas
- a ordem de prioridade entre frentes
- o criterio de sucesso da iniciativa
- a interpretacao consolidada dos achados

Se a mudanca for apenas novo item, novo passo tecnico ou novo achado local, o lugar correto e o backlog.

## Criterio de sucesso

A iniciativa sera considerada bem-sucedida quando:

- as jornadas prioritarias fizerem menos chamadas
- processo e subprocesso pararem de recompor contexto sem necessidade
- workflows criticos perderem custo sincronico evitavel
- o sistema ficar mais previsivel para evolucao de frontend e backend
