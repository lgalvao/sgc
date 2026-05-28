# Plano Prático de Simplificação Estrutural do Frontend

## Objetivo

Reduzir a infraestrutura caseira no frontend do SGC em dois eixos:

- adoção de `Pinia Colada` para **server state**;
- remoção de orquestração artificial, helpers transitivos e contratos internos obsoletos.

## Estado Atual

### Já concluído

- `historico`, `painel`, `processo`, `notificacoes admin` e `feedbacks admin` migrados para query cache;
- `configuracoes` migrado para query cache e mutation cache;
- `unidade` migrado para query cache nas telas principais (`UnidadeView`, `UnidadesView` e fluxo de atribuição temporária);
- `notificacoes admin` evoluído para usar query + mutation também para reenvio e URL do leitor de e-mails de teste;
- remoção dos stores remotos manuais de `historico` e `processo`;
- `painelStore` reduzido a estado local real;
- invalidação por query consolidada nos domínios já migrados;
- redução relevante da orquestração artificial em fluxos de processo, subprocesso, cadastro e mapa.
- `organizacao` mantido fora de Colada por enquanto: a store é instanciada em contextos demais fora de `setup()`, e a migração limpa exigiria redesenho mais amplo dos consumidores.
- a régua de lint estrutural foi restaurada após um desvio temporário que havia desligado regras de complexidade, tamanho e parâmetros.
- auditoria arquitetural recalibrada para reduzir orientação perversa (retornos aninhados não contam como superfície exportada da função principal).
- composables co-localizados em `views/` passaram a ser tratados como contrato de tela para DI/superfície, evitando falso positivo estrutural.
- `useMapas` foi simplificado para contrato atual do Colada, removendo API morta e testes legados acoplados.
- `stores/perfil.ts` foi limpo com remoção da bolsa artificial `EstadoSessaoRefs` e helpers inlinados no escopo real da store.

### Diagnóstico novo

Os ganhos fáceis com `Pinia Colada` já foram capturados. O que restou relevante em loading/cache é mais híbrido e não deve ser migrado por simetria. Os próximos ganhos devem vir de:

- apertar contratos internos frouxos que preservam compatibilidades obsoletas;
- eliminar APIs callback-based onde a dependência real é um valor obrigatório;
- reduzir helpers que só empacotam `dependencias`, `estado` e `contexto`;
- aceitar mudanças mais agressivas em contratos internos de composables/views quando o chamador é único ou controlado.

### Baseline da auditoria arquitetural

A auditoria foi endurecida. Ela não mede mais só vocabulário literal (`cache`, `forcar`, `snapshot`), e agora incorpora análise estrutural de AST para:

- fan-out arquitetural por arquivo;
- chamadas diretas a store e service;
- vazamento de estratégia de cache;
- bolsas largas de `dependencias`/`estado`/`contexto`;
- superfícies exportadas amplas;
- mistura de camadas arquiteturais no mesmo arquivo.

Última medição registrada por `node etc/scripts/sgc.js frontend arquitetura auditar`:

- score total: `203` (`critico`)
- views com vazamento de estratégia de cache: `0`
- views com service direto: `1`
- views com server state caseiro: `0`
- views com fan-out alto: `0`
- acessos diretos a cache de store: `0`
- booleanos posicionais: `1`
- bolsas largas de dependências/estado: `1`
- superfícies exportadas amplas: `16`
- arquivos com mistura de camadas: `0`
- arquivos com server state caseiro: `1`
- hubs centrais com sinais: `2`

Hotspots prioritários desta baseline:

1. `frontend/src/stores/organizacao.ts`
2. `frontend/src/composables/useSubprocessoTela.ts`
3. `frontend/src/stores/perfilAutenticacao.ts`
4. `frontend/src/composables/usePerfil.ts`
5. `frontend/src/views/LimpezaProcessosView.vue`

### Metas de progresso arquitetural

As próximas rodadas devem ser medidas contra esta baseline. A meta não é “zerar o score”, e sim reduzir sinais estruturais nos pontos de maior custo cognitivo.

Curto prazo:

