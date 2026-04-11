# Backlog de racionalização (somente pendências atuais)

## Contexto

Este backlog mantém **apenas o que ainda falta executar** após as rodadas 3, 4 e 5 já iniciadas.

## O que foi concluído nesta atualização

- **Rodada 3 (A.1)**
  - Refatorado o fluxo `processo + unidade -> contexto de edição` para **1 chamada** quando não há cache local:
    - antes: `GET /api/subprocessos/buscar` + `GET /api/subprocessos/{codigo}/contexto-edicao`
    - agora: `GET /api/subprocessos/contexto-edicao/buscar`
  - Mantido cache por `codSubprocesso` e por chave `processo:unidade`, com invalidação explícita nas ações de workflow.

- **Novos achados de monitoramento (e2e/monitoramento-e2e.txt)**
  - Persistia alta recorrência de `GET /api/subprocessos/buscar` (70 chamadas no recorte), indicando custo de composição em cadeia.
  - `GET /api/subprocessos/{codigo}/contexto-edicao` aparece em rajadas por mesma jornada (ex.: código 400 com 33 chamadas).
  - `POST /api/subprocessos/{codigo}/cadastro/disponibilizar` segue como outlier pontual (amostra com pico ~152ms).
  - `POST /api/processos/{codigo}/acao-em-bloco` já aparece abaixo do outlier principal no recorte analisado.
- **Novo recorte da rodada (SGC_MONITORAMENTO=on + E2E cdu-24)**
  - Com `npx playwright install --only-shell` + `npx playwright install-deps`, a suíte `cdu-24` passou completa com monitoramento ligado.
  - Principais picos HTTP no recorte:
    - `POST /api/processos/{codigo}/acao-em-bloco` (~321ms),
    - `POST /api/usuarios/login` (pico ~188ms),
    - `GET /api/painel/bootstrap` (pico ~166ms),
    - `GET /api/subprocessos/{codigo}/contexto-edicao` (pico ~144ms).
  - Principais picos TRACE no recorte:
    - `EmailService.enviarEmailHtml` (~333ms),
    - `ProcessoService.executarAcaoEmBloco` (~296ms),
    - `SubprocessoTransicaoService.disponibilizarMapaEmBloco` (~284ms),
    - `SubprocessoNotificacaoService.notificarTransicao` (~259ms).
- **Nova coleta (rodada longa 2 — SGC_MONITORAMENTO=on + E2E cdu-24)**
  - Suíte executada novamente após otimizações de lookup por sigla.
  - Picos HTTP observados:
    - `POST /api/processos/{codigo}/acao-em-bloco` (~426ms),
    - `GET /api/subprocessos/{codigo}/contexto-edicao` (pico ~211ms),
    - `POST /api/usuarios/login` (pico ~176ms).
  - Picos TRACE observados:
    - `ProcessoService.executarAcaoEmBloco` (~389ms),
    - `SubprocessoTransicaoService.disponibilizarMapaEmBloco` (~372ms),
    - `SubprocessoNotificacaoService.notificarTransicao` (~339ms),
    - `EmailService.enviarEmailHtml` (~252ms).
- **Otimização aplicada após o recorte**
  - `UnidadeService.buscarCodigoPorSigla` foi introduzido para lookup enxuto (`codigo` somente), usando `UnidadeRepo.buscarCodigoAtivoPorSigla`.
  - Fluxos de alta frequência (controllers de subprocesso e fixtures E2E) deixaram de resolver `Unidade` completa quando só o código é necessário.
  - `UnidadeService.buscarCodigoPorSigla` passou a usar cache dedicado (`unidadeCodigoPorSigla`) com chave normalizada em maiúsculas para reduzir reconsulta por sigla.
  - Estratégia de volatilidade aplicada:
    - `buscarPorSigla` cacheia leitura estrutural (unidade + superior, sem responsabilidade), adequada para dados que mudam em ciclos longos;
    - `buscarPorSiglaComResponsavel` permanece sem cache dedicado para manter responsividade a substituições/afastamentos de chefia.

## Pendências prioritárias

## Bloco A — fluxo de leitura e composição de tela

### Item A.2 — reduzir sobreposição entre `contexto-completo` e `detalhes` (processo)

**Objetivo**

Eliminar sobreposição funcional e payload redundante no domínio de processo.

**Pendências restantes**

- fechar inventário de campos realmente consumidos por tela no fluxo de processo;
- separar payload de primeira renderização vs. carga sob demanda;
- propor contrato final:
  - manter dois endpoints com responsabilidades distintas **ou**
  - consolidar com DTO enxuto e campos opcionais por caso de uso.

**Saída esperada**

- tabela final de consumo por tela;
- recomendação de enxugamento de DTO/endpoint com impacto estimado.

## Bloco B — workflow com custo elevado

### Item B.1 — aprofundar tracing em `cadastro/disponibilizar`

**Objetivo**

Isolar com precisão o custo relativo de validações, persistência e notificações no fluxo de disponibilização.

**Pendências restantes**

- detalhar quebra de tempo por etapa interna em ambiente de homologação;
- identificar 2-3 simplificações de baixa complexidade para remover custo evitável (foco atual: notificação/e-mail no caminho crítico de `acao-em-bloco`);
- validar impacto após ajuste com novo recorte monitorado.

**Saída esperada**

- top 3 gargalos com evidência;
- lista de simplificações de implementação imediata.

### Item B.2 — matriz comparativa de ações de workflow

**Objetivo**

Comparar custo de `iniciar`, `acao-em-bloco`, `finalizar` e ações de subprocesso para priorização por impacto.

**Pendências restantes**

- consolidar matriz p50/p95/pico por ação com mesma janela de execução;
- publicar ordem de ataque da próxima rodada baseada em custo x frequência.

**Saída esperada**

- matriz de latência por ação;
- priorização objetiva da rodada seguinte.

## Próximas rodadas (remanescente)

### Rodada 6

- fechar A.2 (inventário de consumo + proposta de contrato para processo).

### Rodada 7

- executar B.1 com tracing interno por etapa e aplicar simplificações iniciais.

### Rodada 8

- consolidar B.2 com matriz comparativa final e plano de ataque por impacto.
