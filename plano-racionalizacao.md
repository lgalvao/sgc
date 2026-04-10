# Plano de racionalização de desempenho

## Objetivo

Transformar o problema atual de desempenho em um programa estruturado de racionalização, com foco em reduzir:

- chamadas redundantes no frontend
- fan-out desnecessário entre frontend e backend
- recomputações e recargas de tela sem ganho funcional
- consultas repetidas ou potencialmente N+1 no backend
- custo de montagem de telas ricas em contexto

O objetivo não é só "otimizar queries" ou "diminuir milissegundos". O objetivo principal é diminuir a quantidade de trabalho feita pelo sistema para entregar a mesma experiência funcional.

## Diagnóstico atual

Com base no monitoramento já implantado e na execução integral da suíte E2E registrada em `e2e/monitoramento-e2e.txt`, os sinais mais fortes são:

- há muitas chamadas repetidas para compor as mesmas telas
- várias telas parecem recompor contexto inteiro ao navegar entre cards, modais ou estados
- o backend costuma responder rápido em chamadas isoladas, mas a quantidade total de chamadas por fluxo é alta
- existem picos no primeiro acesso de alguns endpoints, indicando warm-up e/ou inicialização tardia
- `painel/alertas` tende a custar mais que `painel/processos`
- há forte indício de oportunidades de consolidação de endpoints orientados à view
- é plausível que existam N+1 ou loops de montagem de DTO em endpoints de contexto mais rico

Importante:

- a suíte E2E completa não foi desenhada para medir desempenho e contém repetição estrutural de login, troca de perfil e reentrada em telas
- por isso, frequência absoluta da suíte completa não deve ser tomada isoladamente como proxy de produção
- os testes mais representativos para reality check funcional de ponta a ponta passam a ser [captura.spec.ts](/Users/leonardo/sgc/e2e/captura.spec.ts) e [jornada.spec.ts](/Users/leonardo/sgc/e2e/jornada.spec.ts)
- a primeira tentativa de coleta dedicada desses cenários foi invalidada por infra E2E reaproveitada de forma inconsistente; essa causa já foi endurecida em `playwright.config.ts` e `e2e/lifecycle.js`

## Achados já confirmados

Os itens abaixo já deixaram de ser hipótese genérica e passaram a ser evidência concreta no código atual:

- o [PainelView.vue](/Users/leonardo/sgc/frontend/src/views/PainelView.vue) dispara sempre duas chamadas independentes na carga principal da tela: `GET /api/painel/processos` e `GET /api/painel/alertas`
- o [PainelView.vue](/Users/leonardo/sgc/frontend/src/views/PainelView.vue) recarrega o painel inteiro em `onMounted` e também em `onActivated`, o que aumenta a chance de repetição ao voltar para a rota
- a ordenação do painel não reaproveita o resultado local: ela dispara nova chamada de `processos`, mesmo quando só muda o critério visual
- `GET /api/unidades/diagnostico-organizacional` não vem do painel; os chamadores confirmados no frontend são [ProcessoCadastroView.vue](/Users/leonardo/sgc/frontend/src/views/ProcessoCadastroView.vue) e [UnidadesView.vue](/Users/leonardo/sgc/frontend/src/views/UnidadesView.vue)
- `GET /api/unidades/arvore-com-elegibilidade` também não vem do painel; o chamador confirmado é [ProcessoCadastroView.vue](/Users/leonardo/sgc/frontend/src/views/ProcessoCadastroView.vue)
- no backend, [PainelFacade.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/painel/PainelFacade.java) trata `alertas` e `processos` em rotas separadas, sem bootstrap único orientado à tela
- em [PainelFacade.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/painel/PainelFacade.java), a carga de alertas tem efeito colateral: além de listar, ela busca leituras e marca itens como lidos, o que merece cuidado antes de qualquer cache
- em [ProcessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java), a lista do painel já é paginada em duas etapas: primeiro busca só códigos e depois recarrega a página com participantes
- em [PainelFacade.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/painel/PainelFacade.java), a montagem de `ProcessoResumoDto` ainda usa mapa de hierarquia completo e pode buscar siglas complementares para participantes sem sigla carregada
- em [ValidadorDadosOrganizacionais.java](/Users/leonardo/sgc/backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java), o diagnóstico já usa `@Cacheable`, então a frequência alta observada na suíte indica mais problema de repetição de chamada do que custo bruto por chamada depois do aquecimento
- o tracing interno do backend já está funcional com `SGC_MONITORAMENTO=on`; a causa do sumiço era a escolha do construtor errado em [MonitoramentoAspect.java](/Users/leonardo/sgc/backend/src/main/java/sgc/comum/util/MonitoramentoAspect.java), que podia deixar `ativo=false`