- remover o `serviceDireto` remanescente em view (`LimpezaProcessosView.vue`);
- reduzir os dois hubs centrais que ainda sinalizam (`organizacao` e `perfilAutenticacao`);
- atacar `useSubprocessoTela.ts` para diminuir estratégia de cache exposta;
- continuar reduzindo superfícies amplas em composables/stores remanescentes.

Médio prazo:

- manter `viewsComVazamentoCache` em `0`;
- levar `viewsComServiceDireto` de `1` para `0`;
- manter `viewsComServerStateCaseiro` em `0`;
- manter `viewsComFanoutAlto` em `0`;
- levar `arquivosComBolsaDependenciasLarga` de `1` para `0`;
- levar `arquivosComSuperficieAmpla` de `16` para menos de `10`;
- reduzir `hubsCentraisComSinais` de `2` para `1` ou menos.

### Situação da régua

As regras de lint estrutural (`complexity`, `max-params`, `max-depth`, `max-nested-callbacks`, `max-lines`,
`max-lines-per-function`, `max-statements`) foram restauradas. Elas não devem ser desligadas novamente para “passar o
gate”.

Quando a régua acusar excesso, a ação esperada é uma destas:

- simplificar o código;
- quebrar a função/arquivo em contratos menores;
- ou recalibrar explicitamente o limite com justificativa técnica.

O estado atual do `quality:lint` ainda expõe warnings reais, principalmente em:

- `stores/organizacao.ts`;
- `useSubprocessoTela.ts`;
- `stores/perfilAutenticacao.ts`;
- `usePerfil.ts`;
- `stores/subprocesso/index.ts`.

### Padrão de emaranhamento observado

Os nós mais difíceis do frontend não são apenas “arquivos grandes”. O padrão recorrente é uma mesma abstração concentrar responsabilidades demais ao mesmo tempo:

- cache de dados remotos;
- estado de sessão e autorização;
- invalidação transversal;
- navegação e ciclo de vida;
- adaptação para teste, router e uso fora de `setup()`.

Quando isso acontece, a simplificação local começa a render pouco. Foi esse padrão que apareceu, em graus diferentes, em:

- `stores/perfil.ts`;
- `stores/unidade.ts`;
- `stores/mapas.ts`;
- `stores/subprocesso/*`;
- `useInvalidacaoNavegacao.ts`;
- `useCacheSync.ts`;
- composables de orquestração de cadastro, subprocesso e mapa.

### Consequência prática

Os próximos ganhos não devem vir principalmente de “mais limpeza interna”, e sim de reduzir o raio de ação dessas abstrações:

- separar melhor `server state` de `application state`;
- apertar contratos internos que hoje aceitam optionalidade ou generalidade artificiais;
- mover integração transversal para a borda da aplicação;
- quebrar hubs centrais em superfícies menores e mais honestas.

### Mudança de direção do trabalho

O trabalho deixa de ser principalmente “migrar mais coisas para Colada” ou “limpar arquivos grandes um a um”. A direção agora passa a ser arquitetural:

- definir contratos corretos para views;
- esconder estratégia de cache dos consumidores;
- reduzir o número de abstrações centrais que coordenam tudo;
- só então decidir se um domínio deve usar `Pinia`, `Pinia Colada` or composição local.

## Princípios

- `Pinia` continua responsável por estado de aplicação e sessão.
- `Pinia Colada` entra apenas onde o problema principal é **cache de dados remotos**.
- quando uma abstração mistura cache remoto com sessão, permissão, navegação e UI local, o primeiro passo é separar responsabilidades antes de “refatorar melhor”.
- view não deve conhecer estratégia de cache, stale, snapshot, invalidação local, nem detalhes de reaproveitamento de dados.
- contratos consumidos por view devem ser orientados a caso de uso da tela, não a mecanismo de armazenamento.
- Migrar por fatias pequenas, começando pelos casos mais simples.
- Preferir contratos explícitos a helpers genéricos quando a dependência real é obrigatória.
- Mudar contratos internos obsoletos quando a compatibilidade já não paga o custo cognitivo.
- Cada fase precisa terminar com validação real e redução perceptível de código.

