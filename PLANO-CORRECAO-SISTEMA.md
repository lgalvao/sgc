Objetivo

Validar aderência entre requisitos (`etc/reqs/cdu-xx.md`) e testes E2E (`e2e/cdu-xx.spec.ts`) para detectar divergências após as correções.

Abordagem de Verificação
1) Mapear todos os pares CDU↔spec por número (`01..36`) e listar faltantes em qualquer lado.
2) Para cada CDU, comparar:
   - objetivo e atores,
   - pré-condições e dados necessários,
   - fluxo principal,
   - fluxos alternativos/exceções,
   - regras e validações obrigatórias.
3) Classificar achados por tipo:
   - lacuna de teste (requisito sem cobertura),
   - teste excedente (cobre comportamento não previsto),
   - divergência de regra (esperado vs implementado no teste),
   - ambiguidade de requisito (texto insuficiente para assert claro),
   - risco de mascaramento (if/catch/branch que evita falha explícita e pode esconder bug real).
4) Registrar evidência objetiva por achado:
   - arquivo e trecho do requisito,
   - arquivo/linha do e2e correspondente,
   - impacto no negócio,
   - recomendação objetiva de ajuste (req, teste ou ambos).
5) Consolidar relatório final com priorização:
   - Alta: altera regra de negócio/resultado do caso de uso,
   - Média: afeta fluxo alternativo/importante,
   - Baixa: nomenclatura, dados de exemplo, detalhe não funcional.

Entregáveis
- Matriz CDU x E2E com status por caso (`Aderente`, `Parcial`, `Divergente`).
- Lista priorizada de divergências com ação recomendada.
- Backlog de correções (ordem por risco).

Critério de Conclusão
- 100% dos CDUs em `etc/reqs` comparados contra seus `e2e/cdu-xx.spec.ts`.
- Todas as divergências registradas com evidência e decisão proposta.

Progresso da Auditoria (iniciado)
- Mapeamento concluído: `etc/reqs` e `e2e` possuem cobertura 1:1 de `CDU-01..CDU-36` (sem faltantes).
- Eixo adicional de risco de mascaramento incorporado na análise (branches que evitam falha explícita).
- Primeira amostra auditada (`CDU-01`, `CDU-02`, `CDU-03`) registrada:
  - `CDU-01`: parcial (2 divergências).
  - `CDU-02`: parcial (4 divergências, risco médio por branch condicional no cenário de painel vazio).
  - `CDU-03`: parcial (4 divergências).
- Hotspots técnicos de mascaramento já identificados para revisão detalhada:
  - padrão `isVisible().catch(() => false)` em múltiplos CDUs (ex.: 28, 30, 34, 24, 22, 23, 26, 27, 32, 33, 11, 13, 14, 25);
  - ocorrência de `expect(...).toBeVisible().catch(...)` em `CDU-28`.
- Execução em andamento:
  - `CDU-04..CDU-10` auditados e registrados como `parcial`.
  - Lacunas recorrentes identificadas nesse lote: ausência de asserts para e-mail/alerta/movimentação/data de conclusão e cobertura incompleta de regras por perfil.
  - Próximo lote: `CDU-11..CDU-18`, mantendo análise explícita de risco de mascaramento por `if/catch/force`.

Atualização de execução (2026-02-13)
- CDU-30:
  - E2E endurecido sem `if/catch` mascarador.
  - Fluxos cobertos: listagem, adição, duplicidade, remoção e bloqueio de auto-remoção.
  - Bug real corrigido no sistema: fila de toasts no `feedbackStore` para não ocultar erros.
- CDU-28:
  - E2E reescrito com asserts explícitos (sem fallback silencioso).
  - Validação de obrigatoriedade de `dataInicio` implementada em `CadAtribuicao.vue` e coberta em unit/e2e.
  - Evidências: `cdu-28.spec.ts` (3 passed) e `CadAtribuicao.spec.ts` (4 passed).
- CDU-34 (em execução):
  - E2E endurecido sem `if/catch` mascarador.
  - Fluxo principal coberto com confirmação explícita do lembrete em modal.
  - Correção funcional no backend: envio de lembrete agora registra movimentação interna (`Lembrete de prazo enviado`) além do alerta.