## Reality Check: captura e jornada

Com base na execução dedicada registrada em `e2e/monitoramento-reality-check.txt`, os sinais mais confiáveis para priorização inicial são:

- em [captura.spec.ts](/Users/leonardo/sgc/e2e/captura.spec.ts), os campeões de frequência continuam sendo `painel/processos`, `painel/alertas`, `usuarios/login`, `unidades/diagnostico-organizacional`, `subprocessos/{codigo}/contexto-edicao` e `subprocessos/buscar`
- em [jornada.spec.ts](/Users/leonardo/sgc/e2e/jornada.spec.ts), a frequência relevante se concentra menos em diversidade e mais em ciclos repetidos de `subprocessos/buscar` + `subprocessos/{codigo}/contexto-edicao` + `processos/{codigo}/contexto-completo`
- no reality check, quase todos os tempos absolutos ficaram baixos; isso reforça que o problema dominante é chatice de protocolo e recomposição de contexto, não latência extrema unitária
- o outlier mais claro de `captura` continua sendo `POST /api/subprocessos/405/cadastro/disponibilizar` com 143 ms
- em `jornada`, o ponto mais suspeito foi `GET /api/subprocessos/401/validar-cadastro` com 92 ms, seguido por `POST /api/subprocessos/401/disponibilizar-revisao` com 36 ms
- `painel/alertas` e `painel/processos` seguem abrindo praticamente toda transição de papel ou retorno a fluxo, inclusive em jornadas já contextualizadas
- os pares `subprocessos/buscar` e `subprocessos/{codigo}/contexto-edicao` reaparecem várias vezes dentro do mesmo subprocesso, o que fortalece a hipótese de reentrada de tela e recomposição redundante

Conclusão operacional desta rodada:

- o primeiro foco continua correto: racionalizar chamadas do painel, reduzir reabertura de contexto de subprocesso e revisar validações/workflows específicos
- o segundo foco deve ser a sequência de leitura em telas de subprocesso, porque ela reaparece até nos fluxos mais representativos e não só na suíte completa

## Reality Check Com Trace Interno

Com base na coleta com trace interno registrada em `e2e/monitoramento-reality-check-trace.txt`, surgiram sinais novos e mais úteis sobre o backend:

- `painel/alertas` é de fato mais caro que `painel/processos` porque sua cadeia inclui `AlertaFacade.listarPorUnidade`, `AlertaFacade.obterMapaDataHoraLeitura` e, em muitos casos, `AlertaFacade.marcarComoLidos`
- `painel/processos` também tem custo composto: `ProcessoRepo.listarCodigos*`, `ProcessoRepo.listarPorCodigosComParticipantes` e enriquecimento de siglas por `UnidadeService.buscarSiglasPorCodigos`
- o maior outlier interno não é query pesada, e sim `EmailService.enviarEmailHtml`, com picos na faixa de 100 a 200 ms
- `SubprocessoNotificacaoService.notificarTransicao` e `SubprocessoTransicaoService.disponibilizarCadastro` aparecem como custo relevante em transições de workflow
- `ProcessoService.obterDetalhesCompleto` teve pico de 60 ms e merece revisão de composição, mas ficou atrás do custo de e-mail e notificação
- `SubprocessoConsultaService.obterContextoEdicao` teve pico de 41 ms; isso confirma que o endpoint não é só repetido, ele também tem trabalho interno não trivial
- `UnidadeHierarquiaService.buscarArvoreComElegibilidade` apareceu com pico de 21 ms, mas ainda abaixo dos principais pontos de atenção

