# Plano de Redução de Fetch EAGER

## Objetivo

Reduzir carregamentos implícitos de associações JPA que hoje ampliam o custo de leitura, aumentam o risco de `N+1`, estouram budgets de queries e dificultam previsibilidade de desempenho no backend.

O plano adota uma abordagem incremental:

1. converter associações de maior risco para `LAZY`
2. explicitar carregamentos necessários com `join fetch` ou projeções
3. validar cada rodada com testes de budget e integrações

## Contexto

A auditoria inicial identificou associações `@ManyToOne` e `@OneToOne` ainda dependentes do fetch implícito do JPA, portanto efetivamente `EAGER`.

Os dois incidentes já corrigidos confirmam o padrão de falha:

- uma leitura simples carrega entidade principal
- o Hibernate materializa associação não usada
- consultas organizacionais ou relacionais extras são disparadas
- o budget de queries falha sem que o caso de uso tenha mudado semanticamente

## Princípios

- Não converter tudo para `LAZY` em lote.
- Cada mudança deve ter escopo pequeno e hipótese clara.
- Toda associação convertida para `LAZY` precisa de revisão dos pontos de leitura.
- Quando a leitura realmente precisar dos dados relacionados, o carregamento deve ser explícito no repositório ou via DTO projection.
- Fluxos críticos devem ganhar ou manter testes de budget.
- Não aceitar regressão silenciosa de serialização ou `LazyInitializationException`.

## Metas

- Reduzir queries acidentais em fluxos quentes de painel, detalhe, histórico, mapa e organização.
- Tornar o custo de leitura previsível por endpoint e serviço.
- Centralizar decisões de carregamento em queries de leitura, e não em defaults das entidades.
- Criar trilha de validação contínua para impedir reintrodução de `EAGER` problemático.

## Inventário Inicial de Risco

### Prioridade 1

- `sgc.subprocesso.model.Movimentacao`
  - `subprocesso`
  - `unidadeOrigem`
  - `unidadeDestino`
- `sgc.subprocesso.model.Subprocesso`
  - `processo`
  - `unidade`
  - `mapa`
- `sgc.alerta.model.Alerta`
  - `processo`
  - `unidadeOrigem`
  - `unidadeDestino`

### Prioridade 2

- `sgc.alerta.model.AlertaUsuario`
  - `alerta`
  - `usuario`
- `sgc.mapa.model.Mapa`
  - `subprocesso`
- `sgc.mapa.model.Atividade`
  - `mapa`

### Prioridade 3

- `sgc.organizacao.model.UnidadeMapa`
  - `mapaVigente`
- `sgc.alerta.model.Notificacao`
  - `subprocesso`
  - `unidadeOrigem`
  - `unidadeDestino`
- `sgc.organizacao.model.AtribuicaoTemporaria`
  - `unidade`
  - `usuario`
- `sgc.mapa.model.Competencia`
  - associação pai
- `sgc.mapa.model.Conhecimento`
  - associação pai

## Estratégia de Execução

### Fase 1: Subprocesso e Movimentação

Objetivo:
reduzir carga implícita nos fluxos mais frequentes de detalhe, histórico, localização e workflow.

Ações:

- converter associações de `Movimentacao` para `LAZY`
- converter associações de `Subprocesso` para `LAZY`
- revisar repositórios como:
  - `SubprocessoRepo`
  - `MovimentacaoRepo`
- adicionar `join fetch` apenas onde a leitura realmente usa:
  - unidade
  - processo
  - mapa
  - unidades de origem e destino
- revisar serialização indireta em DTOs e `JsonView`

Validação mínima:

- budgets de detalhe de subprocesso
- budgets de histórico
- integrações de workflow
- testes de permissões que consultam subprocesso

### Fase 2: Alertas

Objetivo:
reduzir custo de listagem no painel e consultas por usuário/unidade.

Ações:

- converter relações de `Alerta` para `LAZY`
- converter relações de `AlertaUsuario` para `LAZY`
- revisar queries de:
  - `AlertaRepo`
  - `AlertaFacade`
  - `PainelFacade`
- preferir projeções quando o painel só precisar de campos sintéticos

Validação mínima:

- budget de alertas no painel
- paginação por unidade
- paginação por servidor
- leitura de `dataHoraLeitura`

### Fase 3: Mapa e Atividade

Objetivo:
reduzir sobrecarga em leituras de mapa, atividades, cópia e manutenção.

