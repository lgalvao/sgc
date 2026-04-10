# Backlog inicial de racionalização

## Contexto

Este backlog deriva de:

- [plano-racionalizacao.md](/Users/leonardo/sgc/plano-racionalizacao.md)
- execução monitorada parcial da suíte E2E registrada em [monitoramento-e2e.txt](/Users/leonardo/sgc/e2e/monitoramento-e2e.txt)

Importante:

- a coleta considerada aqui é a execução integral da suíte E2E registrada em `e2e/monitoramento-e2e.txt`
- parte dos logins repetidos é consequência do desenho da suíte E2E, não necessariamente do produto
- as prioridades abaixo valorizam repetição por fluxo e impacto em tela, não apenas picos isolados

## Hotspots observados

### Hotspot 1: painel

#### Evidência

- `GET /api/painel/processos?page=0&size=10&unidade=1`
- `GET /api/painel/alertas?page=0&size=10&unidade=1&sort=dataHora,desc`

Esses endpoints apareceram como os mais frequentes da amostra monitorada e se repetem em praticamente toda entrada de fluxo autenticado.

#### Sinais

- o primeiro acesso ao painel foi significativamente mais caro que os seguintes
- `painel/alertas` tende a ser mais caro que `painel/processos`
- há recargas do painel em transições de fluxo onde talvez uma atualização parcial bastasse

#### Hipóteses

- falta de bootstrap único do painel
- tela recarregando processos e alertas mesmo quando apenas um bloco muda
- `alertas` pode estar fazendo mais trabalho que o necessário
- existe oportunidade de cache curto ou reuso local por unidade/perfil

## Hotspot 2: contexto de processo

#### Evidência

- `GET /api/processos/{codigo}/contexto-completo`
- `GET /api/processos/{codigo}/detalhes`

Essas rotas aparecem repetidamente em múltiplos fluxos de processo.

#### Sinais

- entram várias vezes na mesma jornada
- convivem com outras chamadas complementares para subprocessos e árvore de unidades
- podem estar entregando mais informação que a tela usa imediatamente

#### Hipóteses

- contexto excessivamente abrangente
- reentrada em tela disparando recarga integral
- oportunidade de segmentação por aba/estado ou cache local

## Hotspot 3: contexto de subprocesso

#### Evidência

- `GET /api/subprocessos/{codigo}/contexto-edicao`
- `GET /api/subprocessos/buscar?...`
- `GET /api/subprocessos/contexto-cadastro-atividades/buscar?...`

Essas rotas aparecem em cascata e com repetição em várias jornadas.

#### Sinais

- buscas e contextos são refeitos várias vezes na mesma navegação
- navegação por cards/modais parece recompor contexto já conhecido

#### Hipóteses

- falta de cache ou reuso no frontend
- design de tela pedindo dados fragmentados demais
- possível consolidação de contratos orientados à edição/visualização

## Hotspot 4: ações de workflow específicas

#### Evidência

- `POST /api/subprocessos/{codigo}/cadastro/disponibilizar`
- `POST /api/processos/{codigo}/iniciar`
- `POST /api/processos/{codigo}/acao-em-bloco`
- `POST /api/processos/{codigo}/finalizar`

#### Sinais

- `cadastro/disponibilizar` apareceu como outlier relevante
- outras ações de workflow ficaram em geral rápidas, mas devem ser revisitadas com tracing interno

#### Hipóteses

- regra de negócio concentrada
- muitas atualizações encadeadas
- notificações, validações ou recomputações excessivas

## Hipóteses prioritárias

## Hipótese A: telas recarregam mais do que precisam

### Descrição

Parte do custo percebido pode vir de recomposição repetida de estado no frontend.

### Sinais a confirmar

- mesma chamada disparada mais de uma vez na mesma rota sem mudança material
- recarga total após ação pontual
- watcher e mount acionando a mesma carga

### Como validar

- revisar `views`, `stores` e `composables` do painel
- mapear triggers de fetch
- identificar duplicatas consecutivas no log monitorado

## Hipótese B: `painel/alertas` faz trabalho mais pesado que `painel/processos`

### Descrição

`alertas` parece consistentemente mais caro e muito frequente.

### Sinais a confirmar

- joins extras
- ordenação custosa
- pós-processamento na camada de serviço
- regras de permissão ou enriquecimento por item

