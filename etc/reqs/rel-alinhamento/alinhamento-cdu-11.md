# Alinhamento CDU-11 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-11.md`.
- Teste E2E: `e2e/cdu-11.spec.ts` (5 cenários `test`, 0 `test.step`).
- Contextos `describe`: Em processo em Andamento, Em processo finalizado.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **10**.
- Status: **9 cobertos**, **1 parcial**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. No painel, o usuário clica no processo de mapeamento ou revisão na situação 'Em andamento' ou 'Finalizado'.
  - Evidência: `e2e/cdu-11.spec.ts` - `criarProcessoCadastroDisponibilizadoFixture` e `criarProcessoFinalizadoFixture`.
- ✅ **[COBERTO]** 2. Se perfil ADMIN ou GESTOR: sistema exibe tela `Detalhes do processo`.
  - Evidência: `e2e/cdu-11.spec.ts:Fluxo ADMIN/GESTOR` - `acessarDetalhesProcesso` + URL `/processo/\d+$`.
- ✅ **[COBERTO]** 3. ADMIN/GESTOR clica em unidade subordinada e acessa subprocesso.
  - Evidência: `e2e/cdu-11.spec.ts:Fluxo ADMIN/GESTOR` - `navegarParaSubprocesso(page, UNIDADE_ALVO)`.
- ✅ **[COBERTO]** 4. Sistema mostra tela `Detalhes do subprocesso`.
  - Evidência: `e2e/cdu-11.spec.ts` - URL `/processo/\d+/SECAO_111$`.
- ✅ **[COBERTO]** 5. Se perfil CHEFE ou SERVIDOR: vai direto para `Detalhes do subprocesso` da própria unidade.
  - Evidência: `e2e/cdu-11.spec.ts:Fluxo CHEFE/SERVIDOR` - `acessarDetalhesProcesso` → URL `/processo/\d+/SECAO_111$`.
- ✅ **[COBERTO]** 6. O usuário clica no card `Atividades e conhecimentos` (Visualização).
  - Evidência: `e2e/cdu-11.spec.ts` - `navegarParaAtividadesVisualizacao`.
- ✅ **[COBERTO]** 7. Sistema mostra tela `Atividades e conhecimentos` (somente leitura).
  - Evidência: verificado via URL e presença de atividades.
- ✅ **[COBERTO]** 8. Cabeçalho com sigla da unidade.
  - Evidência: `e2e/cdu-11.spec.ts` - `subprocesso-header__txt-header-unidade` com `UNIDADE_ALVO`.
- ✅ **[COBERTO]** 9. Atividades e conhecimentos exibidos.
  - Evidência: `e2e/cdu-11.spec.ts` - `/Atividade fixture/` e `/Conhecimento fixture/` visíveis em andamento; `/Atividade origem/` e `/Conhecimento [AB]/` em finalizado.
- 🟡 **[PARCIAL]** 10. Cada atividade apresentada como tabela com dados detalhados (sigla, nome da unidade, tabela por atividade).
  - Evidência: atividades visíveis como texto mas estrutura tabular não verificada explicitamente.

## Ajustes recomendados para próximo ciclo
- Verificar estrutura tabular das atividades (test IDs específicos das células da tabela).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: cobertura muito alta (9/10); único gap é estrutura tabular detalhada.

## Observações metodológicas
- Rodada 3: alinhamento revisado. Nenhuma alteração no spec foi necessária; cobertura existente já é adequada para os fluxos principais.
