# Backlog de racionalização (somente pendências atuais)

## Contexto

Este backlog mantém **apenas o que ainda falta executar** após as rodadas iniciadas.

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