### Como validar

- abrir controller/service/repo relacionados
- medir cadeia interna com `MonitoramentoAspect`
- revisar eventual N+1 ou montagem excessiva de DTO

## Hipótese C: `contexto-completo` e `contexto-edicao` entregam payload demais

### Descrição

Os contextos parecem ser chamados várias vezes e podem estar retornando mais do que a tela usa imediatamente.

### Sinais a confirmar

- payload grande
- blocos da resposta não usados na primeira renderização
- telas que pedem contexto integral e depois ainda fazem requests adicionais

### Como validar

- inspecionar contratos e consumo no frontend
- verificar quais campos da resposta são usados por etapa da tela
- avaliar segmentação do endpoint

## Hipótese D: existem oportunidades de consolidação de API

### Descrição

Algumas telas parecem depender de múltiplos endpoints estáveis para montar um mesmo estado visual.

### Como validar

- mapear requests por tela
- identificar grupos recorrentes disparados juntos
- avaliar endpoint de bootstrap por caso de uso

## Backlog executável

## Bloco 1: painel

### Item 1.1

Mapear toda a cadeia de carregamento do painel no frontend.

### Objetivo

Descobrir quem dispara:

- `painel/processos`
- `painel/alertas`
- `diagnostico-organizacional`

### Saída esperada

- lista de componentes/composables/stores envolvidos
- gatilhos de recarga
- duplicações observadas

### Item 1.2

Mapear cadeia interna do backend para:

- `GET /api/painel/processos`
- `GET /api/painel/alertas`

### Objetivo

Descobrir onde o tempo é gasto dentro do backend.

### Saída esperada

- controller/facade/service/repo envolvidos
- top métodos chamados
- hipótese de N+1 ou enriquecimento excessivo

### Item 1.3

Avaliar se o painel pode ter endpoint de bootstrap.

### Objetivo

Reduzir round-trips de entrada de tela.

### Saída esperada

- proposta de contrato
- custo/benefício de adoção

## Bloco 2: processo

### Item 2.1

Inventariar consumo de `processo/{codigo}/contexto-completo` e `processo/{codigo}/detalhes`.

### Objetivo

Entender se há sobreposição funcional entre os dois.

### Saída esperada

- tabela com telas consumidoras
- dados usados por tela
- pontos de redundância

### Item 2.2

Rastrear sequência de requests ao abrir detalhes de processo.

### Objetivo

Entender se a tela depende de composição fragmentada demais.

### Saída esperada

- sequência canônica de requests
- oportunidades de consolidação

## Bloco 3: subprocesso

### Item 3.1

Mapear sequência de requests de abertura e edição de subprocesso.

### Objetivo

Entender o fan-out entre:

- `subprocessos/buscar`
- `subprocessos/{codigo}/contexto-edicao`
- `contexto-cadastro-atividades/buscar`

### Saída esperada

- grafo da sequência
- duplicações
- oportunidades de cache e consolidação

### Item 3.2

Avaliar endpoint de contexto orientado ao estado atual da tela.

### Objetivo

Reduzir ida e volta para cards, mapas, cadastro e revisão.

## Bloco 4: workflow e ações lentas

### Item 4.1

Abrir fluxo de `cadastro/disponibilizar`.

### Objetivo

Entender por que apareceu como outlier.

### Saída esperada

- cadeia interna
- hipóteses de custo
- primeira lista de simplificações possíveis

### Item 4.2

Medir ações de workflow com tracing interno.

### Objetivo

Separar custo de:

- validação
- atualização de estado
- persistência
- notificações

## Sequência sugerida

1. Painel frontend
2. Painel backend
3. Processo `contexto-completo`
4. Subprocesso `contexto-edicao`
5. `cadastro/disponibilizar`

## Entregáveis esperados das próximas rodadas

### Rodada 1

- mapa do fluxo do painel
- hotspots internos de `painel/processos` e `painel/alertas`
- lista curta de ajustes imediatos

### Rodada 2

- revisão de `contexto-completo`
- revisão de `contexto-edicao`
- proposta de redução de round-trips

### Rodada 3

- ajustes de API e frontend
- validação comparativa antes/depois

## Próximo passo recomendado

Executar o item 1.1 e o item 1.2 em paralelo, começando pelo painel.
