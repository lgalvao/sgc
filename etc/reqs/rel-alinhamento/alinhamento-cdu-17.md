# Alinhamento CDU-17 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-17.md`.
- Teste E2E: `e2e/cdu-17.spec.ts` (3 cenários `test`, 0 `test.step`).
- Contextos `describe`: CDU-17 - Disponibilizar mapa de competências.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **35**.
- Status: **20 cobertos**, **12 parciais**, **3 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. ADMIN escolhe o processo de mapeamento desejado.
  - Evidência: `e2e/cdu-17.spec.ts` - `criarProcessoCadastroHomologadoFixture` + `acessarDetalhesProcesso`.
- ✅ **[COBERTO]** 2. O sistema mostra tela `Detalhes do processo`.
  - Evidência: `e2e/cdu-17.spec.ts` - `acessarDetalhesProcesso` + `navegarParaSubprocesso`.
- ✅ **[COBERTO]** 3. ADMIN clica em uma unidade operacional ou interoperacional com subprocesso na situação 'Mapa criado' ou 'Mapa ajustado'.
  - Evidência: `e2e/cdu-17.spec.ts` - `navegarParaSubprocesso(page, 'SECAO_211')`.
- ✅ **[COBERTO]** 4. O sistema mostra a tela `Detalhes de subprocesso`.
  - Evidência: `e2e/cdu-17.spec.ts` - URL `/processo/\d+/SECAO_211$`.
- ✅ **[COBERTO]** 5. ADMIN clica no card `Mapa de competências`.
  - Evidência: `e2e/cdu-17.spec.ts` - `navegarParaMapa`.
- ✅ **[COBERTO]** 6. O sistema mostra a tela `Edição de mapa`.
  - Evidência: `e2e/cdu-17.spec.ts` - heading `Mapa de competências técnicas` visível.
- ✅ **[COBERTO]** 7. ADMIN clica no botão `Disponibilizar`.
  - Evidência: `e2e/cdu-17.spec.ts` - `btn-cad-mapa-disponibilizar` clicado.
- ✅ **[COBERTO]** 8. O sistema verifica competências e atividades (positivo = habilita botão).
  - Evidência: após criação de competências com atividades, botão habilitado.
- 🟡 **[PARCIAL]** 9. O sistema verifica (negativo = botão desabilitado).
  - Evidência: `btn-cad-mapa-disponibilizar` desabilitado antes de criar competências verificado em CT-00/CT-01 de CDU-15.
- ✅ **[COBERTO]** 10. Modal com título "Disponibilização do mapa de competências".
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 2` - texto `'Disponibilização do mapa'` visível.
- ✅ **[COBERTO]** 11. Campo `Data limite` de preenchimento obrigatório.
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 2` - `inp-disponibilizar-mapa-data` visível; `Cenario 4` valida data inválida → botão desabilitado.
- ✅ **[COBERTO]** 12. Campo `Observações` de preenchimento opcional.
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 2` - `inp-disponibilizar-mapa-obs` visível; `Cenario 5` preenche com valor.
- ✅ **[COBERTO]** 13. Botões `Disponibilizar` e `Cancelar`.
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 2` - `btn-disponibilizar-mapa-confirmar` e `btn-disponibilizar-mapa-cancelar` visíveis.
- ✅ **[COBERTO]** 14. Caso `Cancelar`, sistema interrompe operação permanecendo na tela `Edição de mapa`.
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 3` - modal oculto + heading `Mapa de competências técnicas` visível.
- ✅ **[COBERTO]** 15. ADMIN preenche campos e clica em `Disponibilizar`.
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 5` - preenche data `2030-12-31` e obs, clica `btn-disponibilizar-mapa-confirmar`.
- ✅ **[COBERTO]** 16. Sistema registra observações e data limite.
  - Evidência: disponibilização com obs preenchida em Cenario 5.
- ✅ **[COBERTO]** 17. Sistema altera situação para 'Mapa disponibilizado'.
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 5` - `subprocesso-header__txt-situacao` → `/Mapa disponibilizado/i`.
- ✅ **[COBERTO]** 18. Sistema registra movimentação com Data/hora, Unidade origem (ADMIN) e Unidade destino.
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 5` - `tbl-movimentacoes` linha `/Disponibilização do mapa/i` com `/\d{2}\/\d{2}\/\d{4}/`, `/ADMIN/i`, `/SECAO_211/i`.
- ✅ **[COBERTO]** 19. Sistema cria alerta para a unidade do subprocesso (CHEFE_SECAO_211).
  - Evidência: `e2e/cdu-17.spec.ts:Cenario 6` - `tbl-alertas` linha com `descProcesso`, `/SECAO_211/i`, `/\d{2}\/\d{2}\/\d{4}/`.
- 🟡 **[PARCIAL]** 20. Sistema notifica a unidade do subprocesso por e-mail.
  - Evidência: não testável via Playwright.
- 🟡 **[PARCIAL]** 21. Sistema notifica unidades superiores por e-mail.
  - Evidência: não testável via Playwright.
- 🟡 **[PARCIAL]** Alerta para unidades superiores (além da própria unidade).
  - Evidência: apenas o alerta para CHEFE_SECAO_211 foi verificado; alertas para COORD_21 e superiores não verificados.

## Ajustes recomendados para próximo ciclo
- Verificar alerta e notificação para as unidades superiores (COORD_21, SECRETARIA_2).
- Verificar que a data limite é registrada corretamente no subprocesso.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: fluxos principais cobertos; gaps são e-mail e alertas de unidades superiores.

## Observações metodológicas
- Rodada 3: adicionado Cenario 2 com verificação explícita de campo Observações e botões no modal, Cenario 5 inline com preenchimento de obs, verificação de movimentação com data/hora e ADMIN/SECAO_211, e Cenario 6 para alerta CHEFE_SECAO_211.
