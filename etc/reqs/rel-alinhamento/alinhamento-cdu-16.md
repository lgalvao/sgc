# Alinhamento CDU-16 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-16.md`.
- Teste E2E: `e2e/cdu-16.spec.ts` (2 cenários `test`, 4 `test.step`).
- Contextos `describe`: CDU-16 - Ajustar mapa de competências.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **11**.
- Status: **9 cobertos**, **2 parciais**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. No `Painel`, o usuário escolhe o processo de revisão desejado.
  - Evidência: `e2e/cdu-16.spec.ts` - `criarProcessoRevisaoCadastroHomologadoFixture` + painel via fixture.
- ✅ **[COBERTO]** 2. O sistema mostra tela `Detalhes do processo`.
  - Evidência: `e2e/cdu-16.spec.ts` - `acessarSubprocessoAdmin` com navegação ao processo.
- ✅ **[COBERTO]** 3. O usuário clica em unidade com subprocesso 'Revisão do cadastro homologada' ou 'Mapa ajustado'.
  - Evidência: `e2e/cdu-16.spec.ts` - `acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO)`.
- ✅ **[COBERTO]** 4. O sistema mostra a tela `Detalhes do subprocesso`.
  - Evidência: `e2e/cdu-16.spec.ts` - URL `/processo/\d+/SECAO_211$`.
- ✅ **[COBERTO]** 5. O usuário clica no card `Mapa de Competências`.
  - Evidência: `e2e/cdu-16.spec.ts` - `navegarParaMapa`.
- ✅ **[COBERTO]** 6. O sistema mostra a tela `Edição de mapa` com botões `Impactos no mapa` e `Disponibilizar`.
  - Evidência: `e2e/cdu-16.spec.ts:Cenário 1` - `cad-mapa__btn-impactos-mapa` e `btn-cad-mapa-disponibilizar` visíveis; competências fixtures presentes.
- ✅ **[COBERTO]** 7. O usuário clica em `Impactos no mapa`.
  - Evidência: `e2e/cdu-16.spec.ts:Cenário 2` - `cad-mapa__btn-impactos-mapa` clicado.
- ✅ **[COBERTO]** 8. O sistema mostra o modal `Impactos no mapa` com seções de atividades e competências impactadas.
  - Evidência: `e2e/cdu-16.spec.ts:Cenário 2` - `modal-impacto-body` + `TEXTOS.mapa.impacto.ATIVIDADES_INSERIDAS` + `TEXTOS.mapa.impacto.COMPETENCIAS_IMPACTADAS`.
- ✅ **[COBERTO]** 9. O usuário ajusta o mapa (editar, criar novas competências).
  - Evidência: `e2e/cdu-16.spec.ts:Cenário 3 e 4` - edição de competência + criação de nova.
- 🟡 **[PARCIAL]** 9.1. Associar todas as atividades não associadas a uma competência.
  - Evidência: `Cenário 4` cria competência com `atividadeNovaRevisao` mas sem verificar que todas as atividades estão associadas.
- 🟡 **[PARCIAL]** 9.2. Clicar em `Disponibilizar` ao concluir ajustes.
  - Evidência: `btn-cad-mapa-disponibilizar` está visível mas não é clicado no spec atual (fluxo de disponibilização delegado ao CDU-17).

## Ajustes recomendados para próximo ciclo
- Adicionar cenário que clica em `Disponibilizar` e verifica o fluxo de disponibilização do mapa ajustado.
- Verificar que todas as atividades foram associadas antes de disponibilizar.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: fluxo de visualização de impactos e ajuste de competências bem cobertos; gap é o fluxo completo até disponibilização.

## Observações metodológicas
- Rodada 3: alinhamento revisado. Nenhuma alteração no spec foi necessária nesta rodada; cobertura melhorou de 7/11 para 9/11 com base na leitura mais precisa do spec existente.
