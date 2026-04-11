# Backlog de racionalização (pendências)

## Contexto

Este backlog mantém **apenas** o que ainda falta executar a partir do plano de racionalização.

## Estado atual resumido

- Painel (`/painel/processos` e `/painel/alertas`): frente estabilizada.
- Contexto de processo e subprocesso: cache de sessão e invalidação explícita já implantados nas telas principais.
- Diagnóstico organizacional e elegibilidade: frente estabilizada.

## Pendências prioritárias

## Bloco A — fluxo de leitura e composição de tela

### Item A.1 — consolidar sequência de abertura de processo/subprocesso

**Objetivo**

Reduzir round-trips na abertura de tela quando ainda há composição por múltiplas chamadas em cadeia.

**Escopo pendente**

- mapear sequência canônica de requests em produção para:
  - `GET /api/processos/{codigo}/contexto-completo`
  - `GET /api/subprocessos/buscar`
  - `GET /api/subprocessos/{codigo}/contexto-edicao`
- confirmar pontos em que a UI depende de dados que já existem no payload anterior.

**Saída esperada**

- grafo final por tela (detalhe de processo, detalhe de subprocesso, visualização de cadastro);
- proposta concreta de contrato consolidado (bootstrap por caso de uso) **ou** justificativa para manter endpoints separados.

### Item A.2 — revisar sobreposição entre `contexto-completo` e `detalhes`

**Objetivo**

Eliminar sobreposição funcional e payload redundante no domínio de processo.

**Escopo pendente**

- inventariar campos realmente consumidos por cada tela;
- separar o que é necessário na primeira renderização do que pode ser carregado sob demanda.

**Saída esperada**

- tabela de consumo por tela;
- recomendação de enxugamento de DTO/endpoint.

## Bloco B — workflow com custo elevado

### Item B.1 — abrir cadeia interna de `cadastro/disponibilizar`

**Objetivo**

Entender o outlier de latência e identificar simplificações de regra/persistência.

**Escopo pendente**

- rastrear validações, atualizações, notificações e recomputações;
- medir tempo relativo por etapa com tracing interno.

**Saída esperada**

- top 3 gargalos com evidência;
- lista de simplificações implementáveis em baixa complexidade.

### Item B.2 — medir ações de workflow correlatas

**Objetivo**

Comparar custo de `iniciar`, `acao-em-bloco`, `finalizar` e ações de subprocesso para priorização por impacto.

**Saída esperada**

- matriz de latência por ação;
- ordem de ataque para próxima rodada.

## Próximas rodadas (execução)

### Rodada 3 (em andamento)

- concluir mapeamento de composição de requests por tela (Bloco A.1);
- fechar inventário de sobreposição entre `contexto-completo` e `detalhes` (Bloco A.2).

### Rodada 4

- atacar outlier `cadastro/disponibilizar` com tracing e simplificações iniciais (Bloco B.1).

### Rodada 5

- consolidar medições comparativas das ações de workflow e priorizar nova leva de refatorações (Bloco B.2).