### Diretrizes arquiteturais para views

As views devem operar com estes tipos de contrato:

- `carregarDadosDaTela()`
- `recarregarDadosDaTela()`, apenas quando a semântica de atualização explícita for relevante
- `executarAcaoDeTela(...)`
- estado pronto para renderização: `carregando`, `erro`, `dados`

As views não devem operar com estes tipos de contrato:

- `temXEmCache()`
- `obterXEmCache()`
- `forcar`
- `stale`
- `invalidar`
- `reaplicarSnapshot`

Se uma view precisa decidir entre “usar cache ou não”, então a abstração ainda está vazando detalhe estrutural.

### Regra para contratos de leitura

Para dados de tela, preferir nesta ordem:

1. uma operação única semântica, por exemplo `carregarDadosTelaUnidade()`;
2. uma operação complementar explícita de atualização, apenas se o caso de uso exigir distinção real;
3. nunca expor `forcar`, `cache`, `stale` ou equivalentes para a view.

Separar `obterDados` de `recarregarDados` só é aceitável quando o chamador realmente precisa escolher conscientemente entre reutilização e atualização. Se a view não deveria tomar essa decisão, a distinção deve ficar interna.

### Regra para desfazer nós

Diante de um emaranhamento, preferir nesta ordem:

1. remover compatibilidade interna obsoleta;
2. apertar o contrato para refletir a dependência real;
3. separar estado remoto de estado local;
4. quebrar a abstração central em unidades menores;
5. só então simplificar a implementação restante.

Isso evita refatoração cosmética, em que o código muda de forma mas preserva o mesmo acoplamento estrutural.

## Escopo

### Fica em Pinia

- sessão e autorização (`perfil`);
- preferências locais e tema;
- toast pendente;
- estado efêmero de modal, formulário e workflow;
- coordenação de logout e reset de sessão.

### Candidatos imediatos ao Colada

- `historico`;
- `painel`;
- `processo`;
- leituras administrativas simples feitas direto nas views.

### Casos para depois

- `subprocesso`;
- `mapas`;
- `unidade`;
- `organizacao`;
- `relatorios`.

## Ordem de execução

## Fase 0 - Preparação [CONCLUÍDO]

### Meta

Instalar e preparar a infraestrutura mínima do `Pinia Colada` sem alterar comportamento funcional.

### Passos

1. Adicionar a dependência do `Pinia Colada` no frontend.
2. Registrar o plugin no bootstrap da aplicação.
3. Definir uma convenção local para chaves de query.
4. Definir uma convenção de invalidação por domínio.

### Convenção inicial de chaves

- `["historico"]`
- `["painel"]`
- `["processo", codProcesso]`
- `["notificacoes-admin"]`
- `["feedbacks-admin"]`

### Critério de sucesso

- aplicação sobe normalmente;
- nenhum comportamento existente muda;
- typecheck e lint seguem verdes.

## Fase 1 - Piloto no Histórico [CONCLUÍDO]

### Meta

Substituir o cache manual do histórico por uma query simples.

### Alvo

- `frontend/src/stores/historico.ts`
- `frontend/src/views/HistoricoView.vue`

### Estratégia

1. Criar query `historico` com `useQuery`.
2. Remover `carregado`, `carregando`, `garantirDados()` e dedupe manual do store.
3. Fazer a view consumir a query em vez do store de cache.
4. Manter ordenação local da tabela fora da query.

### Resultado esperado

- eliminação do store como cache remoto;
- remoção de `dadosValidos()` e parte da lógica de `onActivated()`;
- loading e erro vindos da query.

### Critério de sucesso

- histórico continua carregando no primeiro acesso;
- navegação em `keepAlive` não força gambiarras de recarga;
- testes da view e fluxo de histórico continuam passando.

## Fase 2 - Migração do Painel [CONCLUÍDO]

### Meta

Trocar o cache manual do painel por query com invalidação explícita.

### Alvo

- `frontend/src/stores/painel.ts`
- `frontend/src/views/PainelView.vue`
- `frontend/src/composables/useInvalidacaoNavegacao.ts`
- `frontend/src/composables/useCacheSync.ts`

