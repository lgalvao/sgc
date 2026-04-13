# Backlog de racionalização (pendências + avanço da rodada atual)

## Contexto

Este backlog mantém o que ainda falta executar e registra os achados da rodada corrente para orientar a próxima medição.

## Pendências prioritárias

## Bloco A — fluxo de leitura e composição de tela

### Item A.2 — reduzir sobreposição entre `contexto-completo` e `detalhes` (processo)

**Objetivo**

Eliminar sobreposição funcional e payload redundante no domínio de processo.

**Achados consolidados (rodada 6)**

- Inventário de consumo por tela (fluxo de processo):

| Tela / fluxo | Endpoint atual | Campos efetivamente usados |
|---|---|---|
| `ProcessoCadastroView` (edição de processo em `CRIADO`) | `GET /processos/{codigo}/detalhes` | `codigo`, `descricao`, `tipo`, `situacao`, `dataLimite`, `unidades` |
| `ProcessoDetalheView` (processo em andamento + ações em bloco) | `GET /processos/{codigo}/contexto-completo` | `codigo`, `descricao`, `tipo`, `situacao`, `unidades`, `podeFinalizar`, `acoesBloco`, `elegiveis` |

- Sobreposição identificada: `detalhes` e `contexto-completo` compartilhavam montagem de elegibilidade/ações em bloco no backend, mesmo quando a tela consumidora não precisava.
- Refatoração aplicada:
  - `obterDetalhesCompleto(..., incluirElegiveis=false)` agora **não** calcula `elegiveis` nem `acoesBloco`;
  - `contexto-completo` segue com cálculo completo (ações + elegíveis) quando `incluirElegiveis=true`.

**Pendências restantes**

- coletar ganho real de latência/p95 em homologação para `GET /processos/{codigo}/detalhes`;
- avaliar se `contexto-completo` deve ser quebrado em carga incremental (`acoes-bloco` sob demanda) na rodada seguinte.

**Saída esperada**

- tabela final de consumo por tela;
- recomendação de enxugamento de DTO/endpoint com impacto estimado.

## Bloco B — workflow com custo elevado

### Item B.1 — aprofundar tracing em `cadastro/disponibilizar`

**Objetivo**

Isolar com precisão o custo relativo de validações, persistência e notificações no fluxo de disponibilização.

**Achados consolidados (rodada 7 e extensão da rodada 9)**

- Tracing interno adicionado ao fluxo de disponibilização de mapa com log por etapa:
  - `carregar-subprocesso`
  - `validar-situacao`
  - `validar-regras-mapa`
  - `validar-associacoes-mapa`
  - `registrar-transicao`
- O tracing detalhado respeita o monitoramento por requisição (header `X-Monitoramento-Ativo: true`), evitando ruído fora de sessão investigativa.
- Tracing de workflow foi ampliado para separar explicitamente:
  - `persistir-transicao` (movimentação + save de subprocesso);
  - `notificar-transicao` (notificação/efeitos derivados).
- Simplificações aplicadas no caminho de `acao-em-bloco` (disponibilização):
  1. reuso dos `Subprocesso` já carregados no `ProcessoService` (remoção de recarga redundante por código);
  2. preservação de um único `Usuario` para lote inteiro;
  3. instrumentação pronta para separar custo de validação vs. transição/notificação.

**Pendências restantes**

- executar coleta em homologação com janela única e comparar antes/depois;
- validar se `registrar-transicao` concentra custo por efeito colateral (alerta/e-mail) para decisão de desacoplamento.

**Saída esperada**

- top 3 gargalos com evidência;
- lista de simplificações de implementação imediata.

### Item B.2 — matriz comparativa de ações de workflow

**Objetivo**

Comparar custo de `iniciar`, `acao-em-bloco`, `finalizar` e ações de subprocesso para priorização por impacto.

**Achados consolidados (rodada 8 + rodada 9)**

- Contrato mínimo para medição comparável fechado:
  - mesma janela temporal;
  - mesmo perfil de usuário;
  - monitoramento HTTP + tracing interno ativados;
  - agrupamento por ação (`iniciar`, `acao-em-bloco`, `finalizar`, ações de subprocesso).
- `acao-em-bloco` passou a emitir tracing por etapa para permitir matriz comparável no mesmo request:
  - `carregar-subprocessos`
  - `validar-selecao`
  - `executar-disponibilizacao-mapa-bloco` ou `executar-analise-bloco`
- Matriz final ainda depende de execução monitorada em homologação.

**Pendências restantes**

- consolidar matriz p50/p95/pico por ação com a nova instrumentação;
- publicar ordem de ataque baseada em custo x frequência observada.

**Saída esperada**

- matriz de latência por ação;
- priorização objetiva da rodada seguinte.

## Próximas rodadas (remanescente)

### Rodada 6 (executada)

- inventário de consumo por tela consolidado;
- contrato de `detalhes` enxugado sem cálculo de bloco/elegíveis.

### Rodada 7 (executada)

- tracing interno por etapa implantado em disponibilização de mapa;
- simplificação de recarga redundante no fluxo de disponibilização em bloco aplicada.

### Rodada 8 (parcial)

- protocolo de coleta comparável fechado;
- matriz final pendente de coleta em homologação.

### Rodada 9 (executada)

- tracing de `acao-em-bloco` por etapa instrumentado no backend;
- tracing de `registrarTransicao` quebrado em persistência vs notificação;
- backlog atualizado para orientar coleta final p50/p95/pico com segmentação de custo interno.
