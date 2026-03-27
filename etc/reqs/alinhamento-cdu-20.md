# Alinhamento CDU-20 - Situação após reforço E2E (2026-03-26)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-20.md`
- Teste E2E: `e2e/cdu-20.spec.ts`

## Resumo executivo
- Status do CDU: **PRONTO_COM_GAPS**
- O spec já cobre os fluxos principais mais relevantes da análise de validação do mapa.
- O reforço recente consolidou:
  - acesso ao subprocesso e à tela `Visualização de mapa`
  - aceite por `GESTOR`
  - homologação por `ADMIN`
  - devolução da validação com efeito visível no subprocesso
  - histórico de análise com validação de cabeçalhos e data/hora
  - cenário de `Mapa com sugestões` com `Ver sugestões`
  - regra de visibilidade ligada ao bug `#1376`

## Cobertura validada
- **COBERTO** acesso ao subprocesso e à visualização de mapa
- **COBERTO** presença e uso dos botões principais de análise
- **COBERTO** `Ver sugestões` quando a situação é `Mapa com sugestões`
- **COBERTO** histórico de análise com colunas e dados essenciais
- **COBERTO** cancelamento do aceite
- **COBERTO** aceite com redirecionamento ao painel e mensagem `Aceite registrado`
- **COBERTO** devolução com mensagem `Devolução realizada`
- **COBERTO** movimentação visível no subprocesso após devolução, incluindo `Data/hora`, origem, destino e descrição
- **COBERTO** homologação por `ADMIN` com mensagem de sucesso

## Gaps remanescentes
- **PARCIAL** cancelamento de homologação. O requisito prevê esse ramo, mas a combinação de estado/permissão disponível no fluxo real ainda não sustentou um cenário válido e estável.
- **PARCIAL** notificações por e-mail e alerta interno decorrentes de devolução e aceite.
- **PARCIAL** alguns campos internos de análise e auditoria temporal não exibidos de forma consistente na UI.

## Leitura prática
- Este CDU não deve mais ser listado como pendência principal de `Histórico de análise` ou `Devolver para ajustes`.
- O que resta aqui é refinamento de borda e complemento backend para efeitos colaterais internos.

## Evidência de execução
- Regressão direcionada executada com sucesso em `e2e/cdu-20.spec.ts`.