Conclusão operacional desta rodada com trace:

- a racionalização agora tem três frentes iniciais claramente justificadas:
- reduzir round-trips do painel e da leitura de subprocesso
- tirar custo síncrono de notificação/e-mail do caminho crítico das transições
- revisar a montagem de contexto completo e contexto de edição para reduzir recomposição interna

## Princípios de atuação

### 1. Otimizar o fluxo, não só a query

Antes de micro-otimizar um endpoint, verificar se ele deveria existir isoladamente naquele fluxo ou se poderia ser consolidado com outro.

### 2. Priorizar jornada real

A prioridade deve ser dada às jornadas mais usadas e mais caras, não apenas aos endpoints mais lentos em termos absolutos.

### 3. Medir antes e depois

Toda racionalização relevante deve comparar:

- número de requests por tela
- tempo total para a tela ficar utilizável
- tempo dos endpoints mais críticos
- eventual redução de queries ou carregamentos repetidos

### 4. Evitar regressão funcional

Qualquer consolidação de chamada deve preservar:

- regras de acesso
- estados do fluxo
- atualização correta da UI
- tratamento de erros de negócio

### 5. Trabalhar por fluxo vertical

É melhor fechar um fluxo de ponta a ponta do que espalhar pequenas otimizações superficiais por muitas áreas.

## Eixos de trabalho

## Eixo 1: observabilidade útil para racionalização

### Objetivo

Usar o monitoramento implantado não só para "mostrar tempos", mas para orientar decisões.

### Ações

- Manter execução de E2E com `SGC_MONITORAMENTO=on` para coleta dirigida.
- Gerar snapshots periódicos de logs em arquivo para análise por grep/sort/uniq.
- Destacar chamadas `HTTP-LENTO` e `HTTP-MUITO-LENTO` como gatilho inicial.
- Ampliar gradualmente o uso do `MonitoramentoAspect` para rotas/serviços prioritários.
- Capturar, para fluxos críticos, a sequência de requests por jornada.

### Entregáveis

- Ranking de endpoints mais lentos
- Ranking de endpoints mais frequentes
- Mapa de sequências repetidas por jornada
- Lista de oportunidades de consolidação

## Eixo 2: racionalização do frontend

### Objetivo

Diminuir chamadas redundantes e recomposições desnecessárias na UI.

### Problemas típicos a procurar

- `onMounted` e watcher disparando a mesma carga
- componentes irmãos chamando endpoints distintos para montar a mesma tela
- ações pontuais provocando recarga total desnecessária
- reentrada em tela recarregando dados invariantes
- navegação entre cards/modais refazendo contexto já conhecido
- stores/composables sem cache por chave lógica

### Estratégia

#### Fase 2.1: inventário de fluxos

Mapear para cada tela crítica:

- quais requests são disparadas ao entrar
- quais requests são disparadas ao trocar estado
- quais são duplicadas ou fortemente correlatas

#### Fase 2.2: eliminar duplicações locais

Revisar:

- watchers imediatos
- efeitos de reatividade redundantes
- recargas desencadeadas por mudanças cosméticas
- chamadas repetidas na mesma rota com os mesmos parâmetros

#### Fase 2.3: introduzir cache de curta duração por contexto

Aplicar cache controlado em dados que:

- não mudam durante uma interação curta
- são reusados ao voltar para a mesma tela
- são usados por múltiplos componentes da mesma view

Exemplos prováveis:

- contexto de processo
- contexto de subprocesso
- árvores de unidade e elegibilidade
- diagnóstico organizacional
- configurações

