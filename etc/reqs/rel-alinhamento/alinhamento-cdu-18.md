# Alinhamento CDU-18 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-18.md`.
- Teste E2E: `e2e/cdu-18.spec.ts` (2 cenários `test`, 9 `test.step`).
- Contextos `describe`: CDU-18: Visualizar mapa de competências.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **15**.
- Status: **14 cobertos**, **1 parcial**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. No `Painel`, o usuário clica no processo de mapeamento ou revisão na situação 'Em andamento' ou 'Finalizado'.
  - Evidência: `e2e/cdu-18.spec.ts` - usa dados do seed (Processo 99 finalizado) + `acessarDetalhesProcesso`.
- ✅ **[COBERTO]** 2. Se perfil ADMIN ou GESTOR: exibe tela `Detalhes do processo` com unidades participantes.
  - Evidência: `e2e/cdu-18.spec.ts:Cenário 1` - ADMIN acessa e navega pela tabela de unidades.
- ✅ **[COBERTO]** 3. ADMIN/GESTOR clica em unidade e acessa `Detalhes do subprocesso`.
  - Evidência: `e2e/cdu-18.spec.ts:Cenário 1` - `getByRole('row', {name: /ASSESSORIA_/})` + click.
- ✅ **[COBERTO]** 4. Se perfil CHEFE ou SERVIDOR: vai direto para `Detalhes do subprocesso` da própria unidade.
  - Evidência: `e2e/cdu-18.spec.ts:Cenário 2` - CHEFE_ASSESSORIA_12 → URL `/processo/\d+/ASSESSORIA_12$`.
- ✅ **[COBERTO]** 5. O usuário clica no card `Mapa de competências`.
  - Evidência: `e2e/cdu-18.spec.ts` - `navegarParaMapa`.
- ✅ **[COBERTO]** 6. Sistema mostra tela `Visualização de mapa` com título "Mapa de competências técnicas".
  - Evidência: `e2e/cdu-18.spec.ts` - `TEXTOS.mapa.TITULO_TECNICO` visível.
- ✅ **[COBERTO]** 7. Identificação da unidade (sigla) no cabeçalho.
  - Evidência: `e2e/cdu-18.spec.ts` - `subprocesso-header__txt-header-unidade` com `/ASSESSORIA_\d+/` e `'ASSESSORIA_12'`.
- ✅ **[COBERTO]** 8. Competências do mapa exibidas.
  - Evidência: `e2e/cdu-18.spec.ts` - `'Competência técnica seed 99'` visível.
- ✅ **[COBERTO]** 9. Atividades associadas a cada competência exibidas.
  - Evidência: `e2e/cdu-18.spec.ts` - `'Atividade seed 1'` e `'Atividade seed 2'` visíveis.
- ✅ **[COBERTO]** 10. Conhecimentos das atividades exibidos.
  - Evidência: `e2e/cdu-18.spec.ts` - `'Conhecimento seed 1.1'` e `'Conhecimento seed 2.1'` visíveis.
- ✅ **[COBERTO]** 11. Tela é de somente leitura (sem botões de edição).
  - Evidência: sem `btn-abrir-criar-competencia` ou `btn-cad-mapa-disponibilizar` na visualização.
- ✅ **[COBERTO]** 12. Perfil ADMIN visualiza mapa de unidade não diretamente subordinada.
  - Evidência: `e2e/cdu-18.spec.ts:Cenário 1` - ADMIN acessa `ASSESSORIA_12`.
- ✅ **[COBERTO]** 13. Perfil CHEFE visualiza mapa da própria unidade.
  - Evidência: `e2e/cdu-18.spec.ts:Cenário 2` - CHEFE_ASSESSORIA_12 visualiza `ASSESSORIA_12`.
- ✅ **[COBERTO]** 14. URL do subprocesso correto após navegação.
  - Evidência: `e2e/cdu-18.spec.ts` - `page.toHaveURL(/\/processo\/\d+\/ASSESSORIA_12$/)`.
- 🟡 **[PARCIAL]** 15. Visualização de mapa em processo 'Em andamento' (não finalizado).
  - Evidência: spec usa apenas Processo 99 (finalizado); não há cenário com processo em andamento para CDU-18.

## Ajustes recomendados para próximo ciclo
- Adicionar cenário com processo em andamento para cobrir visualização de mapa não finalizado.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: cobertura muito alta; único gap é visualização em processo Em andamento.

## Observações metodológicas
- Rodada 3: alinhamento revisado. Nenhuma alteração no spec foi necessária; cobertura já estava em 14/15.
