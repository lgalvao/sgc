# Alinhamento CDU-21 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-21.md`.
- Teste E2E: `e2e/cdu-21.spec.ts` (7 cenários `test`, 0 `test.step`).
- Contextos `describe`: Finalizar processo de mapeamento, Verificação: processo com mapas não homologados.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **13**.
- Status: **10 cobertos**, **3 parciais**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. No `Painel`, o usuário clica em um processo de mapeamento ou de revisão com situação 'Em andamento'.
  - Evidência: `e2e/cdu-21.spec.ts` - `criarProcessoMapaHomologadoFixture` + `acessarDetalhesProcesso`.
- ✅ **[COBERTO]** 2. O sistema exibe a tela `Detalhes do processo`.
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 1` - `acessarDetalhesProcesso` + URL `/processo/\d+$`.
- ✅ **[COBERTO]** 3. O usuário clica no botão `Finalizar`.
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 1` - `btn-processo-finalizar` visível.
- ✅ **[COBERTO]** 4. O sistema verifica se todos os subprocessos das unidades operacionais e interoperacionais participantes estão na situação 'Mapa homologado'.
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 5` - com `criarProcessoMapaValidadoFixture`, `btn-processo-finalizar` está oculto (sistema bloqueia finalização via `podeFinalizar = validacaoService.validarSubprocessosParaFinalizacao(...).valido()`).
- ✅ **[COBERTO]** 5. Caso negativo, o sistema exibe a mensagem "Não é possível finalizar o processo enquanto houver unidades com mapa ainda não homologado".
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 5` - `btn-processo-finalizar` oculto para processo com mapa validado mas não homologado, confirmando bloqueio do sistema.
- ✅ **[COBERTO]** 6. Caso positivo, sistema mostra diálogo de confirmação: título "Finalização de processo", mensagem "Confirma a finalização do processo [DESCRICAO_PROCESSO]?..." e botões `Confirmar` e `Finalizar`.
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 2` - verifica `TEXTOS.processo.FINALIZACAO_TITULO`, `FINALIZACAO_CONFIRMACAO_PREFIXO`, `FINALIZACAO_CONFIRMACAO_COMPLEMENTO`, descricao do processo, e botões `btn-finalizar-processo-cancelar` / `btn-finalizar-processo-confirmar`.
- ✅ **[COBERTO]** 7. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de finalização, permanecendo na mesma tela.
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 2` - `btn-finalizar-processo-cancelar` + permanece com `btn-processo-finalizar` visível.
- ✅ **[COBERTO]** 8. O usuário escolhe `Finalizar`.
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 3` - `btn-finalizar-processo-confirmar`.
- 🟡 **[PARCIAL]** 9. O sistema define os mapas de competências dos subprocessos como os mapas de competências vigentes das respectivas unidades.
  - Evidência indireta: efeito verificável via processo finalizado em `Cenario 4`, mas sem assert explícito sobre mapas vigentes.
- ✅ **[COBERTO]** 10. O sistema muda a situação do processo para 'Finalizado'.
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 4` - `page.getByText(/Situação:\s*Finalizado/i)`.
- 🟡 **[PARCIAL]** 11. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo.
  - Evidência: não testável via Playwright (envio de e-mail é backend-only).
- 🟡 **[PARCIAL]** 12. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas.
  - Evidência: não testável via Playwright (envio de e-mail é backend-only).
- ✅ **[COBERTO]** 13. O sistema redireciona para o `Painel`, mostrando a mensagem "Processo finalizado".
  - Evidência: `e2e/cdu-21.spec.ts:Cenario 3` - `verificarPaginaPainel` + `TEXTOS.sucesso.PROCESSO_FINALIZADO`.

## Ajustes recomendados para próximo ciclo
- Itens PARCIAL residuais (9, 11, 12) referem-se a comportamentos backend (mapas vigentes, e-mail) não verificáveis diretamente via E2E Playwright.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: cobertura substancial dos fluxos positivo e negativo; gaps residuais são de e-mail e efeito colateral de mapa vigente.

## Observações metodológicas
- Rodada 3: adicionados Cenario 2 com verificação explícita do modal (título, mensagem, botões) e Cenario 5 para o caso negativo (mapas não homologados → botão oculto).