#### Fase 2.4: consolidar fetch por tela

Sempre que possível, uma tela deve ter menos pontos independentes de carregamento.

Meta desejável:

- uma tela principal não depender de 5 a 10 chamadas pequenas para montar o estado inicial sem necessidade real

### Critérios de pronto

- redução do número médio de requests por jornada
- remoção de duplicatas consecutivas
- menor acoplamento entre componente e endpoint
- sem regressão de feedback visual ou atualização de estado

## Eixo 3: racionalização do backend

### Objetivo

Reduzir custo interno de endpoints e evitar trabalho redundante na composição dos dados.

### Problemas típicos a procurar

- N+1 em entidades relacionadas
- busca repetida do mesmo agregado dentro da mesma request
- montagem de DTO com loops e novas consultas
- validações ou verificações de permissão repetidas sem cache/request-scope
- endpoints de contexto que buscam muito mais do que a view precisa

### Estratégia

#### Fase 3.1: priorizar endpoints

Começar por:

- `/api/painel/alertas`
- `/api/painel/processos`
- `/api/unidades/diagnostico-organizacional`
- `/api/unidades/arvore-com-elegibilidade`
- `/api/processos/{codigo}/contexto-completo`
- `/api/subprocessos/{codigo}/contexto-edicao`
- `/api/subprocessos/buscar`

Esses endpoints aparecem com frequência alta ou compõem jornadas de muita navegação.

#### Fase 3.2: abrir a cadeia interna

Para cada endpoint prioritário, identificar:

- controller/facade/service envolvidos
- repos chamados
- quantidade de acessos a banco
- custo de serialização/mapeamento

#### Fase 3.3: eliminar N+1 e fan-out interno

Possíveis ações:

- ajustar fetch strategy
- criar query mais agregada
- trocar laço com consultas por consulta em lote
- pré-carregar dependências
- simplificar DTOs para a necessidade real da tela

#### Fase 3.4: reduzir custo de contexto

Endpoints "contexto-completo" e similares devem ser revistos com rigor:

- o payload retornado é realmente usado inteiro?
- há blocos que poderiam ser lazy no frontend?
- há partes que poderiam ser carregadas sob demanda?

### Critérios de pronto

- menos queries por request
- menor dispersão de tempo entre chamadas equivalentes
- redução dos tempos nos endpoints mais frequentes
- menor sensibilidade a volume de itens relacionados

## Eixo 4: redesenho de API orientado à tela

### Objetivo

Parar de compor telas complexas por múltiplas chamadas fragmentadas quando um endpoint de bootstrap faria mais sentido.

### Heurística

Se uma tela sempre precisa de três ou mais chamadas estáveis para montar seu estado inicial, isso é candidato a endpoint agregado.

### Candidatos prováveis

- bootstrap do painel
- bootstrap de processo
- bootstrap de subprocesso
- contexto de edição mais enxuto e orientado ao estado atual

### Cuidados

- não criar endpoints monolíticos demais
- manter contratos claros por caso de uso
- evitar acoplamento excessivo entre frontend e objeto gigante genérico

### Critério de pronto

- menos round-trips na entrada de telas críticas
- menor latência percebida
- fluxo de dados mais previsível

## Frentes prioritárias

## Prioridade 1: painel

### Motivo

- altíssima frequência
- ponto de entrada recorrente
- já mostrou picos
- base para grande parte da navegação

### Escopo

- `painel/processos`
- `painel/alertas`
- eventual consolidacao de bootstrap do painel

### Perguntas

- as duas chamadas precisam ser independentes?
- os filtros/ordenações justificam endpoints separados?
- há consulta cara em alertas?
- o frontend recarrega mais do que deveria ao voltar ao painel?

## Prioridade 2: processo / contexto-completo

### Motivo

- centro da maior parte dos fluxos de negócio
- aparece em muitos cenários E2E

### Escopo

- `processos/{codigo}/contexto-completo`
- `processos/{codigo}/detalhes`
- impactos de recargas após ações de workflow

