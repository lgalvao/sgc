# Alinhamento CDU-13 - Situação após reforço E2E (2026-03-26)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-13.md`
- Teste E2E: `e2e/cdu-13.spec.ts`

## Resumo executivo
- Status do CDU: **PRONTO_COM_GAPS**
- Houve avanço material na cobertura E2E do fluxo de análise do cadastro.
- O spec agora cobre de forma explícita:
  - entrada no subprocesso e navegação até `Atividades e conhecimentos`
  - visibilidade das ações de análise por perfil
  - histórico de análise com validação de cabeçalhos, data/hora preenchida, unidade, resultado e observação
  - devolução com redirecionamento ao painel e mudança de situação
  - cancelamento de devolução
  - aceite por unidades hierárquicas
  - homologação por `ADMIN`
  - cancelamento de homologação
  - mensagem final de homologação

## Cobertura validada
- **COBERTO** fluxo principal de acesso ao subprocesso e ao card `Atividades e conhecimentos`
- **COBERTO** botão `Histórico de análise` e conteúdo principal da tabela modal
- **COBERTO** devolução para ajustes, incluindo cancelamento
- **COBERTO** aceite por `GESTOR`
- **COBERTO** homologação por `ADMIN`, incluindo cancelamento
- **COBERTO** mudança de situação observável na UI
- **COBERTO** redirecionamentos finais e mensagens de sucesso observáveis

## Gaps remanescentes
- **PARCIAL** prova literal do passo "o sistema mostra a tela `Detalhes do processo`" antes da seleção da unidade. O fluxo passa por esse caminho via helpers, mas a evidência direta no spec ainda é fraca.
- **PARCIAL** campos de movimentação e alerta interno associados à devolução e ao aceite, quando a UI não expõe tudo de forma estável no mesmo fluxo.
- **PARCIAL** auditoria de `Data/hora atual` fora do histórico visível, especialmente para movimentações e alertas.
- **PARCIAL** notificações por e-mail e alerta interno, por serem efeitos colaterais sem superfície E2E suficientemente estável.

## Leitura prática
- Este CDU não deve mais ser tratado como pendência centrada em `Histórico de análise`.
- O próximo incremento, se houver, deve mirar:
  - evidência direta da tela `Detalhes do processo`
  - complemento por integração backend para notificação, alerta e auditoria temporal

## Evidência de execução
- Regressão direcionada executada com sucesso em `e2e/cdu-13.spec.ts`.
