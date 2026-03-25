# Alinhamento CDU-29 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-29.md`.
- Teste E2E: `e2e/cdu-29.spec.ts` (4 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **8**.
- Status: **8 cobertos**, **0 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. Em qualquer tela do sistema, na barra de navegação, usuário clica `Histórico`.
  - Palavras-chave usadas: `qualquer, barra, navegação, clica, histórico`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:9` -> `* 1. Na navbar, usuário clica em Histórico`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:16` -> `// CENÁRIO 1: Navegação para página de histórico`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:4` -> `* CDU-29 - Consultar histórico de processos`
- ✅ **[COBERTO]** 2. Sistema apresenta uma tabela com todos os processos com situação 'Finalizado', com:
  - Palavras-chave usadas: `processos, situação, apresenta, todos, finalizado`
  - Evidência (score 3): `e2e/cdu-29.spec.ts:10` -> `* 2. Sistema apresenta tabela de processos finalizados`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:48` -> `// CENÁRIO 4: Verificar estrutura da tabela de processos finalizados`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:4` -> `* CDU-29 - Consultar histórico de processos`
- ✅ **[COBERTO]** 3. `Processo`: Descrição do processo.
  - Palavras-chave usadas: `processo, descrição`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:60` -> `await expect(headers.filter({hasText: /Processo|Descrição/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:4` -> `* CDU-29 - Consultar histórico de processos`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:10` -> `* 2. Sistema apresenta tabela de processos finalizados`
- ✅ **[COBERTO]** 4. `Tipo`: Tipo do processo.
  - Palavras-chave usadas: `processo, tipo`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:57` -> `// Colunas esperadas: Processo, Tipo, Finalizado em, Unidades participantes`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:4` -> `* CDU-29 - Consultar histórico de processos`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:10` -> `* 2. Sistema apresenta tabela de processos finalizados`
- ✅ **[COBERTO]** 5. `Finalizado em`: Data de finalização do processo
  - Palavras-chave usadas: `processo, finalizado, data, finalização`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:10` -> `* 2. Sistema apresenta tabela de processos finalizados`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:48` -> `// CENÁRIO 4: Verificar estrutura da tabela de processos finalizados`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:57` -> `// Colunas esperadas: Processo, Tipo, Finalizado em, Unidades participantes`
- ✅ **[COBERTO]** 6. `Unidades participantes`: Lista de unidades participantes, agregando pelas unidades que tiverem todas as
  - Palavras-chave usadas: `unidades, participantes, lista, agregando, pelas, tiverem`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:57` -> `// Colunas esperadas: Processo, Tipo, Finalizado em, Unidades participantes`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:56` -> `// Verificar que a tabela ou lista está presente`
- ✅ **[COBERTO]** 7. Usuário clica em um processo para detalhamento.
  - Palavras-chave usadas: `processo, clica, detalhamento`
  - Evidência (score 3): `e2e/cdu-29.spec.ts:11` -> `* 3. Usuário clica em um processo para detalhamento`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:4` -> `* CDU-29 - Consultar histórico de processos`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:9` -> `* 1. Na navbar, usuário clica em Histórico`
- ✅ **[COBERTO]** 8. O sistema apresenta a página `Detalhes do processo`, sem permitir mudanças ou mostrar botões de ação.
  - Palavras-chave usadas: `processo, apresenta, página, detalhes, permitir, mudanças`
  - Evidência (score 3): `e2e/cdu-29.spec.ts:12` -> `* 4. Sistema apresenta Detalhes do processo sem botões de ação`
  - Evidência (score 2): `e2e/cdu-29.spec.ts:10` -> `* 2. Sistema apresenta tabela de processos finalizados`
  - Evidência (score 1): `e2e/cdu-29.spec.ts:4` -> `* CDU-29 - Consultar histórico de processos`

## Ajustes recomendados para próximo ciclo
- Manter suíte atual e adicionar apenas testes de regressão de estabilidade (flakiness e dados).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Motivos: base de análise e pendências objetivas definidas.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Manter suíte atual e adicionar apenas testes de regressão de estabilidade (flakiness e dados).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
