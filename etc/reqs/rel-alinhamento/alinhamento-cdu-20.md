# Alinhamento CDU-20 - Situação após reforço E2E (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-20.md`
- Teste E2E: `e2e/cdu-20.spec.ts` (307 linhas, múltiplos cenários `test`)

## Resumo executivo
- Status do CDU: **PRONTO_COM_GAPS**
- O spec cobre os fluxos principais relevantes da análise de validação do mapa.
- Cobertura consolidada:
  - acesso ao subprocesso e à tela `Visualização de mapa`
  - aceite por `GESTOR`
  - homologação por `ADMIN`
  - devolução da validação com efeito visível no subprocesso
  - histórico de análise com validação de cabeçalhos e data/hora
  - cenário de `Mapa com sugestões` com `Ver sugestões`
  - regra de visibilidade do botão `Ver sugestões`
  - movimentação com data/hora na tabela `tbl-movimentacoes`
  - alerta na tabela `tbl-alertas` para unidade superior

## Cobertura validada
- **COBERTO** acesso ao subprocesso e à visualização de mapa
- **COBERTO** aceite (GESTOR) com modal e redirecionamento
- **COBERTO** homologação (ADMIN) com modal e redirecionamento
- **COBERTO** devolução com observação e efeito no subprocesso
- **COBERTO** histórico de análise (cabeçalhos, data/hora, resultado)
- **COBERTO** mapa com sugestões e botão `Ver sugestões`
- **COBERTO** movimentação com data/hora, unidade origem/destino, descrição
- **COBERTO** alerta para unidade superior com data/hora

## Gaps residuais
- Notificação por e-mail (não testável via Playwright)
- Verificação de que o mapa do subprocesso se torna o mapa vigente após homologação

## Observações metodológicas
- Rodada 3: alinhamento revisado com base na leitura completa do spec (307 linhas). Nenhuma alteração no spec desta rodada; análise confirmou cobertura existente.