### Perguntas

- o contexto está grande demais?
- a tela usa tudo que o backend entrega?
- há oportunidades de segmentar por aba/etapa?

## Prioridade 3: subprocesso / contexto-edicao

### Motivo

- alto número de chamadas por navegação
- aparece em fluxos de cadastro, mapa, revisão e validação

### Escopo

- `subprocessos/{codigo}/contexto-edicao`
- `subprocessos/buscar`
- `contexto-cadastro-atividades/buscar`

### Perguntas

- há requests repetidas entre busca, detalhe e contexto?
- parte desse contexto poderia ser reaproveitada localmente?
- a mesma tela pede dados complementares em cascata?

## Plano de execução sugerido

## Etapa 1: consolidar evidência

### Objetivo

Transformar logs em listas acionáveis.

### Tarefas

- Coletar execução E2E monitorada por arquivo.
- Gerar ranking dos endpoints mais frequentes.
- Gerar ranking dos endpoints mais lentos.
- Identificar sequências repetidas por jornada.
- Registrar top 10 oportunidades iniciais.

### Saída

- documento curto com ranking e hipóteses

## Etapa 2: inspeção dirigida do frontend

### Objetivo

Encontrar duplicações e recargas evitáveis nos fluxos prioritários.

### Tarefas

- abrir views, composables, stores e services do painel
- mapear triggers de fetch
- remover duplicações óbvias
- introduzir cache/reuso local onde fizer sentido

### Saída

- PR ou conjunto de ajustes do painel

## Etapa 3: inspeção dirigida do backend

### Objetivo

Descobrir onde o backend ainda faz trabalho redundante nos endpoints priorizados.

### Tarefas

- ampliar uso do `MonitoramentoAspect` nos fluxos observados
- correlacionar request HTTP com `Service`/`Repo`
- revisar repositórios e composição de DTO
- atacar N+1 e consultas em laço

### Saída

- lista de hotspots internos por endpoint
- ajustes de query, fetch ou composição

## Etapa 4: consolidação de contratos

### Objetivo

Reduzir round-trips em telas críticas.

### Tarefas

- desenhar contratos orientados à view
- negociar payload mínimo suficiente
- adaptar frontend para consumir menos endpoints

### Saída

- menor quantidade de requests por jornada

## Etapa 5: validação comparativa

### Objetivo

Provar ganho real.

### Métricas

- número de requests por fluxo
- tempo médio e p95 por endpoint-chave
- tempo total de montagem das telas críticas
- quantidade de chamadas repetidas por cenário

### Saída

- antes/depois por fluxo

## Ferramentas de apoio

- `SGC_MONITORAMENTO=on npx playwright test`
- `e2e/monitoramento-e2e.txt`
- `rg`, `sort`, `uniq`, `awk`
- `MonitoramentoAspect`
- análise de services/repos por endpoint
- snapshots do dashboard de QA quando a mudança afetar desempenho geral

## Riscos e cuidados

- otimizar cedo demais o endpoint errado
- consolidar APIs sem entender o consumo real da tela
- reduzir requests mas aumentar acoplamento excessivo
- mascarar problema de desenho com cache local indevido
- confundir ruído da suíte E2E com comportamento real de produção

## Critério de sucesso

O plano será bem-sucedido quando:

- os fluxos prioritários fizerem menos chamadas
- os endpoints críticos tiverem menor variabilidade
- o tempo percebido de tela cair
- a navegação exigir menos recomposição de contexto
- a base ficar mais previsível e menos dependente de trabalho redundante

## Próximos passos imediatos

1. Consolidar análise do log E2E monitorado em um resumo curto de hotspots.
2. Escolher o primeiro fluxo vertical para racionalização.
3. Recomenda-se começar pelo painel.
4. Em seguida, atacar `processo/contexto-completo`.
5. Depois, revisar `subprocesso/contexto-edicao` e chamadas associadas.
