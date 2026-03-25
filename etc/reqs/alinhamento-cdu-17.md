# Alinhamento CDU-17 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-17.md`.
- Teste E2E analisado: `e2e/cdu-17.spec.ts` (2 cenários `test`, 0 `test.step`, 85 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenários CDU-17: Fluxo completo de disponibilização do mapa pelo ADMIN

## Pontos do requisito sem evidência direta no E2E
- ⚠️ `Observações`: de preenchimento opcional (palavras-chave do requisito: observações, preenchimento, opcional)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
