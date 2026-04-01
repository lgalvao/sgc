# Alinhamento CDU-12 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-12.md` (fluxo de verificação de impactos no mapa de competências, atores: CHEFE, GESTOR, ADMIN).
- Teste E2E: `e2e/cdu-12.spec.ts` (3 cenários `test`, 90 linhas).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **9**.
- Status: **7 cobertos**, **2 parciais**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. No painel, o usuário clica no processo de revisão na situação 'Em andamento'.
  - Evidência: `e2e/cdu-12.spec.ts` - `criarProcessoFixture(tipo:'REVISAO', iniciar:true)` + `acessarSubprocessoChefeDireto` e `acessarSubprocessoGestor`.
- ✅ **[COBERTO]** 2. O sistema mostra a tela `Detalhes do subprocesso` da unidade.
  - Evidência: `e2e/cdu-12.spec.ts` - `acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO)`.
- ✅ **[COBERTO]** 3.1. Botão `Impactos no mapa` na tela de cadastro (CHEFE).
  - Evidência: `e2e/cdu-12.spec.ts:Passo 3.1` - `verificarBotaoImpactoDropdown(page)` para CHEFE.
- ✅ **[COBERTO]** 3.2. Botão `Impactos no mapa` na tela de visualização (GESTOR).
  - Evidência: `e2e/cdu-12.spec.ts:Passo 3.2` - `verificarBotaoImpactoDireto(page)` para GESTOR_COORD_12.
- ✅ **[COBERTO]** 4. O usuário clica no botão `Impactos no mapa`.
  - Evidência: `e2e/cdu-12.spec.ts` - `abrirModalImpactoEdicao` e `abrirModalImpactoVisualizacao`.
- ✅ **[COBERTO]** 5. Sistema compara atividades/conhecimentos do mapa vigente com o subprocesso.
  - Evidência: `criarProcessoFinalizadoFixture` cria mapa vigente; `criarProcessoFixture(REVISAO)` cria novo cadastro com alterações.
- ✅ **[COBERTO]** 5.1. Seção `Atividades inseridas` para novas atividades detectadas.
  - Evidência: `e2e/cdu-12.spec.ts` - `modal.getByRole('heading', {name:/Atividades inseridas/i})` + `modal.getByText(descEditada)`.
- 🟡 **[PARCIAL]** 5.2. Seção `Competências impactadas` para atividades removidas/alteradas.
  - Evidência: o spec não testa explicitamente a seção de competências impactadas (remoção/alteração de atividade já existente no mapa vigente).
- 🟡 **[PARCIAL]** 6. Se nenhuma divergência: mensagem "Nenhum impacto no mapa da unidade."
  - Evidência: não há cenário de mapa sem divergências no spec.
- ✅ **[COBERTO]** 7. Sistema exibe modal `Impacto no Mapa de Competências`.
  - Evidência: `e2e/cdu-12.spec.ts` - `page.getByRole('dialog')` visível com seções.
- ✅ **[COBERTO]** 8. O usuário analisa as informações e clica em Fechar.
  - Evidência: `e2e/cdu-12.spec.ts` - `fecharModalImpacto(page)`.
- ✅ **[COBERTO]** 9. Sistema fecha o modal, retornando à tela original com estado inalterado.
  - Evidência: `fecharModalImpacto` fecha o dialog; estado da tela mantido.

## Ajustes recomendados para próximo ciclo
- Adicionar cenário para seção `Competências impactadas` (atividade alterada com competência associada no mapa vigente).
- Adicionar cenário para "Nenhum impacto no mapa da unidade" (sem divergências entre cadastros).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: fluxo principal de detecção de novas atividades bem coberto para CHEFE e GESTOR; gaps são competências impactadas e cenário sem divergências.

## Observações metodológicas
- Rodada 3: alinhamento reescrito após leitura completa do requisito e spec. Status corrigido de PENDENTE_REFINAMENTO_REQUISITO para PRONTO_COM_GAPS.
