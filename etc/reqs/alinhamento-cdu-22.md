# Alinhamento CDU-22 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-22.md`.
- Teste E2E analisado: `e2e/cdu-22.spec.ts` (7 cenários `test`, 0 `test.step`, 139 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenario 1: GESTOR abre modal e cancela aceite em bloco
- ✅ Cenario 3a: Botão desabilitado quando item está com gestor subordinado
- ✅ Cenario 3b: Botão habilitado após gestor subordinado aceitar
- ✅ Cenario 4: Botão desabilitado para gestor superior quando item está com intermediário
- ✅ Setup: processo de revisão com cadastro disponibilizado
- ✅ Cenario REVISAO: GESTOR aceita revisão de cadastro em bloco

## Pontos do requisito sem evidência direta no E2E
- ⚠️ Registra internamente uma movimentação para o subprocesso: (palavras-chave do requisito: registra, internamente, movimentação, subprocesso)
- ⚠️ Registra internamente um alerta: (palavras-chave do requisito: registra, internamente, alerta)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