Ações:

- converter `Mapa.subprocesso` para `LAZY`
- converter `Atividade.mapa` para `LAZY`
- revisar serviços:
  - `MapaManutencaoService`
  - `MapaVisualizacaoService`
  - `ImpactoMapaService`
- substituir dependência em entidade completa por DTO de leitura onde couber

Validação mínima:

- budgets de visualização de subprocesso e mapa
- integrações de edição e ajuste
- validações de impacto

### Fase 4: Organização e Demais Entidades

Objetivo:
fechar superfícies residuais e consolidar política de fetch.

Ações:

- revisar `UnidadeMapa`
- revisar `Notificacao`
- revisar `AtribuicaoTemporaria`
- revisar entidades de mapa de menor risco
- remover qualquer dependência restante de carregamento implícito

Validação mínima:

- integrações organizacionais
- integrações de notificação
- budgets relacionados a mapas vigentes e atribuições temporárias

## Critérios Para Converter uma Associação

Converter para `LAZY` quando:

- a associação não é usada na maior parte das leituras
- a entidade aparece em listagens ou históricos
- o acesso atual depende de fetch implícito não documentado
- o custo de materialização é alto ou multiplica queries

Manter `EAGER` apenas se:

- houver justificativa técnica explícita
- o relacionamento for invariavelmente necessário
- a mudança para `LAZY` introduzir custo maior ou complexidade sem ganho mensurável

Se `EAGER` for mantido, documentar no código o motivo.

## Padrões de Correção

### Padrão A: `LAZY` + `join fetch`

Usar quando a leitura ainda precisa da entidade relacionada completa.

Exemplos:

- detalhe com unidade e processo
- histórico com origem e destino
- leitura com serialização controlada

### Padrão B: `LAZY` + projection

Usar quando só poucos campos são necessários.

Exemplos:

- sigla da unidade
- descrição do processo
- código do mapa
- nome resumido

### Padrão C: `LAZY` + lookup em lote

Usar quando várias entidades referenciam poucos relacionamentos repetidos.

Exemplos:

- histórico de análises
- listas por unidade
- agregações por processo

## Riscos Conhecidos

- `LazyInitializationException` em montagem de resposta fora do escopo transacional
- regressão em `JsonView` quando getter acessa associação não preparada
- `N+1` reintroduzido por stream ou mapper após troca para `LAZY`
- testes passarem localmente sem capturar budgets de produção
- endpoints antigos dependerem de efeitos colaterais de materialização

## Mitigações

- revisar getters sintéticos que acessam associações
- priorizar DTOs de leitura em vez de serialização de entidade
- manter serviços de consulta `@Transactional(readOnly = true)` nos fluxos que ainda transitam por entidades
- ampliar budgets em integrações de fluxos quentes
- comparar `totalSqls`, `sqlsViewsOrganizacionais` e `preparedStatements`

## Critérios de Aceite por Rodada

- nenhuma `LazyInitializationException`
- nenhum budget existente piora
- budgets corrigidos ficam estáveis em execuções repetidas
- queries quentes ficam explícitas no repositório ou service
- não há mudança funcional no payload ou nas regras de negócio

## Entregáveis

### Entregável 1

Hardening de `Subprocesso` e `Movimentacao` com budgets atualizados.

### Entregável 2

Hardening de `Alerta` e `AlertaUsuario` com validação do painel.

### Entregável 3

Hardening de `Mapa` e `Atividade` com validação de leitura e manutenção.

### Entregável 4

Fechamento dos remanescentes e política documentada de fetch.

## Política Recomendada Para Novos Mapeamentos

- `ManyToOne` deve nascer como `LAZY`
- `OneToOne` deve nascer como `LAZY`, salvo justificativa explícita
- fetch necessário deve ser decidido na query, não no default da entidade
- endpoints de leitura frequente devem preferir projection ou DTO
- novos fluxos críticos devem ter teste de budget desde o início

## Próxima Execução Recomendada

1. atacar `Movimentacao`
2. atacar `Subprocesso`
3. rodar budgets de subprocesso
4. atacar `Alerta` e `AlertaUsuario`
5. rodar budgets do painel

## Observação Final

Este plano não trata apenas de estilo de mapeamento. Ele redefine a forma como o backend controla custo de leitura: associações deixam de carregar dados por acidente e passam a ser carregadas por intenção explícita.
