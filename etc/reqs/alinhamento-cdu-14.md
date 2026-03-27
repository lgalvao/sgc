# Alinhamento CDU-14 - Situação após reforço E2E (2026-03-26)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-14.md`
- Teste E2E: `e2e/cdu-14.spec.ts`

## Resumo executivo
- Status do CDU: **PRONTO_COM_GAPS**
- O spec foi fortalecido com evidência mais objetiva do fluxo de análise da revisão de cadastro.
- O teste agora cobre de forma explícita:
  - acesso ao subprocesso e navegação até `Atividades e conhecimentos`
  - uso do botão `Impactos no mapa`
  - uso do botão `Histórico de análise`
  - devolução com observação
  - cancelamento de devolução
  - redisponibilização pelo chefe
  - aceite da revisão por `GESTOR`
  - visualização do histórico final por `ADMIN`

## Cobertura validada
- **COBERTO** fluxo principal até a tela `Atividades e conhecimentos`
- **COBERTO** `Impactos no mapa` com abertura de modal e validação de conteúdo funcional
- **COBERTO** `Histórico de análise` com validação de cabeçalhos, data/hora preenchida, resultado e observação
- **COBERTO** devolução para ajustes com efeito visível na situação do subprocesso
- **COBERTO** cancelamento da devolução
- **COBERTO** redisponibilização da revisão após ajuste
- **COBERTO** aceite da revisão

## Gaps remanescentes
- **PARCIAL** homologação por `ADMIN`. O requisito prevê dois ramos de homologação dependentes de impacto, mas a superfície real observada neste fluxo ainda não sustentou um cenário E2E robusto sem risco de falso positivo.
- **PARCIAL** mensagem final de aceite e redirecionamento ao painel com assert direto no mesmo cenário.
- **PARCIAL** movimentações, alertas internos e notificações por e-mail, que seguem melhores candidatos a integração backend complementar.
- **PARCIAL** auditoria de `Data/hora atual` fora do histórico mostrado em tela.

## Leitura prática
- Este CDU não deve mais aparecer como pendência principal de `Impactos no mapa`, `Histórico de análise` ou `Devolver para ajustes`.
- O próximo passo útil aqui é complementar homologação e efeitos colaterais não observáveis na UI.

## Evidência de execução
- Regressão direcionada executada com sucesso em `e2e/cdu-14.spec.ts`.
