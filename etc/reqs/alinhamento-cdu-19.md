# Alinhamento CDU-19 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-19.md`.
- Teste E2E analisado: `e2e/cdu-19.spec.ts` (6 cenários `test`, 0 `test.step`, 136 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenários CDU-19: Fluxo completo de validação do mapa pelo CHEFE
- ✅ Setup data
- ✅ Cenario 1: CHEFE apresenta sugestões com sucesso
- ✅ Cenario 2: GESTOR devolve mapa para ajustes
- ✅ Cenario 3: CHEFE reabre modal com pré-preenchimento das sugestões anteriores

## Pontos do requisito sem evidência direta no E2E
- ⚠️ Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser exibido também o botão `Histórico de análise`. (palavras-chave do requisito: subprocesso, tiver, retornado, análise)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
