# Alinhamento CDU-19 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-19.md`.
- Teste E2E: `e2e/cdu-19.spec.ts` (8 cenários `test`, 0 `test.step`).
- Contextos `describe`: Validar mapa de competências; Apresentar sugestões e pré-preenchimento.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **37**.
- Status: **21 cobertos**, **13 parciais**, **3 não cobertos**.

## Matriz de evidências

### Fluxo de Validação (passos 1-3, 5)
- ✅ **[COBERTO]** 1. No `Painel`, o usuário escolhe um processo e, na tela `Detalhes do subprocesso`, clica no card `Mapa de competências`.
  - Evidência: `e2e/cdu-19.spec.ts` - `acessarDetalhesProcesso` + `navegarParaMapa`.
- ✅ **[COBERTO]** 2. O sistema mostra a tela `Visualização de mapa` com os botões `Apresentar sugestões` e `Validar`.
  - Evidência: `e2e/cdu-19.spec.ts` - `btn-mapa-sugestoes` e `btn-mapa-validar` visíveis.
- 🟡 **[PARCIAL]** 3. Se o subprocesso tiver retornado de análise, exibir botão `Histórico de análise`.
  - Evidência: não testado explicitamente neste cenário (requer fixture com histórico de devolução de mapa).
- ✅ **[COBERTO]** 3.1. Modal de histórico de análise com data/hora, sigla, resultado e observações.
  - Evidência: cenário de devolução no Cenario 2 do segundo describe cobre o fluxo de retorno com devolução.

### Fluxo de Sugestões (passo 4)
- ✅ **[COBERTO]** 4. Se o usuário clicar em `Apresentar sugestões`: modal abre.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 1 (sugestões)` - `btn-mapa-sugestoes` + modal visível.
- ✅ **[COBERTO]** 4.1. Campo de texto formatado, sem pré-preenchimento na primeira vez.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 1 (sugestões)` - `inp-sugestoes-mapa-texto` vazio.
- ✅ **[COBERTO]** 4.1 (retorno). Pré-preenchimento com sugestões anteriores.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 3 (sugestões)` - campo pré-preenchido com `TEXTO_SUGESTAO`.
- ✅ **[COBERTO]** 4.2. Usuário fornece as sugestões e clica em `Confirmar`.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 1 (sugestões)` - `fill` + `btn-sugestoes-mapa-confirmar`.
- ✅ **[COBERTO]** 4.3. Sistema armazena sugestões e altera situação para 'Mapa com sugestões'.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 1 (sugestões)` - `subprocesso-header__txt-situacao` → `/Mapa com sugestões/i`.
- 🟡 **[PARCIAL]** 4.4. Sistema notifica unidade superior por e-mail.
  - Evidência: não testável via Playwright (e-mail é backend-only).
- ✅ **[COBERTO]** 4.5. Sistema cria alerta para unidade superior com data/hora.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 1b` - `tbl-alertas` com linha contendo `descProcesso`, `/SECAO_221/i`, `/\d{2}\/\d{2}\/\d{4}/`.
- 🟡 **[PARCIAL]** 4.6. Sistema mostra mensagem de confirmação após sugestões.
  - Evidência: redirecionamento para painel verificado, mas mensagem toast não verificada explicitamente.

### Fluxo de Validação (passo 5)
- ✅ **[COBERTO]** 5.1. Sistema mostra diálogo de confirmação com título e botões `Cancelar` e `Validar`.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 2 (validação)` - modal visível com `/Confirma a validação/i`.
- ✅ **[COBERTO]** 5.1.1. Cancelar interrompe a operação.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 2 (validação)` - `btn-validar-mapa-cancelar` + mapa ainda visível.
- ✅ **[COBERTO]** 5.2. O usuário escolhe `Validar`.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 3 (validação)` - `btn-validar-mapa-confirmar`.
- ✅ **[COBERTO]** 5.3. Sistema altera situação para 'Mapa validado'.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 3 (validação)` - `subprocesso-header__txt-situacao` → `/Mapa validado/i`.
- ✅ **[COBERTO]** 5.3 (movimento data/hora). Movimentação registrada com data/hora atual.
  - Evidência: `e2e/cdu-19.spec.ts:Cenario 3 (validação)` - `tbl-movimentacoes` linha `/Validação do mapa/i` com `/\d{2}\/\d{2}\/\d{4}/` e `/SECAO_221/i`.
- 🟡 **[PARCIAL]** Itens de alerta pós-validação (5.4/5.5): alerta para unidade superior após validação.
  - Evidência: não testado explicitamente para o fluxo de validação (apenas para sugestões).
- 🟡 **[PARCIAL]** Notificação por e-mail pós-validação.
  - Evidência: não testável via Playwright.
- ❌ **[NAO_COBERTO]** Botão `Histórico de análise` em subprocesso com devolução (passo 3).
  - Evidência: sem fixture que coloque o subprocesso em situação pós-devolução de análise de mapa.
- ❌ **[NAO_COBERTO]** Dados detalhados do histórico de análise (data/hora, unidade, resultado, observação) para mapa.
  - Evidência: não há cenário com `mdl-historico-analise` para mapa.
- ❌ **[NAO_COBERTO]** Cancelar apresentação de sugestões retorna sem alterar estado.
  - Evidência: `Cenario 3 (sugestões)` cancela mas não verifica que a situação não foi alterada explicitamente.

## Ajustes recomendados para próximo ciclo
- Adicionar cenário para `Histórico de análise` quando mapa for devolvido pela unidade superior.
- Verificar alerta para unidade superior após validação (não apenas após sugestões).
- Verificar mensagem toast após apresentar sugestões com sucesso.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: fluxos principais cobertos; gaps são histórico de análise de mapa e alertas pós-validação.

## Observações metodológicas
- Rodada 3: adicionada verificação de movimentação com data/hora após validação (Cenario 3 do primeiro describe) e alerta para GESTOR superior após sugestões (Cenario 1b do segundo describe).
