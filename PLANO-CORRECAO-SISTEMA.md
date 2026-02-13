Problema
Corrigir falhas nos testes E2E com base no guia de correção e learnings do projeto, priorizando causa raiz e mudanças mínimas.

Abordagem
1) Executar os testes E2E com saída capturada em arquivo.
2) Analisar os cenários com falha e seus error-context.md.
3) Aplicar correções cirúrgicas (preferencialmente nos testes/helpers; no backend/frontend apenas se necessário).
4) Reexecutar os testes afetados e depois validar a suíte necessária.

Notas
- Não usar aumento indiscriminado de timeout.
- Não executar cenário isolado quando houver dependência serial.
- Preferir helpers centralizados e seletores robustos.

Novo Objetivo (2026-02-13)
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