### Estratégia

1. Criar query `painel` baseada no bootstrap já existente.
2. Remover TTL manual, `dadosValidos()` e `definirDados()` do store.
3. Preservar separadamente apenas o que for estado local útil, como marcação otimista de alertas lidos, se ainda fizer sentido.
4. Adaptar invalidação para `invalidateQueries({ key: ["painel"] })`.
5. Adaptar o SSE organizacional para invalidar queries do domínio afetado, em vez de invalidar stores inteiras.

### Resultado esperado

- menos acoplamento entre `PainelView`, `painelStore`, `useCacheSync` e `useInvalidacaoNavegacao`;
- fim do TTL manual no store;
- query cache mais previsível.

### Critério de sucesso

- painel carrega normalmente;
- mutações que afetam o painel continuam refletindo após invalidação;
- evento SSE continua forçando recarga na próxima leitura.

## Fase 3 - Contexto de Processo [CONCLUÍDO]

### Meta

Migrar o contexto completo de processo, hoje tratado como cache/dedupe manual.

### Alvo

- `frontend/src/stores/processo.ts`
- `frontend/src/views/ProcessoDetalheView.vue`
- pontos de invalidação que hoje chamam `processoStore.invalidar()`

### Estratégia

1. Criar query `["processo", codProcesso]`.
2. Remover `carregamentosEmAndamento`, `dadosValidos()` e `garantirContextoCompleto()` do store.
3. Ajustar a tela para consumir o estado reativo da query.
4. Preservar no `Pinia` apenas o que ainda for estado local de navegação/sessão, caso reste algo.

### Critério de sucesso

- processo abre e reabre corretamente;
- keepAlive não exige recarga manual baseada em store stale;
- mutações continuam invalidando a query certa.

## Fase 4 - Leituras Administrativas Simples [CONCLUÍDO]

### Meta

Usar Colada diretamente em views que hoje fazem fetch manual com `ref + try/finally`.

### Alvos prioritários

- `NotificacoesAdminView.vue`
- `FeedbacksAdminView.vue`

### Estratégia

1. Criar queries dedicadas por tela.
2. Substituir `carregando`, `erro` e `carregar()` manuais por query e refresh.
3. Manter no componente apenas estado local de modal, item selecionado e ação em andamento.

### Critério de sucesso

- menos boilerplate por tela;
- refresh explícito continua funcionando;
- modais e ações seguem intactos.

## Fase 5 - Reavaliação dos Casos Híbridos [EM ANDAMENTO / DESIGN]

### Meta

Decidir com evidência se vale migrar parcialmente `subprocesso`, `mapas`, `unidade` e `configuracoes`.

### Perguntas para responder

- o ganho é real ou só muda o lugar da complexidade?
- a chave de query é clara e estável?
- há atualização local rica demais para uma query simples?
- parte do estado deve permanecer em `Pinia`?

### Regra

Não migrar esses casos por simetria. Só migrar se os pilotos anteriores tiverem reduzido código e simplificado testes de verdade.

## Fase 6 - Apertar Contratos Internos [PLANEJADO]

### Meta

Parar de preservar contratos internos que ficaram obsoletos depois da simplificação de cache e orquestração.

### Alvos

- callbacks do tipo `executarComSubprocesso(cb)` quando a necessidade real é “obter `codigoSubprocesso` ou falhar”;
- helpers do tipo `concluirAcaoPainel(mensagem, fecharModal)` quando um objeto explícito ou fechamento local for mais claro;
- props/composables locais que ainda aceitam `number | string` sem necessidade real;
- helpers que recebem `dependencias`, `estado` ou `contexto` inteiros só para acessar 2-3 campos.

### Regra

Se o contrato é interno ao módulo/feature e só aumenta indireção:

1. preferir fechamentos locais;
2. preferir dependências explícitas;
3. remover compatibilidade de transição.

### Critério de sucesso

- menos callback nesting;
- menos `ReturnType<typeof ...>` em contratos locais;
- menos objetos “contexto/dependencias” artificiais;
- testes mais diretos, sem aumento de fragilidade.