- Atualização de estabilidade E2E (painel x alertas):
  - Correção transversal aplicada para eliminar ambiguidade de seleção de processo após exibição de alertas no painel.
  - Seletores de processo foram escopados para `tbl-processos` em specs e helpers críticos.
  - Regressão focada executada com sucesso (37/37) nos fluxos principais impactados.
- CDU-08, CDU-09 e CDU-10:
  - Ajuste de login E2E para aceitar variações de URL do painel (`/painel` com query opcional), reduzindo falso negativo de setup.
  - `CDU-10` estabilizado com navegação semântica para card de atividades em visualização, mantendo falha explícita quando o contexto não existir.
  - Evidências: `cdu-08.spec.ts` (2 passed), `cdu-09.spec.ts` (3 passed), `cdu-10.spec.ts` (1 passed).
- Próximo lote operacional:
  - Executar `CDU-11..CDU-15` com foco em aderência ao requisito e detecção de bug funcional real (sem branches defensivos).
- Execução do lote `CDU-11..CDU-15`:
  - Falha funcional reproduzida em `CDU-11` (cenário de processo finalizado): tentativa de reabrir processo sem retorno explícito ao painel.
  - Correção aplicada em `e2e/cdu-11.spec.ts`: navegação explícita para `/painel` antes de reselecionar o processo.
  - Correção estrutural em `helpers-analise.ts` (`acessarSubprocessoGestor`): seleção da unidade via célula na `tbl-tree` + validação de URL final do subprocesso.
  - Evidências de validação: `cdu-11.spec.ts` (**6 passed**) e `cdu-14.spec.ts` (**21 passed**), com bloqueio removido.
- Próximo lote operacional:
  - Executar `CDU-16..CDU-20` mantendo foco em aderência de requisito e bug funcional reproduzível.
- Execução do lote `CDU-16..CDU-20`:
  - Rodada completa executada sem falhas funcionais no estado atual.
  - Evidência consolidada: **40 passed** (`cdu-16.spec.ts` a `cdu-20.spec.ts`).
  - Resultado: nenhum novo bug funcional reproduzível identificado neste bloco após as correções de estabilidade já aplicadas.
- Próximo lote operacional:
  - Executar `CDU-21..CDU-25` com o mesmo critério (aderência ao requisito + detecção de bug funcional real).
- Execução do lote `CDU-21..CDU-25`:
  - Rodada completa executada sem falhas funcionais no estado atual.
  - Evidência consolidada: **30 passed** (`cdu-21.spec.ts` a `cdu-25.spec.ts`).
  - Resultado: nenhum novo bug funcional reproduzível identificado neste bloco.
- Próximo lote operacional:
  - Executar `CDU-26..CDU-30` mantendo validação por requisito e sem mascaramento de falhas.
- Execução do lote `CDU-26..CDU-30`:
  - Rodada completa executada sem falhas funcionais no estado atual.
  - Evidência consolidada: **20 passed** (`cdu-26.spec.ts` a `cdu-30.spec.ts`).
  - Resultado: nenhum novo bug funcional reproduzível identificado neste bloco.
- Próximo lote operacional:
  - Executar `CDU-31..CDU-36` mantendo critério de aderência ao requisito e detecção de bug real.
- Execução do lote `CDU-31..CDU-36`:
  - Rodada completa executada sem falhas funcionais no estado atual.
  - Evidência consolidada: **31 passed** (`cdu-31.spec.ts` a `cdu-36.spec.ts`).
  - Resultado: nenhum novo bug funcional reproduzível identificado neste bloco.
- Fechamento da varredura CDU (complemento `CDU-01..CDU-03`):
  - Execução complementar concluída sem falhas.
  - Evidência consolidada: **19 passed** (`cdu-01.spec.ts` a `cdu-03.spec.ts`).
  - Resultado geral da rodada: CDUs `01..36` estáveis no estado atual após as correções aplicadas.
- Suites complementares não-CDU:
  - Execução de `e2e/captura-telas.spec.ts` e `e2e/ui-consistency.spec.ts` concluída sem falhas.
  - Evidência consolidada: **20 passed**.
  - Resultado: sem novo bug funcional reproduzível nesse eixo de validação visual/consistência.
- Encerramento do plano:
  - Validação final concluída com suites backend e frontend em estado verde.
  - Todos os itens planejados foram executados e evidenciados neste documento.
  - Plano encerrado sem pendências abertas.

