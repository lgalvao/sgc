# Plano de Melhorias E2E

## Objetivo

Elevar a confiabilidade, previsibilidade e manutenibilidade da suíte E2E, reduzindo flakiness e acoplamento entre cenários.

## Diagnóstico Resumido

- Alto acoplamento por uso extensivo de `test.describe.serial`.
- Reset de banco por arquivo (não por teste), quebrando isolamento.
- Uso frequente de `waitForTimeout`.
- Extração de código de processo inconsistente em alguns cenários.
- Fixtures com URL hardcoded para backend.
- Uso de `force: true` em interações críticas.

## Princípios de Execução

- Priorizar estabilização antes de otimização de tempo de execução.
- Evitar mudanças massivas em um único PR.
- Garantir evidência de melhoria com métricas por etapa.
- Manter nomenclatura, mensagens e documentação em português.

## Fase 1 - Estabilização Imediata (P1)

1. Corrigir captura de código de processo nos cenários de revisão.
2. Padronizar extração de código usando helper único (`extrairProcessoId`).
3. Remover `waitForTimeout` da suíte `smoke.spec.ts`.
4. Substituir waits fixos por esperas explícitas de UI/rede/URL.
5. Garantir cleanup consistente para processos criados em cada teste.

### Critério de conclusão da Fase 1

- `smoke.spec.ts` sem `waitForTimeout`.
- Zero falhas por cleanup ausente nos cenários revisados.
- Execução local consecutiva (3x) sem flakiness nos arquivos alterados.

## Fase 2 - Isolamento e Independência de Testes (P1/P2)

1. Ajustar fixture `complete-fixtures` para reset por teste (ou estratégia equivalente isolada).
2. Separar claramente:
   - testes de fluxo encadeado (integração longa),
   - testes independentes de regressão.
3. Reduzir uso de `describe.serial` nos módulos de maior retorno (`cdu-05`, `cdu-11`, `cdu-14`).
4. Criar fixtures de preparação via API para reduzir setup por UI quando possível.

### Critério de conclusão da Fase 2

- Redução de pelo menos 40% de blocos `serial` nos módulos priorizados.
- Testes migrados executando isoladamente sem depender de ordem.

## Fase 3 - Robustez de Seletores e Interações (P2)

1. Mapear e remover `force: true` onde for possível.
2. Substituir seletores frágeis por `data-testid` e `getByRole` sem ambiguidades.
3. Revisar helpers para encapsular condições de prontidão (modal aberto, botão habilitado, tabela carregada).

### Critério de conclusão da Fase 3

- Redução mínima de 70% do uso de `force: true`.
- Nenhum helper central com seletor baseado em estrutura visual instável sem fallback robusto.

## Fase 4 - Configuração e Portabilidade (P2/P3)

1. Remover URLs hardcoded (`http://localhost:10000`) das fixtures.
2. Centralizar endpoints E2E via configuração de ambiente.
3. Documentar estratégia de execução local/CI com variáveis necessárias.

### Critério de conclusão da Fase 4

- Fixtures funcionando com backend em porta configurável.
- Execução em CI sem necessidade de ajuste manual de URL no código.

## Métricas de Qualidade

- Taxa de sucesso em execução repetida (10 execuções seguidas).
- Quantidade de `waitForTimeout`.
- Quantidade de `describe.serial`.
- Quantidade de `force: true`.
- Tempo médio de execução da suíte principal.

## Sequência Recomendada de Entrega

1. PR 1: Correções críticas de estabilidade (Fase 1).
2. PR 2: Isolamento de fixtures e redução de serial em 1-2 arquivos críticos.
3. PR 3: Robustez de helpers e seletores.
4. PR 4: Portabilidade de configuração e documentação final.

## Riscos e Mitigações

- Risco: quebra de cenários legados ao remover serial.
  - Mitigação: migração incremental por arquivo e execução repetida antes de merge.
- Risco: aumento temporário do tempo de suíte com reset por teste.
  - Mitigação: uso de fixtures API para setup rápido e paralelização posterior.
- Risco: falso positivo ao remover `force: true`.
  - Mitigação: adicionar esperas explícitas de estado interativo antes do clique.

## Próximo Passo Imediato

Executar a Fase 1 com foco em:

1. `e2e/cdu-05.spec.ts`
2. `e2e/smoke.spec.ts`
3. `e2e/helpers/*` relacionados a waits/interações