## Fase 7 - Desfazer Hubs Centrais [PLANEJADO]

### Meta

Atacar explicitamente os pontos que concentram responsabilidades demais e por isso travam novas simplificações.

### Alvos prioritários

- `stores/organizacao.ts`
- `composables/useSubprocessoTela.ts`
- `stores/perfilAutenticacao.ts`
- `composables/usePerfil.ts`
- `views/LimpezaProcessosView.vue`

### Estratégia

1. identificar qual responsabilidade de cada hub é realmente central e qual é acoplamento acidental;
2. separar o que é:
   sessão/autorização;
   cache remoto;
   invalidação;
   snapshot local;
   navegação/orquestração;
3. quebrar o hub em superfícies menores com consumidores mais específicos;
4. só depois considerar migração adicional para `Pinia Colada`.

### Observação importante

`organizacao` virou o caso de referência aqui: a migração direta para Colada não pagou porque o problema principal não era o fetch, e sim o excesso de contextos de uso da store. O padrão deve ser tratado como alerta para os próximos candidatos híbridos.

## Frente Arquitetural Prioritária

### Frente 1 - Contratos de Tela

Meta:
remover das views qualquer decisão de estratégia de cache.

Critério:
- a view pede dados prontos;
- a abstração decide como obter, reaproveitar ou atualizar;
- `forcar`, `xxxEmCache`, `stale` e equivalentes desaparecem da borda consumida pela view.

### Frente 2 - Hubs Centrais

Meta:
reduzir o poder dos hubs antes de continuar com novas migrações ou limpezas locais.

Critério:
- menos coordenação transversal em stores/composables centrais;
- menos dependência entre sessão, cache, navegação e invalidação;
- menor necessidade de mocks estruturais nos testes.

### Frente 3 - Casos Híbridos

Meta:
reavaliar somente depois de melhorar contratos e bordas.

Critério:
- só migrar para Colada quando o problema principal for realmente `server state`;
- se o nó principal for contexto de uso, contrato frouxo ou abstração central, resolver isso antes.

## Próximos Passos Sugeridos em Ordem

1. Eliminar `serviceDireto` de `LimpezaProcessosView.vue` movendo a borda para composable/store de caso de uso.
2. Quebrar responsabilidades transversais em `stores/organizacao.ts` (service + server-state caseiro no mesmo ponto).
3. Enxugar `useSubprocessoTela.ts` para reduzir estratégia de cache exposta e superfície de contrato.
4. Reduzir `stores/perfilAutenticacao.ts` e `usePerfil.ts` para contratos menores e menos pass-through.

## Mudanças esperadas na arquitetura

### Depois das fases 1 a 4

- menos stores usadas como cache remoto;
- menos `invalidar()` genérico espalhado;
- menos `onActivated()` com checagem manual de stale;
- menos `loading/error` replicado por view;
- invalidação mais semântica por chave de query.

### Depois das frentes arquiteturais

- views deixam de saber se existe cache;
- stores e composables passam a expor contratos orientados a caso de uso;
- hubs centrais perdem responsabilidade transversal;
- migrações futuras para Colada ficam mais baratas e previsíveis.

### O que continua existindo

- `Pinia` para sessão e UI local;
- orquestração de mutações;
- invalidação orientada a domínio;
- estados locais de fluxo e formulários.

## Validação por fase

Executar no `frontend/`:

```bash
npm run quality:lint
npm run quality:typecheck
npm run test:unit
```

Quando a fase tocar navegação relevante, também validar o fluxo real da tela afetada.

## Critério de aprovação do plano

O plano só está funcionando se, ao final das primeiras fases:

- houver menos código de cache manual;
- houver menos estados duplicados de loading/erro;
- a invalidação ficar mais localizada;
- os testes ficarem mais simples, não mais frágeis.
- views não precisarem mais decidir estratégia de cache.

Se os próximos cortes não reduzirem indireção real, parar a limpeza local e partir para mudanças de contrato mais agressivas nos módulos restantes.
